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

package com.zetaris.lightning.parser

import org.antlr.v4.runtime.ParserRuleContext
import org.apache.spark.sql.catalyst.parser.ParseException

// from org.apache.spark.sql.errors.QueryParsingErrors
object QueryParsingErrors {

  def fromToIntervalUnsupportedError(from: String, to: String, ctx: ParserRuleContext): Throwable = {
    new ParseException(s"Intervals FROM $from TO $to are not supported.", ctx)
  }

//  def charTypeMissingLengthError(dataType: String, ctx: ParserRuleContext): Throwable = {
//    new ParseException("PARSE_CHAR_MISSING_LENGTH", Array(dataType, dataType), ctx)
//  }

  def dataTypeUnsupportedError(dataType: String, ctx: ParserRuleContext): Throwable = {
    new ParseException(s"DataType $dataType is not supported.", ctx)
  }
}
