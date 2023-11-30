// Generated from /Users/jaesungjun/temp/project/omni-connect/spark/v3.3/spark-extensions/src/main/antlr/LightningParser.g4 by ANTLR 4.10.1
package org.urdata.slwh.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SemanticLWHParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SemanticLWHParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#singleStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleStatement(SemanticLWHParser.SingleStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(SemanticLWHParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#ddlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDdlStatement(SemanticLWHParser.DdlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#registerJdbcDataSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegisterJdbcDataSource(SemanticLWHParser.RegisterJdbcDataSourceContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#createTable}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateTable(SemanticLWHParser.CreateTableContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#createDefinitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDefinitions(SemanticLWHParser.CreateDefinitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#createDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreateDefinition(SemanticLWHParser.CreateDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#columnDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnDefinition(SemanticLWHParser.ColumnDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notNullColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotNullColumnConstraint(SemanticLWHParser.NotNullColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKeyColumnConstraint(SemanticLWHParser.PrimaryKeyColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uniqueKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUniqueKeyColumnConstraint(SemanticLWHParser.UniqueKeyColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code foreignKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeignKeyColumnConstraint(SemanticLWHParser.ForeignKeyColumnConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primaryKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimaryKeyTableConstraint(SemanticLWHParser.PrimaryKeyTableConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code uniqueKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUniqueKeyTableConstraint(SemanticLWHParser.UniqueKeyTableConstraintContext ctx);
	/**
	 * Visit a parse tree produced by the {@code foreignKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForeignKeyTableConstraint(SemanticLWHParser.ForeignKeyTableConstraintContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#indexColumnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexColumnNames(SemanticLWHParser.IndexColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#referenceDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceDefinition(SemanticLWHParser.ReferenceDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#referenceAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceAction(SemanticLWHParser.ReferenceActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#referenceControlType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReferenceControlType(SemanticLWHParser.ReferenceControlTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#fullColumnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullColumnName(SemanticLWHParser.FullColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorCapturingIdentifier(SemanticLWHParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitErrorIdent(SemanticLWHParser.ErrorIdentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRealIdent(SemanticLWHParser.RealIdentContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultipartIdentifier(SemanticLWHParser.MultipartIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#identifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdentifier(SemanticLWHParser.IdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnquotedIdentifier(SemanticLWHParser.UnquotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifierAlternative(SemanticLWHParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuotedIdentifier(SemanticLWHParser.QuotedIdentifierContext ctx);
	/**
	 * Visit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexDataType(SemanticLWHParser.ComplexDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitYearMonthIntervalDataType(SemanticLWHParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDayTimeIntervalDataType(SemanticLWHParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimitiveDataType(SemanticLWHParser.PrimitiveDataTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#complexColTypeList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColTypeList(SemanticLWHParser.ComplexColTypeListContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#complexColType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComplexColType(SemanticLWHParser.ComplexColTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link SemanticLWHParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnsiNonReserved(SemanticLWHParser.AnsiNonReservedContext ctx);
}