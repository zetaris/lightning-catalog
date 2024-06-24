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

package com.zetaris.lightning.datasources.v2

import com.zetaris.lightning.datasources.v2.UnstructuredData.ScanType
import org.apache.hadoop.fs.Path
import org.apache.spark.sql.execution.datasources.v2.FileTable
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.streaming.{FileStreamSink, MetadataLogFileIndex}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.sql.{SparkSQLBridge, SparkSession}

import scala.collection.JavaConverters._

trait UnstructuredScan { this: FileTable =>
  val options: CaseInsensitiveStringMap

  val scanType: ScanType = {
    val scanType = options.getOrDefault("scanType", "file_scan")
    ScanType(scanType)
  }

  val sparkSession: SparkSession
  val paths: Seq[String]
  val userSpecifiedSchema: Option[StructType]

  private def globPaths: Boolean = {
    val entry = options.get(DataSource.GLOB_PATHS_KEY)
    Option(entry).map(_ == "true").getOrElse(true)
  }

  private val caseSensitiveMap = options.asCaseSensitiveMap.asScala.toMap
  // Hadoop Configurations are case sensitive.
  private val hadoopConf = sparkSession.sessionState.newHadoopConfWithOptions(caseSensitiveMap)

  private[datasources] val rootPathsSpecified = {
    val hadoopConf = sparkSession.sessionState.newHadoopConfWithOptions(caseSensitiveMap)
    SparkSQLBridge.checkAndGlobPathIfNecessary(paths, hadoopConf,
      checkEmptyGlobPath = true, checkFilesExist = true, enableGlobbing = globPaths)
  }

  override lazy val fileIndex: PartitioningAwareFileIndex = {
    if (FileStreamSink.hasMetadata(paths, hadoopConf, sparkSession.sessionState.conf)) {
      // We are reading from the results of a streaming query. We will load files from
      // the metadata log instead of listing them using HDFS APIs.
      new MetadataLogFileIndex(sparkSession, new Path(paths.head),
        options.asScala.toMap, userSpecifiedSchema)
    } else {
      val fileStatusCache = FileStatusCache.getOrCreate(sparkSession)
      val fileScanOption = scanType match {
        case UnstructuredData.FILE_SCAN | UnstructuredData.PARTS_SCAN =>
          Map(FileIndexOptions.RECURSIVE_FILE_LOOKUP -> "false")
        case UnstructuredData.RECURSIVE_SCAN =>
          Map(FileIndexOptions.RECURSIVE_FILE_LOOKUP -> "true")
      }

      new InMemoryFileIndex(
        sparkSession, rootPathsSpecified, caseSensitiveMap ++ fileScanOption, userSpecifiedSchema, fileStatusCache)
    }
  }

  val recursiveScanSchema: StructType = {
    if (scanType == UnstructuredData.RECURSIVE_SCAN) {
      StructType(List(StructField(UnstructuredData.SUBDIR, StringType)))
    } else {
      StructType(List())
    }
  }

  override lazy val schema: StructType = {
    val caseSensitive = false//sparkSession.sessionState.conf.caseSensitiveAnalysis
    SparkSQLBridge.checkSchemaColumnNameDuplication(dataSchema, caseSensitive)
    dataSchema.foreach { field =>
      if (!supportsDataType(field.dataType)) {
        throw SparkSQLBridge.dataTypeUnsupportedByDataSourceError(formatName, field)
      }
    }

    scanType match {
      case UnstructuredData.FILE_SCAN =>
        dataSchema
      case UnstructuredData.RECURSIVE_SCAN =>
        StructType(dataSchema.fields ++ recursiveScanSchema.fields)
      case UnstructuredData.PARTS_SCAN =>
        val partitionSchema = fileIndex.partitionSchema
        SparkSQLBridge.checkSchemaColumnNameDuplication(partitionSchema, caseSensitive)
        val partitionNameSet: Set[String] =
          partitionSchema.fields.map(PartitioningUtils.getColName(_, caseSensitive)).toSet

        val fields = dataSchema.fields.filterNot { field =>
          val colName = PartitioningUtils.getColName(field, caseSensitive)
          partitionNameSet.contains(colName)
        } ++ partitionSchema.fields
        StructType(fields)
    }
  }


}
