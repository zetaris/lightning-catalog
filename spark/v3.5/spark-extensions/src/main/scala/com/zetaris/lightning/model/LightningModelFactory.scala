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

import com.zetaris.lightning.catalog.LightningSource

import org.apache.spark.sql.types._
import org.apache.spark.sql.util.CaseInsensitiveStringMap


/**
 * Lightning model object encapsulating implementation details
 */
object LightningModelFactory extends LightningSource {

  var cached: LightningModel = null

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
}
