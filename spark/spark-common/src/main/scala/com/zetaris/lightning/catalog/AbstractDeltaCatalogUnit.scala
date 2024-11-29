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

import com.zetaris.lightning.model.HdfsFileSystem
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.types.StructType

abstract class AbstractDeltaCatalogUnit(properties: Map[String, String]) extends CatalogUnit {
  val SESSION_CATALOG_NAME: String = "spark_catalog"

  protected def tablePath(table: String): String = {
    val path = properties("path")
    if (path.endsWith("/")) {
      s"$path$table}"
    } else {
      s"$path/${table}"
    }
  }

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    throw new RuntimeException("delta lake doesn't support list namespaces")
  }

  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    throw new RuntimeException("delta lake doesn't support create namespace")
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    val path = properties("path")
    val parentAndChild = HdfsFileSystem.toFolderUrl(path)
    val fs = new HdfsFileSystem(properties, parentAndChild._1)
    val dirs = fs.listDirectories(parentAndChild._2)
    dirs.filter(!_.startsWith(".")).map(Identifier.of(namespace, _)).toArray
  }

  override def namespaceExists(namespace: Array[String]): Boolean = {
    throw new RuntimeException("delta lake doesn't support namespace exists")
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    throw new RuntimeException("delta lake doesn't support drop namespace")
  }

  override def createTable(ident: Identifier, schema: StructType, partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table

  override def dropTable(ident: Identifier): Boolean = {
    val fullPath = tablePath(ident.name())

    val fs = new HdfsFileSystem(properties, fullPath)
    fs.deleteDirectory(fullPath)

    //FileSystemUtils.deleteDirectory(fullPath)
    true
  }

  override def tableExists(ident: Identifier): Boolean

}

