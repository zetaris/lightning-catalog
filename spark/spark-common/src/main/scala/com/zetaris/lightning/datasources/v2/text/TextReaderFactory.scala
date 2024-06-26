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

package com.zetaris.lightning.datasources.v2.text

import com.zetaris.lightning.datasources.v2.UnstructuredFilePartitionReaderFactory
import org.apache.hadoop.fs.Path
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types.StructType
import org.apache.spark.util.SerializableConfiguration

case class TextReaderFactory(broadcastedConf: Broadcast[SerializableConfiguration],
                             readDataSchema: StructType,
                             partitionSchema: StructType,
                             rootPathsSpecified: Seq[Path],
                             pushedFilters: Array[Filter],
                             opts: Map[String, String],
                             isContentTable: Boolean = false)
  extends UnstructuredFilePartitionReaderFactory(broadcastedConf,
    readDataSchema,
    partitionSchema,
    rootPathsSpecified,
    pushedFilters,
    opts,
    isContentTable) {

  override def textPreviewFromBinary(content: Array[Byte]): String = {
    if (previewLen > 0 && content.length > previewLen) {
      val previewArray = new Array[Byte](previewLen)
      content.copyToArray(previewArray)
      new String(previewArray)
    } else {
      new String(content)
    }
  }
}
