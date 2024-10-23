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

import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType

import java.util

case class LightningCatalogUnit(lakehouse: String, model: LightningModel) extends CatalogUnit {

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = ???

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    ???
  }

  override def loadTable(ident: Identifier, tagSchema: StructType): Table = {

    model.loadTable(ident)
  }

  override def createNamespace(namespace: Array[String], metadata: util.Map[String, String]): Unit = {
    ???
  }

  override def namespaceExists(namespace: Array[String]): Boolean = {
    ???
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    ???
  }

  override def createTable(ident: Identifier,
                           schema: StructType,
                           partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table = {
    ???
  }

  override def dropTable(ident: Identifier): Boolean = {
    ???
  }

  override def tableExists(ident: Identifier): Boolean = {
    ???
  }

}
