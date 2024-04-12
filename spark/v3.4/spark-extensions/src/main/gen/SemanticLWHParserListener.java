// Generated from /Users/jaesungjun/temp/project/omni-connect/spark/v3.3/spark-extensions/src/main/antlr/LightningParser.g4 by ANTLR 4.10.1
package org.urdata.slwh.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link SemanticLWHParser}.
 */
public interface SemanticLWHParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#singleStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleStatement(SemanticLWHParser.SingleStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#singleStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleStatement(SemanticLWHParser.SingleStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(SemanticLWHParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(SemanticLWHParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdlStatement(SemanticLWHParser.DdlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdlStatement(SemanticLWHParser.DdlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#registerJdbcDataSource}.
	 * @param ctx the parse tree
	 */
	void enterRegisterJdbcDataSource(SemanticLWHParser.RegisterJdbcDataSourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#registerJdbcDataSource}.
	 * @param ctx the parse tree
	 */
	void exitRegisterJdbcDataSource(SemanticLWHParser.RegisterJdbcDataSourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterCreateTable(SemanticLWHParser.CreateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitCreateTable(SemanticLWHParser.CreateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#createDefinitions}.
	 * @param ctx the parse tree
	 */
	void enterCreateDefinitions(SemanticLWHParser.CreateDefinitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#createDefinitions}.
	 * @param ctx the parse tree
	 */
	void exitCreateDefinitions(SemanticLWHParser.CreateDefinitionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void enterCreateDefinition(SemanticLWHParser.CreateDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void exitCreateDefinition(SemanticLWHParser.CreateDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefinition(SemanticLWHParser.ColumnDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefinition(SemanticLWHParser.ColumnDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notNullColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterNotNullColumnConstraint(SemanticLWHParser.NotNullColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notNullColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitNotNullColumnConstraint(SemanticLWHParser.NotNullColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKeyColumnConstraint(SemanticLWHParser.PrimaryKeyColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKeyColumnConstraint(SemanticLWHParser.PrimaryKeyColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uniqueKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterUniqueKeyColumnConstraint(SemanticLWHParser.UniqueKeyColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uniqueKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitUniqueKeyColumnConstraint(SemanticLWHParser.UniqueKeyColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code foreignKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterForeignKeyColumnConstraint(SemanticLWHParser.ForeignKeyColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code foreignKeyColumnConstraint}
	 * labeled alternative in {@link SemanticLWHParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitForeignKeyColumnConstraint(SemanticLWHParser.ForeignKeyColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKeyTableConstraint(SemanticLWHParser.PrimaryKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKeyTableConstraint(SemanticLWHParser.PrimaryKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uniqueKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterUniqueKeyTableConstraint(SemanticLWHParser.UniqueKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uniqueKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitUniqueKeyTableConstraint(SemanticLWHParser.UniqueKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code foreignKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterForeignKeyTableConstraint(SemanticLWHParser.ForeignKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code foreignKeyTableConstraint}
	 * labeled alternative in {@link SemanticLWHParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitForeignKeyTableConstraint(SemanticLWHParser.ForeignKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#indexColumnNames}.
	 * @param ctx the parse tree
	 */
	void enterIndexColumnNames(SemanticLWHParser.IndexColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#indexColumnNames}.
	 * @param ctx the parse tree
	 */
	void exitIndexColumnNames(SemanticLWHParser.IndexColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#referenceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterReferenceDefinition(SemanticLWHParser.ReferenceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#referenceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitReferenceDefinition(SemanticLWHParser.ReferenceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#referenceAction}.
	 * @param ctx the parse tree
	 */
	void enterReferenceAction(SemanticLWHParser.ReferenceActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#referenceAction}.
	 * @param ctx the parse tree
	 */
	void exitReferenceAction(SemanticLWHParser.ReferenceActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#referenceControlType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceControlType(SemanticLWHParser.ReferenceControlTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#referenceControlType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceControlType(SemanticLWHParser.ReferenceControlTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void enterFullColumnName(SemanticLWHParser.FullColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void exitFullColumnName(SemanticLWHParser.FullColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterErrorCapturingIdentifier(SemanticLWHParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#errorCapturingIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitErrorCapturingIdentifier(SemanticLWHParser.ErrorCapturingIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterErrorIdent(SemanticLWHParser.ErrorIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitErrorIdent(SemanticLWHParser.ErrorIdentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void enterRealIdent(SemanticLWHParser.RealIdentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code realIdent}
	 * labeled alternative in {@link SemanticLWHParser#errorCapturingIdentifierExtra}.
	 * @param ctx the parse tree
	 */
	void exitRealIdent(SemanticLWHParser.RealIdentContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterMultipartIdentifier(SemanticLWHParser.MultipartIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#multipartIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitMultipartIdentifier(SemanticLWHParser.MultipartIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#identifier}.
	 * @param ctx the parse tree
	 */
	void enterIdentifier(SemanticLWHParser.IdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#identifier}.
	 * @param ctx the parse tree
	 */
	void exitIdentifier(SemanticLWHParser.IdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterUnquotedIdentifier(SemanticLWHParser.UnquotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unquotedIdentifier}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitUnquotedIdentifier(SemanticLWHParser.UnquotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifierAlternative(SemanticLWHParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code quotedIdentifierAlternative}
	 * labeled alternative in {@link SemanticLWHParser#strictIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifierAlternative(SemanticLWHParser.QuotedIdentifierAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void enterQuotedIdentifier(SemanticLWHParser.QuotedIdentifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#quotedIdentifier}.
	 * @param ctx the parse tree
	 */
	void exitQuotedIdentifier(SemanticLWHParser.QuotedIdentifierContext ctx);
	/**
	 * Enter a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterComplexDataType(SemanticLWHParser.ComplexDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code complexDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitComplexDataType(SemanticLWHParser.ComplexDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterYearMonthIntervalDataType(SemanticLWHParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code yearMonthIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitYearMonthIntervalDataType(SemanticLWHParser.YearMonthIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDayTimeIntervalDataType(SemanticLWHParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dayTimeIntervalDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDayTimeIntervalDataType(SemanticLWHParser.DayTimeIntervalDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveDataType(SemanticLWHParser.PrimitiveDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primitiveDataType}
	 * labeled alternative in {@link SemanticLWHParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveDataType(SemanticLWHParser.PrimitiveDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void enterComplexColTypeList(SemanticLWHParser.ComplexColTypeListContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#complexColTypeList}.
	 * @param ctx the parse tree
	 */
	void exitComplexColTypeList(SemanticLWHParser.ComplexColTypeListContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void enterComplexColType(SemanticLWHParser.ComplexColTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#complexColType}.
	 * @param ctx the parse tree
	 */
	void exitComplexColType(SemanticLWHParser.ComplexColTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link SemanticLWHParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void enterAnsiNonReserved(SemanticLWHParser.AnsiNonReservedContext ctx);
	/**
	 * Exit a parse tree produced by {@link SemanticLWHParser#ansiNonReserved}.
	 * @param ctx the parse tree
	 */
	void exitAnsiNonReserved(SemanticLWHParser.AnsiNonReservedContext ctx);
}