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
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.model.{DataQualityDuplicatedException, DataQualityNotFoundException, LightningModelFactory, TableNotFoundException}
import org.apache.spark.sql.catalyst.QueryPlanningTracker
import org.apache.spark.sql.catalyst.analysis.UnresolvedRelation
import org.apache.spark.sql.catalyst.expressions.{AttributeReference, Not}
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LogicalPlan}
import org.apache.spark.sql.types.{LongType, StringType}
import org.apache.spark.sql.{DataFrame, Row, SparkSQLBridge, SparkSession}

import scala.util.{Failure, Success, Try}

object DataQualitySpec extends LightningSource {
  def validateExpressions(sparkSession: SparkSession, table: Seq[String], expression: String): Unit = {
    val unresolved = UnresolvedRelation(table)

    val exp = tryParse(expression, sparkSession.sessionState.sqlParser.parseExpression)
    val filter = Filter(exp, unresolved)
    val analysed = sparkSession.sessionState.analyzer.execute(filter)
    val optimised = sparkSession.sessionState.optimizer.execute(analysed)

    sparkSession.sessionState.planner.plan(optimised)
  }

  /**
   * run dq
   *
   * @param sparkSession
   * @param table
   * @param expression
   * @return total record, data frame for good record, bad record
   */
  def runDQ(sparkSession: SparkSession, table: Seq[String], expression: String): (Long, DataFrame, DataFrame) = {
    val tracker = new QueryPlanningTracker

    val totRecord = sparkSession.sql(s"select count(*) from ${toFqn(table)}").collect()(0).getLong(0)

    val unresolved = UnresolvedRelation(table)
    val exp = tryParse(expression, sparkSession.sessionState.sqlParser.parseExpression)
    val goodFilter = Filter(exp, unresolved)

    val goodRecord = SparkSQLBridge.ofRows(sparkSession,
      sparkSession.sessionState.analyzer.execute(goodFilter), tracker)

    val badFilter = Filter(Not(exp), unresolved)
    val badRecord = SparkSQLBridge.ofRows(sparkSession,
      sparkSession.sessionState.analyzer.execute(badFilter), tracker)

    (totRecord, goodRecord, badRecord)
  }

  def tryParse[T](sqlText: String, f: String => T): T = {
    Try {
      f(sqlText)
    } match {
      case Success(parsed) => parsed
      case Failure(exception) => throw exception
    }
  }

}

case class RegisterDataQualitySpec(name: String, table: Seq[String], expression: String) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("json", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    checkTableNamespaceLen(table)
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val withoutCatalog = table.drop(1)
    validateTable(model, withoutCatalog.toArray)
    makeSureTableActivated(model, withoutCatalog.toArray)

    DataQualitySpec.validateExpressions(sparkSession, table, expression)

    val uslName = table.dropRight(1).last
    val uslNameSpace = table.dropRight(2).drop(1)

    val usl = model.loadUnifiedSemanticLayer(uslNameSpace, uslName)
    val createTableSpec = usl.tables.find(_.name.equalsIgnoreCase(table.last)).get
    val existing = createTableSpec.dqAnnotations.find(_.name.equalsIgnoreCase(name))

    if (existing.isDefined) {
      throw DataQualityDuplicatedException(s"$name has been already registered")
    }

    val dqAdded = createTableSpec.dqAnnotations :+ DataQuality(name, expression)
    val tableSpec = usl.tables diff Seq(createTableSpec)
    val tableSpecWithDq = tableSpec :+ createTableSpec.copy(dqAnnotations = dqAdded)

    model.saveUnifiedSemanticLayer(uslNameSpace, uslName, tableSpecWithDq)

    val json = UnifiedSemanticLayer.toJson(uslNameSpace, uslName, tableSpecWithDq)
    Row(json) :: Nil
  }
}

case class ListDataQualitySpec(uslNamespace: Seq[String]) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("name", StringType, false)(),
    AttributeReference("table", StringType, false)(),
    AttributeReference("type", StringType, false)(),
    AttributeReference("expression", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val uslName = uslNamespace.last
    val uslNameSpace = uslNamespace.dropRight(1).drop(1)
    val usl = model.loadUnifiedSemanticLayer(uslNameSpace, uslName)
    usl.tables.flatMap { createTableSpec =>
      createTableSpec.primaryKey.map { pk =>
        Row(pk.columns.mkString(","), createTableSpec.name, "Primary key constraints", "")
      } ++ createTableSpec.columnSpecs.find(_.primaryKey.isDefined).map { pk =>
        Row(pk.name, createTableSpec.name, "Primary key constraints", "")
      } ++ createTableSpec.foreignKeys.map { fk =>
        Row(fk.columns.mkString(","), createTableSpec.name, "Foreign key constraints", "")
      } ++ createTableSpec.columnSpecs.find(_.foreignKey.isDefined).map { fk =>
        Row(fk.name, createTableSpec.name, "Foreign key constraints", "")
      } ++ createTableSpec.unique.map { uk =>
        Row(uk.columns.mkString(","), createTableSpec.name, "Unique constraints", "")
      } ++ createTableSpec.columnSpecs.find(_.unique.isDefined).map { uk =>
        Row(uk.name, createTableSpec.name, "Unique key constraints", "")
      } ++ createTableSpec.dqAnnotations.map { dq =>
        Row(dq.name, createTableSpec.name, "Custom DQ", dq.expression)
      }
    }
  }
}

case class RunDataQualitySpec(name: Option[String], table: Seq[String]) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("name", StringType, false)(),
    AttributeReference("table", StringType, false)(),
    AttributeReference("total_record", LongType, false)(),
    AttributeReference("good_record", LongType, false)(),
    AttributeReference("bad_record", LongType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val tableName = table.last
    val uslName = table.dropRight(1).last
    val uslNameSpace = table.dropRight(2).drop(1)
    val usl = model.loadUnifiedSemanticLayer(uslNameSpace, uslName)

    val createTableSpec = usl.tables.find(_.name.equalsIgnoreCase(tableName)).getOrElse(
      throw TableNotFoundException(s"${toFqn(table)} is not defined")
    )

    if (name.isDefined) {
      val dq = createTableSpec.dqAnnotations.find(_.name.equalsIgnoreCase(name.get)).getOrElse(
        throw DataQualityNotFoundException(s"${name.get} is not found in table ${toFqn(table)}")
      )
      val dqStat = DataQualitySpec.runDQ(sparkSession,
          createTableSpec.namespace :+ createTableSpec.name, dq.expression)
      Row(dq.name, createTableSpec.name, dqStat._1, dqStat._2.count(), dqStat._3.count()) :: Nil
    } else {
      createTableSpec.dqAnnotations.map { dq =>
        val dqStat = DataQualitySpec.runDQ(sparkSession,
          createTableSpec.namespace :+ createTableSpec.name, dq.expression)
        Row(dq.name, createTableSpec.name, dqStat._1, dqStat._2.count(), dqStat._3.count())
      }
    }
  }
}
