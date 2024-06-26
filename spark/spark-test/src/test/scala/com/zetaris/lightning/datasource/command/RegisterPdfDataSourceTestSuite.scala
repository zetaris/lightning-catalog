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
class RegisterPdfDataSourceTestSuite extends FileDataSourceTestBase {
  test("should run query over a pdf file") {
    registerFileDataSource("spark_history", "pdf", getClass.getResource("/pdf/apache_spark_history.pdf").getPath, "file_scan", "*.pdf")

    val rec = sparkSession.sql("select * from lightning.datasource.file.spark_history").collect()
    assert(rec.size == 1)
    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf/apache_spark_history.pdf"))
    assert(rec(0).getLong(3) == 220899)
    assert(rec(0).getString(4).startsWith("Spark Research"))
  }

  test("should run query over files in a directory with file_scan mode and glob filter") {
    registerFileDataSource("pdf_files", "pdf", getClass.getResource("/pdf").getPath, "file_scan", "*.pdf")
    val rec = sparkSession.sql("select * from lightning.datasource.file.pdf_files order by path").collect()
    assert(rec.size == 3)
    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf/aa_88.pdf"))
    assert(rec(0).getLong(3) == 9482)
    assert(rec(0).getString(4).startsWith("Aa"))

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/pdf/apache_spark_history.pdf"))
    assert(rec(1).getLong(3) == 220899)
    assert(rec(1).getString(4).startsWith("Spark Research"))

    assert(rec(2).getString(0) == "pdf")
    assert(rec(2).getString(1).endsWith("/pdf/ee_12.pdf"))
    assert(rec(2).getLong(3) == 9384)
    assert(rec(2).getString(4).startsWith("Ee"))
  }

  test("should run query over only files in a root directory") {
    registerFileDataSource("pdf_files_no_subdir", "pdf", getClass.getResource("/pdf-subdir").getPath, "file_scan", "*.pdf")
    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_files_no_subdir order by path")

    df.show()

    val rec = df.collect()
    assert(rec.size == 2)
    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf-subdir/aa_88.pdf"))
    assert(rec(0).getLong(3) == 9482)
    assert(rec(0).getString(4).startsWith("Aa"))

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/pdf-subdir/ee_12.pdf"))
    assert(rec(1).getLong(3) == 9384)
    assert(rec(1).getString(4).startsWith("Ee"))

  }

  test("should run query over all files including sub directories with recursive scan mode") {
    registerFileDataSource("pdf_subdir", "pdf", getClass.getResource("/pdf-subdir").getPath, "recursive_scan", "*.pdf")

    val schema = sparkSession.sql("describe lightning.datasource.file.pdf_subdir").collect()
    assert(schema.size == 6)
    assert(schema(5).getString(0) == "subdir")

    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_subdir")
    val rec = df.collect()

    assert(rec.size == 3)

    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf-subdir/aa_88.pdf"))
    assert(rec(0).getLong(3) == 9482)
    assert(rec(0).getString(4).startsWith("Aa"))
    assert(rec(0).getString(5) == "")

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/pdf-subdir/ee_12.pdf"))
    assert(rec(1).getLong(3) == 9384)
    assert(rec(1).getString(4).startsWith("Ee"))
    assert(rec(1).getString(5) == "")

    assert(rec(2).getString(0) == "pdf")
    assert(rec(2).getString(1).endsWith("/pdf-subdir/subdir/pdf-subdir.pdf"))
    assert(rec(2).getLong(3) == 8786)
    assert(rec(2).getString(4).startsWith("Aa"))
    assert(rec(2).getString(5) == "/subdir")
  }

  test("should support partition with parts scan mode") {
    registerFileDataSource("pdf_parts", "pdf", getClass.getResource("/pdf-parts").getPath, "parts_scan", "*.pdf")

    val schema = sparkSession.sql("describe lightning.datasource.file.pdf_parts").collect()
    assert(schema.size == 9)
    assert(schema(5).getString(0) == "ct")

    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_parts order by path")
    df.show()
    val rec = df.collect()

    assert(rec.size == 6)

    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/ct=alpha/aabbccdd.pdf"))
    assert(rec(0).getString(5) == "alpha")

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/ct=alpha/eeffgghh.pdf"))
    assert(rec(1).getString(5) == "alpha")

    assert(rec(2).getString(0) == "pdf")
    assert(rec(2).getString(1).endsWith("/ct=alphanumeric/aa_88.pdf"))
    assert(rec(2).getString(5) == "alphanumeric")

    assert(rec(3).getString(0) == "pdf")
    assert(rec(3).getString(1).endsWith("/ct=alphanumeric/ee_12.pdf"))
    assert(rec(3).getString(5) == "alphanumeric")

    assert(rec(4).getString(0) == "pdf")
    assert(rec(4).getString(1).endsWith("/ct=numeric/11223344.pdf"))
    assert(rec(4).getString(5) == "numeric")

    assert(rec(5).getString(0) == "pdf")
    assert(rec(5).getString(1).endsWith("/ct=numeric/55667788.pdf"))
    assert(rec(5).getString(5) == "numeric")

  }

  test("should pushdown partition column") {
    registerFileDataSource("pdf_parts", "pdf", getClass.getResource("/pdf-parts").getPath, "parts_scan", "*.pdf")

    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_parts where ct = 'numeric' order by path")
    println(df.queryExecution)
    val rec = df.collect()

    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/ct=numeric/11223344.pdf"))
    assert(rec(0).getString(5) == "numeric")

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/ct=numeric/55667788.pdf"))
    assert(rec(1).getString(5) == "numeric")

  }

  test("should pushdown other column than partition") {
    registerFileDataSource("pdf_subdir", "pdf", getClass.getResource("/pdf-subdir").getPath, "recursive_scan", "*.pdf")

    var df = sparkSession.sql("select * from lightning.datasource.file.pdf_subdir where path like '%subdir.pdf'")
    var rec = df.collect()

    assert(rec.size == 1)

    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf-subdir/subdir/pdf-subdir.pdf"))
    assert(rec(0).getLong(3) == 8786)
    assert(rec(0).getString(4).startsWith("Aa"))
    assert(rec(0).getString(5) == "/subdir")

    df = sparkSession.sql("select * from lightning.datasource.file.pdf_subdir where path like '%subdir.pdf' or sizeInBytes > 0")
    rec = df.collect()

    assert(rec.size == 3)

    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf-subdir/aa_88.pdf"))
    assert(rec(0).getLong(3) == 9482)
    assert(rec(0).getString(4).startsWith("Aa"))
    assert(rec(0).getString(5) == "")

    assert(rec(1).getString(0) == "pdf")
    assert(rec(1).getString(1).endsWith("/pdf-subdir/ee_12.pdf"))
    assert(rec(1).getLong(3) == 9384)
    assert(rec(1).getString(4).startsWith("Ee"))
    assert(rec(1).getString(5) == "")

    assert(rec(2).getString(0) == "pdf")
    assert(rec(2).getString(1).endsWith("/pdf-subdir/subdir/pdf-subdir.pdf"))
    assert(rec(2).getLong(3) == 8786)
    assert(rec(2).getString(4).startsWith("Aa"))
    assert(rec(2).getString(5) == "/subdir")


    df = sparkSession.sql("select * from lightning.datasource.file.pdf_subdir where path like '%subdir.pdf' and sizeInBytes > 1000000")
    rec = df.collect()

    assert(rec.size == 0)
  }

  test("should pushdown both non partition column and partition column") {
    registerFileDataSource("pdf_parts", "pdf", getClass.getResource("/pdf-parts").getPath, "parts_scan", "*.pdf")

    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_parts where ct = 'numeric' and path like '%44.pdf'")
    val rec = df.collect()

    assert(rec.size == 1)
    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/ct=numeric/11223344.pdf"))
    assert(rec(0).getString(5) == "numeric")
  }

  test("should display file type") {
    registerFileDataSource("pdf_files", "pdf", getClass.getResource("/pdf").getPath, "file_scan", "*")
    val rec = sparkSession.sql("select * from lightning.datasource.file.pdf_files order by path").collect()
    assert(rec.size == 4)
    assert(rec(0).getString(0) == "pdf")
    assert(rec(0).getString(1).endsWith("/pdf/aa_88.pdf"))
    assert(rec(0).getLong(3) == 9482)
    assert(rec(0).getString(4).startsWith("Aa"))

    assert(rec(1).getString(0) == "pdfx")
    assert(rec(1).getString(1).endsWith("/pdf/aa_88.pdfx"))
    assert(rec(1).getLong(3) == 9482)
    assert(rec(1).getString(4).startsWith("Aa"))

    assert(rec(2).getString(0) == "pdf")
    assert(rec(2).getString(1).endsWith("/pdf/apache_spark_history.pdf"))
    assert(rec(2).getLong(3) == 220899)
    assert(rec(2).getString(4).startsWith("Spark Research"))

    assert(rec(3).getString(0) == "pdf")
    assert(rec(3).getString(1).endsWith("/pdf/ee_12.pdf"))
    assert(rec(3).getLong(3) == 9384)
    assert(rec(3).getString(4).startsWith("Ee"))
  }

  test("should throw table not found exception") {
    registerFileDataSource("pdf_subdir", "pdf", getClass.getResource("/pdf-subdir").getPath, "recursive_scan", "*.pdf")
    var exception = intercept[RuntimeException] {
      sparkSession.sql("select * from lightning.datasource.file.pdf_subdir_nonexist").show()
    }

    assert(exception.getMessage == "namespace(datasource.file), name(pdf_subdir_nonexist) is not defined")

    exception = intercept[RuntimeException] {
      sparkSession.sql("select * from lightning.datasource.file.pdf_subdir.not_content")
    }
    assert(exception.getMessage == "namespace(datasource.file.pdf_subdir), name(not_content) is not defined")
  }

  test("should run query over contents") {
    registerFileDataSource("pdf_subdir", "pdf", getClass.getResource("/pdf-subdir").getPath, "recursive_scan", "*.pdf")

    val df = sparkSession.sql("select * from lightning.datasource.file.pdf_subdir.content")
    val rec = df.collect()

    assert(rec.size == 3)

    assert(rec(0).getString(0).endsWith("/pdf-subdir/aa_88.pdf"))
    assert(rec(0).getString(1).startsWith("Aa"))
    assert(rec(0).getString(3).startsWith(""))

    assert(rec(1).getString(0).endsWith("/pdf-subdir/ee_12.pdf"))
    assert(rec(1).getString(1).startsWith("Ee"))
    assert(rec(1).getString(3).startsWith(""))

    assert(rec(2).getString(0).endsWith("/pdf-subdir/subdir/pdf-subdir.pdf"))
    assert(rec(2).getString(1).startsWith("Aa"))
    assert(rec(2).getString(3).startsWith("/subdir"))
  }
}
