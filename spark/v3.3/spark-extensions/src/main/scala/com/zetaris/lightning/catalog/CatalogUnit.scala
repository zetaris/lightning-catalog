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

import com.zetaris.lightning.execution.command.DataSourceType.{AVRO, CSV, DELTA, ICEBERG, JDBC, JSON, ORC, PARQUET}
import com.zetaris.lightning.model.LightningModel
import com.zetaris.lightning.model.serde.DataSource.DataSource
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType

object CatalogUnit {
  def apply(dataSource: DataSource): CatalogUnit = {
    val properties = dataSource.properties.map { prop =>
      prop.key -> prop.value
    }.toMap

    dataSource.dataSourceType match {
      case JDBC => JDBCDataSourceCatalogUnit(dataSource.name, properties)
      case ICEBERG => IcebergCatalogUnit(dataSource.name, properties)
      case DELTA => DeltaCatalogUnit(dataSource.name, properties)
      case PARQUET | ORC | AVRO | CSV | JSON => FileCatalogUnit(dataSource, LightningModel.cached)
      case _ => ???
    }
  }

  trait CatalogUnit {
    def listNamespaces(namespace: Array[String]): Array[Array[String]]

    def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit

    def listTables(namespace: Array[String]): Array[Identifier]

    def loadTable(ident: Identifier): Table

    def namespaceExists(namespace: Array[String]): Boolean

    def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean

    def createTable(ident: Identifier,
                    schema: StructType,
                    partitions: Array[Transform],
                    properties: java.util.Map[String, String]): Table

    def dropTable(ident: Identifier): Boolean

    def tableExists(ident: Identifier): Boolean
  }

}
