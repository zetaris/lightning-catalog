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
class RegisterIcebergDataSourceTestSuite extends SparkExtensionsTestBase {
  val dbName = "icebergdb"
  val lakehousePath = "/tmp/iceberg-warehouse"

  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.iceberg")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.iceberg")
  }

  test("should validate parameters") {

    intercept[IllegalArgumentException] {
      sparkSession.sql(
        s"""
           |REGISTER OR REPLACE ICEBERG DATASOURCE $dbName OPTIONS(
           |  warehouse "/tmp/iceberg-warehouse"
           |) NAMESPACE lightning.datasource.iceberg
           |""".stripMargin)
    }

    intercept[IllegalArgumentException] {
      sparkSession.sql(
        s"""
           |REGISTER OR REPLACE ICEBERG DATASOURCE $dbName OPTIONS(
           |  type "hadoop"
           |) NAMESPACE lightning.datasource.iceberg
           |""".stripMargin)
    }
  }

  override def beforeEach(): Unit = {
    dropIcebergNamespaces()
    registerDataSource()
  }

  private def dropIcebergNamespaces() = {
    FileSystemUtils.deleteDirectory(lakehousePath)
//    sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.iceberg.$dbName").collect().foreach { row1 =>
//      val ns = row1.getString(0)
//      sparkSession.sql(s"SHOW TABLES IN lightning.datasource.iceberg.$dbName.$ns").collect().foreach { row2 =>
//        val table = row2.getString(1)
//        println(s"drop table : lightning.datasource.iceberg.$dbName.$ns.$table")
//        sparkSession.sql(s"DROP TABLE IF EXISTS lightning.datasource.iceberg.$dbName.$ns.$table")
//      }
//
//      sparkSession.sql(s"DROP NAMESPACE lightning.datasource.iceberg.$dbName.$ns")
//    }
  }

  private def registerDataSource() = {
    sparkSession.sql(
      s"""
         |REGISTER OR REPLACE ICEBERG DATASOURCE $dbName OPTIONS(
         |  type "hadoop",
         |  warehouse "$lakehousePath"
         |) NAMESPACE lightning.datasource.iceberg
         |""".stripMargin)

  }

  test("should create namespace") {
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource"), Seq(Row("iceberg")))
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource.iceberg"),
      Seq(Row(s"$dbName")))

    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.iceberg.$dbName.subspace")
    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.iceberg.$dbName"),
      Seq(Row("subspace")))
  }

  test("should drop namespace") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.iceberg.$dbName.subspace2")
    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.iceberg.$dbName"),
      Seq(Row("subspace2")))

    sparkSession.sql(s"DROP NAMESPACE lightning.datasource.iceberg.$dbName.subspace2")

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.iceberg.$dbName"),
      Seq())
  }

  test("should create, show, insert, select and drop table") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.iceberg.$dbName.nyc")
    sparkSession.sql(
      s"""
         |CREATE TABLE lightning.datasource.iceberg.$dbName.nyc.taxis (
         |vendor_id bigint,
         |trip_id bigint,
         |trip_distance float,
         |fare_amount double,
         |store_and_fwd_flag string
         |) PARTITIONED BY (vendor_id)
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.iceberg.$dbName.nyc"),
      Seq(Row("nyc", "taxis", false)))

    sparkSession.sql(
      s"""
        |INSERT INTO lightning.datasource.iceberg.$dbName.nyc.taxis
        |VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")
        |""".stripMargin)

    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.iceberg.$dbName.nyc.taxis order by trip_id"),
      Seq(Row(1l, 1000371l, 1.8f, 15.32d, "N"),
          Row(2l, 1000372l, 2.5f, 22.15d, "N"),
          Row(2l, 1000373l, 0.9f, 9.01d, "N"),
          Row(1l, 1000374l, 8.4f, 42.13d, "Y")))

    sparkSession.sql(s"drop table lightning.datasource.iceberg.$dbName.nyc.taxis")
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.iceberg.$dbName.nyc"), Seq())
  }
}
