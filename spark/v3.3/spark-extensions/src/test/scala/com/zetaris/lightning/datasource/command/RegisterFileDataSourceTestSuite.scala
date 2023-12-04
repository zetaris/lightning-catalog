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

import com.zetaris.lightning.spark.SparkExtensionsTestBase
import com.zetaris.lightning.util.FileSystemUtils
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterFileDataSourceTestSuite extends SparkExtensionsTestBase with TestDataSet {

  val dbName = "deltadb"
  val fileDbPath = "/tmp/file-db"

  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.file")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.file")

  }

  override def beforeEach(): Unit = {
    dropFileDbNamespaces()
  }

  private def dropFileDbNamespaces() = {
    FileSystemUtils.deleteDirectory(fileDbPath)
  }

  private def registerFileDataSource(database: String, fileName: String, format: String) = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE ${format.toUpperCase} DATASOURCE $database OPTIONS (
         |path "$fileDbPath/$fileName"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)
  }

  test("should register parquet file") {
    val taxis = Seq(Taxis(1l, 1000371l, 1.8f, 15.32d, "N"),
      Taxis(2l, 1000372l, 2.5f, 22.15d, "N"),
      Taxis(2l, 1000373l, 0.9f, 9.01d, "N"),
      Taxis(1l, 1000374l, 8.4f, 42.13d, "Y"))
    val parquetFile = "taxis"
    createDataSourceFile(taxis, s"$fileDbPath/parquet/$parquetFile", "parquet")
    registerFileDataSource("parquetDb", parquetFile, "parquet")
  }
}
