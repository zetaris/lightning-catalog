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

package com.zetaris.lightning.analysis

import com.zetaris.lightning.datasources.v2.LightningTable
import com.zetaris.lightning.execution.command.CreateTableSpec
import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.{And, Expression}
import org.apache.spark.sql.catalyst.plans.logical.Filter
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.plans.logical.Project
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

object DataQualityImpl extends Rule[LogicalPlan] {
  private def dataQualityExpressions(lakehouse: String, table: String): Seq[Expression] = {
    LightningTableCache.getCreateTableSpec(lakehouse, table)
      .map(_.dqAnnotationExpression)
      .getOrElse(Seq.empty)
  }

  private def applyDataQuality(prj: Project, filter: Option[Filter], lakehouse: String, table: String): LogicalPlan = {
    val dqExps = dataQualityExpressions(lakehouse, table)
    val combined = dqExps.foldLeft[And](And(null, null)) {
      case (accumulator, coming) =>
        accumulator match {
          case and@And(left, _) if left == null => and.copy(left = coming)
          case and@And(_, right) if right == null => and.copy(right = coming)
          case and => And(and, null)
        }
    } match {
      case And(left, right) if left == null && right == null => None
      case And(left, right) if right == null => Some(left)
      case and@And(_, _) => Some(and)
    }

    combined.map { dqExp =>
      filter.map { exiting =>
        exiting.copy(condition = And(dqExp, exiting.condition))
      }.getOrElse {
        Filter(dqExp, prj.child)
      }
    }.map { filterWithDq =>
      val planWithDq = prj.copy(child = filterWithDq)
      val analysed = SparkSession.getActiveSession.get.sessionState.analyzer.execute(planWithDq)
      analysed
    }.getOrElse(prj)

  }

  override def apply(plan: LogicalPlan): LogicalPlan = {
    LightningTableCache.loadLakeHouseTablesIfNotLoaded(plan.conf)
    plan.transform {
      case prj@Project(_, DataSourceV2Relation(LightningTable(
      CreateTableSpec(fqn, _, _, _, _, _, Some(lakehouse), _), _), _, _, _, _)) =>
        applyDataQuality(prj, None, lakehouse, LightningModel.toFqn(fqn))
      case prj@Project(_, filter@Filter(_, DataSourceV2Relation(LightningTable(
      CreateTableSpec(fqn, _, _, _, _, _, Some(lakehouse), _), _), _, _, _, _))) =>
        applyDataQuality(prj, Some(filter), lakehouse, LightningModel.toFqn(fqn))
    }
  }
}
