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

package com.zetaris.lightning.execution.command

import com.zetaris.lightning.catalog.LightningSource
import com.zetaris.lightning.model.{LightningModel, LightningModelFactory}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.command.LeafRunnableCommand
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters.mapAsJavaMap
import scala.util.{Failure, Success, Try}

abstract class LightningCommandBase extends LeafRunnableCommand with LightningSource {
  override def makeCopy(newArgs: Array[AnyRef]): LogicalPlan = this

  def beforeRun(sparkSession: SparkSession): Unit = {}

  def afterRun(sparkSession: SparkSession, results: Seq[Row]): Seq[Row] = {
    results
  }

  def runCommand(sparkSession: SparkSession): Seq[Row]

  protected def dataSourceConfigMap(sparkSession: SparkSession): CaseInsensitiveStringMap = {
    val sparkConf = sparkSession.sparkContext.getConf
    new CaseInsensitiveStringMap(
      mapAsJavaMap(sparkConf.getAllWithPrefix(s"${LIGHTNING_CATALOG}.").toMap)
    )
  }

  protected def validateNamespace(model: LightningModel, namespace: Array[String]): Unit = {
    val meta = model.loadNamespaceMeta(namespace)
  }

  protected def checkTableNamespaceLen(table: Seq[String]): Unit = {
    if (table.size < 3) {
      throw new RuntimeException(s"table name identifier should be at least 3 level")
    }
  }

  /**
   * validate table
   * @param model
   * @param table should start without "lightning" prefix
   */
  protected def validateTable(model: LightningModel, table: Array[String]): Unit = {
    checkTableNamespaceLen(table)
    val ident = new Identifier {
      override def namespace(): Array[String] = table.dropRight(1)

      override def name(): String = table.last
    }
    val saved = model.loadTable(ident)

    assert(saved.name().equalsIgnoreCase(toFqn("lightning" +: table)))
  }

  /**
   * Check table is activated
   * @param model
   * @param table should start without "lightning" prefix
   */
  protected def makeSureTableActivated(model: LightningModel, table: Array[String]): Unit = {
    model.loadUnifiedSemanticLayerTableQuery(table.dropRight(1), table.last)
  }

  protected def saveJson(model: LightningModel, namespace: Array[String], fileName: String): Unit = {
    val withoutCatalog = namespace.drop(1)
    val parentNamespace = withoutCatalog.dropRight(1)
    val lastNamespace = namespace.last

    if (!model.listNamespaces(parentNamespace).exists(_.equalsIgnoreCase(lastNamespace))) {
      throw new RuntimeException(s"parent namespace: ${namespace.mkString(".")} is not existing")
    }
  }

  override def run(sparkSession: SparkSession): Seq[Row] = {
    beforeRun(sparkSession)
    afterRun(sparkSession, runCommand(sparkSession))
  }
}
