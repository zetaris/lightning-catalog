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

package com.zetaris.lightning.datasources.v2.usl

import com.zetaris.lightning.catalog.LightningSource
import com.zetaris.lightning.execution.command.CreateTableSpec
import com.zetaris.lightning.model.TableNotActivatedException
import com.zetaris.lightning.model.serde.UnifiedSemanticLayer.UnifiedSemanticLayerException
import org.apache.spark.sql.connector.catalog.{SupportsRead, Table, TableCapability}
import org.apache.spark.sql.connector.catalog.TableCapability.BATCH_READ
import org.apache.spark.sql.types.{StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap

case class USLTable(createTableSpec: CreateTableSpec, registeredSql: Option[String]) extends Table
  with SupportsRead with LightningSource {
  override def name(): String = toFqn(createTableSpec.namespace :+ createTableSpec.name)

  override def schema(): StructType = {
    val fields = createTableSpec.columnSpecs.map { colSpec =>
      StructField(colSpec.name, colSpec.dataType, colSpec.notNull.isEmpty)
    }

    StructType(fields)
  }

  override def capabilities(): java.util.Set[TableCapability] =  java.util.EnumSet.of(BATCH_READ)

  override def newScanBuilder(options: CaseInsensitiveStringMap): USLTableScanBuilder = {
    if (registeredSql.isEmpty) {
      throw TableNotActivatedException(s"table(${toFqn(createTableSpec.namespace)}.${createTableSpec.namespace}) is not activated")
    }
    USLTableScanBuilder(createTableSpec, registeredSql.get)
  }

}
