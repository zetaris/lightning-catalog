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

package com.zetaris.lightning.datasources.v2.video

import com.drew.metadata.Tag
import com.zetaris.lightning.datasources.v2.{UnstructuredData, UnstructuredFilePartitionReaderFactory}
import org.apache.hadoop.fs.Path
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.util.SerializableConfiguration

import java.awt.Dimension
import scala.collection.JavaConverters.mapAsJavaMap

case class VideoReaderFactory(broadcastedConf: Broadcast[SerializableConfiguration],
                              readDataSchema: StructType,
                              partitionSchema: StructType,
                              tagSchema: StructType,
                              rootPathsSpecified: Seq[Path],
                              pushedFilters: Array[Filter],
                              opts: Map[String, String],
                              isContentTable: Boolean = false)
  extends UnstructuredFilePartitionReaderFactory(broadcastedConf,
    readDataSchema,
    partitionSchema,
    tagSchema,
    rootPathsSpecified,
    pushedFilters,
    opts,
    isContentTable) {

  override def textPreviewFromBinary(content: Array[Byte]): String = ???

  override def getResolution(content: Array[Byte]): Dimension = {
    if (embeddedTags == null) {
      embeddedTags = extractEmbeddedTags(content)
    }

    val widthTag = embeddedTags.find(_.getTagName.equals(UnstructuredData.WIDTH))
    val heightTag = embeddedTags.find(_.getTagName.equals(UnstructuredData.HEIGHT))

    if (widthTag.isDefined && heightTag.isDefined) {
      val width = widthTag.get.getDescription.toInt
      val height = heightTag.get.getDescription.toInt
      new Dimension(width, height)
    } else {
      new Dimension(-1, -1)
    }
  }

  override def getDuration(content: Array[Byte]): Float = {
    if (embeddedTags == null) {
      embeddedTags = extractEmbeddedTags(content)
    }

    val durationTag = embeddedTags.find(_.getTagName.equalsIgnoreCase(UnstructuredData.DURATION))
    val timeScaleTag = embeddedTags.find(_.getTagName.equalsIgnoreCase(UnstructuredData.MEDIA_TIME_SCALE))
    if (durationTag.isDefined && timeScaleTag.isDefined) {
      durationTag.get.getDescription.toInt.toFloat / timeScaleTag.get.getDescription.toInt
    } else {
      -1.0f
    }
  }

  override def getFormat(content: Array[Byte]): String = {
    if (embeddedTags == null) {
      embeddedTags = extractEmbeddedTags(content)
    }

    val formatTag = embeddedTags.find(_.getTagName.equalsIgnoreCase(UnstructuredData.DETECTED_FILE_TYPE_NAME))
    if (formatTag.isDefined) {
      formatTag.get.getDescription
    } else {
      ""
    }
  }

  override def thumbnailImage(content: Array[Byte]): Array[Byte] = {
    val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(opts))

    val width = ciMap.getInt(UnstructuredData.IMAGE_THUMBNAIL_WIDTH_KEY, UnstructuredData.IMAGE_THUMBNAIL_WIDTH_DEFAULT)
    val height = ciMap.getInt(UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_KEY, UnstructuredData.IMAGE_THUMBNAIL_HEIGHT_DEFAULT)

    UnstructuredData.thumbnailImage(content, width, height)
  }
}


