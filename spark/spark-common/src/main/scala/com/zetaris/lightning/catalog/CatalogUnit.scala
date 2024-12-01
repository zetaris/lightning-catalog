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

import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType

/**
 * common catalogue definition for each source inside Lightning Catalog
 * Lightning catalog supports custom tag schema on top of the exsiting schema, which be define when registering data source like:
 *
 * REGISTER OR REPLACE IMAGE DATASOURCE spark_images OPTIONS (
 *   path "${getClass.getResource("/image").getPath}",
 *   scanType "file_scan",
 *   pathGlobFilter "{*.png,*.jpg}",
 *   image_thumbnail_with "50",
 *   image_thumbnail_height "50"
 * ) NAMESPACE lightning.datasource.file
 * TAG (
 *   firstName String,
 *   lastName varchar(200),
 *   age int,
 *   fscore float,
 *   dscore double,
 *   time Timestamp,
 *   date Date
 * )
 *
 * these tag schema will be updated using update command
 * update lightning.datasource.file.spark_images set age = 10
 *
 */
trait CatalogUnit {

  /**
   * list name spaces under the given parent
   * @param namespace parent name space
   * @return
   */
  def listNamespaces(namespace: Array[String]): Array[Array[String]]

  /**
   * create name space for the given parent
   * @param namespace parent name space
   * @param metadata custom meta data
   */
  def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit

  /**
   * list tables under the given parent namespace
   * @param namespace parent name space
   * @return Array of identifier
   */
  def listTables(namespace: Array[String]): Array[Identifier]

  /**
   * load table definition with schema for custom tagging
   * @param ident identifier
   * @param tagSchema tagging schema if available, empty schema if not available
   * @return Table instance
   */
  def loadTable(ident: Identifier, tagSchema: StructType): Table

  /**
   * load table definition with the given version and custom tagging
   * @param ident identifier
   * @param version a specific version
   * @param tagSchema tagging schema if available, empty schema if not available
   * @return Table instance
   */
  def loadTable(ident: Identifier, version: String, tagSchema: StructType): Table = {
    throw new RuntimeException(
      s"time travel is not supported for this datasource : ${ident.namespace().mkString(".")}.${ident.name()}"
    )
  }

  /**
   * load table definition with the given timestamp for time traveling and custom tagging
   * @param ident identifier
   * @param version a specific version
   * @param tagSchema tagging schema if available, empty schema if not available
   * @return Table instance
   */
  def loadTable(ident: Identifier, timestamp: Long, tagSchema: StructType): Table = {
    throw new RuntimeException(
      s"time travel is not supported for this datasource : ${ident.namespace().mkString(".")}.${ident.name()}"
    )
  }

  /**
   * Check the given name space is existing
   * @param namespace
   * @return
   */
  def namespaceExists(namespace: Array[String]): Boolean

  /**
   * Drop namespace
   * @param namespace
   * @param cascade
   * @return
   */
  def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean

  /**
   * Create table
   * @param ident
   * @param schema
   * @param partitions
   * @param properties
   * @return
   */
  def createTable(ident: Identifier,
                  schema: StructType,
                  partitions: Array[Transform],
                  properties: java.util.Map[String, String]): Table

  /**
   * drop table
   * @param ident
   * @return
   */
  def dropTable(ident: Identifier): Boolean

  /**
   * check the given table is existing
   * @param ident
   * @return
   */
  def tableExists(ident: Identifier): Boolean

}
