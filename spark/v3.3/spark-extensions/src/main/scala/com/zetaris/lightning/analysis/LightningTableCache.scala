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

package com.zetaris.lightning.analysis

import com.zetaris.lightning.execution.command.CreateTableSpec
import com.zetaris.lightning.model.LightningModel
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util.HashMap
import java.util.regex.Pattern

object LightningTableCache {
  private val lakeWarehouseTables = scala.collection.mutable.Map.empty[(String, String), CreateTableSpec]
  private var initialized = false

  def loadLakeHouseTablesIfNotLoaded(conf: SQLConf) {
    if (!initialized) {
      val opts = catalogOptions(conf)
      val slwhModel = LightningModel(opts)
      val allTables = for {
        lwh <- slwhModel.listLakeHouse()
        table <- slwhModel.listLakeHouseTables(lwh)
      } yield {
        val tableSpec = slwhModel.loadLakeHouseTable(lwh, table)
        (lwh.toLowerCase, table.toLowerCase) -> tableSpec
      }

      allTables.foreach { case (k, v) =>
        lakeWarehouseTables += (k -> v)
      }

      initialized = true
    }
  }

  def loadLakeHouseTable(conf: SQLConf, lakehouse: String, table: String) {
    val opts = catalogOptions(conf)
    val slwhModel = LightningModel(opts)
    val tableSpec = slwhModel.loadLakeHouseTable(lakehouse, table)
    lakeWarehouseTables.put((lakehouse.toLowerCase, table.toLowerCase), tableSpec)
  }

  def getCreateTableSpec(lakehouse: String, table: String): Option[CreateTableSpec]
  = lakeWarehouseTables.get((lakehouse.toLowerCase, table.toLowerCase))

  private def catalogOptions(conf: SQLConf) = {
    val prefix = Pattern.compile("^spark\\.sql\\.catalog\\." + LightningModel.LIGHTNING_CATALOG_NAME + "\\.(.+)")
    val options = new HashMap[String, String]
    conf.getAllConfs.foreach {
      case (key, value) =>
        val matcher = prefix.matcher(key)
        if (matcher.matches && matcher.groupCount > 0) options.put(matcher.group(1), value)
    }
    new CaseInsensitiveStringMap(options)
  }

}
