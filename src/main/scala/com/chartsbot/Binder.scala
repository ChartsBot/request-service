package com.chartsbot

import com.chartsbot.config.ConfigProvider
import com.chartsbot.controllers.DefaultFileHandlerGrpcController
import com.chartsbot.models.{DefaultSqlFilesDAO, SqlFilesDAO}
import com.chartsbot.services._
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.{AbstractModule, Module}
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Binder extends AbstractModule {

  def SqlClient: ScopedBindingBuilder = bind(classOf[SqliteConnector]).to(classOf[DefaultSqliteConnector])
  def Config: ScopedBindingBuilder = bind(classOf[Config]).toProvider(classOf[ConfigProvider])
  def ExecutionContext: ScopedBindingBuilder = bind(classOf[ExecutionContext]).toProvider(classOf[ExecutionProvider])
  def FileHandlerServerSystem: ScopedBindingBuilder = bind(classOf[FileHandlerServerSystem]).to(classOf[DefaultFileHandlerServerSystem])
  def FileHandlerServer: ScopedBindingBuilder = bind(classOf[FileHandlerServer]).to(classOf[DefaultFileHandlerServer])
  def SqlFilesDAO: ScopedBindingBuilder = bind(classOf[SqlFilesDAO]).to(classOf[DefaultSqlFilesDAO])
  def FileHandlerAkkaService: ScopedBindingBuilder = bind(classOf[FileHandlerAkkaService]).to(classOf[DefaultFileHandlerGrpcController])

  override def configure(): Unit = {
    SqlClient
    Config
    ExecutionContext
    FileHandlerServerSystem
    FileHandlerServer
    SqlFilesDAO
    FileHandlerAkkaService
  }

}

object Binder {
  def modules: List[Module] = List(new Binder)
}
