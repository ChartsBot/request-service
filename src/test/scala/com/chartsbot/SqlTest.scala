package com.chartsbot

import com.chartsbot.models.{ SqlFilePath, DefaultSqlFilesDAO }
import com.chartsbot.services.{ DefaultSqliteConnector, SqlConnectorForTests, SqliteConnector, TestQueries }
import com.google.inject.binder.ScopedBindingBuilder
import com.typesafe.config.ConfigFactory
import io.getquill.{ Literal, Query, SnakeCase, SqliteJdbcContext }
import javax.inject.Inject

import scala.collection.JavaConverters._
import scala.util.Random

object SqlTest {
  def main(args: Array[String]): Unit = {

    val Injector = new InjectorHelper(List(new Binder {
      override def SqlClient: ScopedBindingBuilder = bind(classOf[SqliteConnector]).to(classOf[SqlConnectorForTests])
    })) {}

    val sqlConnector: SqliteConnector = Injector.get[SqliteConnector]

    val ctx: SqliteJdbcContext[Literal.type] = sqlConnector.ctx

    import ctx._

    implicit val eventSchemaMeta: ctx.SchemaMeta[SqlFilePath] = schemaMeta[SqlFilePath]("FilesPaths")

    ctx.executeAction(TestQueries.createTable)

    val t = Injector.get[DefaultSqlFilesDAO]

    val res = t.selectAll()

    println(res)
    val toInject = SqlFilePath(
      chatId = Random.nextInt(10000),
      chatTitle = "none",
      fileClassification = "meme",
      fileType = "image",
      fileName = "coucou",
      author = "moi",
      timeCreation = 1112345
    )

    val res2 = t.addFile(toInject)
    println(res2)
    val res2_1 = t.addFile(toInject)
    println(res2_1)

    val res3 = t.selectAll()
    println(res3)

    val res4 = t.deleteFile(toInject.chatId, toInject.fileType, toInject.fileName)
    println(res4)

    val res5 = t.selectAll()
    println(res5)
    println("hey now, you're a rock star")
    //    val testClass = new TestClass(Injector.get[SqliteConnector])
    //    testClass.doStuff()
  }

}

class TestClass @Inject() (sqlConnector: SqliteConnector) {

  import sqlConnector.ctx._

  def doStuff(): Unit = {
    val peopleQuery = quote(query[faq].filter(_.channel_id < 0))
    val peopleResult = sqlConnector.ctx.run(peopleQuery)
    println(peopleResult)
  }

}

case class faq(
    channel_id: Int,
    faq_message: String
)

