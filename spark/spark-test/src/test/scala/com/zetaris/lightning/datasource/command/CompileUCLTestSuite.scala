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

import com.zetaris.lightning.model.NamespaceNotFoundException
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.spark.SparkExtensionsTestBase
import org.apache.spark.sql.Row
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CompileUCLTestSuite extends SparkExtensionsTestBase {
  override def beforeAll(): Unit = {
    super.beforeAll()
    initRoootNamespace()
  }

  override def beforeEach(): Unit = {
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.metastore.crm")
  }

  test("throw InvalidNamespaceException if no namespace is defined") {
    intercept[NamespaceNotFoundException] {
      val df = sparkSession.sql(
        """
          |COMPILE USL IF NOT EXISTS crm NAMESPACE lightning.metastore.crm DDL
          |-- create table customer
          |CREATE TABLE IF NOT EXISTS customer (
          | id int NOT NULL PRIMARY KEY,
          | name varchar(200),
          | /*+@AccessControl(accessType="REGEX", regex="$ss", users = "*", groups = "*")*/
          | uid int UNIQUE,
          | address varchar(200),
          | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
          |);
          |
          |CREATE TABLE IF NOT EXISTS department (
          | id int NOT NULL,
          | name varchar(200),
          | CONSTRAINT pk_id PRIMARY KEY(id)
          |)
          |""".stripMargin)

    }
  }

  test("deploy DDLs & load, update USL") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    val df = sparkSession.sql(
      s"""
        |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
        |-- create table customer
        |CREATE TABLE IF NOT EXISTS customer (
        | id int NOT NULL PRIMARY KEY,
        | name varchar(200),
        | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
        | uid int UNIQUE,
        | address varchar(200),
        | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
        |);
        |
        |CREATE TABLE IF NOT EXISTS department (
        | id int NOT NULL,
        | name varchar(200),
        | CONSTRAINT pk_id PRIMARY KEY(id)
        |)
        |""".stripMargin)
    val srcJson = df.collect()(0).getString(0)
    val srcUSL = UnifiedSemanticLayer(srcJson)

    val loadJson = sparkSession.sql("LOAD USL crmdb NAMESPACE lightning.metastore.crm").collect()(0).getString(0)
    val loadUSL = UnifiedSemanticLayer(loadJson)

    assert(srcUSL.namespace.mkString(".") == loadUSL.namespace.mkString("."))
    assert(srcUSL.name == loadUSL.name)

    assert(srcUSL.tables(0).name == loadUSL.tables(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).name == loadUSL.tables(0).columnSpecs(0).name)
    assert(srcUSL.tables(0).columnSpecs(0).dataType == loadUSL.tables(0).columnSpecs(0).dataType)

    assert(srcUSL.tables(0).columnSpecs(1).name == loadUSL.tables(0).columnSpecs(1).name)
    assert(srcUSL.tables(0).columnSpecs(1).dataType == loadUSL.tables(0).columnSpecs(1).dataType)

    assert(srcUSL.tables(0).columnSpecs(2).name == loadUSL.tables(0).columnSpecs(2).name)
    assert(srcUSL.tables(0).columnSpecs(2).dataType == loadUSL.tables(0).columnSpecs(2).dataType)

    assert(srcUSL.tables(0).columnSpecs(3).name == loadUSL.tables(0).columnSpecs(3).name)
    assert(srcUSL.tables(0).columnSpecs(3).dataType == loadUSL.tables(0).columnSpecs(3).dataType)

    assert(srcUSL.tables(0).columnSpecs(4).name == loadUSL.tables(0).columnSpecs(4).name)
    assert(srcUSL.tables(0).columnSpecs(4).dataType == loadUSL.tables(0).columnSpecs(4).dataType)

    assert(srcUSL.tables(1).name == loadUSL.tables(1).name)
    assert(srcUSL.tables(1).columnSpecs(0).name == loadUSL.tables(1).columnSpecs(0).name)
    assert(srcUSL.tables(1).columnSpecs(0).dataType == loadUSL.tables(1).columnSpecs(0).dataType)

    assert(srcUSL.tables(1).columnSpecs(1).name == loadUSL.tables(1).columnSpecs(1).name)
    assert(srcUSL.tables(1).columnSpecs(1).dataType == loadUSL.tables(1).columnSpecs(1).dataType)
  }

  test("should list namespace, tables") {
    sparkSession.sql(s"CREATE NAMESPACE lightning.metastore.crm")

    sparkSession.sql(
      s"""
         |COMPILE USL IF NOT EXISTS crmdb DEPLOY NAMESPACE lightning.metastore.crm DDL
         |-- create table customer
         |CREATE TABLE IF NOT EXISTS customer (
         | id int NOT NULL PRIMARY KEY,
         | name varchar(200),
         | /*+@AccessControl(accessType="REGEX", regex="ss", users = "*", groups = "*")*/
         | uid int UNIQUE,
         | address varchar(200),
         | part_id int FOREIGN KEY REFERENCES department(id) ON DELETE RESTRICT ON UPDATE CASCADE
         |);
         |
         |CREATE TABLE IF NOT EXISTS department (
         | id int NOT NULL,
         | name varchar(200),
         | CONSTRAINT pk_id PRIMARY KEY(id)
         |)
         |""".stripMargin)

    checkAnswer(sparkSession.sql(s"SHOW NAMESPACES IN lightning.metastore.crm"),
      Seq(Row("crmdb")))

    checkAnswer(sparkSession.sql(s"SHOW TABLES in lightning.metastore.crm.crmdb"),
      Seq(Row("crmdb", "customer", false), Row("crmdb", "department", false)))
  }
}
