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

import org.apache.spark.sql.catalyst.analysis.{NoSuchNamespaceException, NoSuchTableException}
import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.jdbc.{JDBCOptions, JDBCRDD, JdbcUtils}
import org.apache.spark.sql.execution.datasources.v2.jdbc.{JDBCTable, JDBCTableCatalog}
import org.apache.spark.sql.jdbc.SnowflakeDialect
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.sql.SQLException
import scala.collection.JavaConverters.mapAsJavaMap

case class JDBCDataSourceCatalogUnit(catalog: String, properties: Map[String, String]) extends CatalogUnit
  with LightningSource {

  private def buildJDBCTableCatalog(namespace: Array[String],
                                    extra: Map[String, String] = Map.empty): JDBCTableCatalog = {
    val schema = if (namespace.isEmpty) {
      catalog
    } else {
      namespace.last
    }

    val jdbcTableCatalog = new JDBCTableCatalog()
    val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(properties ++ extra))
    jdbcTableCatalog.initialize(schema, ciMap)

    jdbcTableCatalog
  }

  private def listMultiLevelNamespaces(namespace: Array[String]): Array[Array[String]] = {
    val namespaceMap = scala.collection.mutable.Map.empty[String, String]
    val jdbcTableCatalog = if (namespace.isEmpty) {
      namespaceMap += "namespace" -> "/"
      buildJDBCTableCatalog(namespace, namespaceMap.toMap)
    } else {
      namespaceMap += "namespace" -> toFqn(namespace)
      buildJDBCTableCatalog(namespace, namespaceMap.toMap)
    }

    namespace match {
      case Array() =>
        jdbcTableCatalog.listNamespaces()
      case Array(_) if jdbcTableCatalog.namespaceExists(namespace) =>
        val options = new JDBCOptions(CaseInsensitiveMap(
          properties ++ namespaceMap ++ Map(JDBCOptions.JDBC_TABLE_NAME -> "__invalid_dbtable")))

        JdbcUtils.withConnection(options) { conn =>
          JdbcUtils.listSchemas(conn, options)
        }
      case _ if namespace.length == 2 =>
        val options = new JDBCOptions(CaseInsensitiveMap(
          properties ++ namespaceMap ++ Map(JDBCOptions.JDBC_TABLE_NAME -> "__invalid_dbtable")))
        JdbcUtils.withConnection(options) { conn =>
          if (SnowflakeDialect.schemasExists(conn, namespace(0), namespace(1))) {
            Array()
          } else {
            throw new NoSuchNamespaceException(namespace)
          }
        }
      case _ => throw new NoSuchNamespaceException(namespace)
    }
  }

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    val url = properties("url").toLowerCase

    if (url.startsWith("jdbc:snowflake") || url.startsWith("jdbc:redshift")) {
      listMultiLevelNamespaces(namespace)
    } else {
      val jdbcTableCatalog = buildJDBCTableCatalog(namespace)
      jdbcTableCatalog.listNamespaces(namespace)
    }
  }

  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    val jdbcTableCatalog = buildJDBCTableCatalog(namespace)
    jdbcTableCatalog.createNamespace(namespace, metadata)
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    if (namespace.isEmpty) {
      Array()
    } else {
      val fromSchema = Array(namespace.last)
      val jdbcTableCatalog = buildJDBCTableCatalog(namespace)
      jdbcTableCatalog.listTables(fromSchema)
    }
  }

  private def loadSnowflakeTable(ident: Identifier): Table = {
    val tableName = (ident.namespace() :+ ident.name()).map(SnowflakeDialect.quoteIdentifier).mkString(".")
    val optionsWithTableName = new JDBCOptions(CaseInsensitiveMap(
      properties ++ Map(JDBCOptions.JDBC_TABLE_NAME -> tableName)))

    try {
      val schema = JDBCRDD.resolveTable(optionsWithTableName)
      JDBCTable(ident, schema, optionsWithTableName)
    } catch {
      case _: SQLException => throw new NoSuchTableException(ident)
    }
  }

  override def loadTable(ident: Identifier): Table = {
    val jdbcTableCatalog = buildJDBCTableCatalog(ident.namespace())
    if (properties("url").toLowerCase.startsWith("jdbc:snowflake")) {
      loadSnowflakeTable(ident)
    } else {
      val fromSchema = Array(ident.namespace().last)
      val tweakedIdent = new Identifier {
        override def namespace(): Array[String] = fromSchema

        override def name(): String = ident.name()
      }
      jdbcTableCatalog.loadTable(tweakedIdent)
    }
  }

  override def namespaceExists(namespace: Array[String]): Boolean = {
    val fromSchema = Array(namespace.last)
    val jdbcTableCatalog = buildJDBCTableCatalog(namespace)

    jdbcTableCatalog.namespaceExists(fromSchema)
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    val jdbcTableCatalog = buildJDBCTableCatalog(namespace)
    jdbcTableCatalog.dropNamespace(namespace, cascade)
  }

  override def createTable(ident: Identifier,
                           schema: StructType,
                           partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table = {
    val jdbcTableCatalog = buildJDBCTableCatalog(ident.namespace())
    jdbcTableCatalog.createTable(ident, schema, partitions, properties)
  }

  override def dropTable(ident: Identifier): Boolean = {
    val jdbcTableCatalog = buildJDBCTableCatalog(ident.namespace())
    jdbcTableCatalog.dropTable(ident)
  }

  override def tableExists(ident: Identifier): Boolean = {
    val jdbcTableCatalog = buildJDBCTableCatalog(ident.namespace())
    jdbcTableCatalog.tableExists(ident)
  }

}
