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

import com.zetaris.lightning.execution.command.{AccessControl, Annotation, AnnotationStatement, DataQuality}
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.misc.Interval
import org.apache.spark.sql.catalyst.trees.CurrentOrigin
import org.apache.spark.sql.catalyst.trees.Origin

object LightningParserUtils {

  private[lightning] def withOrigin[T](ctx: ParserRuleContext)(f: => T): T = {
    val current = CurrentOrigin.get
    CurrentOrigin.set(position(ctx.getStart))
    try {
      f
    } finally {
      CurrentOrigin.set(current)
    }
  }

  private[lightning] def position(token: Token): Origin = {
    val opt = Option(token)
    Origin(opt.map(_.getLine), opt.map(_.getCharPositionInLine))
  }

  /** Get the command which created the token. */
  private[lightning] def command(ctx: ParserRuleContext): String = {
    val stream = ctx.getStart.getInputStream
    stream.getText(Interval.of(0, stream.size() - 1))
  }

  private[lightning] def parseAnnotation(stmt: AnnotationStatement): Annotation = {
    stmt.name.toLowerCase match {
      case "dataquality" =>
        val name = stmt.params.find(_.key == "name")
        if (name.isEmpty) {
          throw new IllegalArgumentException("Data Quality name is not provided")
        }
        val expression = stmt.params.find(_.key.toLowerCase == "expression")
        if (expression.isEmpty) {
          throw new IllegalArgumentException("Data Quality expression is not provided")
        }
        DataQuality(name.get.value, expression.get.value)

      case "accesscontrol" =>
        val name = stmt.params.find(_.key == "accessType")
        if (name.isEmpty) {
          throw new IllegalArgumentException("accessType is not provided")
        }
        val accessType = name.get.value.toLowerCase
        List("deny", "regex").find(_ == accessType).orElse(
          throw new IllegalArgumentException("deny | regex are supported accessType"))

        val regExParam = if (accessType == "regex") {
          val regex = stmt.params.find(_.key.toLowerCase == "regex")
          if (regex.isEmpty) {
            throw new IllegalArgumentException("regex is not provided")
          }
          Some(regex.get.value)
        } else {
          None
        }

        var values = stmt.params.find(_.key.toLowerCase == "users")
        if (values.isEmpty) {
          throw new IllegalArgumentException("users is not provided")
        }
        val users = values.get.value.split(",")

        values = stmt.params.find(_.key.toLowerCase == "groups")
        if (values.isEmpty) {
          throw new IllegalArgumentException("users is not provided")
        }
        val groups = values.get.value.split(",")

        AccessControl(name.get.value, regExParam, users, groups)
    }
  }
}