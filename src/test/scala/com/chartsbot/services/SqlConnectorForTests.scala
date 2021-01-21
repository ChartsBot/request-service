package com.chartsbot.services

import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill.{ Literal, MysqlAsyncContext, CamelCase, SqliteJdbcContext }

import javax.inject.{ Inject, Singleton }
import scala.collection.JavaConverters._

@Singleton
class SqlConnectorForTests @Inject() () extends MySQLConnector {

  val cfg: Config = ConfigFactory.parseMap(Map(
    "dataSourceClassName" -> "dataSourceClassName=com.mysql.cj.jdbc.MysqlDataSource",
    "url" -> s"jdbc:mysql:8.0://88.99.226.36:3306/testplayground",
    "user" -> "testuser",
    "password" -> "password",
    "cachePrepStmts" -> "true",
    "prepStmtCacheSize" -> 250,
    "prepStmtCacheSqlLimit" -> 2048,
    "connectionTimeout" -> 30000
  ).asJava)

  lazy val ctx: MysqlAsyncContext[CamelCase.type] = new MysqlAsyncContext(CamelCase, cfg)

}

object TestQueries {

  val createTable = """CREATE TABLE IF NOT EXISTS FilesPaths (
chatId INT NOT NULL,
chatTitle VARCHAR(128),
fileClassification VARCHAR(128) NOT NULL,
fileType VARCHAR(128) NOT NULL,
fileName VARCHAR(128) NOT NULL,
author VARCHAR(128) NOT NULL,
timeCreation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT unique_meme UNIQUE(chatId, fileClassification, fileType, fileName)
);"""

  val dropTable = """DROP TABLE FilesPaths;"""
  val deleteAll = """DELETE FROM FilesPaths;"""
}
