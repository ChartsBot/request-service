package com.chartsbot.util

import org.scalatest.featurespec.AnyFeatureSpecLike
import org.scalatest.matchers.should.Matchers

import java.io.{ File, FileInputStream }

class ImageHashSpec extends AnyFeatureSpecLike with Matchers {

  Feature("hash image") {
    Scenario("hashing an image should return a valid hash of the desired size") {
      val imagePath = "src/test/resources/images/lena.jpg"
      val fileInputStream = new FileInputStream(new File(imagePath))
      val imageHasher = new ImageHash()
      val res = imageHasher.getHash(fileInputStream)
      res shouldBe "1100011101101000100111101010100110010001011100100110011110011100"
    }

    Scenario("different images should give different hashes") {
      val image1Path = "src/test/resources/images/dali1.jpg"
      val image2Path = "src/test/resources/images/dali2.jpg"
      val image3Path = "src/test/resources/images/dali3.jpg"
      val imagePathsToHash = List(image1Path, image2Path, image3Path)
      val hasher = new ImageHash()
      val hashes = for (path <- imagePathsToHash) yield {
        val fisImage = new FileInputStream(new File(path))
        hasher.getHash(fisImage)
      }
      hashes.distinct shouldBe hashes
    }
  }

}
