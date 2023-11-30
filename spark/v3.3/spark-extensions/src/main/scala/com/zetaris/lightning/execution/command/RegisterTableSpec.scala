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

import com.zetaris.lightning
import com.zetaris.lightning.error.LightningException.LightningSchemaNotMatchException
import com.zetaris.lightning.error.LightningException.LightningSQLAnalysisException
import com.zetaris.lightning.error.LightningException.LightningSQLParserException
import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType

import scala.util.Failure
import scala.util.Success
import scala.util.Try

case class RegisterTableSpec(fqn: Seq[String], query: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("registered", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val lightningModel = lightning.model.LightningModel(
      dataSourceConfigMap(s"${LightningModel.LIGHTNING_CATALOG}.", sparkSession))
    val lakeHouse = fqn.head
    val tableName = fqn.tail
    val createTableSpec = lightningModel.loadTableSpec(lakeHouse, tableName)

    val analysedPlan = Try {
      sparkSession.sessionState.sqlParser.parsePlan(query)
    } match {
      case Failure(exception) => throw new LightningSQLParserException("register sql", exception)
      case Success(parsed) =>
        Try {
          sparkSession.sessionState.analyzer.execute(parsed)
        } match {
          case Failure(exception) => throw new LightningSQLAnalysisException("table adn column", exception)
          case Success(analysed) => analysed
        }
    }

    val registeredDataTypes = createTableSpec.columnSpecs.map(_.dataType)
    val queriedDataTypes = analysedPlan.schema.map(_.dataType)

    if (queriedDataTypes.size < registeredDataTypes.size) {
      throw new LightningSchemaNotMatchException("")
    }

    for (index <- 0 to registeredDataTypes.size - 1) {
      val defined = registeredDataTypes(index)
      val queried = queriedDataTypes(index)
      if (!LightningModel.dataTypeQueryable(defined, queried)) {
        throw new LightningSchemaNotMatchException(s"defined: $defined, query: $queried")
      }
    }

    val path = lightningModel.saveRegisterTable(this, true)
    Row(path) :: Nil
  }
}
