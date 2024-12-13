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

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.connector.catalog.Table
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.delta.catalog.DeltaCatalog
import org.apache.spark.sql.types.StructType

// we're using different version along with spark version.
case class DeltaCatalogUnit(dsName: String, properties: Map[String, String])
  extends AbstractDeltaCatalogUnit(properties) {

  val deltaCatalog: DeltaCatalog = {
    val delegate = SparkSession.active.sessionState.catalogManager.catalog(SESSION_CATALOG_NAME)
    val catalog = new DeltaCatalog()
    catalog.setDelegateCatalog(delegate)
    catalog
  }

  override def loadTable(ident: Identifier, tagSchema: StructType): Table = {
    val table = tablePath(ident.name())
    deltaCatalog.loadTable(Identifier.of(Array("delta"), table))
  }

  override def createTable(ident: Identifier, schema: StructType, partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table = {
    val table = tablePath(ident.name())
    val withProvider = new java.util.HashMap[String, String](properties)
    withProvider.put("provider", "delta")
    deltaCatalog.createTable(Identifier.of(Array("delta"), table), schema, partitions, withProvider)
  }

  override def tableExists(ident: Identifier): Boolean = {
    val table = tablePath(ident.name())
    deltaCatalog.tableExists(Identifier.of(Array("delta"), table))
  }
}
