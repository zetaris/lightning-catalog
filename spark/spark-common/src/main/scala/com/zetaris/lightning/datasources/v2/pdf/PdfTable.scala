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

import com.zetaris.lightning.datasources.v2.{UnstructuredData, UnstructuredScan}
import org.apache.hadoop.fs.FileStatus
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.connector.write.{LogicalWriteInfo, WriteBuilder}
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.datasources.v2.FileTable
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class PdfTable(name: String,
                    sparkSession: SparkSession,
                    options: CaseInsensitiveStringMap,
                    paths: Seq[String],
                    userSpecifiedSchema: Option[StructType],
                    fallbackFileFormat: Class[_ <: FileFormat])
  extends FileTable(sparkSession, options, paths, userSpecifiedSchema) with UnstructuredScan {
    override def newScanBuilder(options: CaseInsensitiveStringMap): PdfFileScanBuilder =
      PdfFileScanBuilder(sparkSession,
        fileIndex,
        schema,
        dataSchema,
        recursiveScanSchema,
        rootPathsSpecified,
        options,
        name.toLowerCase.equals(UnstructuredData.CONTENT))

    override def inferSchema(files: Seq[FileStatus]): Option[StructType] = None

    override def newWriteBuilder(info: LogicalWriteInfo): WriteBuilder =
      throw new RuntimeException("Wring pdf table is not supported")

    override def supportsDataType(dataType: DataType): Boolean = true

    override def formatName: String = "pdf"
  }

