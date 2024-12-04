/*
 * Copyright 2023 ZETARIS Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zetaris.lightning.datasource.command

import com.zetaris.lightning.spark.{H2TestBase, SparkExtensionsTestBase}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterJDBCDataSourceSuite extends SparkExtensionsTestBase with H2TestBase {
  val dbName = "registerDb"
  val schema = "testschema"

  override def beforeAll(): Unit = {
    super.beforeAll()
    createH2SimpleTable(dbName, schema)
    initRoootNamespace()
    registerH2DataSource(dbName)
  }

  test("should create namespace") {
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource"), Seq(Row("h2")))
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource.h2"), Seq(Row(dbName)))

    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.h2.$dbName.subschema")
    val allSchema = sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.h2.$dbName").collect()
    assert(allSchema.find(_.getString(0) == schema).isDefined)
    assert(allSchema.find(_.getString(0) == "subschema").isDefined)
  }

  test("should drop namespace") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.h2.$dbName.subspace2")
    var schemas = sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.h2.$dbName").collect()
    assert(schemas.find(_.getString(0) == "subspace2").isDefined)

    sparkSession.sql(s"DROP NAMESPACE lightning.datasource.h2.$dbName.subspace2")

    schemas = sparkSession.sql(s"SHOW NAMESPACES IN lightning.datasource.h2.$dbName").collect()
    assert(schemas.find(_.getString(0) == "subspace2").isEmpty)
  }

  test("should drop datasource itself") {
    val dropping = "dropping"
    sparkSession.sql(s"""
                        |REGISTER OR REPLACE JDBC DATASOURCE $dropping OPTIONS(
                        | url "jdbc:h2:mem:$dropping;DB_CLOSE_DELAY=-1",
                        | user ""
                        |) NAMESPACE lightning.datasource.h2
                        |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES in lightning.datasource.h2"),
      Seq(Row(dbName), Row(dropping)))

    sparkSession.sql(s"DROP NAMESPACE lightning.datasource.h2.$dropping")

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES in lightning.datasource.h2"),
      Seq(Row(dbName)))

    sparkSession.sql(s"SHOW NAMESPACES in lightning.datasource.h2.$dropping")
  }

  test("should create, show, insert, select and drop table") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.h2.$dbName.nyc")
    sparkSession.sql(
      s"""
         |CREATE TABLE lightning.datasource.h2.$dbName.nyc.taxis (
         |vendor_id bigint,
         |trip_id bigint,
         |trip_distance float,
         |fare_amount double,
         |store_and_fwd_flag string
         |)
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.h2.$dbName.nyc"),
      Seq(Row("nyc", "taxis", false)))

    sparkSession.sql(
      s"""
         |INSERT INTO lightning.datasource.h2.$dbName.nyc.taxis
         |VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.h2.$dbName.nyc.taxis order by trip_id"),
      Seq(Row(1l, 1000371l, 1.8f, 15.32d, "N"),
        Row(2l, 1000372l, 2.5f, 22.15d, "N"),
        Row(2l, 1000373l, 0.9f, 9.01d, "N"),
        Row(1l, 1000374l, 8.4f, 42.13d, "Y")))

    sparkSession.sql(s"drop table lightning.datasource.h2.$dbName.nyc.taxis")
    checkAnswer(sparkSession.sql(s"show tables in lightning.datasource.h2.$dbName.nyc"), Seq())
  }

  test("should show existing tables") {
    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.datasource.h2.${dbName}.${schema}"),
      Seq(Row("testschema", "test_users", false), Row("testschema", "test_jobs", false)))
  }

  test("should run query over existing table") {
    checkAnswer(sparkSession.sql(s"select * from lightning.datasource.h2.${dbName}.${schema}.test_users"),
      Seq(Row(1, 1), Row(2, 2), Row(3, 3), Row(4, 4), Row(5, 5)))
  }
}
