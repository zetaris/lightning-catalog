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

package com.zetaris.lightning.error

object LightningException {
  abstract class LightningException(msg: String, root: Throwable = null) extends Exception(msg, root) {
    def readableMessage: String
  }

  class LightningSQLParserException(msg: String, root: Throwable) extends LightningException(msg, root) {
    def readableMessage: String = {
      s"SQL Parser error : $msg, check out SQL syntax"
    }
  }

  class LightningSQLAnalysisException(msg: String, root: Throwable) extends LightningException(msg, root) {
    def readableMessage: String = {
      s"SQL Analysis error : $msg, check out the right table and column name"
    }
  }

  class LightningTableNotFoundException(table: String, root: Throwable) extends LightningException(table, root) {
    def readableMessage: String = {
      s"Table not found error : $table"
    }
  }

  class LightningSchemaNotMatchException(msg: String) extends LightningException(msg) {
    def readableMessage: String = {
      s"Schema not matched error : $msg"
    }
  }
}
