package com.chartsbot

import akka.actor.{ Actor, ActorSystem, Props }
import akka.stream.Materializer
import com.chartsbot.models.SftpDAO
import com.typesafe.scalalogging.LazyLogging

object SftpTest {

  def main(args: Array[String]): Unit = {

    val system = ActorSystem("pingpong")

    val pinger = system.actorOf(Props[Test](), "pinger")

    pinger ! Ping

  }

}

case object Ping

class Test extends Actor with LazyLogging {

  def start(): Unit = {

    implicit val mat: Materializer = Materializer(context)

    val sftpEmbedded = new SftpServerEmbedded()

    sftpEmbedded.startServer(9000, "hey", "you")

    logger.info("coucou")

    //    val Injector = new InjectorHelper(List(new Binder {
    //
    //    })) {}

    //    val ftpDAO: SftpDAO = Injector.get[SftpDAO]
    //
    //    val res: Source[FtpFile, NotUsed] = ftpDAO.listFiles("home/ben/Documents/tests/playground")
    //
    //    val fetchedFiles = res.map(ftp => {
    //      println(ftp.path)
    //      ftp.path
    //    }).runWith(Sink.seq)
    //
    //    val res2 = Await.result(fetchedFiles, 100.second)
    //
    //    println(res)
    //    println("finished 1")
    //
    //    val ourFile: Future[Done] = res.filter(files => files.name == "a.txt")
    //      .map(f => {println("found: " + f.lastModified + f.name); f})
    //      .runWith(Sink.ignore)
    //
    //    Await.result(ourFile, 100.second)
    //
    //    println("removing file")
    //
    //    val r = ftpDAO.removeFile("home/ben/Documents/tests/playground", "a.txt")
    //
    //    val p = Await.result(r, 100.second)
    //    println(p)
    //
    //    val ourFile2: Future[Done] = res.filter(files => files.name == "a.txt")
    //      .map(f => {println("found: " + f.lastModified + f.name); f})
    //      .runWith(Sink.ignore)
    //
    //    Await.result(ourFile2, 100.second)
    //
    //
    //    println("coucou")
    //
    //
    //    Thread.currentThread.join()
  }

  override def receive: Receive = {
    case Ping =>
      start()
  }

}
