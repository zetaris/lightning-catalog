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

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl
import org.apache.hadoop.mapreduce.{JobID, TaskAttemptID, TaskID, TaskType}
import org.apache.spark.sql.execution.datasources.{PartitionedFile, RecordReaderIterator}

import java.io.Closeable

class HadoopBinaryFileReader (file: PartitionedFile, conf: Configuration, length: Long = -1)
  extends Iterator[Array[Byte]] with Closeable {
  private val _iterator = {
    val fileSplit = if (length > 0 && length < file.length ) {
      new CombineFileSplit(
        Array(file.toPath),
        Array(file.start),
        Array(length),
        // The locality is decided by `getPreferredLocations` in `FileScanRDD`.
        Array.empty[String])
    } else {
      new CombineFileSplit(
        Array(file.toPath),
        Array(file.start),
        Array(file.length),
        // The locality is decided by `getPreferredLocations` in `FileScanRDD`.
        Array.empty[String])
    }
    val attemptId = new TaskAttemptID(new TaskID(new JobID(), TaskType.MAP, 0), 0)
    val hadoopAttemptContext = new TaskAttemptContextImpl(conf, attemptId)
    val reader = new WholeFileRecordReader(fileSplit, hadoopAttemptContext, 0)
    reader.initialize(fileSplit, hadoopAttemptContext)
    new RecordReaderIterator(reader)
  }

  override def hasNext: Boolean = _iterator.hasNext

  override def next(): Array[Byte] = _iterator.next()

  override def close(): Unit = _iterator.close()
}
