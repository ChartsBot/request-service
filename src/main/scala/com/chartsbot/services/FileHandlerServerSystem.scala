package com.chartsbot.services

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.typesafe.config.{ Config, ConfigFactory }

import javax.inject.{ Inject, Singleton }

trait FileHandlerServerSystem {
  val system: ActorSystem[Nothing]
}

@Singleton
class DefaultFileHandlerServerSystem @Inject() () extends FileHandlerServerSystem {

  val conf: Config = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())
  val system: ActorSystem[Nothing] = ActorSystem[Nothing](Behaviors.empty, "FileHandlerServer", conf)

}
