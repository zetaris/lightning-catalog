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

package com.zetaris.lightning.execution.command

import com.zetaris.lightning.catalog.LightningSource
import com.zetaris.lightning.model.LightningModelFactory
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType

case class LoadUSL(namespace: Seq[String], name: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("json", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    checkTableNamespaceLen(namespace)

    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val usl = model.loadUnifiedSemanticLayer(namespace.drop(1), name)
    val json = UnifiedSemanticLayer.toJson(usl.namespace, usl.name, usl.tables)

    Row(json) :: Nil
  }
}

case class UpdateUSL(namespace: Seq[String], name: String, json: String)
  extends LightningCommandBase with LightningSource{
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("updated", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    checkTableNamespaceLen(namespace)
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))

    val namespaceWithoutPrefix = namespace.drop(1)
    model.loadUnifiedSemanticLayer(namespaceWithoutPrefix, name)
    val usl = UnifiedSemanticLayer(json)
    model.saveUnifiedSemanticLayer(namespaceWithoutPrefix, name, usl.tables)

    Row(s"${toFqn(namespace)}.$name") :: Nil
  }
}
