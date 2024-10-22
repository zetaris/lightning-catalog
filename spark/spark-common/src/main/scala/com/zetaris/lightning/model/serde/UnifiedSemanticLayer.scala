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

import com.zetaris.lightning.execution.command.CreateTableSpec
import org.json4s.Formats
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write

object UnifiedSemanticLayer {
  implicit val formats: Formats = CreateTable.formats

  case class UnifiedSemanticLayer(namespace: Seq[String], name: String, tables: Seq[CreateTableSpec])

  def toJson(namespace: Seq[String], name: String, tables: Seq[CreateTableSpec]): String = {
    write(UnifiedSemanticLayer(namespace, name, tables))
  }

  def apply(json: String): UnifiedSemanticLayer = {
    parse(json).extract[UnifiedSemanticLayer]
  }

  case class UnifiedSemanticLayerException(message: String, cause: Throwable) extends RuntimeException(message, cause)
}

object UnifiedSemanticLayerTable {
  implicit val formats: Formats = CreateTable.formats

  case class UnifiedSemanticLayerTable(name: String, query: String)

  def toJson(name: String, query: String): String = {
    write(UnifiedSemanticLayerTable(name, query))
  }

  def apply(json: String): UnifiedSemanticLayerTable = {
    parse(json).extract[UnifiedSemanticLayerTable]
  }
}
