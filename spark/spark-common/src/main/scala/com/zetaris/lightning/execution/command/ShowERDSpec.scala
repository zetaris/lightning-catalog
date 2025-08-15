package com.zetaris.lightning.execution.command

import com.zetaris.lightning.model.LightningModelFactory
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.{Row, SparkSession}

import java.sql.{DatabaseMetaData, DriverManager, ResultSet}
import scala.util.{Failure, Success, Try}

case class ShowERDSpec(fqnSchema: Seq[String]) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("erd", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val model = LightningModelFactory(dataSourceConfigMap(sparkSession))
    val schema = fqnSchema.last
    val fqnDataSource = fqnSchema.dropRight(1)
    val dataSourceName = fqnDataSource.last
    val fqnNameSpace = fqnDataSource.dropRight(1)
    val dataSource = model.loadDataSources(fqnNameSpace.toArray, dataSourceName).headOption.getOrElse(
      throw new IllegalArgumentException(s"${fqnDataSource.mkString(".")} doesn't exist")
    )

    val url = dataSource.properties.find(_.key.equalsIgnoreCase("url")).get.value
    val user = dataSource.properties.find(_.key.equalsIgnoreCase("user")).map(_.value).orNull
    val password = dataSource.properties.find(_.key.equalsIgnoreCase("password")).map(_.value).orNull
    val allTables = ShowERDSpec.getAllTableColumnMetas(url, user, password, schema)
    val allTablesWithConstraints = ShowERDSpec.readDatabaseConstraints(url, user, password, null, allTables)
    val ddl = ShowERDSpec.drawDDL(allTablesWithConstraints)
    Row(ddl) :: Nil
  }
}

object ShowERDSpec {
  case class TableMeta(schema: String, tableName: String, columnMetas: Seq[ColumnMeta])

  case class ColumnMeta(columnName: String,
                        typeName: String,
                        size: Int,
                        nullable: Boolean,
                        primaryKey: Boolean,
                        foreignKeyMeta: Option[ForeignKeyMeta],
                        index: Option[String],
                        autoIncrement: Boolean)

  case class ForeignKeyMeta(schema: String, table: String, column: String, refTable: String, refColumn: String)

  private case class SchemaTableInfo(schema: String, tableName: String)

  private case class ColumnIndexInfo(columnName: String, indexName: String)

  private def isSnowflake(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:snowflake://")

  private def isOcient(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:ocient://")

  private def isMsSql(jdbcUrl: String): Boolean = jdbcUrl.startsWith("jdbc:sqlserver://")

  def closeCloseable(closable: AutoCloseable*): Unit = closable.filter(_ != null).foreach { closable =>
    try {
      closable.close()
    } catch {
      case _: Throwable =>
    }
  }

  def drawDDL(tables: Seq[TableMeta]): String = {
    tables.map { tableMeta =>
      var tableDDL = s"create table ${tableMeta.tableName}\n" +
         "(\n"
      val columnDDL = tableMeta.columnMetas.foreach { columnMeta =>
        val typeName = if (columnMeta.typeName.startsWith("char")) {
          s"char(${columnMeta.size})"
        } else if (columnMeta.typeName.indexOf("varchar") >= 0) {
          s"${columnMeta.typeName}(${columnMeta.size})"
        } else {
          columnMeta.typeName
        }

        val primaryKey = if (columnMeta.primaryKey) {
          "primary key"
        } else {
          ""
        }

        val nullable = if (columnMeta.nullable || columnMeta.primaryKey) {
          ""
        } else {
          "not null"
        }

        val foreignKey = if (columnMeta.foreignKeyMeta.isDefined) {
          s"references ${columnMeta.foreignKeyMeta.get.refTable}(${columnMeta.foreignKeyMeta.get.refColumn})"
        } else {
          ""
        }

        s"  ${columnMeta.columnName} $typeName $primaryKey $nullable $foreignKey\n"
      }

      tableDDL += ");"
    }.mkString("\n")
  }

  def getAllTableColumnMetas(jdbcUrl: String, user: String, password: String, schema: String): Seq[TableMeta] = {
    val schemaTableToColumn = scala.collection.mutable.Map.empty[SchemaTableInfo, List[ColumnMeta]]

    val conn = DriverManager.getConnection(jdbcUrl, user, password)
    val metaData = conn.getMetaData()
    val rs = metaData.getColumns(null, schema, "%", "%")
    while (rs.next()) {
      val tableCatalog = rs.getString("TABLE_CAT")
      val tableSchema = rs.getString("TABLE_SCHEM")

      if (tableSchema.equalsIgnoreCase(schema)) {
        val table = rs.getString("TABLE_NAME")
        val column = rs.getString("COLUMN_NAME")
        val size = rs.getInt("COLUMN_SIZE")
        val nullable = if (isOcient(jdbcUrl) || isSnowflake(jdbcUrl)) {
          rs.getInt("NULLABLE") > 0
        } else {
          rs.getBoolean("NULLABLE")
        }

        val autoIncrement = if (isMsSql(jdbcUrl)) {
          false
        } else {
          Try(rs.getBoolean("IS_AUTOINCREMENT")).getOrElse(false)
        }

        val typeName = rs.getString("TYPE_NAME")

        val columnMeta = ColumnMeta(
          columnName = column,
          typeName = typeName,
          size = size,
          nullable = nullable,
          primaryKey = false,
          foreignKeyMeta = None,
          index = None,
          autoIncrement = autoIncrement
        )

        val schemaTableInfo = SchemaTableInfo(tableSchema, table)
        val cachedColumns = schemaTableToColumn.getOrElse(schemaTableInfo, List.empty[ColumnMeta])
        if (cachedColumns.find(_.columnName.equalsIgnoreCase(columnMeta.columnName)).isEmpty) {
          schemaTableToColumn.put(schemaTableInfo, cachedColumns :+ columnMeta)
        }
      }
    }

    closeCloseable(conn)
    schemaTableToColumn.map { case (SchemaTableInfo(schema, tableName), columnMetas) =>
      TableMeta(schema, tableName, columnMetas)
    }.toSeq
  }

  def readDatabaseConstraints(jdbcUrl: String,
                              user: String,
                              password: String,
                              tableCatalog: String,
                              schemaTables: Seq[TableMeta]): Seq[TableMeta] = {
    val schemaTableToColumn = scala.collection.mutable.Map.empty[SchemaTableInfo, List[ColumnMeta]]

    val conn = DriverManager.getConnection(jdbcUrl, user, password)
    val md = conn.getMetaData

    schemaTables.foreach { schemaTable =>
      schemaTableToColumn += (SchemaTableInfo(schemaTable.schema,
        schemaTable.tableName
      ) -> schemaTable.columnMetas.toList)
    }

    schemaTables.foreach { schemaTable =>
      getPkColumn(tableCatalog, md, schemaTable.schema, schemaTable.tableName) match {
        case Success(Some(column)) =>
          val tableMetaWithPk = schemaTableToColumn(SchemaTableInfo(schemaTable.schema, schemaTable.tableName)).map {
            columnMeta =>
              if (columnMeta.columnName.equalsIgnoreCase(column)) {
                columnMeta.copy(columnName = column, primaryKey = true, foreignKeyMeta = None, index = None)
              } else {
                columnMeta
              }
          }
          schemaTableToColumn(SchemaTableInfo(schemaTable.schema, schemaTable.tableName)) = tableMetaWithPk
        case Failure(_) =>
        case _ =>
      }

      getForeignKey(tableCatalog, md, schemaTable.schema, schemaTable.tableName) match {
        case Success(fks) =>
          fks.foreach { fk =>
            schemaTableToColumn.get(SchemaTableInfo(fk.schema, fk.table)).map { tableMeta =>
              val tableMetaWithFk = tableMeta.map(columnMeta =>
                if (fk.column.equalsIgnoreCase(columnMeta.columnName)) {
                  columnMeta.copy(foreignKeyMeta = Some(fk))
                } else {
                  columnMeta
                }
              )
              schemaTableToColumn(SchemaTableInfo(fk.schema, fk.table)) = tableMetaWithFk
            }
          }
        case Failure(_) =>
      }

      getIndexes(tableCatalog, md, schemaTable.schema, schemaTable.tableName) match {
        case Success(indexes) =>
          val tableMetaWithIndex = schemaTableToColumn(SchemaTableInfo(schemaTable.schema, schemaTable.tableName)).map {
            columnMeta =>
              indexes
                .find(col => columnMeta.columnName.equalsIgnoreCase(col.columnName))
                .map(columnIndexInfo => columnMeta.copy(index = Some(columnIndexInfo.indexName)))
                .getOrElse(columnMeta)
          }
          schemaTableToColumn(SchemaTableInfo(schemaTable.schema, schemaTable.tableName)) = tableMetaWithIndex
        case Failure(th) =>
      }
    }

    closeCloseable(conn)
    schemaTableToColumn.map { case (SchemaTableInfo(schema, tableName), columnMetas) =>
      TableMeta(schema, tableName, columnMetas)
    }.toSeq

    closeCloseable(conn)
    schemaTableToColumn.map { case (SchemaTableInfo(schema, tableName), columnMetas) =>
      TableMeta(schema, tableName, columnMetas)
    }.toSeq
  }

  private def getPkColumn(tableCatalog: String,
                          md: DatabaseMetaData,
                          schema: String,
                          table: String): Try[Option[String]] = {
    useResultSetWithTry(md.getPrimaryKeys(tableCatalog, schema, table)) { it =>
      it.map(_.getString("COLUMN_NAME")).toIterable.headOption
    }.recoverWith { case th: Throwable =>
      Failure(th)
    }
  }

  private def getForeignKey(tableCatalog: String,
                            md: DatabaseMetaData,
                            schema: String,
                            table: String): Try[List[ForeignKeyMeta]] = {
    useResultSetWithTry(md.getExportedKeys(tableCatalog, schema, table)) { it =>
      it.map { rs =>
        val table = rs.getString("FKTABLE_NAME")
        val column = rs.getString("FKCOLUMN_NAME")
        val refTable = rs.getString("PKTABLE_NAME")
        val refColumn = rs.getString("PKCOLUMN_NAME")
        ForeignKeyMeta(schema, table, column, refTable, refColumn)
      }.toList
    }.recoverWith { case th: Throwable =>
      Failure(th)
    }
  }

  private def getIndexes(tableCatalog: String,
                         md: DatabaseMetaData,
                         schema: String,
                         table: String): Try[List[ColumnIndexInfo]] = {
    useResultSetWithTry(md.getIndexInfo(tableCatalog, schema, table, false, false)) { it =>
      it.map { rs =>
        val column = rs.getString("COLUMN_NAME")
        val indexName = rs.getString("INDEX_NAME")
        ColumnIndexInfo(column, indexName)
      }.toList
    }.recoverWith { case th: Throwable =>
      Failure(th)
    }
  }

  private def useResultSetWithTry[T](acquire: => ResultSet)(map: Iterator[ResultSet] => T): Try[T] = {
    Try(acquire).flatMap { resultSet =>
      val iterator = resultSetIterator(resultSet)
      val mapped = Try(map(iterator))
      closeCloseable(resultSet)
      mapped
    }
  }

  private def resultSetIterator(resultSet: ResultSet): Iterator[ResultSet] = {
    new Iterator[ResultSet] {
      def hasNext: Boolean = resultSet.next()

      def next(): ResultSet = resultSet
    }
  }


}