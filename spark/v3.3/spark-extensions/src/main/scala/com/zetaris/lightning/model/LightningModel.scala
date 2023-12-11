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

import org.apache.spark.sql.types._
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import com.zetaris.lightning.model.serde.DataSource.DataSource

/**
 * Lightning model object encapsulating implementation details
 */
object LightningModel {
  val LIGHTNING_CATALOG_NAME = "lightning"
  val LIGHTNING_CATALOG = s"spark.sql.catalog.$LIGHTNING_CATALOG_NAME"
  val LIGHTNING_MODEL_TYPE_KEY = "type"
  val LIGHTNING_MODEL_WAREHOUSE_KEY = "warehouse"
  val LIGHTNING_ACCESS_CONTROL_KEY = "accessControlProvider"

  val DEFAULT_NAMESPACES = List("lightning.datasource", "lightning.metastore")

  var cached: LightningModel = null

  /**
   * convert namespace to fully qualified name connected by dot
   * @param namespace
   * @return
   */
  def toFqn(namespace: Seq[String]): String = namespace.mkString(".")

  /**
   * convert fully qualified name into multi part identifer, array of string
   * @param fqn
   * @return
   */
  def toMultiPartIdentifier(fqn: String): Seq[String] = if (fqn.indexOf(".") > 0) {
    fqn.split(".")
  } else {
    Seq(fqn)
  }

  /**
   * factory instantiating concrete lightning model
   * @param prop
   * @return concrete model
   */
  def apply(prop: CaseInsensitiveStringMap): LightningModel = {
    if (!prop.containsKey(LIGHTNING_MODEL_TYPE_KEY)) {
      throw new RuntimeException(s"${LIGHTNING_MODEL_TYPE_KEY} is not set in spark conf")
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

  /**
   * Interface of lightning model managing CRUD of entity in metastore
   */
  trait LightningModel {

    /**
     * Save data source into sub directory of datasource
     *
     * @param dataSource
     * @param replace
     * @return saved data source
     */
    def saveDataSource(dataSource: DataSource, replace: Boolean): String

    /**
     * load data sources under the given namespace
     * @param namespace
     * @param name
     * @return list of namespace
     */
    def loadDataSources(namespace: Array[String], name: String = null): List[DataSource]

    /**
     * list namespaces under the given namespace
     * @param namespace
     * @return namespaces
     */
    def listNamespaces(namespace: Seq[String]): Seq[String]

    /**
     * list tables under the given namesapce
     * @param namespace
     * @return table names
     */
    def listTables(namespace: Array[String]): Seq[String]

    /**
     * save table under the given namespace
     * @param namespace
     * @param name
     * @param schema
     */
    def saveTable(namespace: Array[String], name: String, schema: StructType): Unit

    /**
     * Create child namespace under the given namespace
     * @param namespace
     * @param metadata
     */
    def createNamespace(namespace: Array[String], metadata: java.util.Map[String, String]): Unit

    /**
     * drop namespace
     * @param namespace
     * @param cascade delete cascade if true
     */
    def dropNamespace(namespace: Array[String], cascade: Boolean): Unit

  }
}