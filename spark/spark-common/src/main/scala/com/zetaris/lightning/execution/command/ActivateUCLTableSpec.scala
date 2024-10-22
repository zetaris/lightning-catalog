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

import com.zetaris.lightning.model.LightningModelFactory
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer.UnifiedSemanticLayerException
import com.zetaris.lightning.model.serde.UnifiedSemanticLayerTable
import org.apache.spark.sql.{Row, SparkSQLBridge, SparkSession}
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType

import scala.util.{Failure, Success, Try}

case class ActivateUCLTableSpec(table: Seq[String], query: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("registered", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    checkTableNamespaceLen(table)

    // lightning.metadata.(namespace)*.ucl.table
    val namespace = table.dropRight(2).drop(1)
    val uslName = table.dropRight(1).last
    val tableName = table.last

    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val usl = model.loadUnifiedSemanticLayer(namespace, uslName)
    val createTableSpec = usl.tables.find(_.name.equalsIgnoreCase(tableName)).getOrElse(
      throw UnifiedSemanticLayerException(s"${toFqn(table)} does not exist", null)
    )

    val analysedPlan = Try {
      sparkSession.sessionState.sqlParser.parsePlan(query)
    } match {
      case Failure(exception) => throw UnifiedSemanticLayerException(s"Not valid sql : $query", exception)
      case Success(parsed) =>
        Try {
          sparkSession.sessionState.analyzer.execute(parsed)
        } match {
          case Failure(exception) => throw UnifiedSemanticLayerException("Analysis exception", exception)
          case Success(analysed) => analysed
        }
    }

    val registeredDataTypes = createTableSpec.columnSpecs.map(_.dataType)
    val queriedDataTypes = analysedPlan.schema.map(_.dataType)

    if (queriedDataTypes.size != registeredDataTypes.size) {
      throw UnifiedSemanticLayerException("schema mismatch", null)
    }

    for (index <- 0 to registeredDataTypes.size - 1) {
      val defined = registeredDataTypes(index)
      val queried = queriedDataTypes(index)

      if(!dataTypeQueryable(defined, queried)) {
        throw UnifiedSemanticLayerException(s"datatype mismatch, defined: $defined, query: $queried", null)
      }
    }

    model.saveUnifiedSemanticLayerTableQuery(namespace, tableName, query)
    val json = UnifiedSemanticLayerTable.toJson(tableName, query)

    Row(json) :: Nil
  }

}
