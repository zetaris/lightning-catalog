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

import org.junit.runner.RunWith
import org.scalatestplus.junit.JUnitRunner
import com.zetaris.lightning.execution.command.DataSourceType
import com.zetaris.lightning.model.serde.DataSource.Property
import com.zetaris.lightning.spark.SparkExtensionsTestBase

@RunWith(classOf[JUnitRunner])
class DataSourceTestSuite extends SparkExtensionsTestBase {
  test("should ser/deser data source object") {
    val options = List(
      Property("url", "jdbc:oracle:thin:@localhost:1551:crm"),
      Property("user", "user"),
      Property("password","password"))

    val oracle = DataSource.DataSource(DataSourceType.JDBC, Array("oracle"), "crm", options)
    val json = DataSource.toJson(oracle)
    val deser = DataSource(json)

    assert(oracle.dataSourceType == deser.dataSourceType)
    assert(oracle.name == deser.name)
    assert(oracle.properties.size == deser.properties.size)
    assert(oracle.properties(0).key == deser.properties(0).key)
    assert(oracle.properties(0).value == deser.properties(0).value)
    assert(oracle.properties(1).key == deser.properties(1).key)
    assert(oracle.properties(1).value == deser.properties(1).value)
    assert(oracle.properties(2).key == deser.properties(2).key)
    assert(oracle.properties(2).value == deser.properties(2).value)
  }
}
