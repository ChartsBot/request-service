package com.chartsbot.models

import com.chartsbot.services.{ MySQLConnector, SqlConnectorForTests, TestQueries }
import com.chartsbot.{ Binder, InjectorHelper, TestUtils }
import com.google.inject.binder.ScopedBindingBuilder
import com.wix.mysql.EmbeddedMysql.anEmbeddedMysql
import com.wix.mysql.distribution.Version.v8_latest
import com.wix.mysql.config.Charset.UTF8
import com.wix.mysql.config.MysqldConfig.aMysqldConfig
import io.getquill.{ MysqlAsyncContext, CamelCase }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ Await, ExecutionContext }
import scala.concurrent.duration.DurationInt
import scala.util.{ Failure, Random, Success }

class SqlFilesDAOSpec extends AnyFeatureSpecLike with Matchers with BeforeAndAfterEach {

  val InjectorTests: InjectorHelper = new InjectorHelper(List(new Binder {
    override def SqlClient: ScopedBindingBuilder = bind(classOf[MySQLConnector]).to(classOf[SqlConnectorForTests])
  })) {}

  val sqlConnector: MySQLConnector = InjectorTests.get[MySQLConnector]

  val ctx: MysqlAsyncContext[CamelCase.type] = sqlConnector.ctx

  import ctx._

  implicit val ec: ExecutionContext = InjectorTests.get[ExecutionContext]

  import com.wix.mysql.EmbeddedMysql
  import com.wix.mysql.ScriptResolver
  import com.wix.mysql.config.MysqldConfig
  import java.util.concurrent.TimeUnit

  val config: MysqldConfig = aMysqldConfig(v8_latest)
    .withCharset(UTF8)
    .withPort(3306)
    .withUser("testuser", "password")
    .withTimeZone("Europe/Berlin")
    .withTimeout(2, TimeUnit.MINUTES)
    .withServerVariable("max_connect_errors", 666)
    .build

  val mysqld: EmbeddedMysql = anEmbeddedMysql(config)
    .addSchema("aschema", ScriptResolver.classPathScript("db_test/schema.sql"))
    .start

  implicit val eventSchemaMeta: ctx.SchemaMeta[SqlFilePath] = schemaMeta[SqlFilePath]("FilesPaths")

  override def beforeEach(): Unit = {
    //    ctx.executeAction(TestQueries.createTable)
    //    Thread.sleep(1000)
  }

  override def afterEach(): Unit = {
    ctx.executeAction(TestQueries.deleteAll)
    Thread.sleep(1000)
  }

  Feature("Add files") {
    Scenario("Add a file") {
      val randomFile = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      Await.result(dao.addFile(randomFile), 100.second)
      val res = Await.result(dao.selectAll(), 100.second)

      res.length shouldBe 1
      res.head shouldBe randomFile

      Thread.sleep(1000)
    }

    Scenario("Add twice the same file") {
      val randomFile = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = Await.result(dao.addFile(randomFile), 100.second)

      val table = Await.result(dao.selectAll(), 100.second)
      table.length shouldBe 1
      table.head shouldBe randomFile
    }

    Scenario("Add twice the same file by different authors") {
      val randomFile = createRandomFile()
      val file2 = SqlFilePath(
        chatId = randomFile.chatId,
        chatTitle = randomFile.chatTitle,
        fileClassification = randomFile.fileClassification,
        fileType = randomFile.fileType,
        fileName = randomFile.fileName,
        author = TestUtils.generateRandomString(),
        timeCreation = Random.nextInt(10000)
      )
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = Await.result(dao.addFile(randomFile), 100.second)

      val table = Await.result(dao.selectAll(), 100.second)
      table.length shouldBe 1
      table.head shouldBe randomFile
    }

    Scenario("Add two different files") {
      val randomFile = createRandomFile()
      val randomFile2 = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = Await.result(dao.addFile(randomFile), 100.second)

      val table = Await.result(dao.selectAll(), 100.second)
      table.length shouldBe 2
      table.sortBy(_.chatId) shouldBe List(randomFile, randomFile2).sortBy(_.chatId)
    }
  }

  def createRandomFile(): SqlFilePath = {
    SqlFilePath(
      chatId = Random.nextInt(10000),
      chatTitle = TestUtils.generateRandomString(),
      fileClassification = TestUtils.generateRandomString(),
      fileType = TestUtils.generateRandomString(),
      fileName = TestUtils.generateRandomString(),
      author = TestUtils.generateRandomString(),
      timeCreation = Random.nextInt(10000)
    )
  }

}
