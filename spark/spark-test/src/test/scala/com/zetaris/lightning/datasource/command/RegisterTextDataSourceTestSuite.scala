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

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterTextDataSourceTestSuite extends FileDataSourceTestBase {
  test("should run query over a text file") {
    registerFileDataSource("aa", "text", getClass.getResource("/text/aa.txt").getPath, "file_scan", "*.txt")

    val df = sparkSession.sql("select * from lightning.datasource.file.aa")
    val rec = df.collect()
    assert(rec.size == 1)
    assert(rec(0).getString(0) == "txt")
    assert(rec(0).getString(1).endsWith("/text/aa.txt"))
    assert(rec(0).getLong(3) == 3)
    assert(rec(0).getString(4).startsWith("aa"))

  }

  test("should run query over files in a directory with file_scan mode and glob filter") {
    registerFileDataSource("text_files", "text", getClass.getResource("/text").getPath, "file_scan", "*.txt")
    val df = sparkSession.sql("select * from lightning.datasource.file.text_files order by path")
    val rec = df.collect()
    assert(rec.size == 2)

    assert(rec(0).getString(0) == "txt")
    assert(rec(0).getString(1).endsWith("/text/aa.txt"))
    assert(rec(0).getLong(3) == 3)
    assert(rec(0).getString(4).startsWith("aa"))
    assert(rec(1).getString(0) == "txt")
    assert(rec(1).getString(1).endsWith("/text/bb.txt"))
    assert(rec(1).getLong(3) == 3)
    assert(rec(1).getString(4).startsWith("bb"))
  }

  test("should run query over files in a directory with file_scan mode and glob filter combination") {
    // see https://hail.is/docs/0.2/hadoop_glob_patterns.html
    registerFileDataSource("text_files", "text", getClass.getResource("/text").getPath, "file_scan", "{*.txt,*.xtxt}")
    val df = sparkSession.sql("select * from lightning.datasource.file.text_files order by path")
    val rec = df.collect()
    assert(rec.size == 3)

    assert(rec(0).getString(0) == "txt")
    assert(rec(0).getString(1).endsWith("/text/aa.txt"))
    assert(rec(0).getLong(3) == 3)
    assert(rec(0).getString(4).startsWith("aa"))
    assert(rec(1).getString(0) == "txt")
    assert(rec(1).getString(1).endsWith("/text/bb.txt"))
    assert(rec(1).getLong(3) == 3)
    assert(rec(1).getString(4).startsWith("bb"))
    assert(rec(2).getString(0) == "xtxt")
    assert(rec(2).getString(1).endsWith("/text/dd.xtxt"))
    assert(rec(2).getLong(3) == 3)
    assert(rec(2).getString(4).startsWith("dd"))
  }

  test("should run query over all files including sub directories with recursive scan mode") {
    registerFileDataSource("text_subdir", "text", getClass.getResource("/text").getPath, "recursive_scan", "*.txt")

    sparkSession.sql("describe lightning.datasource.file.text_subdir").show()
    val schema = sparkSession.sql("describe lightning.datasource.file.text_subdir").collect()
    assert(schema.size == 6)
    assert(schema(5).getString(0) == "subdir")

    val df = sparkSession.sql("select * from lightning.datasource.file.text_subdir")
    val rec = df.collect()

    assert(rec.size == 3)

    assert(rec(0).getString(0) == "txt")
    assert(rec(0).getString(1).endsWith("/text/aa.txt"))
    assert(rec(0).getLong(3) == 3)
    assert(rec(0).getString(4).startsWith("aa"))

    assert(rec(1).getString(0) == "txt")
    assert(rec(1).getString(1).endsWith("/text/bb.txt"))
    assert(rec(1).getLong(3) == 3)
    assert(rec(1).getString(4).startsWith("bb"))

    assert(rec(2).getString(0) == "txt")
    assert(rec(2).getString(1).endsWith("/text/subdir/cc.txt"))
    assert(rec(2).getLong(3) == 3)
    assert(rec(2).getString(4).startsWith("cc"))

  }

  test("should support partition with parts scan mode") {
    registerFileDataSource("text_parts", "text", getClass.getResource("/text-parts").getPath, "parts_scan", "*.txt")

    val schema = sparkSession.sql("describe lightning.datasource.file.text_parts").collect()
    assert(schema.size == 9)
    assert(schema(5).getString(0) == "ct")

    val df = sparkSession.sql("select * from lightning.datasource.file.text_parts order by path")
    df.show()
    val rec = df.collect()

    assert(rec.size == 6)

    assert(rec(0).getString(0) == "txt")
    assert(rec(0).getString(1).endsWith("/ct=alpha/aa.txt"))
    assert(rec(0).getString(5) == "alpha")

    assert(rec(1).getString(0) == "txt")
    assert(rec(1).getString(1).endsWith("/ct=alpha/bb.txt"))
    assert(rec(1).getString(5) == "alpha")

    assert(rec(2).getString(0) == "txt")
    assert(rec(2).getString(1).endsWith("/ct=alphanumeric/11aa.txt"))
    assert(rec(2).getString(5) == "alphanumeric")

    assert(rec(3).getString(0) == "txt")
    assert(rec(3).getString(1).endsWith("/ct=alphanumeric/22bb.txt"))
    assert(rec(3).getString(5) == "alphanumeric")

    assert(rec(4).getString(0) == "txt")
    assert(rec(4).getString(1).endsWith("/ct=numeric/11.txt"))
    assert(rec(4).getString(5) == "numeric")

    assert(rec(5).getString(0) == "txt")
    assert(rec(5).getString(1).endsWith("/ct=numeric/22.txt"))
    assert(rec(5).getString(5) == "numeric")

  }

  test("should run query over contents") {
    registerFileDataSource("text_subdir", "text", getClass.getResource("/text").getPath, "recursive_scan", "*.txt")

    val df = sparkSession.sql("select * from lightning.datasource.file.text_subdir.content")
    df.show()
    val rec = df.collect()

    assert(rec.size == 3)

    assert(rec(0).getString(0).endsWith("/text/aa.txt"))
    assert(rec(0).getString(1).startsWith("aa"))
    assert(rec(0).getString(2).startsWith(""))

    assert(rec(1).getString(0).endsWith("/text/bb.txt"))
    assert(rec(1).getString(1).startsWith("bb"))
    assert(rec(1).getString(2).startsWith(""))

    assert(rec(2).getString(0).endsWith("/text/subdir/cc.txt"))
    assert(rec(2).getString(1).startsWith("cc"))
    assert(rec(2).getString(2).startsWith("/subdir"))
  }

}
