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

package com.zetaris.lightning.datasources.v2

import org.apache.hadoop.fs.{FileStatus, Path}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.datasources.{FileStatusCache, InMemoryFileIndex, NoopCache, PartitionSpec}
import org.apache.spark.sql.types.StructType

import scala.collection.mutable

class InMemoryUnstructuredFileIndex(sparkSession: SparkSession,
                                    rootPathsSpecified: Seq[Path],
                                    parameters: Map[String, String],
                                    userSpecifiedSchema: Option[StructType],
                                    fileStatusCache: FileStatusCache = NoopCache,
                                    userSpecifiedPartitionSpec: Option[PartitionSpec] = None,
                                    metadataOpsTimeNs: Option[Long] = None)
  extends InMemoryFileIndex(sparkSession,
    rootPathsSpecified,
    parameters,
    userSpecifiedSchema,
    fileStatusCache,
    userSpecifiedPartitionSpec,
    metadataOpsTimeNs) {

  override def listLeafFiles(paths: Seq[Path]): mutable.LinkedHashSet[FileStatus] = {
    super.listLeafFiles(paths)
  }
}
