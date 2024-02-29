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
import org.apache.spark.sql.types.IntegerType

case class RegisterCatalogSpec(namespace: Array[String],
                               catalog:String,
                               opts: Map[String, String],
                               source: Array[String],
                               tablePattern: Option[String],
                               replace: Boolean) extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("table_count", IntegerType, false)()
  )

  val javaRegEx = {
    tablePattern.map { sqlPattern =>
      sqlPattern.replace(".", "\\.")
        .replace("_", ".")
        .replace("%", ".*")
    }
  }

  def sqlLike(table: String): Boolean = javaRegEx.map( regEx => table.matches(regEx)).getOrElse(true)

  private def registerTable(sourceNamespace: Array[String]): Int = {
    val parentNamespace = LightningCatalogCache.catalog.listNamespaces(sourceNamespace)
    if (parentNamespace.nonEmpty) {
      parentNamespace.map { ns =>
        registerTable(sourceNamespace ++ ns)
      }.sum
    } else {
      val withFiltered = LightningCatalogCache.catalog.listTables(sourceNamespace)
        .filter(ident => sqlLike(ident.name()))
      val nameAndSchema = withFiltered.flatMap { ident =>
        try {
          val table = LightningCatalogCache.catalog.loadTable(Identifier.of(sourceNamespace, ident.name()))
          Some((ident.name(), table.schema()))
        } catch {
          case th: Throwable => None
        }
      }

      val baseNamespace = namespace.drop(1) :+ catalog
      val namespaceIngesting = sourceNamespace.diff(source)
      val targetNamespace = baseNamespace ++ namespaceIngesting
      val existing = LightningModel.cached.listTables(targetNamespace ++ namespaceIngesting)
      nameAndSchema.foreach { case (name, _) =>
        if(existing.find(_.equalsIgnoreCase(name)).isDefined && !replace) {
          throw new RuntimeException(s"table : $name is already registered. Add REPLACE option to overwrite")
        }
      }

      nameAndSchema.map { case (name, schema) =>
        LightningModel.cached.saveTable(sourceNamespace, targetNamespace, name, schema)
        1
      }.sum
    }
  }

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val sourceNamespace = source.drop(1)
    val tableCount = registerTable(sourceNamespace)
    Row(tableCount) :: Nil
  }
}
