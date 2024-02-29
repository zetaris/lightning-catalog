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

package com.zetaris.lightning.model.serde

import com.zetaris.lightning.execution.command.DataSourceType
import com.zetaris.lightning.execution.command.DataSourceType.DataSourceType
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomSerializer, Formats, JString, NoTypeHints}

object DataSource {
  val MASKED_VALUE = "********(masked)"

  case class Property(key: String, value: String) {
    override def toString: String = s"Property(${MASKED_VALUE})"
  }

  case class DataSource(dataSourceType: DataSourceType,
                        namespace: Array[String],
                        name: String,
                        properties: List[Property])

  // todo : check with java version other than 1.8
  def simpleClassName(clazz: Class[_]): String = {
    val simpleName = clazz.getSimpleName
    simpleName.substring(0, simpleName.length - 1)
  }

  private class DataSourceTypeSerializer
    extends CustomSerializer[DataSourceType](_ =>
      (
        { case JString(s) => DataSourceType(s) },
        { case ds: DataSourceType => JString(simpleClassName(ds.getClass))}
      )
    )

  implicit val formats: Formats = Serialization.formats(NoTypeHints) + new DataSourceTypeSerializer

  def toJson(dataSource: DataSource): String = {
    write(dataSource)
  }

  def apply(json: String): DataSource = {
    parse(json).extract[DataSource]
  }

  def toProperties(options: Map[String, String]): List[Property] = {
    options.map { opt =>
      Property(opt._1, opt._2)
    }.toList
  }
}
