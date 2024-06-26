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

import com.zetaris.lightning.datasources.v2.UnstructuredData
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.binaryfile.BinaryFileFormat
import org.apache.spark.sql.execution.datasources.{OutputWriterFactory, PartitionedFile}
import org.apache.spark.sql.sources.{DataSourceRegister, Filter}
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

case class PdfFileFormat() extends BinaryFileFormat with DataSourceRegister {

  import UnstructuredData._

  val schema: StructType = StructType(
    StructField(FILETYPE, StringType, true) ::
      StructField(PATH, StringType, false) ::
      StructField(MODIFIEDAT, TimestampType, false) ::
      StructField(SIZEINBYTES, LongType, false) ::
      StructField(PREVIEW, StringType, false) :: Nil
  )

  override def shortName(): String = "pdf"

  override def inferSchema(sparkSession: SparkSession,
                           options: Map[String, String],
                           files: Seq[FileStatus]
                          ): Option[StructType] = Some(schema)

  override def prepareWrite(sparkSession: SparkSession,
                            job: Job,
                            options: Map[String, String],
                            dataSchema: StructType
                           ): OutputWriterFactory =
    throw writeUnsupportedForPdfFileDataSourceError()

  override protected def buildReader(sparkSession: SparkSession,
                                     dataSchema: StructType,
                                     partitionSchema: StructType,
                                     requiredSchema: StructType,
                                     filters: Seq[Filter],
                                     options: Map[String, String],
                                     hadoopConf: Configuration
                                    ): PartitionedFile => Iterator[InternalRow] = {

    val fileToRows = buildReaderWithPartitionValues(sparkSession,
      BinaryFileFormat.schema,
      partitionSchema,
      BinaryFileFormat.schema,
      filters,
      options,
      hadoopConf
    )
    (file: PartitionedFile) => {
      val rows = fileToRows(file)
      rows.map(internalRow => {
        val fileType = UTF8String.fromString(shortName())
        val path = UTF8String.fromString(internalRow.getString(0))
        val modificationTime = internalRow.get(1, TimestampType)
        val length = internalRow.getLong(2)

        InternalRow(
          fileType,
          path,
          modificationTime,
          length
        )
      })
    }
  }

  private def writeUnsupportedForPdfFileDataSourceError(): Throwable = {
    new UnsupportedOperationException("Write is not supported for pdf file data source")
  }
}
