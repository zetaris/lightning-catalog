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
import org.apache.spark.sql.catalyst.expressions.aggregate.Count
import org.apache.spark.sql.catalyst.expressions.{AttributeReference, Literal, Not}
import org.apache.spark.sql.catalyst.plans.logical.{Aggregate, Filter}
import org.apache.spark.sql.types.{BooleanType, LongType, StringType}
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

  private def getTotalRecordCount(sparkSession: SparkSession, table: Seq[String]): Long = {
    sparkSession.sql(s"select count(*) from ${toFqn(table)}").collect()(0).getLong(0)
  }

  /**
   * run dq
   *
   * @param sparkSession
   * @param table
   * @param expression
   * @return count of total record, data frame of good record
   */
  def runDQ(sparkSession: SparkSession, table: Seq[String], expression: String): (Long, DataFrame) = {
    val tracker = new QueryPlanningTracker

    val totRecord = getTotalRecordCount(sparkSession, table)

    val unresolved = UnresolvedRelation(table)
    val exp = tryParse(expression, sparkSession.sessionState.sqlParser.parseExpression)
    val goodFilter = Filter(exp, unresolved)

    val goodRecord = SparkSQLBridge.ofRows(sparkSession,
      sparkSession.sessionState.analyzer.execute(goodFilter), tracker)

    (totRecord, goodRecord)
  }

  def runPrimaryKeyConstraints(sparkSession: SparkSession,
                               table: Seq[String],
                               column: Seq[String]): (Long, Long) = {
    val totRecord = getTotalRecordCount(sparkSession, table)

    val goodRecord = sparkSession.sql(
      s"""
         |SELECT COUNT(*) FROM (
         |  SELECT ${column.mkString(",")} FROM ${toFqn(table)}
         |  GROUP BY ${column.mkString(",")} HAVING COUNT(${column.mkString(",")}) == 1
         |)
         |""".stripMargin).collect()(0).getLong(0)

    val badRecord = sparkSession.sql(
      s"""
         |SELECT COUNT(*) FROM (
         |  SELECT ${column.mkString(",")} FROM ${toFqn(table)}
         |  GROUP BY ${column.mkString(",")} HAVING COUNT(${column.mkString(",")}) > 1
         |)
         |""".stripMargin).collect()(0).getLong(0)

    (totRecord, goodRecord)
  }

  def getPrimaryKeyConstraintsRecords(sparkSession: SparkSession,
                                      table: Seq[String],
                                      column: Seq[String]): (DataFrame, DataFrame) = {
    val sqlGoodRecord = if (column.size == 1) {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |WHERE ${column(0)} IN (
         |  SELECT ${column(0)} FROM ${toFqn(table)}
         |  GROUP BY ${column(0)} HAVING COUNT(${column(0)}) == 1
         |)
         |"""
    } else {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |WHERE ARRAY(${column.mkString(",")}) IN (
         |  SELECT ARRAY(${column.mkString(",")}) FROM ${toFqn(table)}
         |  GROUP BY ${column.mkString(",")} HAVING COUNT(${column.mkString(",")}) == 1
         |)
         |"""
    }

    val sqlBadRecord = if (column.size == 1) {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |WHERE ${column(0)} NOT IN (
         |  SELECT ${column(0)} FROM ${toFqn(table)}
         |  GROUP BY ${column(0)} HAVING COUNT(${column(0)}) == 1
         |)
         |"""
    } else {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |WHERE ARRAY(${column.mkString(",")}) NOT IN (
         |  SELECT ARRAY(${column.mkString(",")}) FROM ${toFqn(table)}
         |  GROUP BY ${column.mkString(",")} HAVING COUNT(${column.mkString(",")}) == 1
         |)
         |"""
    }

    (sparkSession.sql(sqlGoodRecord), sparkSession.sql(sqlBadRecord))
  }

  def runForeignKeyConstraints(sparkSession: SparkSession,
                               table: Seq[String],
                               column: Seq[String],
                               refTable: Seq[String],
                               refColumn: Seq[String]): (Long, Long) = {
    val totRecord = getTotalRecordCount(sparkSession, table)

    val goodRecord = if (column.size == 1) {
      sparkSession.sql(
        s"""
           |SELECT COUNT(*) FROM ${toFqn(table)}
           |  WHERE ${column(0)} IN (
           |    SELECT ${refColumn(0)} FROM ${toFqn(refTable)}
           |  )
           |""".stripMargin).collect()(0).getLong(0)
    } else {
      sparkSession.sql(
        s"""
           |SELECT COUNT(*) FROM ${toFqn(table)}
           |  WHERE ARRAY(${column.mkString(",")}) IN (
           |    SELECT ARRAY${refColumn.mkString(",")}) FROM ${toFqn(refTable)}
           |  )
           |""".stripMargin).collect()(0).getLong(0)
    }

    val badRecord = if (column.size == 1) {
      sparkSession.sql(
        s"""
           |SELECT COUNT(*) FROM ${toFqn(table)}
           |  WHERE ${column(0)} NOT IN (
           |    SELECT ${refColumn(0)} FROM ${toFqn(refTable)}
           |  )
           |""".stripMargin).collect()(0).getLong(0)
    } else {
      sparkSession.sql(
        s"""
           |SELECT COUNT(*) FROM ${toFqn(table)}
           |  WHERE ARRAY(${column.mkString(",")}) NOT IN (
           |    SELECT ARRAY${refColumn.mkString(",")}) FROM ${toFqn(refTable)}
           |  )
           |""".stripMargin).collect()(0).getLong(0)
    }

    (totRecord, goodRecord)
  }

  def getForeignKeyConstraintsRecords(sparkSession: SparkSession,
                                      table: Seq[String],
                                      column: Seq[String],
                                      refTable: Seq[String],
                                      refColumn: String): (DataFrame, DataFrame) = {
    val sqlGoodRecord = if (column.size == 1) {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |  WHERE ${column(0)} IN (
         |    SELECT ${refColumn(0)} FROM ${toFqn(refTable)}
         |  )
         |""".stripMargin
    } else {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |  WHERE ARRAY(${column.mkString(",")}) IN (
         |    SELECT ARRAY${refColumn.mkString(",")}) FROM ${toFqn(refTable)}
         |  )
         |""".stripMargin
    }

    val sqlBadRecord = if (column.size == 1) {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |  WHERE ARRAY(${column.mkString(",")}) NOT IN (
         |    SELECT ARRAY(${refColumn.mkString(",")}) FROM ${toFqn(refTable)}
         |  )
         |""".stripMargin
    } else {
      s"""
         |SELECT * FROM ${toFqn(table)}
         |  WHERE ARRAY(${column.mkString(",")}) NOT IN (
         |    SELECT ARRAY${refColumn.mkString(",")}) FROM ${toFqn(refTable)}
         |  )
         |""".stripMargin
    }

    (sparkSession.sql(sqlGoodRecord), sparkSession.sql(sqlBadRecord))
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
        Row(dq.name, createTableSpec.name, "Custom Data Quality", dq.expression)
      }
    }
  }
}

case class RunDataQualitySpec(name: Option[String], table: Seq[String]) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("name", StringType, false)(),
    AttributeReference("table", StringType, false)(),
    AttributeReference("type", StringType, false)(),
    AttributeReference("total_record", LongType, false)(),
    AttributeReference("good_record", LongType, false)(),
    AttributeReference("bad_record", LongType, false)()
  )

  private def runDQ(sparkSession: SparkSession, createTableSpec: CreateTableSpec, dq: DataQuality): Row = {
    val dqStat = DataQualitySpec.runDQ(sparkSession, createTableSpec.namespace :+ createTableSpec.name, dq.expression)
    val goodCount = dqStat._2.count()
    Row(dq.name, createTableSpec.name, "Custom Data Quality", dqStat._1, goodCount, dqStat._1 - goodCount)
  }

  private def runPkConstraints(sparkSession: SparkSession,
                               createTableSpec: CreateTableSpec,
                               constraintOrColumnName: String): Option[Row] = {
    if (createTableSpec.primaryKey.isDefined) {
      val pkConstraints = createTableSpec.primaryKey.get
      val constraintName = pkConstraints.name.get
      if (constraintName.equalsIgnoreCase(constraintOrColumnName) ||
        equalToMultiPartIdentifier(constraintOrColumnName, pkConstraints.columns)) {
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, pkConstraints.columns)
        Some(Row(constraintOrColumnName, createTableSpec.name, "Primary Key constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
      } else {
        None
      }
    } else {
      createTableSpec.columnSpecs.find(cs => cs.primaryKey.isDefined &&
        cs.name.equalsIgnoreCase(constraintOrColumnName)).map { pk =>
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, Seq(pk.name))
        Row(pk.name, createTableSpec.name, "Primary Key constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2)
      }
    }
  }

  private def runPkConstraints(sparkSession: SparkSession, createTableSpec: CreateTableSpec): Seq[Row] = {
    if (createTableSpec.primaryKey.isDefined) {
      val pkConstraints = createTableSpec.primaryKey.get
      val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, pkConstraints.columns)
      Row(createTableSpec.primaryKey.get.name.getOrElse(toFqn(createTableSpec.primaryKey.get.columns, "_")),
        createTableSpec.name, "Primary Key constraint", pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2) :: Nil
    } else {
      createTableSpec.columnSpecs.flatMap { pk =>
        if (pk.primaryKey.isDefined) {
          val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, Seq(pk.name))
          Some(Row(pk.primaryKey.get.name.getOrElse(pk.name), createTableSpec.name, "Primary Key constraint",
            pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
        } else {
          None
        }
      }
    }
  }

  private def runUniqueConstraints(sparkSession: SparkSession,
                                   createTableSpec: CreateTableSpec,
                                   constraintOrColumnName: String): Seq[Row] = {
    createTableSpec.unique.flatMap { uc =>
      if ((uc.name.isDefined && uc.name.get.equalsIgnoreCase(constraintOrColumnName)) ||
        equalToMultiPartIdentifier(constraintOrColumnName, uc.columns)) {
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, uc.columns)
        Some(Row(createTableSpec.primaryKey.get.name.get, createTableSpec.name, "Unique Constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
      } else {
        None
      }
    } ++ createTableSpec.columnSpecs.flatMap { uc =>
      if (uc.unique.isDefined && uc.name.equalsIgnoreCase(constraintOrColumnName)) {
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, Seq(uc.name))
        Some(Row(createTableSpec.primaryKey.get.name.get, createTableSpec.name, "Unique Constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
      } else {
        None
      }
    }
  }

  private def runUniqueConstraints(sparkSession: SparkSession, createTableSpec: CreateTableSpec): Seq[Row] = {
    createTableSpec.unique.flatMap { uc =>
      if (uc.name.isDefined) {
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, uc.columns)
        Some(Row(createTableSpec.primaryKey.get.name.get, createTableSpec.name, "Unique Constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
      } else {
        None
      }
    } ++ createTableSpec.columnSpecs.flatMap { uc =>
      if (uc.unique.isDefined) {
        val pkCheck = DataQualitySpec.runPrimaryKeyConstraints(sparkSession, table, Seq(uc.name))
        Some(Row(createTableSpec.primaryKey.get.name.get, createTableSpec.name, "Unique Constraint",
          pkCheck._1, pkCheck._2, pkCheck._1 - pkCheck._2))
      } else {
        None
      }
    }
  }

  private def runFkConstraints(sparkSession: SparkSession,
                               createTableSpec: CreateTableSpec,
                               constraintOrColumnName: String): Seq[Row] = {
    createTableSpec.foreignKeys.flatMap { fk =>
      if ((fk.name.isDefined && fk.name.get.equalsIgnoreCase(constraintOrColumnName)) ||
        equalToMultiPartIdentifier(constraintOrColumnName, fk.columns)) {
        val fkCheck = DataQualitySpec.runForeignKeyConstraints(sparkSession, table,
          fk.columns, fk.refTable, fk.refColumns)
        Some(Row(constraintOrColumnName, createTableSpec.name, "Foreign Key Constraint",
          fkCheck._1, fkCheck._2, fkCheck._1 - fkCheck._2))
      } else {
        None
      }
    } ++ createTableSpec.columnSpecs.flatMap { cs =>
      if (cs.foreignKey.isDefined && cs.name.equalsIgnoreCase(constraintOrColumnName)) {
        val fkCheck = DataQualitySpec.runForeignKeyConstraints(sparkSession, table,
          Seq(cs.name), cs.foreignKey.get.refTable, cs.foreignKey.get.refColumns)
        Some(Row(constraintOrColumnName, createTableSpec.name, "Foreign Key Constraint",
          fkCheck._1, fkCheck._2, fkCheck._1 - fkCheck._2))
      } else {
        None
      }
    }
  }

  private def runFkConstraints(sparkSession: SparkSession, createTableSpec: CreateTableSpec): Seq[Row] = {
    createTableSpec.foreignKeys.map { fk =>
      val fkCheck = DataQualitySpec.runForeignKeyConstraints(sparkSession, table,
        fk.columns, fk.refTable, fk.refColumns)
      Row(fk.name.getOrElse(toFqn(fk.columns, "_")), createTableSpec.name, "Foreign Key Constraint",
        fkCheck._1, fkCheck._2, fkCheck._1 - fkCheck._2)
    } ++ createTableSpec.columnSpecs.flatMap { cs =>
      if (cs.foreignKey.isDefined) {
        val fkCheck = DataQualitySpec.runForeignKeyConstraints(sparkSession, table,
          Seq(cs.name), cs.foreignKey.get.refTable, cs.foreignKey.get.refColumns)
        Some(Row(cs.foreignKey.get.name.getOrElse(cs.name), createTableSpec.name, "Foreign Key Constraint",
          fkCheck._1, fkCheck._2, fkCheck._1 - fkCheck._2))
      } else {
        None
      }
    }
  }

  private def runDatabaseConstraints(sparkSession: SparkSession,
                                     createTableSpec: CreateTableSpec,
                                     constraintOrColumnName: String): Seq[Row] = {
    val rows = runPkConstraints(sparkSession, createTableSpec, constraintOrColumnName) ++
      runUniqueConstraints(sparkSession, createTableSpec, constraintOrColumnName) ++
      runFkConstraints(sparkSession, createTableSpec, constraintOrColumnName)

    if (rows.isEmpty) {
      throw DataQualityNotFoundException(s"${name} is not found in ${toFqn(table)}")
    }

    rows.toSeq
  }

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
      val dq = createTableSpec.dqAnnotations.find(_.name.equalsIgnoreCase(name.get))
      if (dq.isDefined) {
        runDQ(sparkSession, createTableSpec, dq.get) :: Nil
      } else {
        runDatabaseConstraints(sparkSession, createTableSpec, name.get)
      }
    } else {
      createTableSpec.dqAnnotations.map { dq =>
        val dqStat = DataQualitySpec.runDQ(sparkSession,
          createTableSpec.namespace :+ createTableSpec.name, dq.expression)
        val goodCount = dqStat._2.count()
        Row(dq.name, createTableSpec.name, "Custom Data Quality",
          dqStat._1, goodCount, dqStat._1 - goodCount)
      } ++
        runPkConstraints(sparkSession, createTableSpec) ++
        runUniqueConstraints(sparkSession, createTableSpec) ++
        runFkConstraints(sparkSession, createTableSpec)
    }
  }
}

case class RemovedDataQualitySpec(name: String, table: Seq[String]) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("remove", BooleanType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val uslFqn = table.dropRight(2)
    val usl = model.loadUnifiedSemanticLayer(uslFqn.dropRight(1), uslFqn.last)
    val tableSpecRemoved = usl.tables.map { createTableSpec =>
      val dq = createTableSpec.dqAnnotations.find(_.name.eq(name))
      val removed = if (dq.isDefined) {
        createTableSpec.dqAnnotations.filterNot(_.name.equalsIgnoreCase(name))
      } else {
        createTableSpec.dqAnnotations
      }

      createTableSpec.copy(dqAnnotations = removed)
    }

    model.saveUnifiedSemanticLayer(uslFqn.dropRight(1), uslFqn.last, tableSpecRemoved)
    Row(true) :: Nil
  }
}

