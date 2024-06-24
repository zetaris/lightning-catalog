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

import com.google.common.io.{ByteStreams, Closeables}
import org.apache.hadoop.io.compress.CompressionCodecFactory
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit
import org.apache.hadoop.mapreduce.{InputSplit, RecordReader, TaskAttemptContext}

class WholeFileRecordReader(split: CombineFileSplit,
                            context: TaskAttemptContext,
                            index: Integer) extends RecordReader[String, Array[Byte]] {

  private[this] val path = split.getPath(index)
  private[this] val fs = path.getFileSystem(context.getConfiguration)

  // True means the current file has been processed, then skip it.
  private[this] var processed = false

  private[this] val key: String = path.toString
  private[this] var value: Array[Byte] = null

  override def initialize(split: InputSplit, context: TaskAttemptContext): Unit = {}

  override def close(): Unit = {}

  override def getProgress: Float = if (processed) 1.0f else 0.0f

  override def getCurrentKey: String = key

  override def getCurrentValue: Array[Byte] = value

  override def nextKeyValue(): Boolean = {
    if (!processed) {
      val conf = context.getConfiguration
      val factory = new CompressionCodecFactory(conf)
      val codec = factory.getCodec(path) // infers from file ext.
      val fileIn = fs.open(path)

      value = if (codec != null) {
        ByteStreams.toByteArray(ByteStreams.limit(codec.createInputStream(fileIn), split.getLength))
      } else {
        ByteStreams.toByteArray(ByteStreams.limit(fileIn, split.getLength))
      }

      Closeables.close(fileIn, false)
      processed = true
      true
    } else {
      false
    }
  }
}
