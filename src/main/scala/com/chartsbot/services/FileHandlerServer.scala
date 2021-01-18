package com.chartsbot.services

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.{ ConnectionContext, Http, HttpsConnectionContext }
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.pki.pem.{ DERPrivateKeyLoader, PEMDecoder }
import com.chartsbot.config.ConfigPaths.GrpcPaths
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import java.io.{ BufferedInputStream, FileInputStream }
import java.security.{ KeyStore, SecureRandom }
import java.security.cert.{ Certificate, CertificateFactory }
import javax.inject.{ Inject, Singleton }
import javax.net.ssl.{ KeyManagerFactory, SSLContext }
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.io.{ BufferedSource, Source }
import scala.util.{ Failure, Success }

trait FileHandlerServer {

  def run(): Future[Http.ServerBinding]

}

@Singleton
class DefaultFileHandlerServer @Inject() (fileHandlerSystem: FileHandlerServerSystem, fileHandlerAkkaService: FileHandlerAkkaService, conf: Config)
  extends FileHandlerServer with LazyLogging with GrpcPaths {

  def run(): Future[Http.ServerBinding] = {
    implicit val sys: ActorSystem[_] = fileHandlerSystem.system
    implicit val ec: ExecutionContext = sys.executionContext

    val service: HttpRequest => Future[HttpResponse] =
      FileHandlerAkkaServiceHandler(fileHandlerAkkaService)

    val bound: Future[Http.ServerBinding] = Http(sys)
      .newServerAt(interface = conf.getString(GRPC_SERVER_HOST), port = conf.getInt(GRPC_SERVER_PORT))
      .enableHttps(serverHttpContext)
      .bind(service)
      .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        logger.info("gRPC server bound to {}:{}", address.getHostString, address.getPort)
      case Failure(ex) =>
        logger.error("Failed to bind gRPC endpoint, terminating system", ex)
        sys.terminate()
    }

    bound
  }

  private def serverHttpContext: HttpsConnectionContext = {
    val privateKey =
      DERPrivateKeyLoader.load(PEMDecoder.decode(readPrivateKeyPem()))
    val fact = CertificateFactory.getInstance("X.509")
    val cer = fact.generateCertificate(
      readFileAsInputStream(conf.getString(GRPC_SERVER_PEM))
    )
    val ks = KeyStore.getInstance("PKCS12")
    ks.load(null)
    ks.setKeyEntry(
      "private",
      privateKey,
      new Array[Char](0),
      Array[Certificate](cer)
    )
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(ks, null)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers, null, new SecureRandom)
    ConnectionContext.httpsServer(context)
  }

  private def readPrivateKeyPem(): String = {
    println(conf.getString(GRPC_SERVER_KEY))
    val res: BufferedSource = Source.fromFile(conf.getString(GRPC_SERVER_KEY))
    val string = res.mkString
    res.close()
    string
  }

  private def readFileAsInputStream(filePath: String) = {
    val input = new BufferedInputStream(new FileInputStream(filePath))
    input
  }

}
