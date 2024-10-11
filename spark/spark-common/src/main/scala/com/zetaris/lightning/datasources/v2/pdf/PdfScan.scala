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

package com.zetaris.lightning.datasources.v2.pdf

import org.apache.hadoop.fs.Path
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.connector.read.PartitionReaderFactory
import org.apache.spark.sql.execution.datasources.PartitioningAwareFileIndex
import org.apache.spark.sql.execution.datasources.v2.FileScan
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.util.SerializableConfiguration

import scala.collection.JavaConverters._

case class PdfScan(
  sparkSession: SparkSession,
  fileIndex: PartitioningAwareFileIndex,
  dataSchema: StructType, // data schema (schema - partition schema)
  readDataSchema: StructType, // required(selected) schema from data schema
  readPartitionSchema: StructType, // required(selected) schema from partition schema
  recursiveScanSchema: StructType,
  tagSchema: StructType,
  rootPathsSpecified: Seq[Path],
  pushedFilters: Array[Filter],
  opts: Map[String, String],
  partitionFilters: Seq[Expression] = Seq.empty,
  dataFilters: Seq[Expression] = Seq.empty,
  isContentTable: Boolean = false) extends FileScan {

  override def isSplitable(path: Path): Boolean = false

  override def createReaderFactory(): PartitionReaderFactory = {
    val hadoopConf = sparkSession.sessionState.newHadoopConfWithOptions(opts)
    val broadcastedConf = sparkSession.sparkContext.broadcast(new SerializableConfiguration(hadoopConf))
    PdfReaderFactory(broadcastedConf,
      readDataSchema,
      readPartitionSchema,
      tagSchema,
      rootPathsSpecified,
      pushedFilters,
      opts,
      isContentTable)
  }

  override def readSchema(): StructType =
    StructType(readDataSchema.fields ++ recursiveScanSchema ++ readPartitionSchema.fields)
}
