package com.chartsbot.services

import java.util.concurrent.Executors

import com.typesafe.config.Config
import javax.inject.{ Inject, Provider, Singleton }

import scala.concurrent.{ ExecutionContext, ExecutionContextExecutor }

@Singleton
class ExecutionProvider @Inject() (config: Config) extends Provider[ExecutionContext] {

  val threadPoolSize: Int = 5

  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(threadPoolSize))

  override def get(): ExecutionContext = ec

}
