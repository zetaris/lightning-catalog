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

import CatalogUnit.CatalogUnit
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.v2.jdbc.JDBCTableCatalog
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters.mapAsJavaMap

case class JDBCDataSourceCatalogUnit(catalog: String, properties: Map[String, String]) extends CatalogUnit {

  private def buildJDBCTableCatalog(namespace: Array[String]): JDBCTableCatalog = {
    val schema = if (namespace.isEmpty) {
      catalog
    } else {
      namespace.last
    }

    val jdbcTableCatalog = new JDBCTableCatalog()
    val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(properties))
    jdbcTableCatalog.initialize(schema, ciMap)

    jdbcTableCatalog
  }

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    val jdbcTableCatalog = buildJDBCTableCatalog(namespace)
    jdbcTableCatalog.listNamespaces(namespace)
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

  override def loadTable(ident: Identifier): Table = {
    val fromSchema = Array(ident.namespace().last)
    val jdbcTableCatalog = buildJDBCTableCatalog(ident.namespace())

    val newIdent = new Identifier {
      override def namespace(): Array[String] = fromSchema
      override def name(): String = ident.name()
    }
    jdbcTableCatalog.loadTable(newIdent)
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
