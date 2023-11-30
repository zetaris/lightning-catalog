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

import com.zetaris.lightning.model.LightningModel
import com.zetaris.lightning.parser.LightningExtendedParser
import com.zetaris.lightning.util.FileSystemUtils
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.expressions.AttributeReference
import org.apache.spark.sql.types.StringType

case class DropLakeHouse(name: String) extends LightningCommandBase {
  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val lightningModel = LightningModel(dataSourceConfigMap(s"${LightningModel.LIGHTNING_CATALOG}.", sparkSession))
    lightningModel.dropLakeWarehouse(name)
    Seq.empty
  }
}

case class BuildLakeHouse(name: String, ddlPath: Option[String])  extends LightningCommandBase {
  override val output: Seq[AttributeReference] = Seq(
    AttributeReference("table", StringType, false)(),
    AttributeReference("jsonpath", StringType, false)()
  )

  override def runCommand(sparkSession: SparkSession): Seq[Row] = {
    val lightningModel = LightningModel(dataSourceConfigMap(s"${LightningModel.LIGHTNING_CATALOG}.", sparkSession))

    val jsonPaths = ddlPath.map { path =>
      FileSystemUtils.readFile(path).split(";").flatMap { ddl =>
        val trimmed = ddl.trim
        if (trimmed.isEmpty) {
          None
        } else {
          val lwhParser = new LightningExtendedParser(sparkSession.sessionState.sqlParser)
          val createTableSpec = lwhParser.parseLightning(ddl).asInstanceOf[CreateTableSpec]
          createTableSpec.copy(lakehouse = Some(name))
          Some(createTableSpec)
          //Some(sparkSession.sessionState.sqlParser.parsePlan(ddl))
        }
      }.map { createTable =>
        (createTable.fqn,
          lightningModel.saveCreateTable(name, createTable.asInstanceOf[CreateTableSpec]))
      }.map { case (table, jsonpath) =>
        Row(LightningModel.toFqn(table), jsonpath)
      }
    }

    if (jsonPaths.isDefined) {
      jsonPaths.get
    } else {
      val path = lightningModel.createLakeWarehouse(name)
      Row(path, "") :: Nil
    }
  }
}
