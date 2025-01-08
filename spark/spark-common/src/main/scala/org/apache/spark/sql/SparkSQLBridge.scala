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

package org.apache.spark.sql

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.avro.AvroFileFormat
import org.apache.spark.sql.catalyst.QueryPlanningTracker
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.errors.QueryCompilationErrors
import org.apache.spark.sql.execution.datasources.{DataSource, FileFormat}
import org.apache.spark.sql.types.{DataType, StructField, StructType}
import org.apache.spark.sql.util.SchemaUtils

object SparkSQLBridge {
  def fallbackAvroFileFormat: Class[_ <: FileFormat] = classOf[AvroFileFormat]

  def schemaFromJson(json: String): StructType = StructType.fromString(json)

  def checkAndGlobPathIfNecessary(pathStrings: Seq[String],
                                  hadoopConf: Configuration,
                                  checkEmptyGlobPath: Boolean,
                                  checkFilesExist: Boolean,
                                  numThreads: Integer = 40,
                                  enableGlobbing: Boolean): Seq[Path] =
    DataSource.checkAndGlobPathIfNecessary(pathStrings,
      hadoopConf, checkEmptyGlobPath, checkFilesExist, numThreads, enableGlobbing)

  def checkSchemaColumnNameDuplication(schema: DataType,
                                       aseSensitiveAnalysis: Boolean = false): Unit =
    SchemaUtils.checkSchemaColumnNameDuplication(schema, aseSensitiveAnalysis)

  def dataTypeUnsupportedByDataSourceError(format: String, column: StructField): Throwable =
    QueryCompilationErrors.dataTypeUnsupportedByDataSourceError(format, column)

  def dataSchemaNotSpecifiedError(format: String): Throwable =
    throw QueryCompilationErrors.dataSchemaNotSpecifiedError(format)

  def asNullable(schema: StructType): StructType = {
    val newFields = schema.fields.map {
      case StructField(name, dataType, nullable, metadata) =>
        StructField(name, dataType.asNullable, nullable = true, metadata)
    }

    StructType(newFields)
  }

  def equalsIgnoreNameAndCompatibleNullability(from: DataType, to: DataType): Boolean =
    DataType.equalsIgnoreNameAndCompatibleNullability(from, to)

  def ofRows(sparkSession: SparkSession, plan: LogicalPlan, tracker: QueryPlanningTracker): DataFrame = {
    Dataset.ofRows(sparkSession, plan, tracker)
  }

}
