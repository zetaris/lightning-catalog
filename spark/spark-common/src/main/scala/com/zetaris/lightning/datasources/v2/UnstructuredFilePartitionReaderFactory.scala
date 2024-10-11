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

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.{Metadata, Tag}
import com.zetaris.lightning.datasources.v2.UnstructuredData.{MetaData, createFilter}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.spark.TaskContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.UnsafeRow
import org.apache.spark.sql.catalyst.expressions.codegen.UnsafeRowWriter
import org.apache.spark.sql.catalyst.util.CaseInsensitiveMap
import org.apache.spark.sql.connector.read.PartitionReader
import org.apache.spark.sql.execution.datasources.PartitionedFile
import org.apache.spark.sql.execution.datasources.text.TextOptions
import org.apache.spark.sql.execution.datasources.v2._
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.{DateType, DoubleType, FloatType, IntegerType, LongType, StringType, StructType, TimestampType, VarcharType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.unsafe.types.UTF8String
import org.apache.spark.util.SerializableConfiguration

import java.awt.Dimension
import java.io.ByteArrayInputStream
import scala.collection.JavaConverters._
import scala.util.Try

abstract class UnstructuredFilePartitionReaderFactory(broadcastedConf: Broadcast[SerializableConfiguration],
                                                      readDataSchema: StructType,
                                                      partitionSchema: StructType,
                                                      tagSchema: StructType,
                                                      rootPathsSpecified: Seq[Path],
                                                      pushedFilters: Array[Filter],
                                                      opts: Map[String, String],
                                                      isContentTable: Boolean = false) extends FilePartitionReaderFactory {
  var embeddedTags: List[Tag] = null
  var fileTag: CaseInsensitiveMap[Any] = null

  val options = new TextOptions(opts)

  /**
   * generate preview text from binary contents.
   *
   * @param content, binary contents
   * @return text preview
   */
  def textPreviewFromBinary(content: Array[Byte]): String

  /**
   * generate text from binary contents
   * @param content, binary contents
   * @return text
   */
  def textFromBinary(content: Array[Byte]): String = new String(content)

  def thumbnailImage(content: Array[Byte]): Array[Byte] = content

  def getResolution(content: Array[Byte]): Dimension = ???

  def getDuration(content: Array[Byte]): Float = ???

  def getFormat(content: Array[Byte]): String = ???

  def extractEmbeddedTags(content: Array[Byte]): List[Tag] = {
    val stream = new ByteArrayInputStream(content)
    Try(ImageMetadataReader.readMetadata(stream)).map { md =>
      md.getDirectories.asScala.flatMap(_.getTags.asScala).toList
    }.getOrElse(List())
  }

  def buildJSONTag(tags: List[Tag]): String = {
    "{\n" +
      tags.map { tag =>
        s"\t${tag.getTagName} : '${tag.getDescription}'"
      }.mkString("\n") +
    "\n}"
  }

  val previewLen: Int = {
    val ciMap = new CaseInsensitiveStringMap(mapAsJavaMap(opts))
    ciMap.getInt(UnstructuredData.PDF_PREVIEW_KEY, UnstructuredData.PDF_PREVIEW_LEN)
  }

  private def readFileTag(dataFile: PartitionedFile, configuration: Configuration): CaseInsensitiveMap[Any] = {
    val tagFile = new Path(s"${dataFile.toPath.toUri.toString}.tag")
    val fs = tagFile.getFileSystem(configuration)
    val tagValue = if (fs.exists(tagFile)) {
      Tags.loadTags(tagFile, configuration, tagSchema)
    } else {
      tagSchema.map{col => (col.name, null) }.toMap
    }

    CaseInsensitiveMap(tagValue)
  }

  private def isFileTagNeeded(): Boolean = {
    tagSchema.foreach { field =>
      if (readDataSchema.fields.find(f => f.name.equalsIgnoreCase(field.name)).isDefined) {
        return true
      }
    }

    false
  }

  private def writeTag(fileTag: Map[String, Any], tagName: String, index: Int, writer: UnsafeRowWriter): Unit = {
    val tagValue = fileTag(tagName)
    val field = tagSchema.find(_.name.equalsIgnoreCase(tagName)).get
    field.dataType match {
      case StringType => writer.write(index, if (tagValue == null) {
        UTF8String.fromString("")
      } else {
        UTF8String.fromString(tagValue.toString)
      })
      case IntegerType => writer.write(index, tagValue.asInstanceOf[Integer])
      case LongType => writer.write(index, tagValue.asInstanceOf[Long])
      case FloatType => writer.write(index, tagValue.asInstanceOf[Float])
      case DoubleType => writer.write(index, tagValue.asInstanceOf[Double])
      case DateType => writer.write(index, tagValue.asInstanceOf[Long])
      case TimestampType => writer.write(index, tagValue.asInstanceOf[Long])
      case VarcharType(len) => writer.write(index, if (tagValue == null) {
        UTF8String.fromString("")
      } else {
        UTF8String.fromString(tagValue.toString)
      })
      case other => throw new RuntimeException(s"not supported data type : $other")
    }
  }

  private def buildMetaDataReader(file: PartitionedFile): PartitionReader[InternalRow] = {
    val confValue = broadcastedConf.value.value
    val reader = new HadoopBinaryFileReader(file, confValue)

    Option(TaskContext.get()).foreach(_.addTaskCompletionListener[Unit](_ => reader.close()))

    val iter = if (readDataSchema.isEmpty) {
      val emptyUnsafeRow = new UnsafeRow(0)
      reader.map(_ => emptyUnsafeRow)
    } else {
      val writer = new UnsafeRowWriter(readDataSchema.length)
      writer.resetRowWriter()
      var md = MetaData("pdf", "", -1L, -1L, "", "")

      val contentNeed = readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.WIDTH).isDefined ||
        readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.HEIGHT).isDefined ||
        readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.IMAGETHUMBNAIL).isDefined ||
        readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.TAGS).isDefined
      val bincontent: Array[Byte] = if (contentNeed) {
        reader.next()
      } else {
        Array()
      }
      val imageDim = if (contentNeed) {
        getResolution(bincontent)
      } else {
        null
      }

      val filePath = file.toPath.toString
      val typeIndex = filePath.lastIndexOf(".")
      val typeStr = if (typeIndex > 0) {
        filePath.substring(typeIndex + 1)
      } else {
        null
      }

      md = md.copy(fileType = typeStr)
      md = md.copy(binContent = bincontent)
      md = md.copy(imageDim = imageDim)

      readDataSchema.fields.map(_.name).zipWithIndex.foreach {
        case (UnstructuredData.FILETYPE, index) =>
          writer.write(index, UTF8String.fromString(typeStr))
        case (UnstructuredData.PATH, index) =>
          val path = file.toPath.toUri.toString
          md = md.copy(path = path)
          writer.write(index, UTF8String.fromString(path))
        case (UnstructuredData.MODIFIEDAT, index) =>
          val modifiedAt = file.modificationTime * 1000
          md = md.copy(modifiedAt = modifiedAt)
          writer.write(index, modifiedAt)
        case (UnstructuredData.SIZEINBYTES, index) =>
          md = md.copy(sizeInBytes = file.length)
          writer.write(index, file.length)
        case (UnstructuredData.PREVIEW, index) =>
          val preview = textPreviewFromBinary(reader.next())
          md = md.copy(preview = preview)
          writer.write(index, UTF8String.fromString(preview))
        case (UnstructuredData.WIDTH, index) =>
          writer.write(index, md.imageDim.width)
        case (UnstructuredData.HEIGHT, index) =>
          writer.write(index, md.imageDim.height)
        case (UnstructuredData.IMAGETHUMBNAIL, index) =>
          val thumbnail = thumbnailImage(md.binContent)
          md = md.copy(binContent = thumbnail)
          writer.write(index, thumbnail)

        case (UnstructuredData.SUBDIR, index) =>
          val fullPath = file.toPath.toUri.getPath
          val rootPath = rootPathsSpecified.find(path => fullPath.startsWith(path.toUri.getPath)).get.toUri.getPath

          val lastIndex = fullPath.lastIndexOf("/")
          val subDir = if (lastIndex > 0) {
            fullPath.substring(rootPath.length, lastIndex)
          } else {
            ""
          }
          md = md.copy(subDir = subDir)
          writer.write(index, UTF8String.fromString(subDir))

        case (UnstructuredData.TAGS, index) =>
          if (embeddedTags == null) {
            embeddedTags = extractEmbeddedTags(md.binContent)
          }
          val tagJson = buildJSONTag(embeddedTags)
          md = md.copy(tags = tagJson)
          writer.write(index, UTF8String.fromString(tagJson))

        case (UnstructuredData.DURATION, index) =>
          val duration = getDuration(md.binContent)
          md = md.copy(duration = duration)
          writer.write(index, duration)

        case (UnstructuredData.FORMAT, index) =>
          val format = getFormat(md.binContent)
          md = md.copy(format = format)
          writer.write(index, UTF8String.fromString(format))

        case (tagName, index) =>
          if (fileTag == null) {
            fileTag = readFileTag(file, confValue)
            md.copy(fileTag = fileTag)
          }

          writeTag(fileTag, tagName, index, writer)
      }

      if (pushedFilters.isEmpty) {
        Iterator.single(writer.getRow)
      } else {
        if (pushedFilters.map(createFilter(_)(md)).reduce(_ && _)) {
          Iterator.single(writer.getRow)
        } else {
          Iterator.empty
        }
      }
    }

    val fileReader = new PartitionReaderFromIterator[InternalRow](iter)
    new PartitionReaderWithPartitionValues(fileReader, readDataSchema, partitionSchema, file.partitionValues)
  }

  private def buildContentReader(file: PartitionedFile): PartitionReader[InternalRow] = {
    val confValue = broadcastedConf.value.value
    val reader = new HadoopBinaryFileReader(file, confValue)

    Option(TaskContext.get()).foreach(_.addTaskCompletionListener[Unit](_ => reader.close()))

    val iter = if (readDataSchema.isEmpty) {
      val emptyUnsafeRow = new UnsafeRow(0)
      reader.map(_ => emptyUnsafeRow)
    } else {
      val writer = new UnsafeRowWriter(readDataSchema.length)
      writer.resetRowWriter()
      var md = MetaData("pdf", "", -1L, -1L, "", "")

      val contentNeed = readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.TEXTCONTENT).isDefined ||
        readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.BINCONTENT).isDefined ||
        readDataSchema.fields.find(_.name.toLowerCase == UnstructuredData.IMAGECONTENT).isDefined
      val bincontent: Array[Byte] = if (contentNeed) {
        reader.next()
      } else {
        Array()
      }

      md = md.copy(binContent = bincontent)

      readDataSchema.fields.map(_.name).zipWithIndex.foreach {
        case (UnstructuredData.PATH, index) =>
          val path = file.toPath.toUri.toString
          md = md.copy(path = path)
          writer.write(index, UTF8String.fromString(path))
        case (UnstructuredData.SUBDIR, index) =>
          val fullPath = file.toPath.toUri.getPath
          val rootPath = rootPathsSpecified.find(path => fullPath.startsWith(path.toUri.getPath)).get.toUri.getPath

          val lastIndex = fullPath.lastIndexOf("/")
          val subDir = if (lastIndex > 0) {
            fullPath.substring(rootPath.length, lastIndex)
          } else {
            ""
          }
          md = md.copy(subDir = subDir)
          writer.write(index, UTF8String.fromString(subDir))
        case (UnstructuredData.TEXTCONTENT, index) =>
          val content = textFromBinary(md.binContent)
          writer.write(index, UTF8String.fromString(content))
        case (UnstructuredData.BINCONTENT, index) =>
          writer.write(index, md.binContent)
        case (UnstructuredData.IMAGECONTENT, index) =>
          writer.write(index, md.binContent)
        case (tagName, index) =>
          if (fileTag == null) {
            fileTag = readFileTag(file, confValue)
            md.copy(fileTag = fileTag)
          }
      }

      if (pushedFilters.isEmpty) {
        Iterator.single(writer.getRow)
      } else {
        if (pushedFilters.map(createFilter(_)(md)).reduce(_ && _)) {
          Iterator.single(writer.getRow)
        } else {
          Iterator.empty
        }
      }
    }

    val fileReader = new PartitionReaderFromIterator[InternalRow](iter)
    new PartitionReaderWithPartitionValues(fileReader, readDataSchema, partitionSchema, file.partitionValues)
  }

  override def buildReader(file: PartitionedFile): PartitionReader[InternalRow] = {
    if (isContentTable) {
      buildContentReader(file)
    } else {
      buildMetaDataReader(file)
    }
  }
}
