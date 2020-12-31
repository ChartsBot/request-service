package com.chartsbot.models

import com.chartsbot.{ Binder, InjectorHelper, TestUtils }
import com.chartsbot.services.{ SqlConnectorForTests, SqliteConnector, TestQueries }
import com.google.inject.binder.ScopedBindingBuilder
import io.getquill.{ Literal, SqliteJdbcContext }
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach }
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import scala.util.{ Failure, Random, Success }

class SqlFilesDAOSpec extends AnyFeatureSpecLike with Matchers with BeforeAndAfterEach {

  val InjectorTests: InjectorHelper = new InjectorHelper(List(new Binder {
    override def SqlClient: ScopedBindingBuilder = bind(classOf[SqliteConnector]).to(classOf[SqlConnectorForTests])
  })) {}

  val sqlConnector: SqliteConnector = InjectorTests.get[SqliteConnector]

  val ctx: SqliteJdbcContext[Literal.type] = sqlConnector.ctx

  import ctx._

  implicit val eventSchemaMeta: ctx.SchemaMeta[SqlFilePath] = schemaMeta[SqlFilePath]("FilesPaths")

  override def beforeEach(): Unit = {
    ctx.executeAction(TestQueries.createTable)
  }

  override def afterEach(): Unit = {
    ctx.executeAction(TestQueries.dropTable)
  }

  Feature("Add files") {
    Scenario("Add a file") {
      val randomFile = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = dao.selectAll()
      res.length shouldBe 1
      res.head shouldBe randomFile
    }

    Scenario("Add twice the same file") {
      val randomFile = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = dao.addFile(randomFile)
      res match {
        case Failure(_) => ""
        case Success(_) => fail()
      }
      val table = dao.selectAll()
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
      val res = dao.addFile(file2)
      res match {
        case Failure(_) => ""
        case Success(_) => fail()
      }
      val table = dao.selectAll()
      table.length shouldBe 1
      table.head shouldBe randomFile
    }

    Scenario("Add two different files") {
      val randomFile = createRandomFile()
      val randomFile2 = createRandomFile()
      val dao = InjectorTests.get[DefaultSqlFilesDAO]
      dao.addFile(randomFile)
      val res = dao.addFile(randomFile2)
      res match {
        case Failure(_) => fail()
        case Success(_) => ""
      }
      val table = dao.selectAll()
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
