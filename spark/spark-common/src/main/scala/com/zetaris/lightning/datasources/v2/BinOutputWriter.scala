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

import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.spark.internal.Logging
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources.{CodecStreams, OutputWriter}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.io.OutputStream
import scala.collection.JavaConverters.mapAsJavaMap

class BinOutputWriter(val path: String,
                      context: TaskAttemptContext,
                      dataSchema: StructType,
                      opts: Map[String, String],
                      filePath: String) extends OutputWriter with Logging {

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

  private def getTargetPath(sourcePath: String): String = {
    val dirIndex = sourcePath.lastIndexOf("/")

    val fileName = if (dirIndex > 0) {
      sourcePath.substring(dirIndex + 1)
    } else {
      sourcePath
    }

    if (filePath.endsWith("/")) {
      s"$filePath$fileName"
    } else {
      s"$filePath/$fileName"
    }

  }

  override def write(row: InternalRow): Unit = {
    var targetPath: String = null
    var imageThumbNail: Array[Byte] = null
    var content: Array[Byte] = null

    dataSchema.fields.map(_.name).zipWithIndex.foreach {
      case (UnstructuredData.PATH, index) =>
        val sourcePath = row.getString(index)
        targetPath = getTargetPath(sourcePath)

      case (UnstructuredData.IMAGETHUMBNAIL, index) =>
        imageThumbNail = row.getBinary(index)

      case (UnstructuredData.IMAGECONTENT, index) =>
        content = row.getBinary(index)
        imageThumbNail = buildThumbnailImage(content)

      case (UnstructuredData.BINCONTENT, index) =>
        content = row.getBinary(index)

      case (UnstructuredData.TEXTCONTENT, index) =>
        content = row.getBinary(index)

      case (other, index) =>
        println(s"other column : $other, $index")
    }

    if (imageThumbNail != null) {
      val thumbnailPath = getThumbnailPath(targetPath)
      thumbnailWriter = CodecStreams.createOutputStream(context, new Path(thumbnailPath))
      thumbnailWriter.write(imageThumbNail)
    }

    if (content != null) {
      contentWriter = CodecStreams.createOutputStream(context, new Path(targetPath))
      contentWriter.write(content)
    }
  }

  override def close(): Unit = {
    if (contentWriter != null) {
      contentWriter.close()
    }
    if (thumbnailWriter != null) {
      thumbnailWriter.close()
    }
  }
}