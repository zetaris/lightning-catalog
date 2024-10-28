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
        |create table customer (id BIGINT primary key, name varchar(30), address varchar(50));
        |
        |create table lineitem (id BIGINT primary key, name varchar(30), price decimal);
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

    // hidden unless they are activated
    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.metastore.crm.ordermart"), Seq())

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    // only customer table is poped up because its activated
    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.metastore.crm.ordermart"),
      Seq(Row("ordermart", "customer", false)))

    checkAnswer(sparkSession.sql(s"DESC TABLE lightning.metastore.crm.ordermart.customer"),
      Seq(Row("id", "bigint", null),
        Row("name", "varchar(30)", null),
        Row("address", "varchar(50)", null)))


    checkAnswer(sparkSession.sql(s"select * from lightning.metastore.crm.ordermart.customer"),
      Seq(Row(1, "chris lynch", "100 VIC"),
        Row(2, "wayne bourne", "200 NSW"),
        Row(3, "scott mayson", "300 TAS")))

  }
}
