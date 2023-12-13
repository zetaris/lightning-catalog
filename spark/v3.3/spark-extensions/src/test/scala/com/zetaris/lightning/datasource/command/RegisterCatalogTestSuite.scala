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
  val schema = "testschema"

  override def beforeAll(): Unit = {
    super.beforeAll()

    createH2SimpleTable(dbName, schema)
    initRoootNamespace()
    registerH2DataSource(dbName)
  }

  test("should create namespace") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.h2")
    checkAnswer(sparkSession.sql("SHOW NAMESPACES IN lightning.datasource"), Seq(Row("h2")))
  }


  test("should register all tables from a schema") {
    sparkSession.sql(
      s"""
         |REGISTER CATALOG $schema
         |SOURCE lightning.datasource.h2.$dbName.$schema
         |NAMESPACE lightning.metastore.h2
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.h2"), Seq(Row(schema)))
    checkAnswer(sparkSession.sql(s"SHOW TABLES IN lightning.metastore.h2.$schema"),
      Seq(Row(schema, "test_jobs", false), Row(schema, "test_users", false)))

    sparkSession.sql(s"DESCRIBE lightning.metastore.h2.test_jobs").show()
  }
}
