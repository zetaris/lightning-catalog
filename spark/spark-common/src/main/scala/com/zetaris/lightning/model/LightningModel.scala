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

package com.zetaris.lightning.model

import com.zetaris.lightning.model.serde.DataSource.DataSource
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.types._

/**
 * Interface of lightning model managing CRUD of entity in metastore
 */
trait LightningModel {

  /**
   * Save data source into sub directory of datasource
   *
   * @param dataSource
   * @param replace
   * @return saved data source
   */
  def saveDataSource(dataSource: DataSource, replace: Boolean): String

  /**
   * load data sources under the given namespace
   * @param namespace
   * @param name
   * @return list of namespace
   */
  def loadDataSources(namespace: Array[String], name: String = null): List[DataSource]

  /**
   * Drop datasource definition
   * @param namespace
   * @param name
   */
  def dropDataSource(namespace: Array[String], name: String): Unit

  /**
   * list namespaces under the given namespace
   * @param namespace
   * @return namespaces
   */
  def listNamespaces(namespace: Seq[String]): Seq[String]

  /**
   * list tables under the given namesapce
   * @param namespace
   * @return table names
   */
  def listTables(namespace: Array[String]): Seq[String]

  /**
   * save table under the given namespace
   * @param dsNamespace namespace of data source definition
   * @param namespace
   * @param name
   * @param schema
   */
  def saveTable(dsNamespace: Array[String], namespace: Array[String], name: String, schema: StructType): Unit

  /**
   * Create child namespace under the given namespace
   * @param namespace
   * @param metadata
   */
  def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit

  /**
   * drop namespace
   * @param namespace
   * @param cascade delete cascade if true
   */
  def dropNamespace(namespace: Array[String], cascade: Boolean): Unit

  /**
   * load table
   * @param ident
   * @return
   */
  def loadTable(ident: Identifier): Table

}
