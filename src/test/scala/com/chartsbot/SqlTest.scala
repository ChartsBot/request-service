package com.chartsbot

import com.chartsbot.models.{ DefaultSqlFilesDAO, SqlFilePath }
import com.chartsbot.services.{ DefaultMySQLConnector, MySQLConnector, SqlConnectorForTests, TestQueries }
import com.google.inject.binder.ScopedBindingBuilder
import io.getquill.{ CamelCase, Literal, MysqlAsyncContext, SqliteJdbcContext }

import javax.inject.Inject
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Random, Success }

object SqlTest {
  def main(args: Array[String]): Unit = {

    val Injector = new InjectorHelper(List(new Binder {
      override def SqlClient: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[DefaultMySQLConnector])
    })) {}

    implicit val ec: ExecutionContext = Injector.get[ExecutionContext]

    val t: DefaultSqlFilesDAO = Injector.get[DefaultSqlFilesDAO]

    val res = Await.result(t.selectAll(), 10.second)

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

    val toInject2 = SqlFilePath(
      chatId = Random.nextInt(10000),
      chatTitle = "none",
      fileClassification = "meme",
      fileType = "image",
      fileName = "coucou",
      author = "moi",
      timeCreation = 1112345
    )

    val res2 = Await.result(t.addFile(toInject), 10.second)
    Await.result(t.addFile(toInject2), 10.second)
    //    println(res2)
    println("passed 1")
    //    val res2_1000 = Await.result(t.addFile(toInject), 10.second)

    val res2_1 = Await.ready(t.addFile(toInject), 10.second)

    res2_1 map {
      case Left(value) =>
        println("left")
        println(value)
      case Right(value) =>
        println("right")
        println(value)
    }

    println(res2_1.value)
    //    println(res2_1)
    println("GETTING MEME FROM CHATID " + toInject.chatId)
    val grht = Await.ready(t.getRandomFileFromChatOfType(toInject.chatId, toInject.fileClassification), 10.second)

    grht map {
      case None =>
      case Some(value) => println(value)
    }
    Thread.sleep(1000)
    println("GOT MEME")
    Await.ready(t.getRandomFileFromChatOfType(toInject.chatId, toInject.fileClassification), 10.second) map {
      case None =>
      case Some(value) => println(value)
    }
    Await.ready(t.getRandomFileFromChatOfType(toInject.chatId, toInject.fileClassification), 10.second) map {
      case None =>
      case Some(value) => println(value)
    }
    Await.ready(t.getRandomFileFromChatOfType(toInject.chatId, toInject.fileClassification), 10.second) map {
      case None =>
      case Some(value) => println(value)
    }
    val res3 = Await.result(t.selectAll(), 10.second)
    println(res3)

    val res4 = Await.result(t.deleteFile(toInject.chatId, toInject.fileType, toInject.fileName), 10.second)
    println(res4)

    val res4_bis = Await.result(t.deleteFile(toInject.chatId, toInject.fileType, toInject.fileName), 10.second)
    println(res4_bis)

    val res5 = Await.result(t.selectAll(), 10.second)
    println(res5)

    Await.ready(t.getRandomFileFromChatOfType(toInject.chatId, toInject.fileClassification), 10.second) map {
      case None =>
        println("none")
      case Some(value) =>
        println("some")
        println(value)
    }

    println("hey now, you're a rock star")
    //    val testClass = new TestClass(Injector.get[SqliteConnector])
    //    testClass.doStuff()
  }

}

class TestClass @Inject() (sqlConnector: MySQLConnector, implicit val ec: ExecutionContext) {

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

