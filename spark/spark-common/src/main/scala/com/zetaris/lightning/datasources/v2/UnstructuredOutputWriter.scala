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
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.{CodecStreams, OutputWriter}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.io.OutputStream
import scala.collection.JavaConverters.mapAsJavaMap

class UnstructuredOutputWriter(dataSourceType: DataSourceType.DataSourceType,
                               opts: Map[String, String],
                               context: TaskAttemptContext) extends OutputWriter {
  var contentWriter: OutputStream = null
  var thumbnailWriter: OutputStream = null

  private def buildThumbnailImage(content: Array[Byte]): Array[Byte] = {
    val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(opts))

    val width = ciMap.getInt(UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY, UnstructuredData.IMAGE_THUMBNAIL_WIDTH_DEFAULT)
    val height = ciMap.getInt(UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY, UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_DEFAULT)

    UnstructuredData.thumbnailImage(content, width, height)
  }

  private def getThumbnailPath(contentPath: String): String = {
    val index = contentPath.lastIndexOf(".")
    if (index > 0) {
      contentPath.substring(0, index) + "_thumbnail" + contentPath.substring(index)
    } else {
      contentPath + "_thumbnail"
    }
  }

  override def write(row: InternalRow): Unit = {
    val path = row.getString(0)
    contentWriter = CodecStreams.createOutputStream(context, new Path(path))

    dataSourceType match {
      case IMAGE =>
        val bin = row.getBinary(1)
        contentWriter.write(bin)

        val thumbnailPath = getThumbnailPath(path)
        thumbnailWriter = CodecStreams.createOutputStream(context, new Path(thumbnailPath))
        val thumbnailImage = buildThumbnailImage(bin)
        thumbnailWriter.write(thumbnailImage)

      case PDF =>
        val bin = row.getBinary(1)
        contentWriter.write(bin)

      case TEXT =>
        val utf8string = row.getUTF8String(1)
        utf8string.writeTo(contentWriter)
    }
  }

  override def close(): Unit = {
    contentWriter.close()
    Option(thumbnailWriter).foreach(_.close())
  }

  override def path(): String = ""
}
