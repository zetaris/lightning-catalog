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

import com.zetaris.lightning.catalog.LightningCatalogCache
import com.zetaris.lightning.datasources.v2.usl.USLTable
import com.zetaris.lightning.execution.command.{CreateTableSpec, ShowNamespacesOrTables}
import com.zetaris.lightning.execution.command.DataSourceType.FileTypeSource
import com.zetaris.lightning.model.serde.DataSource.{DataSource, toJson}
import com.zetaris.lightning.model.serde.UnifiedSemanticLayerTable
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer
import com.zetaris.lightning.model.serde.{jsonToMap, mapToJson}
import com.zetaris.lightning.util.FileSystemUtils
import org.apache.spark.sql.connector.catalog.{Identifier, Table}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import scala.collection.JavaConverters._

class LightningHdfsModel(prop: CaseInsensitiveStringMap) extends LightningModel {
  if (!prop.containsKey(LightningModelFactory.LIGHTNING_MODEL_WAREHOUSE_KEY)) {
    throw new RuntimeException(s"${LightningModelFactory.LIGHTNING_MODEL_WAREHOUSE_KEY} is not set in spark conf")
  }

  private val scalaMap = prop.asScala.toMap

  private val modelDir = prop.get(LightningModelFactory.LIGHTNING_MODEL_WAREHOUSE_KEY)
  val DATASOURCE_DIR = "datasource"
  val METASTORE_DIR = "metastore"

  createModelDirIfNotExist()

  private def nameSpaceToDir(namespace: Seq[String]) = namespace.mkString("/")

  private def createModelDirIfNotExist(): Unit = {

    val parentAndChild = HdfsFileSystem.toFolderUrl(modelDir)
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.createFolderIfNotExist(parentAndChild._2)

    fs = new HdfsFileSystem(scalaMap, modelDir)
    fs.createFolderIfNotExist(DATASOURCE_DIR)

    fs = new HdfsFileSystem(scalaMap, modelDir)
    fs.createFolderIfNotExist(METASTORE_DIR)
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
    val filePath = dataSource.dataSourceType match {
      case _: FileTypeSource =>
        s"$modelDir/$dir/${dataSource.name}_fs.json"
      case _ =>
        s"$modelDir/$dir/${dataSource.name}_ds.json"
    }

    val parentAndChild = HdfsFileSystem.toFolderUrl(filePath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.saveFile(parentAndChild._2, json, replace)

    filePath
  }

  /**
   * load data source, data source definition json file.
   * _fs.json suffix is for file source and _ds.json suffix is for other type of data source
   *
   * @param namespace
   * @param name
   * @return list of namespace
   */
  override def loadDataSources(namespace: Array[String], name: String = null): List[DataSource] = {
    val subDir = nameSpaceToDir(namespace)

    if (name == null) {
      val parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
      val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
      fs.listFiles(parentAndChild._2).filter { file =>
        file.endsWith("_fs.json") || file.endsWith("_ds.json")
      }.map { file =>
        val parentAndChild = HdfsFileSystem.toFolderUrl(file)
        val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
        val json = fs.readFile(parentAndChild._2)
        serde.DataSource(json)
      }.toList
    } else {
      val dataSourcePath = s"$modelDir/$subDir/${name}_ds.json"
      val fileSourcePath = s"$modelDir/$subDir/${name}_fs.json"

      var parentAndChild = HdfsFileSystem.toFolderUrl(dataSourcePath)
      var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

      if (fs.fileExists(parentAndChild._2)) {
        val json = fs.readFile(parentAndChild._2)
        List(serde.DataSource(json))
      } else {
        parentAndChild = HdfsFileSystem.toFolderUrl(fileSourcePath)
        fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

        if (fs.fileExists(parentAndChild._2)) {
          val json = fs.readFile(parentAndChild._2)
          List(serde.DataSource(json))
        } else {
          List.empty
        }
      }
    }
  }

  /**
   * Drop datasource definition json file in the given namespace
   * @param namespace
   * @param name
   */
  def dropDataSource(namespace: Array[String], name: String): Unit = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir/${name}_ds.json"

    val parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.deleteFile(parentAndChild._2)
  }

  /**
   * list namespace, sub directories or data source definition under the given namespace
   * _fs.json suffix is for file source and _ds.json suffix is for other type of data source.
   * _usl.json suffix is for unified semantic layer
   *
   * @param namespace
   * @return namespaces
   */
  override def listNamespaces(namespace: Seq[String]): Seq[String] = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"
    val parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.listDirectories(parentAndChild._2).filterNot(_.startsWith(".")) ++
      fs.listFiles(parentAndChild._2).filter { file =>
        file.endsWith("_ds.json")
      }.map(_.dropRight(8)) ++
      fs.listFiles(parentAndChild._2).filter { file =>
        file.endsWith("_usl.json")
      }.map(_.dropRight(9))
  }

  /**
   * list tables under the given namespace, table definition should have _table.json suffix for data source.
   * Tables ared defined in *_usl.json for unified semantic layser
   *
   * @param namespace
   * @return table names
   */
  override def listTables(namespace: Array[String]): Seq[String] = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"
    var parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    val dsTables = fs.listFiles(parentAndChild._2).filter(file =>
      file.endsWith("_table.json") || file.endsWith("_fs.json"))
      .map { file =>
        if (file.endsWith("_table.json")) {
          file.dropRight(11)
        } else {
          file.dropRight(8)
        }
      }

    val uslTables = if (namespace.length > 2) {
      val uslFullPath = s"$modelDir/${nameSpaceToDir(namespace.dropRight(1))}/${namespace.last}_usl.json"
      parentAndChild = HdfsFileSystem.toFolderUrl(uslFullPath)
      fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

      if (fs.fileExists(parentAndChild._2)) {
        val json = fs.readFile(parentAndChild._2)
        UnifiedSemanticLayer(json).tables.map(_.name)
      } else {
        Seq.empty
      }
    } else {
      Seq.empty
    }

    dsTables ++ uslTables
  }

  /**
   * create namespace, create sub directory as well as saving metadata into .properties file
   *
   * @param namespace
   * @param metadata
   */
  override def createNamespace(namespace: Array[String], metadata: Map[String, String]): Unit = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"

    var parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.createFolderIfNotExist(parentAndChild._2)

    val json = mapToJson(metadata)
    parentAndChild = HdfsFileSystem.toFolderUrl(s"$fullPath/.properties")
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    fs.saveFile(parentAndChild._2, json)
  }

  /**
   * Load namespace metadata
   * @param namespace
   * @return
   */
  override def loadNamespaceMeta(namespace: Array[String]): Map[String, String] = {
    val subDir = nameSpaceToDir(namespace)
    var parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    if (!fs.folderExist(parentAndChild._2)) {
      throw NamespaceNotFoundException(s"$modelDir/$subDir")
    }
    val fullPath = s"$modelDir/$subDir/.properties"

    parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    val json = fs.readFile(parentAndChild._2)
    jsonToMap(json)
  }

  /**
   * delete namespace dir
   *
   * @param namespace
   * @param cascade delete cascade if true
   */
  def dropNamespace(namespace: Array[String], cascade: Boolean): Unit = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir"
    val parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    val subNamespaces = fs.listDirectories(parentAndChild._2).filterNot(_.startsWith("."))
    if (!cascade && subNamespaces.nonEmpty) {
      throw new RuntimeException(s"${LightningModelFactory.toFqn(namespace)} has sub namespaces")
    }

    fs.deleteDirectory(parentAndChild._2)
  }

  /**
   * save table under the given namespace
   * @param srcNamespace namespace of data source definition
   * @param destNamespace
   * @param name
   * @param schema
   */
  override def saveTable(srcNamespace: Array[String],
                         destNamespace: Array[String],
                         name: String,
                         schema: StructType): Unit = {
    val subDir = nameSpaceToDir(destNamespace)
    var parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    fs.createFolderIfNotExist(parentAndChild._2)

    val table = serde.Table.Table(LightningModelFactory.toFqn(srcNamespace), schema)
    val json = serde.Table.toJson(table)

    val fullPath = s"$modelDir/$subDir/${name}_table.json"
    parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.saveFile(parentAndChild._2, json)
  }

  /**
   * load table
   *
   * @param ident
   * @return
   */
  def loadTable(ident: Identifier): Table = {
    val uslNameSpace = ident.namespace().dropRight(1)
    val uslName = ident.namespace().last
    val subDir = nameSpaceToDir(uslNameSpace)

    val uslFullPath = s"$modelDir/$subDir/${uslName}_usl.json"
    var parentAndChild = HdfsFileSystem.toFolderUrl(uslFullPath)
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    if (fs.fileExists(parentAndChild._2)) {
      val usl = loadUnifiedSemanticLayer(uslNameSpace, uslName)
      val createTableSpec = usl.tables.find(_.name.equalsIgnoreCase(ident.name())).getOrElse(
        throw TableNotFoundException(s"${ident.namespace().mkString(".")}.${ident.name()}")
      )

      val tableFullPath = s"$modelDir/$subDir/.$uslName/${ident.name()}_activation_query.json"
      parentAndChild = HdfsFileSystem.toFolderUrl(tableFullPath)
      fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

      if (fs.fileExists(parentAndChild._2)) {
        val tableJson = fs.readFile(parentAndChild._2)
        val uslTable = UnifiedSemanticLayerTable(tableJson)

        USLTable(createTableSpec, Some(uslTable.query))
      } else {
        USLTable(createTableSpec, None)
      }

    } else {
      val fullPath = s"$modelDir/${nameSpaceToDir(ident.namespace())}/${ident.name()}_table.json"
      parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
      fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

      if (!fs.fileExists(parentAndChild._2)) {
        throw TableNotFoundException(s"${ident.namespace().mkString(".")}.${ident.name()}")
      }

      val json = fs.readFile(parentAndChild._2)
      val table = serde.Table(json)
      val targetNamespace = LightningModelFactory.toMultiPartIdentifier(table.dsNamespace).toArray
      LightningCatalogCache.catalog.loadTable(table.schema, Identifier.of(targetNamespace, ident.name()))
    }
  }

  /**
   * check unfied semantic layer can be loaded or not
   * @param namespace
   * @param name
   * @param tables
   */
  def canLoadUnifiedSemanticLayer(namespace: Seq[String], name: String): Boolean = {
    val subDir = nameSpaceToDir(namespace)
    val fullPath = s"$modelDir/$subDir/${name}_usl.json"
    val parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    fs.fileExists(parentAndChild._2)
  }

  /**
   * Save unified semantic layer
   * @param namespace
   * @param name
   * @param tables
   */
  override def saveUnifiedSemanticLayer(namespace: Seq[String], name: String, tables: Seq[CreateTableSpec]): Unit = {
    val json = UnifiedSemanticLayer.toJson(namespace, name, tables)
    val subDir = nameSpaceToDir(namespace)

    var parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.createFolderIfNotExist(parentAndChild._2)

    val fullPath = s"$modelDir/$subDir/${name}_usl.json"
    parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.saveFile(parentAndChild._2, json)
  }

  /**
   * load unified semantic layer
   * @param namespace
   * @param name
   */
  override def loadUnifiedSemanticLayer(namespace: Seq[String],
                                        name: String): UnifiedSemanticLayer.UnifiedSemanticLayer = {
    val subDir = nameSpaceToDir(namespace)
    var parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    if (!fs.folderExist(parentAndChild._2)) {
      throw NamespaceNotFoundException(s"$modelDir/$subDir")
    }

    val fullPath = s"$modelDir/$subDir/${name}_usl.json"
    parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

    val json = fs.readFile(parentAndChild._2)
    val usl = UnifiedSemanticLayer(json)
    val tableSpecWithActivatedQuery = usl.tables.map { tableSpec =>
      val queryPath = s"$modelDir/$subDir/.${name}/${tableSpec.name}_activation_query.json"

      parentAndChild = HdfsFileSystem.toFolderUrl(queryPath)
      fs = new HdfsFileSystem(scalaMap, parentAndChild._1)

      if (fs.folderExist(parentAndChild._2)) {
        val json = fs.readFile(parentAndChild._2)
        tableSpec.copy(activateQuery = Some(json))
      } else {
        tableSpec
      }
    }

    usl.copy(tables = tableSpecWithActivatedQuery)
  }

  /**
   * save mapping query for the given USL table
   * @param namespace
   * @param name
   * @param query
   */
  override def saveUnifiedSemanticLayerTableQuery(namespace: Seq[String], name: String, query: String): Unit = {
    val json = UnifiedSemanticLayerTable.toJson(name, query)
    val subDir = nameSpaceToDir(namespace.dropRight(1))

    var parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir")
    var fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.createFolderIfNotExist(parentAndChild._2)

    parentAndChild = HdfsFileSystem.toFolderUrl(s"$modelDir/$subDir/.${namespace.last}")
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.createFolderIfNotExist(parentAndChild._2)

    val fullPath = s"$modelDir/$subDir/.${namespace.last}/${name}_activation_query.json"
    parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    fs = new HdfsFileSystem(scalaMap, parentAndChild._1)
    fs.saveFile(parentAndChild._2, json)
  }

  /**
   * load mapping query for the given USL table
   * @param namespace
   * @param name
   */
  override def loadUnifiedSemanticLayerTableQuery(namespace: Seq[String],
                                                  name: String): UnifiedSemanticLayerTable.UnifiedSemanticLayerTable = {
    val subDir = nameSpaceToDir(namespace.dropRight(1))
    val fullPath = s"$modelDir/$subDir/.${namespace.last}/${name}_activation_query.json"

    val parentAndChild = HdfsFileSystem.toFolderUrl(fullPath)
    val fs = new HdfsFileSystem(scalaMap, parentAndChild._1)


    if (!fs.fileExists(parentAndChild._2)) {
      throw TableNotActivatedException(s"${namespace.mkString(".")}.${name}")
    }

    val json = fs.readFile(parentAndChild._2)
    UnifiedSemanticLayerTable(json)
  }


}
