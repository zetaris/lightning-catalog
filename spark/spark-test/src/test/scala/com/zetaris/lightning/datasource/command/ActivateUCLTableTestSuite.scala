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
class ActivateUCLTableTestSuite extends SparkExtensionsTestBase with H2TestBase {
  val dbName = "registerDb"
  val schema = "testschema"

  override def beforeAll(): Unit = {
    super.beforeAll()

    createCustomerOrderTable(buildH2Connection(dbName: String), schema)
    initRoootNamespace()
    registerH2DataSource(dbName)
  }

  test("should activate table") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      """
        |COMPILE USL IF NOT EXISTS ordermart DEPLOY NAMESPACE lightning.metastore.crm DDL
        |-- create table customer
        |create table customer (id BIGINT primary key, name string, address string);
        |
        |create table lineitem (id BIGINT primary key, name string, price decimal);
        |
        |create table order (id BIGINT primary key,
        |cid BIGINT,
        |iid BIGINT,
        |item_count integer,
        |odate date,
        |otime timestamp,
        |foreign key(cid) references customer(id),
        |foreign key(iid) references lineitem(id)
        |)
        |""".stripMargin)


    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.crm"),
      Seq(Row("ordermart")))

    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.metastore.crm.ordermart"), Seq(
        Row("ordermart", "customer", false),
        Row("ordermart", "lineitem", false),
        Row("ordermart", "order", false)))

    checkAnswer(sparkSession.sql(s"DESC TABLE lightning.metastore.crm.ordermart.customer"),
      Seq(Row("id", "bigint", null),
        Row("name", "string", null),
        Row("address", "string", null)))

    checkAnswer(sparkSession.sql(s"DESC TABLE lightning.metastore.crm.ordermart.lineitem"),
      Seq(Row("id", "bigint", null),
        Row("name", "string", null),
        Row("price", "decimal(10,0)", null)))

    checkAnswer(sparkSession.sql(s"DESC TABLE lightning.metastore.crm.ordermart.order"),
      Seq(Row("id", "bigint", null),
        Row("cid", "bigint", null),
        Row("iid", "bigint", null),
        Row("item_count", "int", null),
        Row("odate", "date", null),
        Row("otime", "timestamp", null)))

    sparkSession.sql(s"desc lightning.datasource.h2.$dbName.$schema.customer").show()

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    val loadJson = sparkSession.sql("LOAD USL ordermart NAMESPACE lightning.metastore.crm").collect()(0).getString(0)
    println(loadJson)


    val df = sparkSession.sql(s"select * from lightning.metastore.crm.ordermart.customer where id > 0")
    checkAnswer(df,
      Seq(Row(1, "chris lynch", "100 VIC"),
        Row(2, "wayne bourne", "200 NSW"),
        Row(3, "scott mayson", "300 TAS")))

  }
}
