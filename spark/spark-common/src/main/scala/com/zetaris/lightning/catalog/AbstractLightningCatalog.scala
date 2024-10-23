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

package com.zetaris.lightning.catalog

import com.zetaris.lightning.datasources.v2.UnstructuredData
import com.zetaris.lightning.execution.command.DataSourceType
import com.zetaris.lightning.model.{LightningModel, LightningModelFactory}
import com.zetaris.lightning.model.serde.DataSource.DataSource
import org.apache.spark.sql.connector.catalog._
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.execution.datasources.v2.jdbc.JDBCTable
import org.apache.spark.sql.jdbc.{JdbcDialects, SnowflakeDialect}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.mutable.{ArrayBuilder, Map}

object LightningCatalogCache {
  var catalog: AbstractLightningCatalog = null;
}

abstract class AbstractLightningCatalog extends TableCatalog with SupportsNamespaces with MetaDataCatalog {
  override val name = LightningModelFactory.LIGHTNING_CATALOG_NAME
  protected var model: LightningModel = null

  override def initialize(name: String, options: CaseInsensitiveStringMap): Unit = {
    model = LightningModelFactory(options)
    LightningCatalogCache.catalog = this
    JdbcDialects.registerDialect(SnowflakeDialect)
  }

  protected def loadDataSource(namespace: Array[String], name: String): Option[DataSource] = {
    try {
      model.loadDataSources(namespace, name).headOption
    } catch {
      case _: Throwable => None
    }
  }

  protected def findParentDataSource(namespace: Array[String], name: String = null): Option[DataSource] = {
    var dsName = if (name == null ) {
      namespace.last
    } else {
      name
    }

    var parent = if (name == null ) {
      namespace.dropRight(1)
    } else {
      namespace
    }

    var found: Option[DataSource] = None

    while (found.isEmpty && parent.length > 1) {
      found = loadDataSource(parent, dsName)
      dsName = parent.last
      parent = parent.dropRight(1)
    }

    found
  }

  def loadCatalogUnit(dataSource: DataSource): CatalogUnit

  override def createTable(ident: Identifier,
                           schema: StructType,
                           partitions: Array[Transform],
                           properties: java.util.Map[String, String]): Table = {
    findParentDataSource(ident.namespace()) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = ident.namespace().drop(datasource.namespace.length + 1)
        catalog.createTable(Identifier.of(sourceNamespace, ident.name()), schema, partitions, properties)
      case None =>
        throw new RuntimeException(s"namespace(${ident.namespace().mkString(".")}) is not defined")
    }
  }

  override def alterTable(ident: Identifier, changes: TableChange*): Table = {
    throw new RuntimeException("alter table is not supported")
  }

  override def dropTable(ident: Identifier): Boolean = {
    val namespace = ident.namespace()
    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        catalog.dropTable(Identifier.of(sourceNamespace, ident.name()))
      case None =>
        throw new RuntimeException("drop table is not supported")
    }
  }

  override def renameTable(oldIdent: Identifier, newIdent: Identifier): Unit = {
    throw new RuntimeException("rename table is not supported")
  }

  override def listNamespaces(): Array[Array[String]] = {
    val nameSpacesBuilder = ArrayBuilder.make[Array[String]]

    nameSpacesBuilder += Array("datasource")
    nameSpacesBuilder += Array("metastore")

    nameSpacesBuilder.result()
  }

  override def listNamespaces(namespace: Array[String]): Array[Array[String]] = {
    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        catalog.listNamespaces(sourceNamespace)
      case None =>
        val nameSpacesBuilder = ArrayBuilder.make[Array[String]]
        model.listNamespaces(namespace).foreach { ns =>
          nameSpacesBuilder += Array(ns)
        }
        nameSpacesBuilder.result()
    }
  }

  override def loadNamespaceMetadata(namespace: Array[String]): java.util.Map[String, String] = {
    import scala.collection.JavaConverters.mapAsJavaMap
    mapAsJavaMap(Map.empty[String, String])
  }

  private def isNamespaceDataSource(namespace: Array[String], dataSource: DataSource): Boolean = {
    namespace.last == dataSource.name &&  namespace.dropRight(1).sameElements(dataSource.namespace)
  }


  override def namespaceExists(namespace: Array[String]): Boolean = {
    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        // check datasource itself
        if (isNamespaceDataSource(namespace, datasource)) {
          true
        } else {
          catalog.namespaceExists(sourceNamespace)
        }
      case None =>
        val parent = namespace.dropRight(1)
        model.listNamespaces(parent).exists(_.equalsIgnoreCase(namespace.last))
    }
  }

  override def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit = {
    import scala.collection.JavaConverters.mapAsScalaMap

    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        catalog.createNamespace(sourceNamespace, metadata)
      case None =>
        model.createNamespace(namespace, mapAsScalaMap(metadata).toMap)
    }
  }

  override def alterNamespace(namespace: Array[String], changes: NamespaceChange*): Unit = {
    throw new RuntimeException("alter namespace is not supported")
  }

  override def dropNamespace(namespace: Array[String], cascade: Boolean): Boolean = {
    if (namespace.length == 1) {
      val toLower = namespace(0).toLowerCase
      if (toLower == "datasource" || toLower == "metastore") {
        throw new RuntimeException("deleting root namespace(datasource, metastore) is not allowed")
      }
    }

    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        if (isNamespaceDataSource(namespace, datasource)) {
          model.dropDataSource(datasource.namespace, datasource.name)
          true
        } else {
          catalog.dropNamespace(sourceNamespace, cascade)
        }
      case None =>
        model.dropNamespace(namespace, cascade)
        true
    }
  }

  override def tableExists(ident: Identifier): Boolean = {
    val namespace = ident.namespace()
    findParentDataSource(namespace, ident.name()) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
        catalog.tableExists(Identifier.of(sourceNamespace, ident.name()))
      case None =>
        false
    }
  }

  override def loadTable(ingestedSchema: StructType, ident: Identifier) : Table = {
    loadTable(ident) match {
      case jdbcTable : JDBCTable =>jdbcTable.copy(schema = ingestedSchema)
      case other => other
    }
  }

  private def validateNameSpace(namespace: Array[String]) = {
    if (namespace.isEmpty) {
      throw new RuntimeException(s"namespace: [${LightningModelFactory.toFqn(namespace)}] is not provided")
    }

    namespace(0).toLowerCase match {
      case "metastore" | "datasource" =>
      case _ => throw new RuntimeException(s"namespace: [${LightningModelFactory.toFqn(namespace)}] is not valid")
    }
  }

  override def loadTable(ident: Identifier): Table = {
    val namespace = ident.namespace()
    validateNameSpace(namespace)

    namespace(0).toLowerCase match {
      case "metastore" =>
        LightningCatalogUnit(namespace(0), model).loadTable(ident, StructType(List.empty))
      case "datasource" =>
        findParentDataSource(ident.namespace(), ident.name()) match {
          case Some(datasource) if datasource.dataSourceType.isInstanceOf[DataSourceType.FileTypeSource] =>
            if ((datasource.namespace.sameElements(ident.namespace()) && datasource.name.equalsIgnoreCase(ident.name()))
              || ident.name().equalsIgnoreCase(UnstructuredData.CONTENT)) {
              val catalog = loadCatalogUnit(datasource)
              val sourceNamespace = ident.namespace().drop(datasource.namespace.length + 1)
              catalog.loadTable(Identifier.of(sourceNamespace, ident.name()), datasource.toTagSchema())
            } else {
              throw new RuntimeException(s"namespace(${ident.namespace().mkString(".")}), name(${ident.name()}) is not defined")
            }
          case Some(datasource) =>
            val catalog = loadCatalogUnit(datasource)
            val sourceNamespace = ident.namespace().drop(datasource.namespace.length + 1)
            catalog.loadTable(Identifier.of(sourceNamespace, ident.name()), datasource.toTagSchema())
          case _ =>
            throw new RuntimeException(s"namespace(${ident.namespace().mkString(".")}), name(${ident.name()}) is not defined")
        }
    }
  }

  private def loadTableWithTmeTravel(ident: Identifier,
                                     loadFunForMeta: () => Table,
                                     loadFunForDataSource: (DataSource) => Table): Table = {
    val namespace = ident.namespace()
    validateNameSpace(namespace)

    namespace(0).toLowerCase match {
      case "metastore" =>
        loadFunForMeta()
      case "datasource" =>
        findParentDataSource(ident.namespace(), ident.name()) match {
          case Some(datasource) =>
            loadFunForDataSource(datasource)
          case None =>
            throw new RuntimeException(s"namespace(${ident.namespace().mkString(".")}), name(${ident.name()}) is not defined")
        }
    }
  }

  override def loadTable(ident: Identifier, version: String): Table = {
    val namespace = ident.namespace()

    loadTableWithTmeTravel(ident, () => {
      LightningCatalogUnit(namespace(0), model).loadTable(ident, version, StructType(List.empty))
    }, (dataSource: DataSource) => {
      val catalog = loadCatalogUnit(dataSource)
      val sourceNamespace = ident.namespace().drop(dataSource.namespace.length + 1)
      catalog.loadTable(Identifier.of(sourceNamespace, ident.name), version, dataSource.toTagSchema())
    })
  }

  override def loadTable(ident: Identifier, timestamp: Long): Table = {
    val namespace = ident.namespace()

    loadTableWithTmeTravel(ident, () => {
      LightningCatalogUnit(namespace(0), model).loadTable(ident, timestamp, StructType(List.empty))
    }, (dataSource: DataSource) => {
      val catalog = loadCatalogUnit(dataSource)
      val sourceNamespace = ident.namespace().drop(dataSource.namespace.length + 1)
      catalog.loadTable(Identifier.of(sourceNamespace, ident.name), timestamp, dataSource.toTagSchema())
    })
  }

  override def listTables(namespace: Array[String]): Array[Identifier] = {
    findParentDataSource(namespace) match {
      case Some(datasource) =>
        val catalog = loadCatalogUnit(datasource)
        if (datasource.dataSourceType == DataSourceType.DELTA) {
          catalog.listTables(Array(namespace.last))
        } else {
          val sourceNamespace = namespace.drop(datasource.namespace.length + 1)
          catalog.listTables(sourceNamespace)
        }

      case None =>
        model.listTables(namespace).map { table =>
          Identifier.of(Array(namespace.last), table)
        }.toArray
    }
  }
}
