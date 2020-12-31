package com.chartsbot.services

import com.chartsbot.models.SqlFilePath
import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill.mirrorContextWithQueryProbing.{ InfixInterpolator, query, quote, schemaMeta }
import io.getquill.{ Literal, Query, SqliteJdbcContext, mirrorContextWithQueryProbing }
import javax.inject.{ Inject, Singleton }

import scala.collection.JavaConverters._

@Singleton
class SqlConnectorForTests @Inject() () extends SqliteConnector {

  val cfg: Config = ConfigFactory.parseMap(Map(
    "jdbcUrl" -> "jdbc:sqlite::memory:",
    "driverClassName" -> "org.sqlite.JDBC"
  ).asJava)

  lazy val ctx: SqliteJdbcContext[Literal.type] = new SqliteJdbcContext(Literal, cfg)

}

object TestQueries {

  val createTable = """CREATE TABLE IF NOT EXISTS FilesPaths (
chatId integer NOT NULL,
chatTitle String,
fileClassification txt NOT NULL,
fileType txt NOT NULL,
fileName str NOT NULL,
author str NOT NULL,
timeCreation int,
UNIQUE(chatId, fileClassification, fileType, fileName)
);"""

  val dropTable = """DROP TABLE FilesPaths;"""
}
