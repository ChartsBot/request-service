package com.chartsbot.models

import com.chartsbot.services.SqliteConnector
import com.typesafe.scalalogging.LazyLogging
import io.getquill.{ Literal, SqliteJdbcContext }
import javax.inject.{ Inject, Singleton }

import scala.util.{ Failure, Success, Try }

@Singleton
class SqlFiles @Inject() (val sqlConnector: SqliteConnector) {

  val ctx: SqliteJdbcContext[Literal.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: SchemaMeta[SqlFilePath] = schemaMeta[SqlFilePath]("FilesPaths")

  def selectAll(): List[SqlFilePath] = {
    run(quote(query[SqlFilePath]))
  }

  def insertFile(filePath: SqlFilePath): Try[Long] = {
    Try(run(quote(query[SqlFilePath].insert(lift(filePath)))))
  }

  def dropFile(chatId: Int, fileType: String, fileName: String): Try[Long] = {
    Try(run(quote(query[SqlFilePath]
      .filter(f => f.chatId == chatId && f.fileName == fileName && f.fileType == fileType)
      .delete)))
  }

  def getAllFilesChat(chatId: Int, fileClassification: String): Try[List[SqlFilePath]] = {

    Try(run(quote(query[SqlFilePath].filter(f => f.chatId == chatId && f.fileClassification == fileClassification))))

  }

  // want to:
  // add
  // remove
  // get specific
  // get random
}

trait SqlFilesDAO {
  def selectAll(): List[SqlFilePath]

  def addFile(filePath: SqlFilePath): Try[Long]

  def deleteFile(chatId: Int, fileType: String, fileName: String): Try[Long]

  def getRandomFileFromChatOfType(chatId: Int, fileClassification: String): Option[SqlFilePath]
}

@Singleton
class DefaultSqlFilesDAO @Inject() (val sqlFiles: SqlFiles) extends SqlFilesDAO with LazyLogging {

  def selectAll(): List[SqlFilePath] = sqlFiles.selectAll()

  def addFile(filePath: SqlFilePath): Try[Long] = sqlFiles.insertFile(filePath)

  def deleteFile(chatId: Int, fileType: String, fileName: String): Try[Long] = {
    sqlFiles.dropFile(chatId, fileType, fileName)
  }

  def getRandomFileFromChatOfType(chatId: Int, fileClassification: String): Option[SqlFilePath] = {
    val maybeFile = sqlFiles.getAllFilesChat(chatId, fileClassification)
    maybeFile match {
      case Failure(exception) =>
        logger.error("SQLFILESDAO - While trying to get random file, error appeared: ", exception)
        None
      case Success(filesPaths) =>
        val randomSelect = filesPaths(scala.util.Random.nextInt(filesPaths.length))
        Some(randomSelect)
    }
  }

}

