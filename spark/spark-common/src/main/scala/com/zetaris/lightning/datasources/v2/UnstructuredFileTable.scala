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
import org.apache.hadoop.fs.{FileStatus, Path}
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.datasources.v2.FileTable
import org.apache.spark.sql.execution.streaming.{FileStreamSink, MetadataLogFileIndex}
import org.apache.spark.sql.types.{DataType, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.sql.{SparkSQLBridge, SparkSession}

import scala.collection.JavaConverters._

abstract class UnstructuredFileTable(sparkSession: SparkSession,
                                     opts: Map[String, String],
                                     paths: Seq[String],
                                     userSpecifiedSchema: Option[StructType],
                                     override val formatName: String)
  extends FileTable(sparkSession, new CaseInsensitiveStringMap(mapAsJavaMap(opts)), paths, userSpecifiedSchema) {
  val options = new CaseInsensitiveStringMap(mapAsJavaMap(opts))

  val scanType: ScanType = {
    val scanType = options.getOrDefault("scanType", "file_scan")
    ScanType(scanType)
  }

  private def globPaths: Boolean = {
    val entry = options.get(DataSource.GLOB_PATHS_KEY)
    Option(entry).map(_ == "true").getOrElse(true)
  }

  //private val caseSensitiveMap = options.asCaseSensitiveMap.asScala.toMap
  // Hadoop Configurations are case sensitive.
  private val hadoopConf = sparkSession.sessionState.newHadoopConfWithOptions(opts)

  private[datasources] val rootPathsSpecified = {
    val hadoopConf = sparkSession.sessionState.newHadoopConfWithOptions(opts)
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

      // see private val pathFilters = PathFilterFactory.create(caseInsensitiveMap) for glob filter
      // in PartitioningAwareFileIndex
      val parameters =  opts ++ fileScanOption
      new InMemoryFileIndex(
        sparkSession, rootPathsSpecified, parameters, userSpecifiedSchema, fileStatusCache)
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

  override def inferSchema(files: Seq[FileStatus]): Option[StructType] = None

  override def newWriteBuilder(info: LogicalWriteInfo): WriteBuilder =
    throw new RuntimeException("Wring table for unstructured data is not supported")

  override def supportsDataType(dataType: DataType): Boolean = true

}
