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

import com.zetaris.lightning.execution.command.ReferenceControl.ReferenceControl
import com.zetaris.lightning.model.serde.CreateTable
import com.zetaris.lightning.parser.LightningParserUtils
import org.apache.spark.sql.catalyst.expressions.{AttributeReference, Expression}
import org.apache.spark.sql.catalyst.parser.ParserInterface
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DataType, StringType}

import scala.util.{Failure, Success, Try}

case class NullableColumn(columns: Seq[String] = Seq.empty, name: Option[String] = None)

case class NotNullColumn(columns: Seq[String] = Seq.empty, name: Option[String] = None)

case class PrimaryKeyColumn(columns: Seq[String], name: Option[String])

case class UniqueKeyColumn(columns: Seq[String], name: Option[String])

object ReferenceControl {
  def apply(control: String): ReferenceControl = {
    control.toLowerCase match {
      case "restrict" => Restrict
      case "cascade" => Cascade
      case "setnull" => SetNull
      case "noaction" => NoAction
      case "setdefault" => SetDefault
      case _ => throw new RuntimeException(s"unknown reference control : $control")
    }
  }

  sealed trait ReferenceControl

  case object Restrict extends ReferenceControl

  case object Cascade extends ReferenceControl

  case object SetNull extends ReferenceControl

  case object NoAction extends ReferenceControl

  case object SetDefault extends ReferenceControl
}

case class ForeignKey(columns: Seq[String],
                      name: Option[String],
                      refTable: Seq[String],
                      refColumns: Seq[String],
                      onDelete: Option[ReferenceControl],
                      onUpdate: Option[ReferenceControl])

case class ColumnSpec(name: String,
                      dataType: DataType,
                      primaryKey: Option[PrimaryKeyColumn],
                      notNull: Option[NotNullColumn],
                      unique: Option[UniqueKeyColumn],
                      foreignKey: Option[ForeignKey],
                      accessControl: Option[AccessControl] = None)

class ColumnConstraintException(msg: String) extends RuntimeException(msg)

case class CreateTableSpec(name: String,
                           columnSpecs: Seq[ColumnSpec],
                           primaryKey: Option[PrimaryKeyColumn],
                           unique: Seq[UniqueKeyColumn],
                           foreignKeys: Seq[ForeignKey],
                           ifNotExit: Boolean,
                           namespace: Seq[String],
                           dqAnnotations: Seq[DataQuality] = Seq.empty,
                           acAnnotations: Seq[AccessControl] = Seq.empty) extends LightningCommandBase {
  @transient
  var dqAnnotationExpression: Seq[Expression] = Seq.empty
  val ctePlan = scala.collection.mutable.Map.empty[String, LogicalPlan]

  def withDQExpression(parser: ParserInterface): CreateTableSpec = {
    dqAnnotationExpression = dqAnnotations.flatMap { dq =>
      val variables = LightningParserUtils.extractVariables(dq.expression)
      if (variables.isEmpty) {
        Some(tryParse(dq.expression, parser.parseExpression))
      } else {
        variables.foreach { variable =>
          ctePlan += variable -> tryParse(dq.cte(variable), parser.parsePlan)
        }
        None
      }
    }

    this
  }

  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("json", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    Row(CreateTable.toJson(this)) :: Nil
  }

}
