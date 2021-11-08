package com.chartsbot.models

import com.chartsbot.services.MySQLConnector
import com.github.mauricio.async.db.mysql.exceptions.MySQLException
import com.github.mauricio.async.db.mysql.message.server.ErrorMessage
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ CamelCase, MysqlAsyncContext }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

@Singleton
class SqlFiles @Inject() (val sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) extends LazyLogging {

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlFilePath] = schemaMeta[SqlFilePath]("FilesPaths")

  def transformFuture[T](future: Future[Right[Nothing, T]], errorLog: String): Future[Either[ErrorMessage, T]] = {

    future transformWith {
      case Success(value) => Future.successful(value)
      case Failure(exception) =>
        exception match {
          case e: MySQLException =>
            logger.info(errorLog + e.errorMessage.toString)
            Future.successful(Left(e.errorMessage))
        }
    }
  }

  def selectAll(): Future[List[SqlFilePath]] = {
    run(quote(query[SqlFilePath]))
  }

  def insertFile(filePath: SqlFilePath): Future[Either[ErrorMessage, Long]] = {
    val f: Future[Right[Nothing, Long]] = run(quote(query[SqlFilePath].insert(lift(filePath)))).map(Right(_))
    transformFuture(f, "SQL error inserting file ")
  }

  def dropFile(chatId: Long, fileType: String, the_fileName: String): Future[Either[ErrorMessage, Long]] = {
    val f: Future[Right[Nothing, Long]] = run(quote(query[SqlFilePath]
      .filter(f => f.chatId == lift(chatId) && f.fileName == lift(the_fileName) && f.fileType == lift(fileType))
      .delete))
      .map(Right(_))
    transformFuture(f, "SQL error dropping file ")
  }

  def getAllFilesChat(chatId: Long, fileClassification: String): Future[List[SqlFilePath]] = {

    run(quote(query[SqlFilePath].filter(f => f.chatId == lift(chatId) && f.fileClassification == lift(fileClassification))))

  }

}

trait SqlFilesDAO {
  def selectAll(): Future[List[SqlFilePath]]

  def addFile(filePath: SqlFilePath): Future[Either[ErrorMessage, Long]]

  def deleteFile(chatId: Long, fileType: String, fileName: String): Future[Either[ErrorMessage, Long]]

  def getRandomFileFromChatOfType(chatId: Long, fileClassification: String): Future[Option[SqlFilePath]]
}

@Singleton
class DefaultSqlFilesDAO @Inject() (val sqlFiles: SqlFiles, implicit val ec: ExecutionContext) extends SqlFilesDAO with LazyLogging {

  def selectAll(): Future[List[SqlFilePath]] = sqlFiles.selectAll()

  def addFile(filePath: SqlFilePath): Future[Either[ErrorMessage, Long]] = sqlFiles.insertFile(filePath)

  def deleteFile(chatId: Long, fileType: String, fileName: String): Future[Either[ErrorMessage, Long]] = {
    sqlFiles.dropFile(chatId, fileType, fileName)
  }

  def getRandomFileFromChatOfType(chatId: Long, fileClassification: String): Future[Option[SqlFilePath]] = {
    val fMaybeFile = sqlFiles.getAllFilesChat(chatId, fileClassification)
    fMaybeFile.map {
      case Nil => None
      case x =>
        val randomSelect = x(scala.util.Random.nextInt(x.length))
        Some(randomSelect)
    }
  }

}

