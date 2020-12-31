package com.chartsbot.models

import akka.stream.alpakka.ftp.scaladsl.Sftp
import akka.stream.alpakka.ftp.{FtpFile, SftpSettings}
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.chartsbot.services.SftpConnector

import java.nio.file.{Path, Paths}
import javax.inject.{Inject, Singleton}
import scala.collection.immutable
import scala.concurrent.Future

class SftpQueries @Inject() (val sftpConnector: SftpConnector) {

  val sftpSettings: SftpSettings = sftpConnector.sftpSettings

  def retrieveFromPath(path: String): Source[ByteString, Future[IOResult]] = {
    Sftp.fromPath(path, sftpSettings)
  }

  def toPath(path: String): Sink[ByteString, Future[IOResult]] = Sftp.toPath(path, sftpSettings)

  def remove(): Sink[FtpFile, Future[IOResult]] =
    Sftp.remove(sftpSettings)

  def mkdir(basePath: String, directoryName: String): Source[Done, NotUsed] =
    Sftp.mkdir(basePath, directoryName, sftpSettings)

  def listFiles(basePath: String): Source[FtpFile, NotUsed] =
    Sftp.ls(basePath, sftpSettings)

}

@Singleton
class SftpDAO @Inject() (val sftpQueries: SftpQueries) {

  def downloadAFileTo(remotePath: String, newFilePath: Path)(implicit materializer: Materializer): Future[IOResult] = {
    sftpQueries.retrieveFromPath(remotePath)
      .runWith(FileIO.toPath(newFilePath))
  }

  def removeFile(directory: String, name: String)(implicit mat: Materializer): Future[IOResult] = {
    sftpQueries.listFiles(directory).filter(p => p.name == name).runWith(sftpQueries.remove())
  }

  def createDir(basePath: String, name: String)(implicit mat: Materializer): Future[immutable.Seq[Done]] = {
    sftpQueries.mkdir(basePath, name).runWith(Sink.seq)
  }

  def uploadAFileTo(pathFile: String, pathTo: String)(implicit materializer: Materializer): Future[IOResult] = {
    //    val fileAsByteStream = Util.fileToByteStr(pathFile)
    FileIO.fromPath(Paths.get(pathFile))
      .runWith(sftpQueries.toPath(pathTo))
    //    Source
    //      .single(fileAsByteStream)
    //      .runWith(sftpQueries.toPath(pathTo))
  }

}
