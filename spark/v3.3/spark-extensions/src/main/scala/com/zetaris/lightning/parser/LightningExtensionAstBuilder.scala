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
import org.antlr.v4.runtime.misc.Interval
import org.antlr.v4.runtime.tree.ParseTree
import org.apache.spark.sql.catalyst.parser.ParserInterface
import org.apache.spark.sql.catalyst.parser.ParserUtils
import org.apache.spark.sql.catalyst.parser.ParserUtils.checkDuplicateKeys
import org.apache.spark.sql.catalyst.parser.ParserUtils.operationNotAllowed
import org.apache.spark.sql.catalyst.parser.ParserUtils.string
import org.apache.spark.sql.catalyst.parser.SqlBaseParser
import org.apache.spark.sql.catalyst.plans.logical.LogicalPlan
import org.apache.spark.sql.internal.SQLConf
import org.apache.spark.sql.types._
import com.zetaris.lightning.execution.command.ReferenceControl._
import com.zetaris.lightning.execution.command.{AccessControl, Annotation, AnnotationStatement, Assignment, BuildLakeHouse, ColumnSpec, CreateTableSpec, DataQuality, DataSourceType, DropLakeHouse, ForeignKey, NotNullColumn, PrimaryKeyColumn, RegisterDataSourceSpec, RegisterTableSpec, UniqueKeyColumn}
import LightningParserUtils.validateTableConstraints
import com.zetaris.lightning.model.LightningModel

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

  override def visitCreateTable(ctx: CreateTableContext): CreateTableSpec = withOrigin(ctx) {
    val dqAnnotations = ctx.hintAnnotations.asScala.map(visitHintAnnotation).map(_.asInstanceOf[DataQuality])
    val ifNotExist = ctx.EXISTS() != null
    val fqn = typedVisit[Seq[String]](ctx.multipartIdentifier)
    val columnSpecs = visitCreateDefinitions(ctx.createDefinitions)

    var primaryKey: Option[PrimaryKeyColumn] = None
    val uniques = scala.collection.mutable.ArrayBuffer.empty[UniqueKeyColumn]
    val foreignKeys = scala.collection.mutable.ArrayBuffer.empty[ForeignKey]

    ctx.tableConstraint().asScala.foreach {
      case constraintContext: PrimaryKeyTableConstraintContext =>
        primaryKey = Some(visitPrimaryKeyTableConstraint(constraintContext))
      case constraintContext: ForeignKeyTableConstraintContext =>
        foreignKeys += visitForeignKeyTableConstraint(constraintContext)
      case constraintContext: UniqueKeyTableConstraintContext =>
        uniques += visitUniqueKeyTableConstraint(constraintContext)
    }

    validateTableConstraints(columnSpecs, primaryKey, uniques, foreignKeys)
    val lakehouse = Option(ctx.lakehouse).map(_.getText)


    CreateTableSpec(fqn, columnSpecs, primaryKey, uniques, foreignKeys, ifNotExist, lakehouse, dqAnnotations)
  }

  override def visitCreateDefinitions(ctx: CreateDefinitionsContext): Seq[ColumnSpec] = withOrigin(ctx) {
    toSeq(ctx.createDefinition).map(visitCreateDefinition)
  }

  override def visitFullColumnName(ctx: FullColumnNameContext): String = withOrigin(ctx) {
    ctx.colName.getText
  }

  override def visitColumnDefinition(ctx: ColumnDefinitionContext): (DataType, Seq[AnyRef]) = withOrigin(ctx) {
    val constraints = ctx.columnConstraint().asScala.map { ccc =>
      typedVisit[AnyRef](ccc)
    }

    (typedVisit[DataType](ctx.dataType), constraints)
  }

  override def visitPrimaryKeyTableConstraint(ctx: PrimaryKeyTableConstraintContext)
  : PrimaryKeyColumn = withOrigin(ctx) {
    val constraintName = if (ctx.name != null) {
      Option(ctx.name.getText)
    } else {
      None
    }
    val primaryKeyColumns = visitIndexColumnNames(ctx.indexColumnNames)
    PrimaryKeyColumn(primaryKeyColumns, constraintName)
  }

  override def visitUniqueKeyTableConstraint(ctx: UniqueKeyTableConstraintContext): UniqueKeyColumn = withOrigin(ctx) {
    val constraintName = if (ctx.name != null) {
      Option(ctx.name.getText)
    } else {
      None
    }
    val uniqueKeyColumns = visitIndexColumnNames(ctx.indexColumnNames)
    UniqueKeyColumn(uniqueKeyColumns, constraintName)
  }

  override def visitForeignKeyTableConstraint(ctx: ForeignKeyTableConstraintContext): ForeignKey = withOrigin(ctx) {
    val constraintName = if (ctx.name != null) {
      Option(ctx.name.getText)
    } else {
      None
    }
    val foreignKeyColumns = visitIndexColumnNames(ctx.indexColumnNames)
    val refDef = visitReferenceDefinition(ctx.referenceDefinition)

    ForeignKey(foreignKeyColumns, constraintName, refDef._1, refDef._2, refDef._3, refDef._4)
  }

  override def visitIndexColumnNames(ctx: IndexColumnNamesContext): Seq[String] = withOrigin(ctx) {
    ctx.fullColumnName().asScala.map(_.getText)
  }

  override def visitReferenceDefinition(ctx: ReferenceDefinitionContext)
  : (Seq[String], Seq[String], Option[ReferenceControl], Option[ReferenceControl]) = withOrigin(ctx) {
    val refTable = visitMultipartIdentifier(ctx.multipartIdentifier)
    val refColumns = visitIndexColumnNames(ctx.indexColumnNames)
    val refActions = Option(ctx.referenceAction).map(visitReferenceAction).getOrElse((None, None))

    (refTable, refColumns, refActions._1, refActions._2)
  }

  override def visitReferenceAction(ctx: ReferenceActionContext)
  : (Option[ReferenceControl], Option[ReferenceControl]) = withOrigin(ctx) {
    (Some(visitReferenceControlType(ctx.onDelete)), Some(visitReferenceControlType(ctx.onUpdate)))
  }

  override def visitReferenceControlType(ctx: ReferenceControlTypeContext): ReferenceControl = withOrigin(ctx) {
    ctx.getText match {
      case "RESTRICT" => Restrict
      case "CASCADE" => Cascade
      case "SET NULL" => SetNull
      case "NO ACTION" => NoAction
      case "SET DEFAULT" => SetDefault
    }
  }

  override def visitComplexColTypeList(ctx: ComplexColTypeListContext): Seq[StructField] = withOrigin(ctx) {
    ctx.complexColType().asScala.map(visitComplexColType).toSeq
  }

  override def visitComplexColType(ctx: ComplexColTypeContext): StructField = withOrigin(ctx) {
    StructField(
      name = ctx.identifier.getText,
      dataType = typedVisit(ctx.dataType),
      nullable = true)
  }

  override def visitMultipartIdentifier(ctx: MultipartIdentifierContext): Seq[String] = withOrigin(ctx) {
    ctx.parts.asScala.map(_.getText)
  }

  override def visitComplexDataType(ctx: ComplexDataTypeContext): DataType = withOrigin(ctx) {
    ctx.complex.getType match {
      case SqlBaseParser.ARRAY =>
        ArrayType(typedVisit(ctx.dataType(0)))
      case SqlBaseParser.MAP =>
        MapType(typedVisit(ctx.dataType(0)), typedVisit(ctx.dataType(1)))
      case SqlBaseParser.STRUCT =>
        StructType(Option(ctx.complexColTypeList).toSeq.flatMap(visitComplexColTypeList))
    }
  }

  override def visitYearMonthIntervalDataType(ctx: YearMonthIntervalDataTypeContext): DataType = withOrigin(ctx) {
    val startStr = ctx.from.getText.toLowerCase(Locale.ROOT)
    val start = YearMonthIntervalType.stringToField(startStr)
    if (ctx.to != null) {
      val endStr = ctx.to.getText.toLowerCase(Locale.ROOT)
      val end = YearMonthIntervalType.stringToField(endStr)
      if (end <= start) {
        throw QueryParsingErrors.fromToIntervalUnsupportedError(startStr, endStr, ctx)
      }
      YearMonthIntervalType(start, end)
    } else {
      YearMonthIntervalType(start)
    }
  }

  override def visitDayTimeIntervalDataType(ctx: DayTimeIntervalDataTypeContext): DataType = withOrigin(ctx) {
    val startStr = ctx.from.getText.toLowerCase(Locale.ROOT)
    val start = DayTimeIntervalType.stringToField(startStr)
    if (ctx.to != null) {
      val endStr = ctx.to.getText.toLowerCase(Locale.ROOT)
      val end = DayTimeIntervalType.stringToField(endStr)
      if (end <= start) {
        throw QueryParsingErrors.fromToIntervalUnsupportedError(startStr, endStr, ctx)
      }
      DayTimeIntervalType(start, end)
    } else {
      DayTimeIntervalType(start)
    }
  }

  override def visitPrimitiveDataType(ctx: PrimitiveDataTypeContext): DataType = withOrigin(ctx) {
    val dataType = ctx.identifier.getText.toLowerCase(Locale.ROOT)
    (dataType, ctx.INTEGER_VALUE().asScala.toList) match {
      case ("boolean", Nil) => BooleanType
      case ("tinyint" | "byte", Nil) => ByteType
      case ("smallint" | "short", Nil) => ShortType
      case ("int" | "integer", Nil) => IntegerType
      case ("bigint" | "long", Nil) => LongType
      case ("float" | "real", Nil) => FloatType
      case ("double", Nil) => DoubleType
      case ("date", Nil) => DateType
      case ("timestamp", Nil) => SQLConf.get.timestampType
      case ("string", Nil) => StringType
      case ("character" | "char", length :: Nil) => CharType(length.getText.toInt)
      case ("varchar", length :: Nil) => VarcharType(length.getText.toInt)
      case ("binary", Nil) => BinaryType
      case ("decimal" | "dec" | "numeric", Nil) => DecimalType.USER_DEFAULT
      case ("decimal" | "dec" | "numeric", precision :: Nil) =>
        DecimalType(precision.getText.toInt, 0)
      case ("decimal" | "dec" | "numeric", precision :: scale :: Nil) =>
        DecimalType(precision.getText.toInt, scale.getText.toInt)
      case ("void", Nil) => NullType
      case ("interval", Nil) => CalendarIntervalType
      case (dt@("character" | "char" | "varchar"), Nil) =>
        throw QueryParsingErrors.charTypeMissingLengthError(dt, ctx)
      case (dt, params) =>
        val dtStr = if (params.nonEmpty) s"$dt(${params.mkString(",")})" else dt
        throw QueryParsingErrors.dataTypeUnsupportedError(dtStr, ctx)
    }
  }

  override def visitCreateDefinition(ctx: CreateDefinitionContext): ColumnSpec = withOrigin(ctx) {
    val accessControlHint = Option(ctx.hintAnnotation()).map(visitHintAnnotation).map(_.asInstanceOf[AccessControl])
    val columnName = visitFullColumnName(ctx.fullColumnName())
    val (dataType, constraints) = visitColumnDefinition(ctx.columnDefinition())

    var primaryKey: Option[PrimaryKeyColumn] = None
    var notNull: Option[NotNullColumn] = None
    var unique: Option[UniqueKeyColumn] = None
    var foreignKey: Option[ForeignKey] = None

    constraints.foreach {
      case pk: PrimaryKeyColumn => primaryKey = Some(pk)
      case nn: NotNullColumn => notNull = Some(nn)
      case un: UniqueKeyColumn => unique = Some(un)
      case fk: ForeignKey => foreignKey = Some(fk)
      case _ =>
    }

    ColumnSpec(columnName, dataType, primaryKey, notNull, unique, foreignKey, accessControlHint)
  }

  override def visitNotNullColumnConstraint(ctx: NotNullColumnConstraintContext): NotNullColumn = withOrigin(ctx) {
    NotNullColumn()
  }

  override def visitPrimaryKeyColumnConstraint(ctx: PrimaryKeyColumnConstraintContext)
  : PrimaryKeyColumn = withOrigin(ctx) {
    PrimaryKeyColumn(Seq.empty, None)
  }

  override def visitUniqueKeyColumnConstraint(ctx: UniqueKeyColumnConstraintContext)
  : UniqueKeyColumn = withOrigin(ctx) {
    UniqueKeyColumn(Seq.empty, None)
  }

  override def visitForeignKeyColumnConstraint(ctx: ForeignKeyColumnConstraintContext): ForeignKey = withOrigin(ctx) {
    val refDef = visitReferenceDefinition(ctx.referenceDefinition())
    ForeignKey(Seq.empty, None, refDef._1, refDef._2, refDef._3, refDef._4)
  }

  override def visitRegisterDataSource(ctx: RegisterDataSourceContext)
  : RegisterDataSourceSpec = withOrigin(ctx) {
    val replace = ctx.REPLACE() != null
    val dataSourceType = DataSourceType(ctx.dataSourceType.getText)
    val options = Option(ctx.options).map(visitPropertyKeyValues).getOrElse(Map.empty)
    val name = ctx.identifier().getText
    val namespace = visitMultipartIdentifier(ctx.multipartIdentifier())

    if (namespace.size < 3) {
      throw new IllegalArgumentException(s"namespace must have at least three namespace : lightning.datasource|metastore.namespace")
    } else {
      val fqn = namespace.take(2).mkString(".").toLowerCase
      if (!LightningModel.DEFAULT_NAMESPACES.exists(_.equals(fqn))) {
        throw new IllegalArgumentException(s"name space must be formed : lightning.datasource|metastore(.namespace)*")
      }
    }

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

  override def visitBuildLakeHouse(ctx: BuildLakeHouseContext): BuildLakeHouse = withOrigin(ctx) {
    val identifier = ctx.identifier().getText
    val path = Option(ctx.path).map { token =>
      ParserUtils.string(token)
    }
    BuildLakeHouse(identifier, path)
  }

  override def visitRegisterTable(ctx: RegisterTableContext): RegisterTableSpec = withOrigin(ctx) {
    val fqn = visitMultipartIdentifier(ctx.multipartIdentifier())
    val query = getFullText(ctx.query)
    RegisterTableSpec(fqn, query)
  }

  private def getFullText(ctx: ParserRuleContext): String = {
    if (ctx.start == null || ctx.stop == null || ctx.start.getStartIndex < 0 || ctx.stop.getStopIndex < 0) {
      ctx.getText
    } else {
      ctx.start.getInputStream.getText(Interval.of(ctx.start.getStartIndex, ctx.stop.getStopIndex))
    }
  }

  override def visitDropLakeHouse(ctx: DropLakeHouseContext): LogicalPlan = withOrigin(ctx) {
    val name = ctx.identifier().getText
    DropLakeHouse(name)
  }

  override def visitHintAnnotation(ctx: HintAnnotationContext): Annotation = withOrigin(ctx) {
    val stmt = visitAnnotationStatement(ctx.annotationStatement())
    LightningParserUtils.parseAnnotation(stmt)
  }

  override def visitAnnotationStatement(ctx: AnnotationStatementContext): AnnotationStatement = withOrigin(ctx) {
    val name = ctx.annotationName.getText
    val params = ctx.parameters.asScala.map(visitAssignment)
    AnnotationStatement(name, params)
  }

  override def visitAssignment(ctx: AssignmentContext): Assignment = withOrigin(ctx) {
    Assignment(ctx.name.getText, string(ctx.value))
  }
}


