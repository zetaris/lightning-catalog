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

package com.zetaris.lightning.parser

import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime._
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.antlr.v4.runtime.tree.TerminalNodeImpl
import org.apache.spark.sql.AnalysisException
import org.apache.spark.sql.catalyst.FunctionIdentifier
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.expressions.Expression
import org.apache.spark.sql.catalyst.parser.ParserInterface
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.catalyst.trees.Origin
import org.apache.spark.sql.types.DataType
import org.apache.spark.sql.types.StructType

import java.util.Locale

class LightningExtendedParser(delegate: ParserInterface) extends ParserInterface {
  private lazy val astBuilder = new LightningExtensionAstBuilder(delegate)

  private def isLightningCommand(sqlText: String): Boolean = {
    val normalized = sqlText.toLowerCase(Locale.ROOT).trim()
      // Strip simple SQL comments that terminate a line, e.g. comments starting with `--` .
      .replaceAll("--.*?\\n", " ")
      // Strip newlines.
      .replaceAll("\\s+", " ")
      // Strip comments of the form  /* ... */. This must come after stripping newlines so that
      // comments that span multiple lines are caught.
      .replaceAll("/\\*.*?\\*/", " ")
      .trim()

    def checkDataSource(normalisedText: String): Boolean = {
      normalisedText.contains("jdbc datasource") ||
        normalisedText.contains("iceberg datasource") ||
        normalisedText.contains("orc datasource") ||
        normalisedText.contains("parquet datasource") ||
        normalisedText.contains("delta datasource") ||
        normalisedText.contains("avro datasource") ||
        normalisedText.contains("csv datasource") ||
        normalisedText.contains("xml datasource") ||
        normalisedText.contains("json datasource")
    }

    def checkNamespace(normalisedText: String): Boolean = {
      normalisedText.contains("namespace")
    }

    def checkSource(normalisedText: String): Boolean = {
      normalisedText.contains("source")
    }

    (normalized.startsWith("register") && checkDataSource(normalized) && checkNamespace(normalized))  ||
      (normalized.startsWith("register or replace") && checkDataSource(normalized) && checkNamespace(normalized)) ||
      (normalized.startsWith("register catalog") && checkSource(normalized) && checkNamespace(normalized)) ||
      (normalized.startsWith("register or replace catalog") && checkSource(normalized) && checkNamespace(normalized))
  }

  def parseLightning(sqlText: String): LogicalPlan = {
    val lexer = new LightningLexer(new UpperCaseCharStream(CharStreams.fromString(sqlText)))
    lexer.removeErrorListeners()
    lexer.addErrorListener(LightningParserErrorListener)

    val tokenStream = new CommonTokenStream(lexer)
    val parser = new LightningParser(tokenStream)
    //parser.addParseListener(SemanticLWHExtensionsPostProcessor)
    parser.removeErrorListeners()
    parser.addErrorListener(LightningParserErrorListener)

    try {
      try {
        parser.getInterpreter.setPredictionMode(PredictionMode.SLL)
        val plan = astBuilder.visit(parser.singleStatement()).asInstanceOf[LogicalPlan]
        plan
      } catch {
        case _: ParseCancellationException =>
          tokenStream.seek(0) // rewind input stream
          parser.reset()

          // Try Again.
          parser.getInterpreter.setPredictionMode(PredictionMode.LL)
          astBuilder.visit(parser.singleStatement()).asInstanceOf[LogicalPlan]
      }
    } catch {
      case e: LightningParserException if e.command.isDefined =>
        throw e
      case e: LightningParserException =>
        e.printStackTrace()
        throw e.withCommand(sqlText)
      case e: AnalysisException =>
        val position = Origin(e.line, e.startPosition)
        throw new LightningParserException(Option(sqlText), e.message, position, position)
    }
  }

  override def parsePlan(sqlText: String): LogicalPlan = {
    if (isLightningCommand(sqlText)) {
      parseLightning(sqlText)
    } else {
      delegate.parsePlan(sqlText)
    }
  }

  override def parseExpression(sqlText: String): Expression = {
    delegate.parseExpression(sqlText)
  }

  override def parseTableIdentifier(sqlText: String): TableIdentifier = {
    delegate.parseTableIdentifier(sqlText)
  }

  override def parseFunctionIdentifier(sqlText: String): FunctionIdentifier = {
    delegate.parseFunctionIdentifier(sqlText)
  }

  override def parseMultipartIdentifier(sqlText: String): Seq[String] = {
    delegate.parseMultipartIdentifier(sqlText)
  }

  override def parseTableSchema(sqlText: String): StructType = {
    delegate.parseTableSchema(sqlText)
  }

  override def parseDataType(sqlText: String): DataType = {
    delegate.parseDataType(sqlText)
  }

  override def parseQuery(sqlText: String): LogicalPlan = {
    parsePlan(sqlText)
  }
}

/**
 * The post-processor validates - copied from iceberg.
 */
case object LightningExtensionsPostProcessor extends LightningParserBaseListener {

  import LightningParser._

  /** Remove the back ticks from an Identifier. */
  override def exitQuotedIdentifier(ctx: QuotedIdentifierContext): Unit = {
    replaceTokenByIdentifier(ctx, 1) { token =>
      // Remove the double back ticks in the string.
      token.setText(token.getText.replace("``", "`"))
      token
    }
  }

  override def exitNonReserved(ctx: NonReservedContext): Unit = {
    replaceTokenByIdentifier(ctx, 0)(identity)
  }

  private def replaceTokenByIdentifier(ctx: ParserRuleContext,
                                       stripMargins: Int)(
                                        f: CommonToken => CommonToken = identity): Unit = {
    val parent = ctx.getParent
    parent.removeLastChild()
    val token = ctx.getChild(0).getPayload.asInstanceOf[Token]
    val newToken = new CommonToken(
      new org.antlr.v4.runtime.misc.Pair(token.getTokenSource, token.getInputStream),
      LightningParser.IDENTIFIER,
      token.getChannel,
      token.getStartIndex + stripMargins,
      token.getStopIndex - stripMargins)
    parent.addChild(new TerminalNodeImpl(f(newToken)))
  }
}
