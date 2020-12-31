package com.chartsbot.util

import akka.util.ByteString
import com.chartsbot.models.SqlFilePath

import java.io.{ File, FileInputStream }
import scala.io.Source
import scala.math.abs

object Util {

  val dicAdjPath = "src/main/resources/words/adjectives.txt"
  val dicAnimalsPath = "src/main/resources/words/animals.txt"
  val dicAdjectives: List[String] = getLinesFromFile(dicAdjPath)
  val dicAnimals: List[String] = getLinesFromFile(dicAnimalsPath)

  val imageHasher = new ImageHash()

  def imageToWords(imagePath: String): String = {
    val fisImage = new FileInputStream(new File(imagePath))
    val hash = imageHasher.getHash(fisImage)
    fisImage.close()
    stringToRandomWords(hash) match {
      case Left(_) => "null"
      case Right(value) => value.mkString("")
    }
  }

  /**
    * Convert the string toConvert into a list of size size of words.
    * This is done by splitting the word into size parts, hashing each part to a number, then associating this word
    * with one of the english lexicon.
    * @param toConvert string to convert.
    * @param size Number of words returned. Can not be longer than the size of the string to convert.
    * @return List of words.
    */
  def stringToRandomWords(toConvert: String, size: Int = 3): Either[String, List[String]] = {
    if (toConvert.length < size) {
      Left("String to convert is shorter than the size it should be cut off to")
    } else {

      val sizeParts = (toConvert.length / size).toInt + 1
      val parts = toConvert.grouped(sizeParts).toList
      val words = for (i <- parts.indices) yield {
        if (i == parts.length - 1) {
          val strInt = abs(parts(i).hashCode % dicAnimals.length)
          dicAnimals(strInt).capitalize
        } else {
          val strInt = abs(parts(i).hashCode % dicAdjectives.length)
          dicAdjectives(strInt).capitalize
        }
      }

      Right(words.toList)
    }

  }

  def getLinesFromFile(filepath: String): List[String] = {
    val bufferedSource = Source.fromFile(filepath)
    val lines = bufferedSource.getLines().toList
    bufferedSource.close
    lines
  }

  def fileToByteStr(filename: String): ByteString = {
    val source = Source.fromFile(filename)
    val base = source.mkString
    source.close()
    ByteString(base)
  }

  def fileDirectoryFromQuery(sqlFilePath: SqlFilePath): String = {
    val chatId: String = sqlFilePath.chatId.abs.toString
    chatId + '/' + sqlFilePath.fileClassification + '/' + sqlFilePath.fileType + '/'
  }

}
