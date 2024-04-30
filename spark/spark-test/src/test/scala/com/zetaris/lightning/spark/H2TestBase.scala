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

package com.zetaris.lightning.spark

import java.sql.{Connection, DriverManager}

trait H2TestBase { self: SparkExtensionsTestBase =>

  val h2Driver = "org.h2.Driver"

  def buildH2Connection(db: String): java.sql.Connection = {
    val h2Url = s"jdbc:h2:mem:$db;DB_CLOSE_DELAY=-1"
    DriverManager.getConnection(h2Url)
  }

  def createH2SimpleTable(db: String, schema: String): Unit = {
    val conn = buildH2Connection(db)

    try{conn.prepareStatement(s"""DROP TABLE IF EXISTS "$schema"."test_users"""").executeUpdate()}
    catch {case _: Throwable =>}

    try{conn.prepareStatement(s"""DROP TABLE IF EXISTS "$schema"."test_jobs"""").executeUpdate()}
    catch {case _: Throwable =>}


    conn.prepareStatement(s"""DROP SCHEMA IF EXISTS "$schema"""").executeUpdate()

    conn.prepareStatement(s"""CREATE SCHEMA "$schema"""").executeUpdate()
    conn.prepareStatement(s"""create table "$schema"."test_users" (uid INTEGER primary key, jid integer)""").executeUpdate()
    conn.prepareStatement(s"""create table "$schema"."test_jobs" (jid INTEGER primary key, name CHAR(10))""").executeUpdate()

    conn.prepareStatement(s"""insert into "$schema"."test_users" values(1, 1)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_users" values(2, 2)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_users" values(3, 3)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_users" values(4, 4)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_users" values(5, 5)""").executeUpdate()

    conn.prepareStatement(s"""insert into "$schema"."test_jobs" values(1, 'job1')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_jobs" values(2, 'job2')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_jobs" values(3, 'job3')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_jobs" values(4, 'job4')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."test_jobs" values(5, 'job5')""").executeUpdate()
  }

  def createCustomerOrderTable(conn: Connection, schema: String): Unit = {
    conn.prepareStatement(s"""CREATE SCHEMA "$schema"""").executeUpdate()
    conn.prepareStatement(s"""create table "$schema"."customer" (id BIGINT primary key, name varchar(30), address varchar(50))""").executeUpdate()
    conn.prepareStatement(s"""create table "$schema"."lineitem" (id BIGINT primary key, name varchar(30), price decimal)""").executeUpdate()
    conn.prepareStatement(s"""create table "$schema"."order" (id BIGINT primary key,
         |cid BIGINT,
         |iid BIGINT,
         |item_count integer,
         |odate date,
         |otime time,
         |foreign key(cid) references "$schema"."customer"(id),
         |foreign key(iid) references "$schema"."lineitem"(id)
         |)""".stripMargin).executeUpdate()

    conn.prepareStatement(s"""insert into "$schema"."customer" values(1, 'chris lynch', '100 VIC')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."customer" values(2, 'wayne bourne', '200 NSW')""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."customer" values(3, 'scott mayson', '300 TAS')""").executeUpdate()

    conn.prepareStatement(s"""insert into "$schema"."lineitem" values(1, 'iphone', 100)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."lineitem" values(2, 'ipad', 50)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."lineitem" values(3, 'macbook', 150)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."lineitem" values(4, 'iwatch', 40)""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."lineitem" values(5, 'imac', 90)""").executeUpdate()

    conn.prepareStatement(s"""insert into "$schema"."order" values(1, 1, 1, 1, CURRENT_DATE(), CURRENT_TIME())""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."order" values(2, 2, 2, 2, CURRENT_DATE(), CURRENT_TIME())""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."order" values(3, 3, 3, 3, CURRENT_DATE(), CURRENT_TIME())""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."order" values(4, 1, 4, 4, CURRENT_DATE(), CURRENT_TIME())""").executeUpdate()
    conn.prepareStatement(s"""insert into "$schema"."order" values(5, 2, 5, 5, CURRENT_DATE(), CURRENT_TIME())""").executeUpdate()

  }

  protected def registerH2DataSource(dbName: String): Unit = {
    sparkSession.sql(s"DROP NAMESPACE IF EXISTS lightning.datasource.h2")
    sparkSession.sql(s"CREATE NAMESPACE lightning.datasource.h2")

    sparkSession.sql(s"""
                        |REGISTER OR REPLACE JDBC DATASOURCE $dbName OPTIONS(
                        | url "jdbc:h2:mem:$dbName;DB_CLOSE_DELAY=-1",
                        | user ""
                        |) NAMESPACE lightning.datasource.h2
                        |""".stripMargin)
  }
}
