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

import com.zetaris.lightning.model.serde.CreateTable
import com.zetaris.lightning.spark.SparkExtensionsTestBase
import org.apache.spark.sql.types.{IntegerType, StringType, VarcharType}
import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CreateTableTestSuite extends SparkExtensionsTestBase {
  test("create table with column constraints") {
    val df = sparkSession.sql(
      """
        |-- create table customer
        |CREATE TABLE IF NOT EXISTS customer.churn (
        | id int NOT NULL PRIMARY KEY,
        | name varchar(200),
        | /*+@AccessControl(accessType="REGEX", regex="$ss", users = "*", groups = "*")*/
        | uid int UNIQUE,
        | address varchar(200),
        | part_id int FOREIGN KEY REFERENCES department(id)
        |)
        |namespace testlh
        |""".stripMargin)
    val json = df.collect()(0).getString(0)
    val createTableSpec = CreateTable(json)

    assert(createTableSpec.fqn.mkString(".") == "customer.churn")
    assert(createTableSpec.columnSpecs.size == 5)
    assert(createTableSpec.columnSpecs(0).name == "id")
    assert(createTableSpec.columnSpecs(0).dataType == IntegerType)
    assert(createTableSpec.columnSpecs(0).notNull.get.name == None)
    assert(createTableSpec.columnSpecs(0).notNull.get.columns == Seq())

    assert(createTableSpec.columnSpecs(1).name == "name")
    assert(createTableSpec.columnSpecs(1).dataType == VarcharType(200))
    assert(createTableSpec.columnSpecs(1).notNull == None)

    assert(createTableSpec.columnSpecs(2).name == "uid")
    assert(createTableSpec.columnSpecs(2).dataType == IntegerType)
    assert(createTableSpec.columnSpecs(2).unique.get.name == None)
    assert(createTableSpec.columnSpecs(2).unique.get.columns == Seq())
    assert(createTableSpec.columnSpecs(2).accessControl.get.accessType == "REGEX")
    assert(createTableSpec.columnSpecs(2).accessControl.get.regEx.get == "$ss")
    assert(createTableSpec.columnSpecs(2).accessControl.get.users == Seq("*"))
    assert(createTableSpec.columnSpecs(2).accessControl.get.groups == Seq("*"))

    assert(createTableSpec.columnSpecs(3).name == "address")
    assert(createTableSpec.columnSpecs(3).dataType == VarcharType(200))
    assert(createTableSpec.columnSpecs(3).notNull == None)

    assert(createTableSpec.columnSpecs(4).name == "part_id")
    assert(createTableSpec.columnSpecs(4).dataType == IntegerType)
    assert(createTableSpec.columnSpecs(4).foreignKey.get.refTable == Seq("department"))
    assert(createTableSpec.columnSpecs(4).foreignKey.get.refColumns == Seq("id"))

  }

  test("create table with table constraints with name") {
    val df = sparkSession.sql(
      """
        |CREATE TABLE IF NOT EXISTS customer.churn (
        | id int NOT NULL,
        | name varchar(200),
        | uid int,
        | part_id int,
        | CONSTRAINT pk_id PRIMARY KEY(id),
        | CONSTRAINT fk_part_id FOREIGN KEY(part_id) REFERENCES department(id),
        | CONSTRAINT unique_uid UNIQUE (uid)
        |)
        |namespace testlh
        |""".stripMargin)
  }

  test("create table with table constraints without name") {
    val df = sparkSession.sql(
      """
        |CREATE TABLE IF NOT EXISTS customer.churn (
        | id int NOT NULL,
        | name varchar(200),
        | uid int,
        | part_id int,
        | PRIMARY KEY(id)
        |)
        |namespace testlh
        |""".stripMargin)
  }

  test("create table with data quality hint") {
    val df = sparkSession.sql(
      """
        |/*+ @DataQuality(name="name_length", expression="length(name) > 10") */
        |/*+ @DataQuality(name="part_id", expression="part_id > 0") */
        |CREATE TABLE IF NOT EXISTS customer.churn (
        | id int NOT NULL,
        | name varchar(200),
        | uid int,
        | part_id int,
        | PRIMARY KEY(id)
        |)
        |namespace testlh
        |""".stripMargin)
  }

  test("create table with access control hint") {
    val df = sparkSession.sql(
      """
        | /*+ @AccessControl(accessType = "regex", regex = "aa", users = "*", groups = "*") */
        | CREATE TABLE IF NOT EXISTS customer.churn (
        | id int NOT NULL,
        | name varchar(200),
        | uid int,
        | part_id int,
        | PRIMARY KEY(id)
        |)
        |namespace testlh
        |""".stripMargin)
  }

}
