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

import com.drew.metadata.{Metadata, Tag}
import org.apache.spark.sql.sources._

import java.awt.Dimension
import scala.collection.JavaConverters._

object UnstructuredData {
  val FILETYPE = "type"
  val FORMAT = "format"
  val PATH = "path"
  val MODIFIEDAT = "modifiedat"
  val SIZEINBYTES = "sizeinbytes"
  val PREVIEW = "preview"
  val IMAGETHUMBNAIL = "imagethumbnail"

  val WIDTH = "width"
  val HEIGHT = "height"
  val CONTENT = "content"

  val TEXTCONTENT = "textcontent"
  val BINCONTENT = "bincontent"

  val SUBDIR = "subdir"

  // tag names
  val DURATION = "duration"
  val DURATION_IN_SECS = "duration in seconds"
  val FILE_SIZE = "file_size"
  val DETECTED_FILE_TYPE_NAME = "detected file type name"
  val COMPRESSION_TYPE = "compression type"

  val PDF_SHORT_NAME = "pdf"
  val PDF_PREVIEW_KEY = "pdf_preview_len"
  val PDF_PREVIEW_LEN = 1000

  val IMAGE_THUMBNAIL_WIDTH_KEY = "image_thumbnail_with"
  val IMAGE_THUMBNAIL_HEIGHT_KEY = "image_thumbnail_height"
  val IMAGE_THUMBNAIL_WIDTH_DEFAULT = 100
  val IMAGE_THUMBNAIL_HEIGHT_DEFAULT = 100

  object ScanType {
    def apply(scanType: String): ScanType = {
      scanType.toLowerCase match {
        case "file_scan" => FILE_SCAN
        case "recursive_scan" => RECURSIVE_SCAN
        case "parts_scan" => PARTS_SCAN
        case _ => throw new IllegalArgumentException(s"unsupported scantype : $scanType")
      }
    }
  }
  sealed trait ScanType
  case object FILE_SCAN extends ScanType
  case object RECURSIVE_SCAN extends ScanType
  case object PARTS_SCAN extends ScanType

  private[v2] case class MetaData(fileType: String,
                                  path: String,
                                  modifiedAt: Long,
                                  sizeInBytes: Long,
                                  preview: String,
                                  subDir: String,
                                  textcontent: String = null,
                                  bincontent: Array[Byte] = null,
                                  imageDim: Dimension = null)

  def extractTags(metadata: Metadata): List[Tag] = {
    metadata.getDirectories.asScala
      .flatMap(dir => dir.getTags.asScala)
      .toList
      .filter { tag =>
        tag.getTagName.toLowerCase match {
          case DURATION_IN_SECS        => true
          case DURATION                => true
          case FILE_SIZE               => true
          case DETECTED_FILE_TYPE_NAME => true
          case COMPRESSION_TYPE        => true
          case WIDTH                   => true
          case HEIGHT                  => true
          case _                       => false
        }
      }
  }

  def getFieldValue(field: String, metaData: MetaData): Any = {
    field.toLowerCase match {
      case UnstructuredData.FILETYPE => metaData.fileType
      case UnstructuredData.PATH => metaData.path
      case UnstructuredData.MODIFIEDAT => metaData.modifiedAt
      case UnstructuredData.SIZEINBYTES => metaData.sizeInBytes
      case UnstructuredData.PREVIEW => metaData.preview
      case UnstructuredData.SUBDIR => metaData.subDir
      case UnstructuredData.WIDTH => metaData.imageDim.width
      case UnstructuredData.HEIGHT => metaData.imageDim.height
      case UnstructuredData.IMAGETHUMBNAIL => metaData.bincontent
    }
  }

  def createFilter(filter: Filter): Function1[MetaData, Boolean] = {
    filter match {
      case IsNull(_) => _ => false
      case IsNotNull(_) => _ => true
      case EqualTo(field, value) => md =>
        getFieldValue(field, md).equals(value)
      case Not(EqualTo(field, value)) => md =>
        !getFieldValue(field, md).equals(value)
      case EqualNullSafe(field, value) => md =>
        getFieldValue(field, md).equals(value)
      case Not(EqualNullSafe(field, value)) => md =>
        !getFieldValue(field, md).equals(value)
      case LessThan(field, value) => md =>
        getFieldValue(field, md).toString.toDouble < value.toString.toDouble
      case LessThanOrEqual(field, value) => md =>
        getFieldValue(field, md).toString.toDouble <= value.toString.toDouble
      case GreaterThan(field, value) => md =>
        getFieldValue(field, md).toString.toDouble > value.toString.toDouble
      case GreaterThanOrEqual(field, value) => md =>
        getFieldValue(field, md).toString.toDouble >= value.toString.toDouble
      case And(left, right) => md =>
        val leftFilter = createFilter(left)
        val rightFilter = createFilter(right)
        leftFilter(md) && rightFilter(md)
      case Or(left, right) => md =>
        val leftFilter = createFilter(left)
        val rightFilter = createFilter(right)
        leftFilter(md) || rightFilter(md)
      case Not(pref) => md =>
        val neg = createFilter(pref)
        !neg(md)
      case In(field, values) => md =>
        val fieldVal = getFieldValue(field, md)
        values.map(v => fieldVal.equals(v)).reduceLeft(_ || _)
      case StringStartsWith(field, prefix) => md =>
        val fieldVal = getFieldValue(field, md)
        fieldVal.toString.startsWith(prefix)
      case StringEndsWith(field, suffix) => md =>
        val fieldVal = getFieldValue(field, md)
        fieldVal.toString.endsWith(suffix)
      case StringContains(field, value) => md =>
        val fieldVal = getFieldValue(field, md)
        fieldVal.toString.indexOf(value) >= 0
      case _ =>  _ => false

    }
  }

}
