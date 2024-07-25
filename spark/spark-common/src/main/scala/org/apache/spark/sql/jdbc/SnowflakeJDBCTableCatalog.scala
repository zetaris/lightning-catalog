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

import org.apache.spark.sql.catalyst.analysis.NoSuchTableException
import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JDBCRDD, JdbcUtils}
import org.apache.spark.sql.execution.datasources.v2.jdbc.{JDBCTable, JDBCTableCatalog}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.sql.SQLException
import scala.collection.JavaConverters._

class SnowflakeJDBCTableCatalog extends JDBCTableCatalog {
  private var options: JDBCOptions = _

  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = {
    super.initialize(name, options)
    val map = options.asCaseSensitiveMap().asScala.toMap
    this.options = new JDBCOptions(map + (JDBCOptions.JDBC_TABLE_NAME -> "__invalid_dbtable"))
  }

  override def loadTable(ident: Identifier): Table = {
    val tableName = (ident.namespace() :+ ident.name()).map(_.toUpperCase).map(SnowflakeDialect.quoteIdentifier).mkString(".")
    val optionsWithTableName = new JDBCOptions(
      options.parameters ++ CaseInsensitiveMap(Map(JDBCOptions.JDBC_TABLE_NAME -> tableName)))
    try {
      val schema = JDBCRDD.resolveTable(optionsWithTableName)
      JDBCTable(ident, schema, optionsWithTableName)
    } catch {
      case _: SQLException => throw new NoSuchTableException(ident)
    }
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    JdbcUtils.withConnection(options) { conn =>
      val catalog = if (namespace.length > 1) {
        namespace.head
      } else {
        null
      }
      val schemaPattern = if (namespace.length > 1) {
        namespace.last
      } else {
        namespace.head
      }

      val rs = conn.getMetaData
        .getTables(catalog.toUpperCase, schemaPattern.toUpperCase, "%", Array("TABLE"))
      new Iterator[Identifier] {
        def hasNext = rs.next()
        def next() = {
          Identifier.of(namespace, rs.getString("TABLE_NAME"))
        }
      }.toArray
    }
  }
}
