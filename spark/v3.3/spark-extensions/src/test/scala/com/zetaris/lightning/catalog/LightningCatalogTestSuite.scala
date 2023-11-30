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

package com.zetaris.lightning.catalog

import com.zetaris.lightning.spark.{H2TestBase, SparkExtensionsTestBase}
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class LightningCatalogTestSuite extends SparkExtensionsTestBase with H2TestBase {
  val dbName = "testdb"
  val schema = "testschem"

  override def beforeAll(): Unit = {
    super.beforeAll()
    val conn = buildConnection(dbName)
    createSimpleTable(conn, schema)

    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.h2")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.h2")

    sparkSession.sql(s"""
                        |REGISTER OR REPLACE JDBC DATASOURCE $dbName OPTIONS(
                        | url "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1"
                        |) NAMESPACE lightning.datasource.h2
                        |""".stripMargin)
  }

  test("should show datasource namespaces") {
    checkAnswer(sparkSession.sql("SHOW NAMESPACES in lightning"),
      Seq(Row("datasource"), Row("metastore")))

    checkAnswer(sparkSession.sql("SHOW NAMESPACES in lightning.datasource"),
      Seq(Row("h2")))

    sparkSession.sql("SHOW NAMESPACES in lightning.datasource.h2").show()

    checkAnswer(sparkSession.sql("SHOW NAMESPACES in lightning.datasource.h2"),
      Seq(Row(s"$dbName")))

    val allSchema = sparkSession.sql(s"SHOW NAMESPACES in lightning.datasource.h2.$dbName").collect()
    assert(allSchema.find(_.getString(0) == schema).isDefined)
  }

  test("should drop namespace in datasource") {
    sparkSession.sql("CREATE NAMESPACE lightning.datasource.testns")
    checkAnswer(sparkSession.sql("SHOW NAMESPACES in lightning.datasource"), Seq(Row("h2"), Row("testns")))

    sparkSession.sql("drop NAMESPACE lightning.datasource.testns")
    checkAnswer(sparkSession.sql("SHOW NAMESPACES in lightning.datasource"), Seq(Row("h2")))

  }

  test("should throw runtime exception when dropping root namespace") {
    intercept[RuntimeException] {
      sparkSession.sql("drop namespace lightning.datasource")
    }

    intercept[RuntimeException] {
      sparkSession.sql("drop namespace lightning.metastore")
    }
  }
}
