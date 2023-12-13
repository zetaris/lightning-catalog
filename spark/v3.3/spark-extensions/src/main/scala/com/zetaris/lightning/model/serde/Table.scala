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

import org.apache.spark.sql.SparkSQLBridge
import org.apache.spark.sql.types.StructType
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.json4s.{CustomSerializer, Formats, JString, NoTypeHints}

object Table {
  case class Table(dsNamespace: String, schema: StructType)

  private class SchemaSerializer
    extends CustomSerializer[StructType](_ =>
      (
        { case JString(json) => SparkSQLBridge.schemaFromJson(json) },
        { case schema: StructType => JString(schema.json)}
      )
    )

  implicit val formats: Formats = Serialization.formats(NoTypeHints) + new SchemaSerializer

  def toJson(table: Table): String = {
    write(table)
  }

  def apply(json: String): Table = {
    parse(json).extract[Table]
  }
}
