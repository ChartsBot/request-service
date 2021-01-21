package com.chartsbot.controllers

import akka.Done
import akka.actor.typed.ActorSystem
import akka.stream.IOResult
import com.chartsbot.config.ConfigPaths.FtpPaths
import com.chartsbot.models.{ SftpDAO, SqlFilePath, SqlFilesDAO }
import com.chartsbot.services._
import com.chartsbot.util.Util
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.google.protobuf.ByteString
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging

import java.nio.file.Files
import java.sql.Timestamp
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

    val tmpFilePath = Util.bytesToFile(fileUpReq.file.toByteArray)

    val fileName: String = fileUpReq.fileType.toLowerCase match {
      case "image" =>
        Util.imageToWords(tmpFilePath) + ".jpg"
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

    val resUploadSql: Future[Either[ErrorMessage, Long]] = sqlDao.addFile(sqlFileDescription)
    val r = resUploadSql map {
      case Right(_) =>
        // try to create the directory if it doesn't exist
        val maybeCreateDir: Future[immutable.Seq[Done]] = sftpDAO.createDir("", fileDirectoryOnFtpServer)
        val q = maybeCreateDir.flatMap { _ =>
          val fUploadRes: Future[IOResult] = sftpDAO.uploadAFileTo(tmpFilePath, filePathOnFtpServer)
          fUploadRes onComplete (r => tmpFilePath.toFile.delete())
          fUploadRes.map {
            _ => FileUploadResponse(status = true, message = fileName.dropRight(4))
          }
        }
        q
      case Left(errorMessage) =>
        Future.successful(FileUploadResponse(status = false, message = errorMessage.errorMessage))
    }
    r.flatten
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
    logger.info("Getting random file " + in.toProtoString)
    sqlDao.getRandomFileFromChatOfType(in.chatId, in.fileClassification) map {
      case Some(file) =>
        val remotePath = config.getString(FTP_BASE_DIR) + '/' + in.chatId.toString + '/' + in.fileClassification + '/' + file.fileType + '/' + file.fileName
        val localPath = Files.createTempFile("pre-", "tmp")
        val fDownRes = sftpDAO.downloadAFileTo(remotePath, localPath)
        val fResponse = for {
          _ <- fDownRes
        } yield {
          val fileAsByte = Files.readAllBytes(localPath) // TODO: check if file isn't null
          FileGetResponse(
            status = false,
            fileType = file.fileType,
            author = file.author,
            timeCreation = file.timeCreation,
            name = file.fileName.dropRight(4),
            file = ByteString.copyFrom(fileAsByte)
          )
        }
        fResponse.onComplete { _ => localPath.toFile.delete() }
      case None =>
        FileGetResponse(status = false, fileType = "No meme found", author = "", timeCreation = 0, null)
    }
    Future.successful(FileGetResponse(status = true, fileType = "", author = "", timeCreation = 0, null))
  }
}
