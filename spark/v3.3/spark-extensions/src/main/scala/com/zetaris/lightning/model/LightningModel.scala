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

import com.zetaris.lightning.analysis.{AccessControlProvider, NotAppliedAccessControlProvider}
import com.zetaris.lightning.execution.command.{CreateTableSpec, RegisterTableSpec}
import org.apache.spark.sql.types.{BooleanType, ByteType, CharType, DataType, DateType, DecimalType, DoubleType, FloatType, IntegerType, LongType, ShortType, StringType, TimestampType, VarcharType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import com.zetaris.lightning.execution.command.DataSourceType.DataSourceType
import com.zetaris.lightning.model.serde.DataSource.DataSource

object LightningModel {
  val LIGHTNING_CATALOG_NAME = "lightning"
  val LIGHTNING_CATALOG = s"spark.sql.catalog.$LIGHTNING_CATALOG_NAME"
  val LIGHTNING_MODEL_TYPE_KEY = "type"
  val LIGHTNING_MODEL_WAREHOUSE_KEY = "warehouse"
  val LIGHTNING_ACCESS_CONTROL_KEY = "accessControlProvider"

  val DEFAULT_NAMESPACES = List("lightning.datasource", "lightning.metastore")

  var cached: LightningModel = null
  var accessControlProvider: AccessControlProvider = new NotAppliedAccessControlProvider

  def toFqn(fqn: Seq[String]): String = fqn.mkString(".")

  def toMultiPartIdentifier(fqn: String): Seq[String] = if (fqn.indexOf(".") > 0) {
    fqn.split(".")
  } else {
    Seq(fqn)
  }

  def apply(prop: CaseInsensitiveStringMap): LightningModel = {
    if (!prop.containsKey(LIGHTNING_MODEL_TYPE_KEY)) {
      throw new RuntimeException(s"${LIGHTNING_MODEL_TYPE_KEY} is not set in spark conf")
    }

    Option(prop.get(LIGHTNING_ACCESS_CONTROL_KEY)).foreach { className =>
      accessControlProvider = Class.forName(className).newInstance.asInstanceOf[AccessControlProvider]
    }

    val modelType = prop.get(LIGHTNING_MODEL_TYPE_KEY)

    synchronized {
      if (cached == null) {
        cached = modelType.toLowerCase match {
          case "hadoop" => new LightningHdfsModel(prop)
          case _ => throw new IllegalArgumentException(s"only hadoop implementation is supported")
        }
      }
    }
    cached
  }


  /**
   * true as long as queried type is bigger than defined type
   *
   * @param defined
   * @param queried
   * @return
   */
  def dataTypeQueryable(defined: DataType, queried: DataType): Boolean = {
    defined match {
      case BooleanType => queried.isInstanceOf[BooleanType]
      case ByteType => queried.isInstanceOf[ByteType] || queried.isInstanceOf[CharType]
      case DateType | TimestampType => queried.isInstanceOf[DateType] || queried.isInstanceOf[TimestampType]
      case _: DecimalType | _: FloatType => queried.isInstanceOf[DecimalType] || queried.isInstanceOf[FloatType] ||
        queried.isInstanceOf[DoubleType]
      case IntegerType | LongType | ShortType => queried.isInstanceOf[IntegerType] || queried.isInstanceOf[LongType] ||
        queried.isInstanceOf[ShortType]
      case _: VarcharType | _: StringType | _: CharType => queried.isInstanceOf[VarcharType] ||
        queried.isInstanceOf[StringType] || queried.isInstanceOf[CharType]
      case _ => DataType.equalsStructurally(defined, queried, true)
    }
  }

  trait LightningModel {

    /**
     * Save data source into sub directory of datasource
     *
     * @param dataSource
     * @param replace
     * @return saved data source
     */
    def saveDataSource(dataSource: DataSource, replace: Boolean): String

    def loadDataSources(namespace: Array[String], name: String = null): List[DataSource]

    def saveCreateTable(lakehouse: String, createTableSpec: CreateTableSpec): String

    def saveRegisterTable(registerTable: RegisterTableSpec, replace: Boolean): String

    def loadRegisteredTable(lakehouse: String, table: String): RegisterTableSpec

    def createLakeWarehouse(lakehouse: String): String

    def dropLakeWarehouse(lakehouse: String): Unit

    def listLakeHouse(): Seq[String]

    def listLakeHouseTables(lakehouse: String): Seq[String]

    def loadLakeHouseTable(lakehouse: String, table: String): CreateTableSpec

    def listNameSpaces(nameSpace: Seq[String]): Seq[String]

    def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit

    def dropNamespace(namespace: Array[String], cascade: Boolean): Unit

    def loadTableSpec(lakehouse: String, fqn: Seq[String]): CreateTableSpec

  }
}
