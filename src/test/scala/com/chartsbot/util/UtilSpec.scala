package com.chartsbot.util

import org.scalatest.BeforeAndAfterAll
import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers
import scala.util.Random

class UtilSpec extends AnyFeatureSpecLike with Matchers {

  Feature("word split") {
    Scenario("Split same string should return same result") {
      val word = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
      val res = Util.stringToRandomWords(word, 4)
      res match {
        case Left(_) => fail()
        case Right(value) => value shouldBe List("Curvy", "Busy", "Cooked", "Turtle")
      }
    }

    Scenario("Split multiple time same stuff should return same result") {
      val word = Random.alphanumeric take 20 mkString ""
      val hash = Util.stringToRandomWords(word)
      for (_ <- 0 to 1000) {
        Util.stringToRandomWords(word) shouldBe hash
      }
    }

    Scenario("two different words should give two different hashes") {
      val word1 = "salut"
      val word2 = "Salut"
      val word3 = "alut"
      val word4 = "aluT"
      val b1 = "1100011101101000100111101010100110010001011100100110011110011100"
      val b2 = "1110011101101000100111101010100110010001011100100110011110011100"
      val hashW1 = Util.stringToRandomWords(word1)
      val hashW2 = Util.stringToRandomWords(word2)
      val hashW3 = Util.stringToRandomWords(word3)
      val hashW4 = Util.stringToRandomWords(word4)
      val hashB1 = Util.stringToRandomWords(b1)
      val hashB2 = Util.stringToRandomWords(b2)
      val l = List(hashB1, hashB2, hashW1, hashW2, hashW3, hashW4)
      l.distinct.size shouldBe l.size
    }
  }

  Feature("image hash") {
    Scenario("hashing images should work blablabla") {
      val image1Path = "src/test/resources/images/dali1.jpg"
      val image2Path = "src/test/resources/images/dali2.jpg"
      val image3Path = "src/test/resources/images/dali3.jpg"
      val imagePathsToHash = List(image1Path, image2Path, image3Path)
      val hashes = for (path <- imagePathsToHash) yield { Util.imageToWords(path) }
      hashes.distinct.length shouldBe imagePathsToHash.length
      println(hashes)
    }
  }

}
