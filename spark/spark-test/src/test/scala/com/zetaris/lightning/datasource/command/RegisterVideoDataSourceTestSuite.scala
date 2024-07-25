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
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterVideoDataSourceTestSuite extends FileDataSourceTestBase {

  def registerFileDataSource(tableName: String,
                             path: String,
                             scanType: String,
                             pathFilter: String): Unit = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE VIDEO DATASOURCE $tableName OPTIONS (
         |path "$path",
         |scanType "$scanType",
         |pathGlobFilter "$pathFilter"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)
  }

  test("should run query over a video file") {
    registerFileDataSource("spark_intro", getClass.getResource("/video").getPath, "file_scan", "{*.mp4}")
    val df = sparkSession.sql("select * from lightning.datasource.file.spark_intro")
    df.show()
    val rec = df.collect()
    println(s"tags : ${rec(0).getString(8)}")
  }
}
