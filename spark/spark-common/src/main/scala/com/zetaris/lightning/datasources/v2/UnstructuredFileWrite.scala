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

import com.zetaris.lightning.execution.command.DataSourceType
import com.zetaris.lightning.execution.command.DataSourceType.{IMAGE, PDF, TEXT}
import org.apache.hadoop.mapreduce.{Job, TaskAttemptContext}
import org.apache.spark.sql.catalyst.util.CompressionCodecs
import org.apache.spark.sql.connector.write.LogicalWriteInfo
import org.apache.spark.sql.execution.datasources.v2.FileWrite
import org.apache.spark.sql.execution.datasources.{OutputWriter, OutputWriterFactory}
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.types.{DataType, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters.mapAsJavaMap

case class UnstructuredFileWrite(dataSourceType: DataSourceType.DataSourceType,
                                 paths: Seq[String],
                                 formatName: String,
                                 supportsDataType: DataType => Boolean,
                                 info: LogicalWriteInfo,
                                 options: Map[String, String]) extends FileWrite {

  val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(options))
  val compressionCodec = Option(ciMap.get("compression")).map(CompressionCodecs.getCodecClassName)

  private def veryPathField(schema: StructType): Unit = {
    schema.find(_.name.equalsIgnoreCase(UnstructuredData.PATH)).getOrElse(
      new RuntimeException(s"writing image table needs ${UnstructuredData.PATH} column")
    )
  }

  private def veryImageSchema(schema: StructType): Unit = {
    veryPathField(schema)
    schema.find(f => f.name.equalsIgnoreCase(UnstructuredData.IMAGECONTENT)).getOrElse(
      new RuntimeException(s"writing image table needs ${UnstructuredData.IMAGECONTENT} or ${UnstructuredData.IMAGETHUMBNAIL} column")
    )
  }

  private def veryContentSchema(schema: StructType): Unit = {
    schema.find(f => f.name.equalsIgnoreCase(UnstructuredData.TEXTCONTENT) ||
      f.name.equalsIgnoreCase(UnstructuredData.BINCONTENT)).getOrElse(
      new RuntimeException(s"writing unstructured table needs ${UnstructuredData.TEXTCONTENT} or ${UnstructuredData.BINCONTENT} column")
    )
  }

  private def verifySchema(schema: StructType): Unit = {
    dataSourceType match {
      case IMAGE => veryImageSchema(schema)
      case PDF | TEXT => veryContentSchema(schema)
      case _ => ???
    }
  }

  override def prepareWrite(sqlConf: SQLConf,
                            job: Job,
                            options: Map[String, String],
                            dataSchema: StructType): OutputWriterFactory = {
    verifySchema(dataSchema)

    val conf = job.getConfiguration

    new OutputWriterFactory {
      override def newInstance(path: String,
                               dataSchema: StructType,
                               context: TaskAttemptContext): OutputWriter = {
        new UnstructuredOutputWriter(dataSourceType, options, context)
      }

      override def getFileExtension(context: TaskAttemptContext): String = ""
    }
  }
}
