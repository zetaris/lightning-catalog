/*
 *
 *  * Copyright 2023 ZETARIS Pty Ltd
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 *  * associated documentation files (the "Software"), to deal in the Software without restriction,
 *  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 *  * subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all copies
 *  * or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.zetaris.lightning.datasource.command

import com.zetaris.lightning.datasources.v2.UnstructuredData
import com.zetaris.lightning.util.FileSystemUtils
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import java.awt.Dimension
import java.io.{ByteArrayInputStream, File}
import javax.imageio.ImageIO

@RunWith(classOf[JUnitRunner])
class RegisterImageDataSourceTestSuite extends FileDataSourceTestBase {
  val saveDir = "/tmp/image-save"

  override def beforeAll(): Unit = {
    super.beforeAll()
    initSaveDir()
  }

  private def initSaveDir(createPart: Boolean = false): Unit = {
    FileSystemUtils.deleteDirectory(saveDir)
    val directory = new File(saveDir)
    if (!directory.exists()) {
      directory.mkdir()

      if (createPart) {
        val ctFed = new File(s"$saveDir/ct=fed")
        ctFed.mkdir()
        val ctLogo = new File(s"$saveDir/ct=logo")
        ctLogo.mkdir()
      }
    }
  }

  def registerFileDataSource(tableName: String,
                             format: String,
                             path: String,
                             scanType: String,
                             pathFilter: String,
                             thumbnailWidth: String,
                             thumbnailHeight: String): Unit = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE ${format.toUpperCase} DATASOURCE $tableName OPTIONS (
         |path "$path",
         |scanType "$scanType",
         |pathGlobFilter "$pathFilter",
         |${UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY} "$thumbnailWidth",
         |${UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY} "$thumbnailHeight"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)
  }

  test("should run query over a image files with default thumbnail resolution") {
    registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}")

    val df = sparkSession.sql("select * from lightning.datasource.file.spark_images order by path")
    df.show()

    val rec = df.collect()

    assert(rec.size == 2)
    assert(rec(0).getString(0) == "jpg")
    assert(rec(0).getString(1).endsWith("/image/spark-fed.jpg"))
    assert(rec(0).getLong(3) == 4909)
    assert(rec(0).getInt(4) == 230)
    assert(rec(0).getInt(5) == 148)

    var image = ImageIO.read(new ByteArrayInputStream(rec(0).get(7).asInstanceOf[Array[Byte]]))
    var dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 100)
    // thumbnail height is prorate with real resolution 230:100 = 148:64
    assert(dim.height == 64)


    assert(rec(1).getString(0) == "png")
    assert(rec(1).getString(1).endsWith("/image/spark-logo.png"))
    assert(rec(1).getLong(3) == 6131)
    assert(rec(1).getInt(4) == 270)
    assert(rec(1).getInt(5) == 148)

    image = ImageIO.read(new ByteArrayInputStream(rec(1).get(7).asInstanceOf[Array[Byte]]))
    dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 100)
    // thumbnail height is prorate with real resolution 270:100 = 148:55
    assert(dim.height == 55)

  }

  test("should throw exception with wrong thumbnail resolution") {
    intercept[IllegalArgumentException] {
      registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "1a", "22")
    }

    intercept[IllegalArgumentException] {
      registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "11", "2a")
    }

    intercept[IllegalArgumentException] {
      registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "1", "1")
    }

    intercept[IllegalArgumentException] {
      registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "2", "1")
    }

  }

  test("should run query over a image files with custom thumbnail resolution") {
    registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "50", "50")

    val df = sparkSession.sql("select * from lightning.datasource.file.spark_images order by path")

    val rec = df.collect()
    assert(rec.size == 2)
    assert(rec(0).getString(0) == "jpg")
    assert(rec(0).getString(1).endsWith("/image/spark-fed.jpg"))
    assert(rec(0).getLong(3) == 4909)
    assert(rec(0).getInt(4) == 230)
    assert(rec(0).getInt(5) == 148)

    var image = ImageIO.read(new ByteArrayInputStream(rec(0).get(7).asInstanceOf[Array[Byte]]))
    var dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 50)
    // thumbnail height is prorate with real resolution 230:100 = 148:64


    assert(rec(1).getString(0) == "png")
    assert(rec(1).getString(1).endsWith("/image/spark-logo.png"))
    assert(rec(1).getLong(3) == 6131)
    assert(rec(1).getInt(4) == 270)
    assert(rec(1).getInt(5) == 148)

    image = ImageIO.read(new ByteArrayInputStream(rec(1).get(7).asInstanceOf[Array[Byte]]))
    dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 50)

  }

  test("should pushdown filter") {
    registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "50", "50")

    def assertResult(df: DataFrame) = {
      val rec = df.collect()
      assert(rec.size == 1)
      assert(rec(0).getString(0) == "jpg")
      assert(rec(0).getString(1).endsWith("/image/spark-fed.jpg"))
      assert(rec(0).getLong(3) == 4909)
      assert(rec(0).getInt(4) == 230)
      assert(rec(0).getInt(5) == 148)

      val image = ImageIO.read(new ByteArrayInputStream(rec(0).get(7).asInstanceOf[Array[Byte]]))
      val dim = new Dimension(image.getWidth, image.getHeight)
      assert(dim.width == 50)
    }

    var df = sparkSession.sql("select * from lightning.datasource.file.spark_images where sizeinbytes < 6000 and width = 230 and height = 148")
    assertResult(df)

    df = sparkSession.sql("select * from lightning.datasource.file.spark_images where sizeinbytes < 6000 and (width = 230 or height = 120)")
    assertResult(df)

    df = sparkSession.sql("select * from lightning.datasource.file.spark_images where sizeinbytes between 4000 and 5000 and width between 220 and 230")
    assertResult(df)

    df = sparkSession.sql("select * from lightning.datasource.file.spark_images where type = 'jpg'")
    assertResult(df)
  }

  test("should write image file with thumbnail") {
    registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "50", "50")
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE IMAGE DATASOURCE spark_images_save OPTIONS (
         |path "$saveDir",
         |scanType "file_scan",
         |${UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY} "50",
         |${UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY} "50"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)

    var df = sparkSession.sql("select * from lightning.datasource.file.spark_images_save")
    assert(df.count() == 0)

    sparkSession.sql("insert into lightning.datasource.file.spark_images_save select * from lightning.datasource.file.spark_images")

    df = sparkSession.sql("select * from lightning.datasource.file.spark_images_save order by path")
    val rec = df.collect()

    assert(rec.size == 2)
    assert(rec(0).getString(0) == "jpg")
    assert(rec(0).getString(1) == s"file://$saveDir/spark-fed_thumbnail.jpg")

    assert(rec(1).getString(0) == "png")
    assert(rec(1).getString(1) == s"file://$saveDir/spark-logo_thumbnail.png")
  }

  test("should write image file with thumbnail from content") {
    initSaveDir()

    registerFileDataSource("spark_images", "image", getClass.getResource("/image").getPath, "file_scan", "{*.png,*.jpg}", "50", "50")
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE IMAGE DATASOURCE spark_images_save OPTIONS (
         |path "$saveDir",
         |scanType "file_scan",
         |${UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY} "50",
         |${UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY} "50"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)

    sparkSession.sql("insert into lightning.datasource.file.spark_images_save.content select * from lightning.datasource.file.spark_images.content")

    val df = sparkSession.sql("select * from lightning.datasource.file.spark_images_save order by path")
    val rec = df.collect()

    assert(rec.size == 4)
    assert(rec(0).getString(0) == "jpg")
    assert(rec(0).getString(1) == s"file://$saveDir/spark-fed.jpg")
    assert(rec(1).getString(0) == "jpg")
    assert(rec(1).getString(1) == s"file://$saveDir/spark-fed_thumbnail.jpg")

    assert(rec(2).getString(0) == "png")
    assert(rec(2).getString(1) == s"file://$saveDir/spark-logo.png")
    assert(rec(3).getString(0) == "png")
    assert(rec(3).getString(1) == s"file://$saveDir/spark-logo_thumbnail.png")
  }

  /**
  test("should write image file with with partition") {
    initSaveDir(true)

    registerFileDataSource("spark_parts_images", "image", getClass.getResource("/image-parts").getPath, "parts_scan", "{*.png,*.jpg}", "50", "50")
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE IMAGE DATASOURCE spark_images_save OPTIONS (
         |path "$saveDir",
         |scanType "parts_scan",
         |${UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY} "50",
         |${UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY} "50"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)

    sparkSession.sql("insert into lightning.datasource.file.spark_images_save.content select * from lightning.datasource.file.spark_parts_images.content")

    val df = sparkSession.sql("select * from lightning.datasource.file.spark_images_save")
    df.show()
  }
  */

}
