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

package com.zetaris.lightning.model.serde

import com.zetaris.lightning.execution.command.{CreateTableSpec, ReferenceControl}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.DataType
import org.json4s.{CustomSerializer, Formats, JString, NoTypeHints}
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object CreateTable {
  private class ReferenceControlSerializer
    extends CustomSerializer[ReferenceControl.ReferenceControl](_ =>
      (
        { case JString(s) => ReferenceControl(s) },
        { case ds: ReferenceControl.ReferenceControl => JString(DataSource.simpleClassName(ds.getClass))}
      )
    )

  private class DataTypeSerializer
    extends CustomSerializer[DataType](_ =>
      (
        { case JString(s) => DataType.fromJson(s) },
        { case ds: DataType => JString(ds.json) }
      )
    )

  implicit val formats: Formats = Serialization.formats(NoTypeHints) + new ReferenceControlSerializer +
    new DataTypeSerializer

  def toJson(createTable: CreateTableSpec): String = {
    write(createTable)
  }

  def apply(json: String): CreateTableSpec = {
    parse(json).extract[CreateTableSpec]
      .withDQExpression(SparkSession.getActiveSession.get.sessionState.sqlParser)
  }
}
