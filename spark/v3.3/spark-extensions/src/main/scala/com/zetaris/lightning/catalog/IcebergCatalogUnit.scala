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

package com.zetaris.lightning.catalog

import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters._

// we're using different iceberg version along with spark version.
case class IcebergCatalogUnit(dsName: String, properties: Map[String, String]) extends CatalogUnit {
  val icebergSparkCatalog = {
    val className = "org.apache.iceberg.spark.SparkCatalog"
    val catalog = Class.forName(className).newInstance.asInstanceOf[org.apache.iceberg.spark.SparkCatalog]
    catalog.initialize(dsName, new CaseInsensitiveStringMap(mapAsJavaMap(properties)))
    catalog
  }

  // namespace is starting with lightning.datasource(.namespace)*
  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    icebergSparkCatalog.listNamespaces()
  }

  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    icebergSparkCatalog.createNamespace(namespace, metadata)
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    val fromSchema = Array(namespace.last)
    icebergSparkCatalog.listTables(fromSchema)
  }

  override def loadTable(ident: Identifier): Table = {
    icebergSparkCatalog.loadTable(ident)
  }

  override def namespaceExists(namespace: Array[String]): Boolean = {
    icebergSparkCatalog.namespaceExists(namespace)
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    icebergSparkCatalog.dropNamespace(namespace, cascade)
  }

  override def createTable(ident: Identifier,
                  schema: StructType,
                  partitions: Array[Transform],
                  properties: java.util.Map[String, String]): Table = {
    icebergSparkCatalog.createTable(ident, schema, partitions, properties)
  }

  override def dropTable(ident: Identifier): Boolean = icebergSparkCatalog.dropTable(ident)

  override def tableExists(ident: Identifier): Boolean = {
    icebergSparkCatalog.tableExists(ident)
  }
}
