package com.chartsbot.controllers

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.IOResult
import com.chartsbot.config.ConfigPaths.FtpPaths
import com.chartsbot.models.{ SftpDAO, SqlFilePath, SqlFilesDAO }
import com.chartsbot.services._
import com.chartsbot.util.Util
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import javax.inject.{ Inject, Singleton }
import scala.collection.immutable
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

@Singleton
class DefaultFileHandlerGrpcController @Inject() (fileHandlerSystem: FileHandlerServerSystem, sqlDao: SqlFilesDAO, sftpDAO: SftpDAO, config: Config)
  extends FileHandlerAkkaService with LazyLogging with FtpPaths {

  private implicit val sys: ActorSystem[_] = fileHandlerSystem.system
  implicit val ec: ExecutionContext = sys.executionContext

  override def uploadFile(fileUpReq: FileUploadRequest): Future[FileUploadResponse] = {
    logger.info("Uploading file on " + fileUpReq.chatTitle + " by " + fileUpReq.author)

    val fileName: String = fileUpReq.fileType.toLowerCase match {
      case "image" => Util.imageToWords(fileUpReq.pathOnDisk) + ".jpg"
      case "video" => "aaa" + ".mp4"
    }

    val sqlFileDescription = SqlFilePath(
      chatId = fileUpReq.chatId,
      chatTitle = fileUpReq.chatTitle,
      fileClassification = fileUpReq.fileClassification,
      fileType = fileUpReq.fileType,
      fileName = fileName,
      author = fileUpReq.author,
      timeCreation = fileUpReq.timeCreation
    )

    val fileDirectoryOnFtpServer = config.getString(FTP_BASE_DIR) + "/" + Util.fileDirectoryFromQuery(sqlFileDescription)
    val filePathOnFtpServer = fileDirectoryOnFtpServer + sqlFileDescription.fileName

    // try to create the directory if it doesn't exist

    val resUploadSql: Try[Long] = Try(1) //sqlDao.addFile(sqlFileDescription)
    resUploadSql match {
      case Failure(e) =>
        logger.info("Error storing file in SQL: ", e)
        Future.successful(FileUploadResponse(status = false, message = "Image already exist"))
      case Success(_) =>
        val maybeCreateDir: Future[immutable.Seq[Done]] = sftpDAO.createDir("", fileDirectoryOnFtpServer)
        maybeCreateDir.flatMap { _ =>
          val fUploadRes: Future[IOResult] = sftpDAO.uploadAFileTo(fileUpReq.pathOnDisk, filePathOnFtpServer)
          fUploadRes.map {
            _ => FileUploadResponse(status = true, message = "OK")
          }
        }
    }
  }

  override def greet(request: SayHelloMessage): Future[SayHelloMessage] = {
    logger.info("Greeting " + request.toString)
    Future.successful(SayHelloMessage(message = "oh hey"))
  }

  override def deleteFile(in: FileDeleteRequest): Future[FileDeleteResponse] = {
    logger.info("Deleting file " + in.toProtoString)
    sqlDao.deleteFile(
      chatId = in.chatId,
      fileType = in.fileType,
      fileName = in.name
    )
    Future.successful(FileDeleteResponse(status = true, message = "deleted"))
  }

  override def getFile(in: FileGetRequest): Future[FileGetResponse] = {
    logger.info("Getting random file" + in.toProtoString)
    Future.successful(FileGetResponse(status = true, fileType = "", author = "", timeCreation = 0, pathOnDisk = ""))
  }
}
