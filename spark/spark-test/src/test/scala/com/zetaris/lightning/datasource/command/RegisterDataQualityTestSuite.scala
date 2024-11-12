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

import com.zetaris.lightning.model.{DataQualityDuplicatedException, TableNotActivatedException}
import com.zetaris.lightning.spark.{H2TestBase, SparkExtensionsTestBase}
import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.analysis.UnresolvedException
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RegisterDataQualityTestSuite extends SparkExtensionsTestBase with H2TestBase {
  val dbName = "registerDb"
  val schema = "testschema"

  override def beforeAll(): Unit = {
    super.beforeAll()

    createCustomerOrderTable(buildH2Connection(dbName: String), schema)
    initRoootNamespace()
    registerH2DataSource(dbName)
  }

  def prepareUSL(): Unit = {
    sparkSession.sql(s"DROP NAMESPACE if exists lightning.metastore.crm")
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

  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    prepareUSL()
  }

  test("should register single dq expression") {

    intercept[TableNotActivatedException] {
      sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS id > 0").show()
    }

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    intercept[UnresolvedException] {
      sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS non_existing_id > 0")
    }

    sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS id > 0")

    intercept[DataQualityDuplicatedException] {
      sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS id > 0")
    }

  }

  test("should register composite dq expression") {
    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    intercept[UnresolvedException] {
      sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS non_existing_id > 0 AND id > 0")
    }

    sparkSession.sql("REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.customer AS id > 0 and length(address) > 0")
  }

  test("should list dq expressions") {
    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.lineitem AS
         |select * from lightning.datasource.h2.$dbName.$schema.lineitem
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.order AS
         |select * from lightning.datasource.h2.$dbName.$schema.order
         |""".stripMargin
    )

    sparkSession.sql("REGISTER DQ customer_id TABLE lightning.metastore.crm.ordermart.customer AS id > 0 and length(address) > 0")
    sparkSession.sql("REGISTER DQ order_id TABLE lightning.metastore.crm.ordermart.order AS id > 0")

    val df = sparkSession.sql(s"LIST DQ USL lightning.metastore.crm.ordermart")
    checkAnswer(df,
      Seq(
        Row("id", "customer", "Primary key constraints", ""),
        Row("id", "lineitem", "Primary key constraints", ""),
        Row("id", "order", "Primary key constraints", ""),
        Row("cid", "order", "Foreign key constraints", ""),
        Row("iid", "order", "Foreign key constraints", ""),

        Row("customer_id", "customer", "Custom DQ", "id > 0 and length(address) > 0"),
        Row("order_id", "order", "Custom DQ", "id > 0")))
  }


  test("should register dq express referencing other table") {
    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.lineitem AS
         |select * from lightning.datasource.h2.$dbName.$schema.lineitem
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.order AS
         |select * from lightning.datasource.h2.$dbName.$schema.order
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |REGISTER DQ dq_item_count TABLE lightning.metastore.crm.ordermart.order AS
         |id > 0 and item_count > 0 and cid IN (select id from lightning.metastore.crm.ordermart.customer)
         |""".stripMargin)

  }

  test("should run dq") {
    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.customer AS
         |select * from lightning.datasource.h2.$dbName.$schema.customer
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.lineitem AS
         |select * from lightning.datasource.h2.$dbName.$schema.lineitem
         |""".stripMargin
    )

    sparkSession.sql(
      s"""
         |ACTIVATE usl TABLE lightning.metastore.crm.ordermart.order AS
         |select * from lightning.datasource.h2.$dbName.$schema.order
         |""".stripMargin
    )

    sparkSession.sql("REGISTER DQ customer_rule TABLE lightning.metastore.crm.ordermart.customer AS id > 0 and length(address) > 0")

    sparkSession.sql(
      s"""
         |REGISTER DQ order_customer_fk TABLE lightning.metastore.crm.ordermart.order AS
         |id > 0 and item_count > 0 and cid IN (select id from lightning.metastore.crm.ordermart.customer)
         |""".stripMargin)

  }

}
