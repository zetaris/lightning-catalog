/*
 * Copyright 2023 ZETARIS Pty Ltd
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.zetaris.lightning.model

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import com.zetaris.lightning.execution.command.DataSourceType.DataSourceType
import com.zetaris.lightning.model.LightningModel.LightningModel
import com.zetaris.lightning.model.serde.DataSource.DataSource
import com.zetaris.lightning.model.serde.DataSource.simpleClassName
import com.zetaris.lightning.model.serde.DataSource.toJson
import com.zetaris.lightning.error.LightningException.LightningTableNotFoundException
import com.zetaris.lightning.execution.command.{CreateTableSpec, DataSourceType, RegisterTableSpec}
import com.zetaris.lightning.model.serde.{CreateTable, RegisterTable, mapToJson}
import com.zetaris.lightning.util.FileSystemUtils

import scala.util.Failure
import scala.util.Success
import scala.util.Try

// TODO : Convert FileSystem API to HDFS API
class LightningHdfsModel(prop: CaseInsensitiveStringMap) extends LightningModel{
  if (!prop.containsKey(LightningModel.LIGHTNING_MODEL_WAREHOUSE_KEY)) {
    throw new RuntimeException(s"${LightningModel.LIGHTNING_MODEL_WAREHOUSE_KEY} is not set in spark conf")
  }

  private val modelDir = prop.get(LightningModel.LIGHTNING_MODEL_WAREHOUSE_KEY)
  val DATASOURCE_DIR = "datasource"
  val METASTORE_DIR = "metastore"

  createModelDirIfNotExist()

  private def nameSpaceToDir(namespace: Seq[String]) = namespace.mkString("/")

  private def createModelDirIfNotExist(): Unit = {
    FileSystemUtils.createFolderIfNotExist(modelDir)

    // datasource
    FileSystemUtils.createFolderIfNotExist(s"$modelDir/$DATASOURCE_DIR")

    // metastore definition
    FileSystemUtils.createFolderIfNotExist(s"$modelDir/$METASTORE_DIR")
  }

  /**
   * Save data source into sub directory of datasource
   *
   * @param dataSource
   * @param replace
   * @return saved file path
   */
  override def saveDataSource(dataSource: DataSource, replace: Boolean): String = {
    val json = toJson(dataSource)
    val dir = nameSpaceToDir(dataSource.namespace)
    val filePath = s"$modelDir/$dir/${dataSource.name}.json"
    FileSystemUtils.saveFile(filePath, json, replace)
    filePath
  }

  override def loadDataSources(namespace: Array[String], name: String = null): List[DataSource] = {
    val subDir = nameSpaceToDir(namespace)
    if (name == null) {
      FileSystemUtils.listFiles(s"$modelDir/$subDir").filter(_.endsWith(".json")).map { file =>
        val json = FileSystemUtils.readFile(file)
        serde.DataSource(json)
      }.toList
    } else {
      val filePath = s"$modelDir/$subDir/$name.json"
      val json = FileSystemUtils.readFile(filePath)
      List(serde.DataSource(json))
    }
  }

  override def saveCreateTable(lakehouse: String, createTableSpec: CreateTableSpec): String = {
    val json = CreateTable.toJson(createTableSpec)
    val filePath = s"$modelDir/$METASTORE_DIR/$lakehouse/${LightningModel.toFqn(createTableSpec.fqn)}_table.json"
    FileSystemUtils.saveFile(filePath, json, createTableSpec.ifNotExit)
    filePath
  }

  override def saveRegisterTable(registerTable: RegisterTableSpec, replace: Boolean): String = {
    val lakehouse = registerTable.fqn.dropRight(1)
    val json = RegisterTable.toJson(registerTable)
    val filePath = s"$modelDir/$METASTORE_DIR/${LightningModel.toFqn(lakehouse)}/${registerTable.fqn.last}_sql.json"
    FileSystemUtils.saveFile(filePath, json, replace)
    filePath
  }

  override def loadRegisteredTable(lakehouse: String, table: String): RegisterTableSpec = {
    val filePath = s"$modelDir/$METASTORE_DIR/$lakehouse/${table}_sql.json"
    val json = FileSystemUtils.readFile(filePath)
    RegisterTable(json)
  }

  override def createLakeWarehouse(lakehouse: String): String = {
    val dir = s"$modelDir/$METASTORE_DIR/$lakehouse"

    if (FileSystemUtils.folderExist(dir)) {
      throw new RuntimeException(s"lake warehouse: $dir is already created")
    }
    FileSystemUtils.createFolderIfNotExist(dir)
    dir
  }

  override def listLakeHouse(): Seq[String] = {
    val dir = s"$modelDir/$METASTORE_DIR"
    FileSystemUtils.listDirectories(dir)
  }

  override def listLakeHouseTables(lakehouse: String): Seq[String] = {
    val path = s"$modelDir/$METASTORE_DIR/$lakehouse"
    FileSystemUtils.listFiles(path).filter(_.endsWith("_table.json")).map { withSuffix =>
      withSuffix.substring(0, withSuffix.length - 11)
    }
  }

  override def loadLakeHouseTable(lakehouse: String, table: String): CreateTableSpec = {
    val path = s"$modelDir/$METASTORE_DIR/$lakehouse/${table}_table.json"
    val json = FileSystemUtils.readFile(path)
    CreateTable(json)
  }

  override def listNameSpaces(nameSpace: Seq[String]): Seq[String] = {
    val subDir = nameSpaceToDir(nameSpace)
    val fullPath = s"$modelDir/$subDir"
    FileSystemUtils.listDirectories(fullPath) ++
      FileSystemUtils.listFiles(fullPath).filter(_.endsWith(".json")).map(_.dropRight(5))

  }


  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    import scala.collection.JavaConverters.mapAsScalaMap
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"
    FileSystemUtils.createFolderIfNotExist(fullPath)

    val json = mapToJson(mapAsScalaMap(metadata).toMap)
    FileSystemUtils.saveFile(s"$fullPath/.properties", json)
  }

  def dropNamespace(namespace: Array[String], cascade: Boolean): Unit = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"
    val subNamespaces = FileSystemUtils.listDirectories(fullPath)
    if (!cascade && subNamespaces.nonEmpty) {
      throw new RuntimeException(s"${LightningModel.toFqn(namespace)} has sub namespaces")
    }

    FileSystemUtils.deleteDirectory(fullPath)
  }

  override def loadTableSpec(lakehouse: String, fqn: Seq[String]): CreateTableSpec = {
    val filePath = s"$modelDir/$METASTORE_DIR/$lakehouse/${LightningModel.toFqn(fqn)}_table.json"
    Try {
      FileSystemUtils.readFile(filePath)
    } match {
      case Success(json) => CreateTable(json)
      case Failure(exception) => throw new LightningTableNotFoundException(LightningModel.toFqn(fqn), exception)
    }
  }

  override def dropLakeWarehouse(lakehouse: String): Unit = {
    val dir = s"$modelDir/$METASTORE_DIR/$lakehouse"
    FileUtils.deleteDirectory(new java.io.File(dir))

  }
}
