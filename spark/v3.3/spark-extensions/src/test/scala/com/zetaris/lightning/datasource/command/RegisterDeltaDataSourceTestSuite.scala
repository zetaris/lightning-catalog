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
class RegisterDeltaDataSourceTestSuite extends SparkExtensionsTestBase {
  val dbName = "deltadb"
  val lakehousePath = "/tmp/delta-lake"

  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.delta")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.delta")
  }

  override def beforeEach(): Unit = {
    dropDeltaNamespaces()
    registerDataSource(dbName)
  }

  private def dropDeltaNamespaces() = {
    FileSystemUtils.deleteDirectory(lakehousePath)
  }

  private def registerDataSource(database: String) = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE DELTA DATASOURCE $database OPTIONS (
         |path "$lakehousePath"
         |) NAMESPACE lightning.datasource.delta
         |""".stripMargin)
  }

  test("should not support create, drop, list namespace") {
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource"), Seq(Row("delta")))
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource.delta"),
      Seq(Row(s"$dbName")))

    intercept[RuntimeException] {
      sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.delta.$dbName.subspace")
    }

    intercept[RuntimeException] {
      sparkSession.sql(s"DROP NAMESPACE lightning.datasource.delta.$dbName.subspace")
    }

    intercept[RuntimeException] {
      sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.delta.$dbName")
    }
  }

  private def createTable(database: String, table: String) = {
    sparkSession.sql(
      s"""
         |CREATE TABLE lightning.datasource.delta.$database.$table (
         |vendor_id bigint,
         |trip_id bigint,
         |trip_distance float,
         |fare_amount double,
         |store_and_fwd_flag string
         |) PARTITIONED BY (vendor_id)
         |""".stripMargin)
  }

  private def insertRecords(database: String, table: String) = {
    sparkSession.sql(
      s"""
         |INSERT INTO lightning.datasource.delta.$database.$table
         |VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")
         |""".stripMargin)
  }

  private def checkRecords(database: String, table: String) = {
    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.delta.$database.$table order by trip_id"),
      Seq(Row(1l, 1000371l, 1.8f, 15.32d, "N"),
        Row(2l, 1000372l, 2.5f, 22.15d, "N"),
        Row(2l, 1000373l, 0.9f, 9.01d, "N"),
        Row(1l, 1000374l, 8.4f, 42.13d, "Y")))
  }


  test("should create, show, insert, select and drop table") {
    val table = "taxis"
    createTable(dbName, table)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.delta.$dbName"),
      Seq(Row(s"$dbName", table, false)))

    insertRecords(dbName, table)
    checkRecords(dbName, table)

    sparkSession.sql(s"drop table lightning.datasource.delta.$dbName.$table")
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.delta.$dbName"), Seq())
  }

  test("should create multiple tables in a single namespace") {
    val table1 = "taxis"
    val table2 = "taxis2"

    createTable(dbName, table1)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.delta.$dbName"),
      Seq(Row(dbName, table1, false)))

    insertRecords(dbName, table1)
    checkRecords(dbName, table1)

    createTable(dbName, table2)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.delta.$dbName"),
      Seq(Row(dbName, table1, false), Row(dbName, table2, false)))

    insertRecords(dbName, table2)
    checkRecords(dbName, table2)
  }

  test("should register existing tables") {
    val table1 = "taxis"
    val table2 = "taxis2"

    createTable(dbName, table1)
    insertRecords(dbName, table1)

    createTable(dbName, table2)
    insertRecords(dbName, table2)

    val anotherDb = "anotherDb"
    registerDataSource(anotherDb)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.delta.$anotherDb"),
      Seq(Row(anotherDb, table1, false), Row(anotherDb, table2, false)))

    checkRecords(anotherDb, table1)
    checkRecords(anotherDb, table2)
  }

}
