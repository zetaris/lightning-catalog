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

import com.zetaris.lightning.datasources.v2.UnstructuredData.{BINCONTENT, FILETYPE, MODIFIEDAT, PATH, PREVIEW, SIZEINBYTES, TEXTCONTENT}
import com.zetaris.lightning.datasources.v2.{UnstructuredData, UnstructuredFileScanBuilder, UnstructuredFileTable}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.types.{BinaryType, LongType, StringType, StructField, StructType, TimestampType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class PdfTable(name: String,
                    sparkSession: SparkSession,
                    opts: Map[String, String],
                    paths: Seq[String],
                    fallbackFileFormat: Class[_ <: FileFormat],
                    tagSchema: StructType)
  extends UnstructuredFileTable(sparkSession, opts, paths, if (name.toLowerCase == UnstructuredData.CONTENT) {
    Some(StructType(
      StructField(PATH, StringType, false) ::
        StructField(TEXTCONTENT, StringType, false) ::
        StructField(BINCONTENT, BinaryType, false) :: Nil
    ))
  } else {
    Some(StructType(
      StructField(FILETYPE, StringType, true) ::
        StructField(PATH, StringType, false) ::
        StructField(MODIFIEDAT, TimestampType, false) ::
        StructField(SIZEINBYTES, LongType, false) ::
        StructField(PREVIEW, StringType, false) :: Nil
    ))
  }, "pdf") {

  override def newScanBuilder(options: CaseInsensitiveStringMap): UnstructuredFileScanBuilder =
    new UnstructuredFileScanBuilder(sparkSession, fileIndex, dataSchema, recursiveScanSchema) {
      override def build(): PdfScan = {
        PdfScan(sparkSession,
          fileIndex,
          dataSchema,
          readDataSchema(),
          readPartitionSchema(),
          recursiveScanSchema,
          tagSchema,
          rootPathsSpecified,
          pushedDataFilters,
          opts,
          partitionFilters,
          dataFilters,
          name.toLowerCase.equals(UnstructuredData.CONTENT))
      }
    }
}

