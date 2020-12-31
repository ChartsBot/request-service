package com.chartsbot.services

import com.chartsbot.config.ConfigPaths.SqlPaths
import com.typesafe.config.{ Config, ConfigFactory }
import io.getquill.{ Literal, SqliteJdbcContext }
import javax.inject.{ Inject, Singleton }

import scala.collection.JavaConverters._

trait SqliteConnector {

  val ctx: SqliteJdbcContext[Literal.type]

}

@Singleton
class DefaultSqliteConnector @Inject() (config: Config) extends SqliteConnector with SqlPaths {

  val cfg: Config = ConfigFactory.parseMap(Map(
    "jdbcUrl" -> s"jdbc:sqlite:${config.getString(SQLITE_CONNECTION_PATH)}",
    "driverClassName" -> "org.sqlite.JDBC"
  ).asJava)

  lazy val ctx: SqliteJdbcContext[Literal.type] = new SqliteJdbcContext(Literal, cfg)

}

