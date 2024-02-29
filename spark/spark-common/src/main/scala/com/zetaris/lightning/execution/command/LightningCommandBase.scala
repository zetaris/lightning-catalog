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
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.execution.command.LeafRunnableCommand
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters.mapAsJavaMap

abstract class LightningCommandBase extends LeafRunnableCommand with LightningSource {
  override def makeCopy(newArgs: Array[AnyRef]): LogicalPlan = this

  def beforeRun(sparkSession: SparkSession): Unit = {}

  def afterRun(sparkSession: SparkSession, results: Seq[Row]): Seq[Row] = {
    results
  }

  def runCommand(sparkSession: SparkSession): Seq[Row]

  protected def dataSourceConfigMap(prefix: String, sparkSession: SparkSession): CaseInsensitiveStringMap = {
    val sparkConf = sparkSession.sparkContext.getConf
    new CaseInsensitiveStringMap(
      mapAsJavaMap(sparkConf.getAllWithPrefix(s"${LIGHTNING_CATALOG}.").toMap)
    )
  }

  override def run(sparkSession: SparkSession): Seq[Row] = {
    beforeRun(sparkSession)
    afterRun(sparkSession, runCommand(sparkSession))
  }
}
