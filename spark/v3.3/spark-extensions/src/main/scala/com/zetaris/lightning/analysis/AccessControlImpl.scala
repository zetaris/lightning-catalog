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
import com.zetaris.lightning.execution.command.{AccessControl, ColumnSpec, CreateTableSpec}
import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.catalyst.expressions.Alias
import org.apache.spark.sql.catalyst.expressions.Cast
import org.apache.spark.sql.catalyst.expressions.Literal
import org.apache.spark.sql.catalyst.expressions.RegExpReplace
import org.apache.spark.sql.catalyst.plans.logical.Filter
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.plans.logical.Project
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.execution.datasources.v2.DataSourceV2Relation

object AccessControlImpl extends Rule[LogicalPlan] {
  private def columnsWithAccessControl(lakehouse: String, table: String): Seq[ColumnSpec] = {
    LightningTableCache.getCreateTableSpec(lakehouse, table).map { tableSpec =>
      tableSpec.columnSpecs.filter(_.accessControl.isDefined)
    }.getOrElse(Seq.empty)
  }

  private def isLoggedUserAppliedTo(users: Seq[String], groups: Seq[String]): Boolean = {
    users.exists(_.trim.equals("*")) ||
      groups.exists(_.trim.equals("*")) ||
      LightningModel.accessControlProvider.isLoggedUserAppliedTo(users, groups)
  }

  private def applyAccessControl(prj: Project, lakehouse: String, table: String): LogicalPlan = {
    val accessControlColumns = columnsWithAccessControl(lakehouse, table)
    if (accessControlColumns.nonEmpty) {
      val withAccessControls = prj.projectList.map { col =>
        accessControlColumns.find(_.name.equalsIgnoreCase(col.name)) match {
          case Some(ColumnSpec(_, _, _, _, _, _, Some(AccessControl(accessType, _, users, groups))))
            if accessType.toLowerCase == "deny" && isLoggedUserAppliedTo(users, groups) =>
            Alias(Cast(Literal(null), col.dataType), col.name)()
          case Some(ColumnSpec(_, _, _, _, _, _, Some(AccessControl(accessType, regEx, users, groups))))
            if accessType.toLowerCase == "regex" && isLoggedUserAppliedTo(users, groups) =>
            val expression = RegExpReplace(col, Literal(regEx.get), Literal("*"))
            Alias(expression, col.name)()
          case _ => col
        }
      }

      prj.copy(projectList = withAccessControls)
    } else {
      prj
    }
  }

  override def apply(plan: LogicalPlan): LogicalPlan = {
    LightningTableCache.loadLakeHouseTablesIfNotLoaded(plan.conf)
    plan.transform {
      case prj@Project(_, DataSourceV2Relation(LightningTable(
      CreateTableSpec(fqn, _, _, _, _, _, Some(lakehouse), _), _), _, _, _, _)) =>
        applyAccessControl(prj, lakehouse, LightningModel.toFqn(fqn))
      case prj@Project(_, Filter(_, DataSourceV2Relation(LightningTable(
      CreateTableSpec(fqn, _, _, _, _, _, Some(lakehouse), _), _), _, _, _, _))) =>
        applyAccessControl(prj, lakehouse, LightningModel.toFqn(fqn))
    }
  }
}
