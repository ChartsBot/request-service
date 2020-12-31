package com.chartsbot.controllers

import akka.actor.typed.ActorSystem
import com.chartsbot.config.ConfigPaths.FtpPaths
import com.chartsbot.models.{SftpDAO, SqlFilesDAO}
import com.chartsbot.services.{FileUploadRequest, FileUploadResponse}
import com.typesafe.config.Config

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait HandleFilesController {

  def uploadFile(fileUpReq: FileUploadRequest)(implicit system: ActorSystem[_]): Future[FileUploadResponse]

  def getFile()(implicit system: ActorSystem[_])

  def removeFile()(implicit system: ActorSystem[_])

}

@Singleton
class DefaultHandleFilesController @Inject() (sqlDao: SqlFilesDAO, sftpDAO: SftpDAO, config: Config)(implicit ec: ExecutionContext) extends HandleFilesController with FtpPaths {

  override def uploadFile(fileUpReq: FileUploadRequest)(implicit system: ActorSystem[_]): Future[FileUploadResponse] = {
    ???
    //
    //    val fileName: String = Util.imageToWords(fileUpReq.pathOnDisk)
    //
    //    val sqlFileDescription = SqlFilePath(
    //      chatId = fileUpReq.chatId,
    //      chatTitle = fileUpReq.chatTitle,
    //      fileClassification = fileUpReq.fileClassification,
    //      fileType = fileUpReq.fileType,
    //      fileName = fileName,
    //      author = fileUpReq.author,
    //      timeCreation = fileUpReq.timeCreation
    //    )
    //
    //    val fileDirectoryOnFtpServer = config.getString(FTP_BASE_DIR) + "/" + Util.fileDirectoryFromQuery(sqlFileDescription)
    //    val filePathOnFtpServer = fileDirectoryOnFtpServer + sqlFileDescription.fileName
    //
    //    // try to create the directory if it doesn't exist
    //
    //    val resUploadSql: Try[Long] = sqlDao.addFile(sqlFileDescription)
    //    resUploadSql match {
    //      case Failure(_) => Future.successful(FileUploadResponse(status = false, message = "Image already exist"))
    //      case Success(_) =>
    //        val maybeCreateDir: Future[immutable.Seq[Done]] = sftpDAO.createDir("", fileDirectoryOnFtpServer)
    //        maybeCreateDir.flatMap { _ =>
    //          val fUploadRes: Future[IOResult] = sftpDAO.uploadAFileTo(fileUpReq.pathOnDisk, filePathOnFtpServer)
    //          fUploadRes.map {
    //            _ => FileUploadResponse(status = true, message = "OK")
    //          }
    //        }
    //
    //    }
  }

  override def getFile()(implicit system: ActorSystem[_]): Unit = ???

  override def removeFile()(implicit system: ActorSystem[_]): Unit = ???
}
