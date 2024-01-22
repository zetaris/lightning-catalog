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

package org.apache.spark.sql.jdbc

import org.apache.spark.sql.execution.datasources.jdbc.JDBCOptions

import java.sql.Connection

object SnowflakeDialect extends JdbcDialect {
  override def canHandle(url: String): Boolean = true

  def schemasExists(conn: Connection, database: String, schema: String): Boolean = {
    val rs = conn.getMetaData.getSchemas()

    while (rs.next()) {
      val srcDb = rs.getString(2)
      val srcSchema = rs.getString(1)

      if (database == srcDb &&  schema == srcSchema) return true;
    }
    false
  }


  override def schemasExists(conn: Connection, options: JDBCOptions, database: String): Boolean = {
    val rs = conn.getMetaData.getSchemas()

    while (rs.next()) {
      val db = rs.getString(2)
      val schema = rs.getString(1)

      if (database == db) return true;
    }
    false
  }

  override def listSchemas(conn: Connection, options: JDBCOptions): Array[Array[String]] = {
    val rs = conn.getMetaData.getSchemas()
    val namespace = options.parameters.getOrElse("namespace", "/")
    val dbs = scala.collection.mutable.Set.empty[String]

    while (rs.next()) {
      val db = rs.getString(2)
      val schema = rs.getString(1)
      if (namespace == "/") {
        // get database
        dbs += db
      } else if (db == namespace) {
        dbs += schema
      }
    }

    dbs.map { db =>
      Array(db)
    }.toArray
  }

}
