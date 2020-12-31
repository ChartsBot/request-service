package com.chartsbot

import com.chartsbot.services.FileHandlerServer

object Service extends InjectorHelper(List(new Binder)) {

  def main(args: Array[String]): Unit = {

    get[FileHandlerServer].run()
  }

}
