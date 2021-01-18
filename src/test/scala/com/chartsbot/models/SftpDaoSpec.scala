package com.chartsbot.models

import akka.actor.ActorSystem
import akka.stream.IOResult
import akka.testkit.{ ImplicitSender, TestActors, TestKit }
import com.chartsbot.{ Binder, InjectorHelper, SftpServerEmbedded }
import com.typesafe.scalalogging.LazyLogging
import org.scalatest._
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.{ Files, Paths }
import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.util.{ Failure, Success }

class SftpDaoSpec()
  extends TestKit(ActorSystem("SftpDaoSpec"))
  with ImplicitSender
  with Matchers
  with AnyFeatureSpecLike
  with BeforeAndAfterAll
  with LazyLogging {

  val sftpEmbedded = new SftpServerEmbedded()

  //sftpEmbedded.startServer(9000, "hey", "you")

  val Injector: InjectorHelper = new InjectorHelper(List(new Binder {})) {}

  val basePath = "/home/ben/Documents/tests/playground/"

  implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  Feature("download") {
    Scenario("download a file that exists") {
      val ftpDAO: SftpDAO = Injector.get[SftpDAO]

      val fRes: Future[IOResult] = ftpDAO.downloadAFileTo(basePath + "/Lorem_ipsum_design.svg", Paths.get(basePath + "fkerg.svg"))

      val res = Await.result(fRes, 1.minute)
      println(res)
    }

    Scenario("download a file that does not exist") {
      val ftpDAO: SftpDAO = Injector.get[SftpDAO]
      val fRes: Future[IOResult] = ftpDAO.downloadAFileTo(basePath + "/Lorem_ipsum_design.svgg", Paths.get(basePath + "fkerg.svg"))

      val res = Await.ready(fRes, 1.minute)
      res.onComplete({
        case Failure(_) => ""
        case Success(_) => fail()
      })
      println(res)
    }
  }

  Feature("Remove files") {
    Scenario("Remove a file that exist -> OK") {
      val sftpDAO = Injector.get[SftpDAO]
      val fileName = "eheheh.eheh"
      val file = new File(basePath + fileName)

      if (!file.createNewFile()) {
        fail()
      }
      val res = sftpDAO.removeFile(basePath, fileName)
      res.onComplete({
        case Failure(exception) =>
          logger.error("error", exception)
          fail()
        case Success(value) =>
          logger.info(value.toString)
          val f = new File(basePath + fileName)
          if (f.exists()) {
            fail()
          }
      })
      Await.ready(res, 1.second)
    }

    Scenario("Remove a file that does not exist -> OK") {
      val sftpDAO = Injector.get[SftpDAO]
      val fileName = "eheheh.eheh"

      val f = new File(basePath + fileName)
      if (f.exists()) fail()
      val res = sftpDAO.removeFile(basePath, fileName)
      res.onComplete({
        case Failure(_) => ""
        case Success(_) => fail()
      })
      Await.ready(res, 1.second)
    }
  }

  Feature("Create directoy") {
    Scenario("Create new directory -> OK") {
      val sftpDAO = Injector.get[SftpDAO]
      val dirName = "ohhh"

      val res = sftpDAO.createDir(basePath, dirName)
      res.onComplete({
        case Failure(_) => fail()
        case Success(_) => ""
      })
      Await.ready(res, 1.second)
      new File(basePath + dirName).delete()
    }

    Scenario("Create new directory 2 -> OK") {
      val sftpDAO = Injector.get[SftpDAO]
      val dirName = "ohhh/yess/boyyyy"

      val res = sftpDAO.createDir("", basePath + "/" + dirName)
      res.onComplete({
        case Failure(_) => fail()
        case Success(_) => ""
      })
      Await.ready(res, 1.second)
      new File(basePath + dirName).delete()
    }

    Scenario("Create dir that already exist -> NOK") {
      val sftpDAO = Injector.get[SftpDAO]
      val dirName = "ohhh/"
      Files.createDirectories(Paths.get(basePath + dirName))
      Thread.sleep(10)
      val res = sftpDAO.createDir(basePath, dirName)
      res.onComplete({
        case Failure(_) => ""
        case Success(_) => fail()
      })
      Await.ready(res, 1.second)
      val dir2 = new File(basePath + dirName)
      if (!dir2.exists()) {
        fail()
      }
      new File(basePath + dirName).delete()
    }
  }

  Feature("something") {
    Scenario("else") {
      val echo = system.actorOf(TestActors.echoActorProps)
      echo ! "hello world"
      expectMsg("hello world")
    }
  }

  Feature("test irl") {
    Scenario("Add image remote") {
      val sftpDAO = Injector.get[SftpDAO]
      val fileDirectoryOnFtpServer = "fomo_bot/102343/meme/image/"
      val imagePath = "src/test/resources/images/dali2.jpg"
      val filePathOnFtpServer = fileDirectoryOnFtpServer + "dali2.jpg"
      //      val t = Util.fileToByteStr(imagePath)
      //      println(t)
      val maybeCreateDir = sftpDAO.createDir("", fileDirectoryOnFtpServer)
      val r: Future[String] = maybeCreateDir.flatMap { _ =>
        val fUploadRes: Future[IOResult] = sftpDAO.uploadAFileTo(Paths.get(imagePath), filePathOnFtpServer)
        fUploadRes.map {
          _ => ""
        }
      }
      //Thread.sleep(10000)
      val r2 = Await.result(r, 20.second)
      println("done")
      println(r2)
    }
  }
}
//
//class SftpDaoSpec() extends TestKit(ActorSystem("MySpec")) with Matchers with Actor {
//
//
//  implicit val mat: Materializer = Materializer(context)
//
//  val sftpEmbedded = new SftpServerEmbedded()
//
//  sftpEmbedded.startServer(9000, "hey", "you")
//
//  val Injector = new InjectorHelper(List(new Binder {
//
//  })) {}
//
//  val ftpDAO: SftpDAO = Injector.get[SftpDAO]
//
//}
