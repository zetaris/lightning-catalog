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

import com.zetaris.lightning.execution.command.{DataSourceType, RegisterCatalogSpec, RegisterDataSourceSpec}
import com.zetaris.lightning.model.LightningModel
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.spark.sql.catalyst.parser.ParserInterface
import org.apache.spark.sql.catalyst.parser.ParserUtils.{checkDuplicateKeys, operationNotAllowed, string}
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan

import java.util.Locale
import scala.collection.JavaConverters._

class LightningExtensionAstBuilder(delegate: ParserInterface) extends LightningParserBaseVisitor[AnyRef] {

  import LightningParser._
  import LightningParserExtension._

  private def typedVisit[T](ctx: ParseTree): T = {
    ctx.accept(this).asInstanceOf[T]
  }

  private def toBuffer[T](list: java.util.List[T]): scala.collection.mutable.Buffer[T] = list.asScala

  private def toSeq[T](list: java.util.List[T]): Seq[T] = toBuffer(list)

  override def visitStatement(ctx: StatementContext): LogicalPlan = withOrigin(ctx) {
    visit(ctx.ddlStatement).asInstanceOf[LogicalPlan]
  }

  override def visitSingleStatement(ctx: SingleStatementContext): LogicalPlan = withOrigin(ctx) {
    visitStatement(ctx.statement())
  }

  override def visitMultipartIdentifier(ctx: MultipartIdentifierContext): Seq[String] = withOrigin(ctx) {
    ctx.parts.asScala.map(_.getText)
  }

  private def validateNamespace(namespace: Seq[String]): Unit = {
    if (namespace.size < 3) {
      throw new IllegalArgumentException(s"namespace must have at least three namespace : lightning.datasource|metastore.namespace")
    } else {
      val fqn = namespace.take(2).mkString(".").toLowerCase
      if (!LightningModel.DEFAULT_NAMESPACES.exists(_.equals(fqn))) {
        throw new IllegalArgumentException(s"name space must be formed : lightning.datasource|metastore(.namespace)*")
      }
    }
  }

  override def visitRegisterDataSource(ctx: RegisterDataSourceContext)
  : RegisterDataSourceSpec = withOrigin(ctx) {
    val replace = ctx.REPLACE() != null
    val dataSourceType = DataSourceType(ctx.dataSourceType.getText)
    val options = Option(ctx.options).map(visitPropertyKeyValues).getOrElse(Map.empty)
    val name = ctx.identifier().getText
    val namespace = visitMultipartIdentifier(ctx.multipartIdentifier())
    validateNamespace(namespace)

    RegisterDataSourceSpec(namespace.toArray, name, dataSourceType, options, replace)
  }

  // from Spark's AstBuilder
  override def visitPropertyList(ctx: PropertyListContext): Map[String, String] = withOrigin(ctx) {
    val properties = ctx.property.asScala.map { property =>
      val key = visitPropertyKey(property.key)
      val value = visitPropertyValue(property.value)
      key -> value
    }
    // Check for duplicate property names.
    checkDuplicateKeys(properties, ctx)
    properties.toMap
  }

  // from Spark's AstBuilder
  def visitPropertyKeyValues(ctx: PropertyListContext): Map[String, String] = {
    val props = visitPropertyList(ctx)
    val badKeys = props.collect { case (key, null) => key }
    if (badKeys.nonEmpty) {
      operationNotAllowed(
        s"Values must be specified for key(s): ${badKeys.mkString("[", ",", "]")}", ctx)
    }
    props
  }

  // from Spark's AstBuilder
  override def visitPropertyKey(key: PropertyKeyContext): String = {
    if (key.STRING != null) {
      string(key.STRING)
    } else {
      key.getText
    }
  }

  // from Spark's AstBuilder
  override def visitPropertyValue(value: PropertyValueContext): String = {
    if (value == null) {
      null
    } else if (value.STRING != null) {
      string(value.STRING)
    } else if (value.booleanValue != null) {
      value.getText.toLowerCase(Locale.ROOT)
    } else {
      value.getText
    }
  }

  private def getFullText(ctx: ParserRuleContext): String = {
    if (ctx.start == null || ctx.stop == null || ctx.start.getStartIndex < 0 || ctx.stop.getStopIndex < 0) {
      ctx.getText
    } else {
      ctx.start.getInputStream.getText(Interval.of(ctx.start.getStartIndex, ctx.stop.getStopIndex))
    }
  }

  override def visitRegisterCatalog(ctx: RegisterCatalogContext): RegisterCatalogSpec = withOrigin(ctx) {
    val replace = ctx.REPLACE() != null
    val options = Option(ctx.options).map(visitPropertyKeyValues).getOrElse(Map.empty)
    val catalog = ctx.identifier().getText
    val source = visitMultipartIdentifier(ctx.source)
    val namespace = visitMultipartIdentifier(ctx.namespace)
    val pattern = if (ctx.LIKE() != null) {
      Some(string(ctx.pattern))
    } else {
      None
    }

    validateNamespace(namespace)
    if (!namespace(1).equalsIgnoreCase("metastore")) {
      throw new IllegalArgumentException("invalid target namespace. target name space should be under lightning.metastore")
    }

    RegisterCatalogSpec(namespace.toArray, catalog, options, source.toArray, pattern, replace)
  }

}


