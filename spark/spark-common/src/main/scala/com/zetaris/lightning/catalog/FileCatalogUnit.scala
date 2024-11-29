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

package com.zetaris.lightning.catalog

import com.fasterxml.jackson.databind.ObjectMapper
import com.zetaris.lightning.datasources.v2.{UnstructuredData, UnstructuredFileFormat}
import com.zetaris.lightning.datasources.v2.image.ImageTable
import com.zetaris.lightning.datasources.v2.pdf.{PdfFileFormat, PdfTable}
import com.zetaris.lightning.datasources.v2.text.TextTable
import com.zetaris.lightning.datasources.v2.video.VideoTable
import com.zetaris.lightning.execution.command.DataSourceType._
import com.zetaris.lightning.model.{HdfsFileSystem, LightningModel}
import com.zetaris.lightning.model.serde.DataSource.DataSource
import com.zetaris.lightning.util.FileSystemUtils
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.FileFormat
import org.apache.spark.sql.execution.datasources.binaryfile.BinaryFileFormat
import org.apache.spark.sql.execution.datasources.csv.CSVFileFormat
import org.apache.spark.sql.execution.datasources.json.JsonFileFormat
import org.apache.spark.sql.execution.datasources.orc.OrcFileFormat
import org.apache.spark.sql.execution.datasources.parquet.ParquetFileFormat
import org.apache.spark.sql.execution.datasources.text.TextFileFormat
import org.apache.spark.sql.execution.datasources.v2.csv.{CSVScanBuilder, CSVTable}
import org.apache.spark.sql.execution.datasources.v2.json.JsonTable
import org.apache.spark.sql.execution.datasources.v2.orc.OrcTable
import org.apache.spark.sql.execution.datasources.v2.parquet.ParquetTable
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.apache.spark.sql.v2.avro.AvroTable
import org.apache.spark.sql.{SparkSQLBridge, SparkSession}

import scala.collection.JavaConverters._

case class FileCatalogUnit(dataSource: DataSource,
                           lightningModel: LightningModel) extends CatalogUnit {
  private val opts = dataSource.properties.map { prop =>
    prop.key -> prop.value
  }.toMap

  private val ciMap: CaseInsensitiveStringMap = new CaseInsensitiveStringMap(mapAsJavaMap(opts))

  def fallbackFileFormat: Class[_ <: FileFormat] = {
    dataSource.dataSourceType match {
      case PARQUET => classOf[ParquetFileFormat]
      case ORC => classOf[OrcFileFormat]
      case CSV => classOf[CSVFileFormat]
      case JSON => classOf[JsonFileFormat]
      case AVRO => SparkSQLBridge.fallbackAvroFileFormat
      case PDF => classOf[PdfFileFormat]
      case TEXT => classOf[TextFileFormat]
      case _ => classOf[UnstructuredFileFormat]
    }

  }

  protected def getPaths(): Seq[String] = {
    val objectMapper = new ObjectMapper()
    val paths = Option(ciMap.get("paths")).map { pathStr =>
      objectMapper.readValue(pathStr, classOf[Array[String]]).toSeq
    }.getOrElse(Seq.empty)
    paths ++ Option(ciMap.get("path")).toSeq
  }

  protected def getOptionsWithoutPaths(): CaseInsensitiveStringMap = {
    val withoutPath = ciMap.asCaseSensitiveMap().asScala.filterKeys { k =>
      !k.equalsIgnoreCase("path") && !k.equalsIgnoreCase("paths")
    }

    new CaseInsensitiveStringMap(withoutPath.asJava)
  }

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    throw new RuntimeException("file source doesn't support list namespaces")
  }

  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    throw new RuntimeException("file source doesn't support create namespace")
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    val namespace = dataSource.namespace
    val path = ciMap.get("path")
    val parentAndChild = HdfsFileSystem.toFolderUrl(path)
    val fs = new HdfsFileSystem(opts, parentAndChild._1)
    fs.listDirectories(parentAndChild._2).filter(!_.startsWith(".")).map(Identifier.of(namespace, _))
      .toArray
  }

  override def loadTable(ident: Identifier, tagSchema: StructType): Table = {
    val paths = getPaths()
    val optionsWithoutPaths = getOptionsWithoutPaths()
    dataSource.dataSourceType match {
      case PARQUET =>
        //org.apache.spark.sql.execution.datasources.v2.parquet.ParquetDataSourceV2
        ParquetTable(ident.name(), SparkSession.active, optionsWithoutPaths, paths, None, fallbackFileFormat)
      case ORC =>
        OrcTable(ident.name(), SparkSession.active, optionsWithoutPaths, paths, None, fallbackFileFormat)
      case CSV =>
        new CSVTable(ident.name(), SparkSession.active, optionsWithoutPaths, paths, None, fallbackFileFormat) {
          override def newScanBuilder(options: CaseInsensitiveStringMap): CSVScanBuilder =
            CSVScanBuilder(sparkSession, fileIndex, schema, dataSchema, optionsWithoutPaths)
        }
      case JSON =>
        JsonTable(ident.name(), SparkSession.active, optionsWithoutPaths, paths, None, fallbackFileFormat)
      case AVRO =>
        AvroTable(ident.name(), SparkSession.active, optionsWithoutPaths, paths, None, fallbackFileFormat)
      case PDF =>
        PdfTable(ident.name(), SparkSession.active,
          UnstructuredData.mapWithFileFormat(opts, PDF), paths, fallbackFileFormat, tagSchema)
      case TEXT =>
        TextTable(ident.name(), SparkSession.active,
          UnstructuredData.mapWithFileFormat(opts, TEXT), paths, fallbackFileFormat, tagSchema)
      case IMAGE =>
        ImageTable(ident.name(), SparkSession.active,
          UnstructuredData.mapWithFileFormat(opts, IMAGE), paths, fallbackFileFormat, tagSchema)
      case VIDEO =>
        VideoTable(ident.name(), SparkSession.active,
          UnstructuredData.mapWithFileFormat(opts, VIDEO), paths, fallbackFileFormat, tagSchema)
      case other =>
        ???
    }
  }

  override def namespaceExists(namespace: Array[String]): Boolean = {
    throw new RuntimeException("file source doesn't support namespace exists")
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    throw new RuntimeException("file source doesn't support drop namespace")
  }

  override def createTable(ident: Identifier, schema: StructType, partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table = {
    throw new RuntimeException("file source doesn't support create table")
  }

  override def dropTable(ident: Identifier): Boolean = {
    throw new RuntimeException("file source doesn't support drop table")
  }

  override def tableExists(ident: Identifier): Boolean = {
    ???
  }

}
