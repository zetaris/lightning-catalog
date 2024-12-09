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

import com.zetaris.lightning.model.HdfsFileSystem
import com.zetaris.lightning.spark.SparkExtensionsTestBase
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterFileDataSourceTestSuite extends SparkExtensionsTestBase with TestDataSet {

  val fileDbPath = "/tmp/file-db"

  val taxis = Seq(Taxis(1l, 1000371l, 1.8f, 15.32d, "N"),
    Taxis(2l, 1000372l, 2.5f, 22.15d, "N"),
    Taxis(2l, 1000373l, 0.9f, 9.01d, "N"),
    Taxis(1l, 1000374l, 8.4f, 42.13d, "Y"))

  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
  }

  override def beforeEach(): Unit = {
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.file")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.file")
    dropFileDbNamespaces()
  }

  private def dropFileDbNamespaces() = {
    val parentAndChild = HdfsFileSystem.toFolderUrl(fileDbPath)
    val fs = new HdfsFileSystem(Map.empty[String, String], parentAndChild._1)

    fs.deleteDirectory(parentAndChild._2)
  }

  private def registerFileDataSource(tableName: String, format: String) = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE ${format.toUpperCase} DATASOURCE $tableName OPTIONS (
         |path "$fileDbPath/$tableName"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)
  }

  private def checkRecords(tableName: String) = {
    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.file.$tableName order by trip_id"),
      Seq(Row(1l, 1000371l, 1.8f, 15.32d, "N"),
        Row(2l, 1000372l, 2.5f, 22.15d, "N"),
        Row(2l, 1000373l, 0.9f, 9.01d, "N"),
        Row(1l, 1000374l, 8.4f, 42.13d, "Y")))
  }

  test("should register parquet file") {
    val tableName = "parquet_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$tableName", "parquet")
    registerFileDataSource(tableName, "parquet")

    checkRecords(tableName)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.file"),
      Seq())
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", tableName, false)))
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file.$tableName"),
      Seq())
  }

  test("should register orc file") {
    val tableName = "orc_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$tableName", "orc")
    registerFileDataSource(tableName, "orc")

    checkRecords(tableName)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.file"),
      Seq())
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", tableName, false)))
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file.$tableName"),
      Seq())
  }

  test("should register avro file") {
    val tableName = "avro_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$tableName", "avro")
    registerFileDataSource(tableName, "avro")

    checkRecords(tableName)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.file"),
      Seq())
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", tableName, false)))
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file.$tableName"),
      Seq())
  }

  test("should register multiple files under a single namespace") {
    var parquetTableName = "parquet_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$parquetTableName", "parquet")
    registerFileDataSource(parquetTableName, "parquet")
    checkRecords(parquetTableName)

    val orcTableName = "orc_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$orcTableName", "orc")
    registerFileDataSource(orcTableName, "orc")
    checkRecords(orcTableName)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", parquetTableName, false),
        Row("file", orcTableName, false)))
  }

  test("should register csv file") {
    val tableName = "csv_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$tableName", "csv")

    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE CSV DATASOURCE $tableName OPTIONS (
         |header "true",
         |inferSchema "true",
         |path "$fileDbPath/$tableName"
         |) NAMESPACE lightning.datasource.file
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.file.$tableName order by trip_id"),
      Seq(Row(1, 1000371, 1.8d, 15.32d, "N"),
        Row(2, 1000372, 2.5d, 22.15d, "N"),
        Row(2, 1000373, 0.9d, 9.01d, "N"),
        Row(1, 1000374, 8.4d, 42.13d, "Y")))

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.file"),
      Seq())
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", tableName, false)))
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file.$tableName"),
      Seq())
  }

  test("should register json file") {
    val tableName = "json_taxis"
    createDataSourceFile(taxis, s"$fileDbPath/$tableName", "json")
    registerFileDataSource(tableName, "json")

    checkAnswer(sparkSession.sql(s"select vendor_id, trip_id, trip_distance, fare_amount, store_and_fwd_flag  from lightning.datasource.file.$tableName order by trip_id"),
      Seq(Row(1l, 1000371l, 1.8d, 15.32d, "N"),
        Row(2l, 1000372l, 2.5d, 22.15d, "N"),
        Row(2l, 1000373l, 0.9d, 9.01d, "N"),
        Row(1l, 1000374l, 8.4d, 42.13d, "Y")))

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.file"),
      Seq())
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file"),
      Seq(Row("file", tableName, false)))
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.file.$tableName"),
      Seq())
  }

  test("should run query over multiline json file") {
    val tableName = "json_taxis"
    //registerFileDataSource(tableName, "json")

    //    sparkSession.sql(
    //      s"""
    //         |REGISTER OR REPLACE JSON DATASOURCE nation OPTIONS (
    //         |path "/tmp/ligt-test/nation.json",
    //         |multiLine "true",
    //         |allowUnquotedFieldNames "true",
    //         |dropFieldIfAllNull "true"
    //         |) NAMESPACE lightning.datasource.file
    //         |""".stripMargin)
    //
    //
    //    sparkSession.sql("select * from lightning.datasource.file.nation").show()

    //    sparkSession.sql(
    //      s"""
    //         |create temporary view nation using json options (
    //         |path "/tmp/ligt-test/nation.json",
    //         |multiLine "true"
    //         |)
    //         |""".stripMargin)
    //
    //    sparkSession.sql("select * from nation").show()


    //    val df = sparkSession.read.option("multiline","true").json("/tmp/ligt-test/nation.json")
    //    df.show()
  }

  private def registerDataSource(sourceType: String, namespace: String): Unit = {
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.file.$namespace")
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE $sourceType DATASOURCE nation_avro OPTIONS (
         |path "dummy"
         |) NAMESPACE lightning.datasource.file.$namespace
         |""".stripMargin)

  }

  test("should register table over namespace having reserved keyword") {
    registerDataSource("avro", "avro")
    registerDataSource("json", "json")
    registerDataSource("csv", "csv")
    registerDataSource("xml", "xml")
    registerDataSource("parquet", "parquet")
    registerDataSource("orc", "orc")
    registerDataSource("orc", "jdbc")
    registerDataSource("orc", "iceberg")
    registerDataSource("delta", "delta")
  }

}
