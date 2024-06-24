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
import org.apache.spark.sql.catalyst.StructFilters
import org.apache.spark.sql.execution.datasources.PartitioningAwareFileIndex
import org.apache.spark.sql.execution.datasources.v2.FileScanBuilder
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types._
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class PdfFileScanBuilder(sparkSession: SparkSession,
                              fileIndex: PartitioningAwareFileIndex,
                              schema: StructType, // data schema + partition schema
                              dataSchema: StructType, // data schema - UnstructuredData.pdfSchema
                              recursiveScanSchema: StructType,
                              rootPathsSpecified: Seq[Path],
                              options: CaseInsensitiveStringMap,
                              isContentTable: Boolean = false)
  extends FileScanBuilder(sparkSession, fileIndex, dataSchema) {

  override def build(): PdfScan = {
    PdfScan(sparkSession,
      fileIndex,
      dataSchema,
      readDataSchema(),
      readPartitionSchema(),
      recursiveScanSchema,
      rootPathsSpecified,
      pushedDataFilters,
      options,
      partitionFilters,
      dataFilters,
      isContentTable)
  }

  override protected def readDataSchema(): StructType = {
    val fields = super.readDataSchema().fields ++ recursiveScanSchema.fields
    StructType(fields)
  }

  override def pushDataFilters(dataFilters: Array[Filter]): Array[Filter] = {
    StructFilters.pushedFilters(dataFilters, dataSchema)
  }
}
