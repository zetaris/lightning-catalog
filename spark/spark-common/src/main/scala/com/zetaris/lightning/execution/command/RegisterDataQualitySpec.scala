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

import com.zetaris.lightning.model.{LightningModel, LightningModelFactory}
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.catalyst.plans.logical.Filter
import org.apache.spark.sql.types.BooleanType
import org.apache.spark.sql.{Row, SparkSession}

case class RegisterDataQualitySpec(name: String, table: Seq[String], expression: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("registered", BooleanType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    checkTableNamespaceLen(table)
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val withoutPrefix = table.drop(1)
    validateTable(model, withoutPrefix.toArray)
    makeSureTableActivated(model, withoutPrefix.toArray)

    val uslFqn = withoutPrefix.dropRight(1)
    val uslName = uslFqn.last

    val ti = new TableIdentifier(table.last, Some(toFqn(uslFqn)), Some("lightning"))

    val exp = tryParse(expression, sparkSession.sessionState.sqlParser.parseExpression)
    val relation = sparkSession.sessionState.catalog.lookupRelation(ti)
    val filter = Filter(exp, relation)

    val analysed = sparkSession.sessionState.analyzer.execute(filter)
    Row(true) :: Nil
  }
}
