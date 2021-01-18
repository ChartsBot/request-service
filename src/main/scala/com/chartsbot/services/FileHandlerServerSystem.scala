package com.chartsbot.services

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.Config

import javax.inject.{ Inject, Singleton }

trait FileHandlerServerSystem {
  val system: ActorSystem[Nothing]
}

@Singleton
class DefaultFileHandlerServerSystem @Inject() (config: Config) extends FileHandlerServerSystem {

  val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "FileHandlerServer", config)

}
