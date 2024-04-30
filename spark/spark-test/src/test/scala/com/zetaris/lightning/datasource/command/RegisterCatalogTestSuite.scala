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

import com.zetaris.lightning.spark.{H2TestBase, SparkExtensionsTestBase}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterCatalogTestSuite extends SparkExtensionsTestBase with H2TestBase {
  val dbName = "registerDb"
  val schema1 = "testschema1"
  val schema2 = "testschema2"

  override def beforeAll(): Unit = {
    super.beforeAll()
    createH2SimpleTable(dbName, schema1)
    createH2SimpleTable(dbName, schema2)
  }

  override def beforeEach(): Unit = {
    initRoootNamespace()
    registerH2DataSource(dbName)
  }

  test("should create namespace") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.h2")
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource"), Seq(Row("h2")))
  }

  test("should register all tables from a schema in rdbms") {
    sparkSession.sql(
      s"""
         |REGISTER CATALOG $schema1
         |SOURCE lightning.datasource.h2.$dbName.$schema1
         |NAMESPACE lightning.metastore.h2
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.h2"), Seq(Row(schema1)))
    checkAnswer(sparkSession.sql(s"SHOW TABLES IN lightning.metastore.h2.$schema1"),
      Seq(Row(schema1, "test_jobs", false), Row(schema1, "test_users", false)))

    checkAnswer(sparkSession.sql(s"select * from lightning.metastore.h2.$schema1.test_jobs"),
      Seq(Row(1, "job1      "), Row(2, "job2      "), Row(3, "job3      "), Row(4, "job4      "), Row(5, "job5      ")))

    checkAnswer(sparkSession.sql(s"select * from lightning.metastore.h2.$schema1.test_users"),
      Seq(Row(1,1), Row(2,2), Row(3,3), Row(4,4), Row(5,5)))
  }

  test("should register tables matching pattern from a schema in rdbms") {
    sparkSession.sql(
      s"""
         |REGISTER CATALOG $schema1
         |SOURCE lightning.datasource.h2.$dbName.$schema1
         |NAME LIKE "%_users"
         |NAMESPACE lightning.metastore.h2
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.h2"), Seq(Row(schema1)))
    checkAnswer(sparkSession.sql(s"SHOW TABLES IN lightning.metastore.h2.$schema1"),
      Seq(Row(schema1, "test_users", false)))

    checkAnswer(sparkSession.sql(s"select * from lightning.metastore.h2.$schema1.test_users"),
      Seq(Row(1,1), Row(2,2), Row(3,3), Row(4,4), Row(5,5)))
  }

  test("should register tables in all schema from database level in rdbms") {
    val singleSchema = "singleSchema"
    sparkSession.sql(
      s"""
         |REGISTER CATALOG $singleSchema
         |SOURCE lightning.datasource.h2.$dbName
         |NAMESPACE lightning.metastore.h2
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.h2"), Seq(Row(singleSchema)))
    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.h2.$singleSchema"),
      Seq(Row("INFORMATION_SCHEMA"), Row(schema1), Row(schema2)))
    checkAnswer(sparkSession.sql(s"SHOW TABLES IN lightning.metastore.h2.$singleSchema.$schema1"),
      Seq(Row(schema1, "test_jobs", false), Row(schema1, "test_users", false)))

    checkAnswer(sparkSession.sql(s"select * from lightning.metastore.h2.$singleSchema.$schema1.test_users"),
      Seq(Row(1,1), Row(2,2), Row(3,3), Row(4,4), Row(5,5)))
  }

}
