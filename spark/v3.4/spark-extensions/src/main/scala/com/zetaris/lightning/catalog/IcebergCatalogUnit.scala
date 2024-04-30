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

package com.zetaris.lightning.catalog

import org.apache.spark.sql.connector.catalog.TableCatalog
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import scala.collection.JavaConverters._

// we're using different iceberg version along with spark version.
case class IcebergCatalogUnit(dsName: String, properties: Map[String, String]) extends AbstractIcebergCatalogUnit {

  val icebergSparkCatalog = {
    val className = "org.apache.iceberg.spark.SparkCatalog"
    val catalog = Class.forName(className).newInstance.asInstanceOf[org.apache.iceberg.spark.SparkCatalog]
    catalog.initialize(dsName, new CaseInsensitiveStringMap(mapAsJavaMap(properties)))

    catalog
  }

  override val namespaces = icebergSparkCatalog
  override val tableCatalog: TableCatalog = icebergSparkCatalog
}
