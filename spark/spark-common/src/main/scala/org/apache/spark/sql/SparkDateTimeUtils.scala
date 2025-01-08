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

package org.apache.spark.sql

import org.apache.spark.sql.catalyst.CatalystTypeConverters
import org.apache.spark.sql.types.UserDefinedType

import java.time.ZoneId
import java.util.TimeZone
import scala.util.control.NonFatal

object SparkDateTimeUtils {
  def getZoneId(timeZoneId: String): ZoneId = {
    val formattedZoneId = timeZoneId
      // To support the (+|-)h:mm format because it was supported before Spark 3.0.
      .replaceFirst("(\\+|\\-)(\\d):", "$10$2:")
      // To support the (+|-)hh:m format because it was supported before Spark 3.0.
      .replaceFirst("(\\+|\\-)(\\d\\d):(\\d)$", "$1$2:0$3")

    ZoneId.of(formattedZoneId, ZoneId.SHORT_IDS)
  }

  def getTimeZone(timeZoneId: String): TimeZone = TimeZone.getTimeZone(getZoneId(timeZoneId))
}

// see classes under org.apache.spark.sql.catalyst.util
trait UDTUtils {
  def toRow(value: Any, udt: UserDefinedType[Any]): Any
}

object UDTUtils extends UDTUtils {
  private val delegate = try {
    val cls = SparkClassUtils.classForName("org.apache.spark.sql.UDTUtilsImpl")
    cls.getConstructor().newInstance().asInstanceOf[UDTUtils]
  } catch {
    case NonFatal(_) =>
      DefaultUDTUtils
  }

  override def toRow(value: Any, udt: UserDefinedType[Any]): Any = delegate.toRow(value, udt)
}

object DefaultUDTUtils extends UDTUtils {
  override def toRow(value: Any, udt: UserDefinedType[Any]): Any = {
    throw new UnsupportedOperationException()
  }
}

class UDTUtilsImpl extends UDTUtils {
  override def toRow(value: Any, udt: UserDefinedType[Any]): Any = {
    CatalystTypeConverters.convertToScala(udt.serialize(value), udt.sqlType)
  }
}