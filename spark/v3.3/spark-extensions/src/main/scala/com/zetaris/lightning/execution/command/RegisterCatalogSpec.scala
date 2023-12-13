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

package com.zetaris.lightning.execution.command

import com.zetaris.lightning.catalog.LightningCatalogCache
import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.connector.catalog.Identifier
import org.apache.spark.sql.types.StringType

import java.util.regex.Pattern

case class RegisterCatalogSpec(namespace: Array[String],
                               catalog:String,
                               opts: Map[String, String],
                               source: Array[String],
                               tablePattern: Option[String],
                               replace: Boolean) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("registered", StringType, false)()
  )

  val javaRegEx = {
    tablePattern.map { pattern =>
      val regPattern = Pattern.quote(pattern)
      regPattern.replace("%", ".*")
    }
  }

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val sourceNamespace = source.drop(1)
    val targetNamespace = namespace.drop(1) :+ catalog
    val withFiltered = if (javaRegEx.isDefined) {
      LightningCatalogCache.catalog.listTables(sourceNamespace)
        .filter(ident => Pattern.matches(javaRegEx.get, ident.name()))
    } else {
      LightningCatalogCache.catalog.listTables(sourceNamespace)
    }

    val nameAndSchema = withFiltered.map { ident =>
        val table = LightningCatalogCache.catalog.loadTable(Identifier.of(sourceNamespace, ident.name()))
        (ident.name(), table.schema())
    }

    val existing = LightningModel.cached.listTables(targetNamespace)
    nameAndSchema.foreach { case (name, _) =>
      if(existing.find(_.equalsIgnoreCase(name)).isDefined && !replace) {
        throw new RuntimeException(s"table : $name is already registered. Add REPLACE option to overwrite")
      }
    }

    nameAndSchema.map { case (name, schema) =>
      LightningModel.cached.saveTable(sourceNamespace, targetNamespace, name, schema)
      Row(s"${LightningModel.toFqn(targetNamespace)}.$name")
    }
  }
}
