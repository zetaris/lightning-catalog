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
import org.apache.spark.sql.DataFrame
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

import java.awt.Dimension
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

@RunWith(classOf[JUnitRunner])
class RegisterImageDataSourceTestSuite extends FileDataSourceTestBase {
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

    val rec = df.collect()
    assert(rec.size == 2)
    assert(rec(0).getString(0) == "jpg")
    assert(rec(0).getString(1).endsWith("/image/spark-fed.jpg"))
    assert(rec(0).getLong(3) == 4909)
    assert(rec(0).getInt(4) == 230)
    assert(rec(0).getInt(5) == 148)

    var image = ImageIO.read(new ByteArrayInputStream(rec(0).get(6).asInstanceOf[Array[Byte]]))
    var dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 100)
    // thumbnail height is prorate with real resolution 230:100 = 148:64
    assert(dim.height == 64)


    assert(rec(1).getString(0) == "png")
    assert(rec(1).getString(1).endsWith("/image/spark-logo.png"))
    assert(rec(1).getLong(3) == 6131)
    assert(rec(1).getInt(4) == 270)
    assert(rec(1).getInt(5) == 148)

    image = ImageIO.read(new ByteArrayInputStream(rec(1).get(6).asInstanceOf[Array[Byte]]))
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

    var image = ImageIO.read(new ByteArrayInputStream(rec(0).get(6).asInstanceOf[Array[Byte]]))
    var dim = new Dimension(image.getWidth, image.getHeight)
    assert(dim.width == 50)
    // thumbnail height is prorate with real resolution 230:100 = 148:64


    assert(rec(1).getString(0) == "png")
    assert(rec(1).getString(1).endsWith("/image/spark-logo.png"))
    assert(rec(1).getLong(3) == 6131)
    assert(rec(1).getInt(4) == 270)
    assert(rec(1).getInt(5) == 148)

    image = ImageIO.read(new ByteArrayInputStream(rec(1).get(6).asInstanceOf[Array[Byte]]))
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

      val image = ImageIO.read(new ByteArrayInputStream(rec(0).get(6).asInstanceOf[Array[Byte]]))
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

}
