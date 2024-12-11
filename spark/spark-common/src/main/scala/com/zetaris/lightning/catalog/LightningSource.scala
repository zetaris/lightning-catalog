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

import org.apache.spark.sql.types._

trait LightningSource {
  val LIGHTNING_CATALOG_NAME = "lightning"
  val LIGHTNING_CATALOG = s"spark.sql.catalog.$LIGHTNING_CATALOG_NAME"
  val LIGHTNING_MODEL_TYPE_KEY = "type"
  val LIGHTNING_MODEL_WAREHOUSE_KEY = "warehouse"
  val LIGHTNING_ACCESS_CONTROL_KEY = "accessControlProvider"

  val DEFAULT_NAMESPACES = List("lightning.datasource", "lightning.metastore")

  /**
   * convert namespace to fully qualified name connected by dot
   * @param namespace
   * @return
   */
  def toFqn(namespace: Seq[String]): String = namespace.mkString(".")

  /**
   * convert namespace to fully qualified name connected by dot
   * @param namespace
   * @param connector
   * @return
   */
  def toFqn(namespace: Seq[String], connector: String): String = namespace.mkString(connector)

  /**
   * convert fully qualified name into multi part identifer, array of string
   * @param fqn
   * @return
   */
  def toMultiPartIdentifier(fqn: String): Seq[String] = if (fqn.indexOf(".") > 0) {
    fqn.split("\\.")
  } else {
    Seq(fqn)
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
      case DateType => queried.isInstanceOf[DateType]
      case TimestampType => queried.isInstanceOf[TimestampType] || queried.isInstanceOf[DateType]
      case _: FloatType => queried.isInstanceOf[FloatType]
      case _: DoubleType => queried.isInstanceOf[FloatType] || queried.isInstanceOf[DoubleType]
      case _: DecimalType => queried.isInstanceOf[FloatType] || queried.isInstanceOf[DoubleType] ||
        queried.isInstanceOf[DecimalType]
      case ShortType => queried.isInstanceOf[ByteType] || queried.isInstanceOf[ShortType]
      case IntegerType => queried.isInstanceOf[ByteType] || queried.isInstanceOf[ShortType] ||
        queried.isInstanceOf[IntegerType]
      case LongType => queried.isInstanceOf[ByteType] || queried.isInstanceOf[ShortType] ||
        queried.isInstanceOf[IntegerType] || queried.isInstanceOf[LongType]
      case _: CharType => queried.isInstanceOf[CharType]
      case _: VarcharType if queried.isInstanceOf[CharType] => true
      case t: VarcharType if queried.isInstanceOf[VarcharType] => t.length >= queried.asInstanceOf[VarcharType].length
      case _: StringType => queried.isInstanceOf[CharType] || queried.isInstanceOf[VarcharType] ||
        queried.isInstanceOf[StringType]
      case _ => DataType.equalsStructurally(defined, queried, true)
    }
  }

  def stripCompositeKeys(key: String): String = {
    if (key.startsWith("`") && key.endsWith("`")) {
      key.substring(1, key.length - 1)
    } else {
      key
    }
  }

  def equalToMultiPartIdentifier(name: String, names: Seq[String], connector: String = ","): Boolean = {
    val stripped = stripCompositeKeys(name)
    stripped.equalsIgnoreCase(names.mkString(connector))
  }
}
