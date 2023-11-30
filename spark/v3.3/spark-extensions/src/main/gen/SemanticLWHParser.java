// Generated from /Users/jaesungjun/temp/project/omni-connect/spark/v3.3/spark-extensions/src/main/antlr/LightningParser.g4 by ANTLR 4.10.1
package org.urdata.slwh.parser;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SemanticLWHParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		SEMICOLON=1, LEFT_PAREN=2, RIGHT_PAREN=3, COMMA=4, DOT=5, LEFT_BRACKET=6, 
		RIGHT_BRACKET=7, ACTION=8, ADD=9, AFTER=10, ALL=11, ALTER=12, ANALYZE=13, 
		AND=14, ANTI=15, ANY=16, ARCHIVE=17, ARRAY=18, AS=19, ASC=20, AT=21, AUTHORIZATION=22, 
		BETWEEN=23, BOTH=24, BUCKET=25, BUCKETS=26, BY=27, CACHE=28, CASCADE=29, 
		CASE=30, CAST=31, CATALOG=32, CATALOGS=33, CHANGE=34, CHECK=35, CLEAR=36, 
		CLUSTER=37, CLUSTERED=38, CODEGEN=39, COLLATE=40, COLLECTION=41, COLUMN=42, 
		COLUMNS=43, COMMENT=44, COMMIT=45, COMPACT=46, COMPACTIONS=47, COMPUTE=48, 
		CONCATENATE=49, CONSTRAINT=50, COST=51, CREATE=52, CROSS=53, CUBE=54, 
		CURRENT=55, CURRENT_DATE=56, CURRENT_TIME=57, CURRENT_TIMESTAMP=58, CURRENT_USER=59, 
		DAY=60, DAYOFYEAR=61, DATA=62, DATABASE=63, DATABASES=64, DATEADD=65, 
		DATEDIFF=66, DBPROPERTIES=67, DEFAULT=68, DEFINED=69, DELETE=70, DELIMITED=71, 
		DESC=72, DESCRIBE=73, DFS=74, DIRECTORIES=75, DIRECTORY=76, DISTINCT=77, 
		DISTRIBUTE=78, DIV=79, DROP=80, ELSE=81, END=82, ESCAPE=83, ESCAPED=84, 
		EXCEPT=85, EXCHANGE=86, EXISTS=87, EXPLAIN=88, EXPORT=89, EXTENDED=90, 
		EXTERNAL=91, EXTRACT=92, FALSE=93, FETCH=94, FIELDS=95, FILTER=96, FILEFORMAT=97, 
		FIRST=98, FOLLOWING=99, FOR=100, FOREIGN=101, FORMAT=102, FORMATTED=103, 
		FROM=104, FULL=105, FUNCTION=106, FUNCTIONS=107, GLOBAL=108, GRANT=109, 
		GROUP=110, GROUPING=111, HAVING=112, HOUR=113, IF=114, IGNORE=115, IMPORT=116, 
		IN=117, INDEX=118, INDEXES=119, INNER=120, INPATH=121, INPUTFORMAT=122, 
		INSERT=123, INTERSECT=124, INTERVAL=125, INTO=126, IS=127, ITEMS=128, 
		JOIN=129, KEY=130, KEYS=131, LAST=132, LATERAL=133, LAZY=134, LEADING=135, 
		LEFT=136, LIKE=137, ILIKE=138, LIMIT=139, LINES=140, LIST=141, LOAD=142, 
		LOCAL=143, LOCATION=144, LOCK=145, LOCKS=146, LOGICAL=147, MACRO=148, 
		MAP=149, MATCHED=150, MERGE=151, MICROSECOND=152, MILLISECOND=153, MINUTE=154, 
		MONTH=155, MSCK=156, NAMESPACE=157, NAMESPACES=158, NATURAL=159, NO=160, 
		NOT=161, NULL=162, NULLS=163, OF=164, ON=165, ONLY=166, OPTION=167, OPTIONS=168, 
		OR=169, ORDER=170, OUT=171, OUTER=172, OUTPUTFORMAT=173, OVER=174, OVERLAPS=175, 
		OVERLAY=176, OVERWRITE=177, PARTITION=178, PARTITIONED=179, PARTITIONS=180, 
		PERCENTILE_CONT=181, PERCENTILE_DISC=182, PERCENTLIT=183, PIVOT=184, PLACING=185, 
		POSITION=186, PRECEDING=187, PRIMARY=188, PRINCIPALS=189, PROPERTIES=190, 
		PURGE=191, QUARTER=192, QUERY=193, RANGE=194, RECORDREADER=195, RECORDWRITER=196, 
		RECOVER=197, REDUCE=198, REFERENCES=199, REFRESH=200, RENAME=201, REPAIR=202, 
		REPEATABLE=203, REPLACE=204, RESET=205, RESPECT=206, RESTRICT=207, REVOKE=208, 
		RIGHT=209, RLIKE=210, ROLE=211, ROLES=212, ROLLBACK=213, ROLLUP=214, ROW=215, 
		ROWS=216, SECOND=217, SCHEMA=218, SCHEMAS=219, SELECT=220, SEMI=221, SEPARATED=222, 
		SERDE=223, SERDEPROPERTIES=224, SESSION_USER=225, SET=226, SETMINUS=227, 
		SETS=228, SHOW=229, SKEWED=230, SOME=231, SORT=232, SORTED=233, START=234, 
		STATISTICS=235, STORED=236, STRATIFY=237, STRUCT=238, SUBSTR=239, SUBSTRING=240, 
		SYNC=241, SYSTEM_TIME=242, SYSTEM_VERSION=243, TABLE=244, TABLES=245, 
		TABLESAMPLE=246, TBLPROPERTIES=247, TEMPORARY=248, TERMINATED=249, THEN=250, 
		TIMESTAMP=251, TIMESTAMPADD=252, TIMESTAMPDIFF=253, TO=254, TOUCH=255, 
		TRAILING=256, TRANSACTION=257, TRANSACTIONS=258, TRANSFORM=259, TRIM=260, 
		TRUE=261, TRUNCATE=262, TRY_CAST=263, TYPE=264, UNARCHIVE=265, UNBOUNDED=266, 
		UNCACHE=267, UNION=268, UNIQUE=269, UNKNOWN=270, UNLOCK=271, UNSET=272, 
		UPDATE=273, USE=274, USER=275, USING=276, VALUES=277, VERSION=278, VIEW=279, 
		VIEWS=280, WEEK=281, WHEN=282, WHERE=283, WINDOW=284, WITH=285, WITHIN=286, 
		YEAR=287, ZONE=288, EQ=289, NSEQ=290, NEQ=291, NEQJ=292, LT=293, LTE=294, 
		GT=295, GTE=296, PLUS=297, MINUS=298, ASTERISK=299, SLASH=300, PERCENT=301, 
		TILDE=302, AMPERSAND=303, PIPE=304, CONCAT_PIPE=305, HAT=306, COLON=307, 
		ARROW=308, HENT_START=309, HENT_END=310, STRING=311, BIGINT_LITERAL=312, 
		SMALLINT_LITERAL=313, TINYINT_LITERAL=314, INTEGER_VALUE=315, EXPONENT_VALUE=316, 
		DECIMAL_VALUE=317, FLOAT_LITERAL=318, DOUBLE_LITERAL=319, BIGDECIMAL_LITERAL=320, 
		IDENTIFIER=321, BACKQUOTED_IDENTIFIER=322, SIMPLE_COMMENT=323, BRACKETED_COMMENT=324, 
		WS=325, UNRECOGNIZED=326, REGISTER=327, JDBC=328, DATASOURCE=329;
	public static final int
		RULE_singleStatement = 0, RULE_statement = 1, RULE_ddlStatement = 2, RULE_registerJdbcDataSource = 3, 
		RULE_createTable = 4, RULE_createDefinitions = 5, RULE_createDefinition = 6, 
		RULE_columnDefinition = 7, RULE_columnConstraint = 8, RULE_tableConstraint = 9, 
		RULE_indexColumnNames = 10, RULE_referenceDefinition = 11, RULE_referenceAction = 12, 
		RULE_referenceControlType = 13, RULE_fullColumnName = 14, RULE_errorCapturingIdentifier = 15, 
		RULE_errorCapturingIdentifierExtra = 16, RULE_multipartIdentifier = 17, 
		RULE_identifier = 18, RULE_strictIdentifier = 19, RULE_quotedIdentifier = 20, 
		RULE_dataType = 21, RULE_complexColTypeList = 22, RULE_complexColType = 23, 
		RULE_ansiNonReserved = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"singleStatement", "statement", "ddlStatement", "registerJdbcDataSource", 
			"createTable", "createDefinitions", "createDefinition", "columnDefinition", 
			"columnConstraint", "tableConstraint", "indexColumnNames", "referenceDefinition", 
			"referenceAction", "referenceControlType", "fullColumnName", "errorCapturingIdentifier", 
			"errorCapturingIdentifierExtra", "multipartIdentifier", "identifier", 
			"strictIdentifier", "quotedIdentifier", "dataType", "complexColTypeList", 
			"complexColType", "ansiNonReserved"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'('", "')'", "','", "'.'", "'['", "']'", "'ACTION'", "'ADD'", 
			"'AFTER'", "'ALL'", "'ALTER'", "'ANALYZE'", "'AND'", "'ANTI'", "'ANY'", 
			"'ARCHIVE'", "'ARRAY'", "'AS'", "'ASC'", "'AT'", "'AUTHORIZATION'", "'BETWEEN'", 
			"'BOTH'", "'BUCKET'", "'BUCKETS'", "'BY'", "'CACHE'", "'CASCADE'", "'CASE'", 
			"'CAST'", "'CATALOG'", "'CATALOGS'", "'CHANGE'", "'CHECK'", "'CLEAR'", 
			"'CLUSTER'", "'CLUSTERED'", "'CODEGEN'", "'COLLATE'", "'COLLECTION'", 
			"'COLUMN'", "'COLUMNS'", "'COMMENT'", "'COMMIT'", "'COMPACT'", "'COMPACTIONS'", 
			"'COMPUTE'", "'CONCATENATE'", "'CONSTRAINT'", "'COST'", "'CREATE'", "'CROSS'", 
			"'CUBE'", "'CURRENT'", "'CURRENT_DATE'", "'CURRENT_TIME'", "'CURRENT_TIMESTAMP'", 
			"'CURRENT_USER'", "'DAY'", "'DAYOFYEAR'", "'DATA'", "'DATABASE'", "'DATABASES'", 
			"'DATEADD'", "'DATEDIFF'", "'DBPROPERTIES'", "'DEFAULT'", "'DEFINED'", 
			"'DELETE'", "'DELIMITED'", "'DESC'", "'DESCRIBE'", "'DFS'", "'DIRECTORIES'", 
			"'DIRECTORY'", "'DISTINCT'", "'DISTRIBUTE'", "'DIV'", "'DROP'", "'ELSE'", 
			"'END'", "'ESCAPE'", "'ESCAPED'", "'EXCEPT'", "'EXCHANGE'", "'EXISTS'", 
			"'EXPLAIN'", "'EXPORT'", "'EXTENDED'", "'EXTERNAL'", "'EXTRACT'", "'FALSE'", 
			"'FETCH'", "'FIELDS'", "'FILTER'", "'FILEFORMAT'", "'FIRST'", "'FOLLOWING'", 
			"'FOR'", "'FOREIGN'", "'FORMAT'", "'FORMATTED'", "'FROM'", "'FULL'", 
			"'FUNCTION'", "'FUNCTIONS'", "'GLOBAL'", "'GRANT'", "'GROUP'", "'GROUPING'", 
			"'HAVING'", "'HOUR'", "'IF'", "'IGNORE'", "'IMPORT'", "'IN'", "'INDEX'", 
			"'INDEXES'", "'INNER'", "'INPATH'", "'INPUTFORMAT'", "'INSERT'", "'INTERSECT'", 
			"'INTERVAL'", "'INTO'", "'IS'", "'ITEMS'", "'JOIN'", "'KEY'", "'KEYS'", 
			"'LAST'", "'LATERAL'", "'LAZY'", "'LEADING'", "'LEFT'", "'LIKE'", "'ILIKE'", 
			"'LIMIT'", "'LINES'", "'LIST'", "'LOAD'", "'LOCAL'", "'LOCATION'", "'LOCK'", 
			"'LOCKS'", "'LOGICAL'", "'MACRO'", "'MAP'", "'MATCHED'", "'MERGE'", "'MICROSECOND'", 
			"'MILLISECOND'", "'MINUTE'", "'MONTH'", "'MSCK'", "'NAMESPACE'", "'NAMESPACES'", 
			"'NATURAL'", "'NO'", null, "'NULL'", "'NULLS'", "'OF'", "'ON'", "'ONLY'", 
			"'OPTION'", "'OPTIONS'", "'OR'", "'ORDER'", "'OUT'", "'OUTER'", "'OUTPUTFORMAT'", 
			"'OVER'", "'OVERLAPS'", "'OVERLAY'", "'OVERWRITE'", "'PARTITION'", "'PARTITIONED'", 
			"'PARTITIONS'", "'PERCENTILE_CONT'", "'PERCENTILE_DISC'", "'PERCENT'", 
			"'PIVOT'", "'PLACING'", "'POSITION'", "'PRECEDING'", "'PRIMARY'", "'PRINCIPALS'", 
			"'PROPERTIES'", "'PURGE'", "'QUARTER'", "'QUERY'", "'RANGE'", "'RECORDREADER'", 
			"'RECORDWRITER'", "'RECOVER'", "'REDUCE'", "'REFERENCES'", "'REFRESH'", 
			"'RENAME'", "'REPAIR'", "'REPEATABLE'", "'REPLACE'", "'RESET'", "'RESPECT'", 
			"'RESTRICT'", "'REVOKE'", "'RIGHT'", null, "'ROLE'", "'ROLES'", "'ROLLBACK'", 
			"'ROLLUP'", "'ROW'", "'ROWS'", "'SECOND'", "'SCHEMA'", "'SCHEMAS'", "'SELECT'", 
			"'SEMI'", "'SEPARATED'", "'SERDE'", "'SERDEPROPERTIES'", "'SESSION_USER'", 
			"'SET'", "'MINUS'", "'SETS'", "'SHOW'", "'SKEWED'", "'SOME'", "'SORT'", 
			"'SORTED'", "'START'", "'STATISTICS'", "'STORED'", "'STRATIFY'", "'STRUCT'", 
			"'SUBSTR'", "'SUBSTRING'", "'SYNC'", "'SYSTEM_TIME'", "'SYSTEM_VERSION'", 
			"'TABLE'", "'TABLES'", "'TABLESAMPLE'", "'TBLPROPERTIES'", null, "'TERMINATED'", 
			"'THEN'", "'TIMESTAMP'", "'TIMESTAMPADD'", "'TIMESTAMPDIFF'", "'TO'", 
			"'TOUCH'", "'TRAILING'", "'TRANSACTION'", "'TRANSACTIONS'", "'TRANSFORM'", 
			"'TRIM'", "'TRUE'", "'TRUNCATE'", "'TRY_CAST'", "'TYPE'", "'UNARCHIVE'", 
			"'UNBOUNDED'", "'UNCACHE'", "'UNION'", "'UNIQUE'", "'UNKNOWN'", "'UNLOCK'", 
			"'UNSET'", "'UPDATE'", "'USE'", "'USER'", "'USING'", "'VALUES'", "'VERSION'", 
			"'VIEW'", "'VIEWS'", "'WEEK'", "'WHEN'", "'WHERE'", "'WINDOW'", "'WITH'", 
			"'WITHIN'", "'YEAR'", "'ZONE'", null, "'<=>'", "'<>'", "'!='", "'<'", 
			null, "'>'", null, "'+'", "'-'", "'*'", "'/'", "'%'", "'~'", "'&'", "'|'", 
			"'||'", "'^'", "':'", "'->'", "'/*+'", "'*/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "SEMICOLON", "LEFT_PAREN", "RIGHT_PAREN", "COMMA", "DOT", "LEFT_BRACKET", 
			"RIGHT_BRACKET", "ACTION", "ADD", "AFTER", "ALL", "ALTER", "ANALYZE", 
			"AND", "ANTI", "ANY", "ARCHIVE", "ARRAY", "AS", "ASC", "AT", "AUTHORIZATION", 
			"BETWEEN", "BOTH", "BUCKET", "BUCKETS", "BY", "CACHE", "CASCADE", "CASE", 
			"CAST", "CATALOG", "CATALOGS", "CHANGE", "CHECK", "CLEAR", "CLUSTER", 
			"CLUSTERED", "CODEGEN", "COLLATE", "COLLECTION", "COLUMN", "COLUMNS", 
			"COMMENT", "COMMIT", "COMPACT", "COMPACTIONS", "COMPUTE", "CONCATENATE", 
			"CONSTRAINT", "COST", "CREATE", "CROSS", "CUBE", "CURRENT", "CURRENT_DATE", 
			"CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DAY", "DAYOFYEAR", 
			"DATA", "DATABASE", "DATABASES", "DATEADD", "DATEDIFF", "DBPROPERTIES", 
			"DEFAULT", "DEFINED", "DELETE", "DELIMITED", "DESC", "DESCRIBE", "DFS", 
			"DIRECTORIES", "DIRECTORY", "DISTINCT", "DISTRIBUTE", "DIV", "DROP", 
			"ELSE", "END", "ESCAPE", "ESCAPED", "EXCEPT", "EXCHANGE", "EXISTS", "EXPLAIN", 
			"EXPORT", "EXTENDED", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIELDS", 
			"FILTER", "FILEFORMAT", "FIRST", "FOLLOWING", "FOR", "FOREIGN", "FORMAT", 
			"FORMATTED", "FROM", "FULL", "FUNCTION", "FUNCTIONS", "GLOBAL", "GRANT", 
			"GROUP", "GROUPING", "HAVING", "HOUR", "IF", "IGNORE", "IMPORT", "IN", 
			"INDEX", "INDEXES", "INNER", "INPATH", "INPUTFORMAT", "INSERT", "INTERSECT", 
			"INTERVAL", "INTO", "IS", "ITEMS", "JOIN", "KEY", "KEYS", "LAST", "LATERAL", 
			"LAZY", "LEADING", "LEFT", "LIKE", "ILIKE", "LIMIT", "LINES", "LIST", 
			"LOAD", "LOCAL", "LOCATION", "LOCK", "LOCKS", "LOGICAL", "MACRO", "MAP", 
			"MATCHED", "MERGE", "MICROSECOND", "MILLISECOND", "MINUTE", "MONTH", 
			"MSCK", "NAMESPACE", "NAMESPACES", "NATURAL", "NO", "NOT", "NULL", "NULLS", 
			"OF", "ON", "ONLY", "OPTION", "OPTIONS", "OR", "ORDER", "OUT", "OUTER", 
			"OUTPUTFORMAT", "OVER", "OVERLAPS", "OVERLAY", "OVERWRITE", "PARTITION", 
			"PARTITIONED", "PARTITIONS", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENTLIT", 
			"PIVOT", "PLACING", "POSITION", "PRECEDING", "PRIMARY", "PRINCIPALS", 
			"PROPERTIES", "PURGE", "QUARTER", "QUERY", "RANGE", "RECORDREADER", "RECORDWRITER", 
			"RECOVER", "REDUCE", "REFERENCES", "REFRESH", "RENAME", "REPAIR", "REPEATABLE", 
			"REPLACE", "RESET", "RESPECT", "RESTRICT", "REVOKE", "RIGHT", "RLIKE", 
			"ROLE", "ROLES", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "SECOND", "SCHEMA", 
			"SCHEMAS", "SELECT", "SEMI", "SEPARATED", "SERDE", "SERDEPROPERTIES", 
			"SESSION_USER", "SET", "SETMINUS", "SETS", "SHOW", "SKEWED", "SOME", 
			"SORT", "SORTED", "START", "STATISTICS", "STORED", "STRATIFY", "STRUCT", 
			"SUBSTR", "SUBSTRING", "SYNC", "SYSTEM_TIME", "SYSTEM_VERSION", "TABLE", 
			"TABLES", "TABLESAMPLE", "TBLPROPERTIES", "TEMPORARY", "TERMINATED", 
			"THEN", "TIMESTAMP", "TIMESTAMPADD", "TIMESTAMPDIFF", "TO", "TOUCH", 
			"TRAILING", "TRANSACTION", "TRANSACTIONS", "TRANSFORM", "TRIM", "TRUE", 
			"TRUNCATE", "TRY_CAST", "TYPE", "UNARCHIVE", "UNBOUNDED", "UNCACHE", 
			"UNION", "UNIQUE", "UNKNOWN", "UNLOCK", "UNSET", "UPDATE", "USE", "USER", 
			"USING", "VALUES", "VERSION", "VIEW", "VIEWS", "WEEK", "WHEN", "WHERE", 
			"WINDOW", "WITH", "WITHIN", "YEAR", "ZONE", "EQ", "NSEQ", "NEQ", "NEQJ", 
			"LT", "LTE", "GT", "GTE", "PLUS", "MINUS", "ASTERISK", "SLASH", "PERCENT", 
			"TILDE", "AMPERSAND", "PIPE", "CONCAT_PIPE", "HAT", "COLON", "ARROW", 
			"HENT_START", "HENT_END", "STRING", "BIGINT_LITERAL", "SMALLINT_LITERAL", 
			"TINYINT_LITERAL", "INTEGER_VALUE", "EXPONENT_VALUE", "DECIMAL_VALUE", 
			"FLOAT_LITERAL", "DOUBLE_LITERAL", "BIGDECIMAL_LITERAL", "IDENTIFIER", 
			"BACKQUOTED_IDENTIFIER", "SIMPLE_COMMENT", "BRACKETED_COMMENT", "WS", 
			"UNRECOGNIZED", "REGISTER", "JDBC", "DATASOURCE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "LightningParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public SemanticLWHParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SingleStatementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(SemanticLWHParser.EOF, 0); }
		public SingleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterSingleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitSingleStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitSingleStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SingleStatementContext singleStatement() throws RecognitionException {
		SingleStatementContext _localctx = new SingleStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_singleStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			statement();
			setState(51);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public DdlStatementContext ddlStatement() {
			return getRuleContext(DdlStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(53);
			ddlStatement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DdlStatementContext extends ParserRuleContext {
		public RegisterJdbcDataSourceContext registerJdbcDataSource() {
			return getRuleContext(RegisterJdbcDataSourceContext.class,0);
		}
		public CreateTableContext createTable() {
			return getRuleContext(CreateTableContext.class,0);
		}
		public DdlStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ddlStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterDdlStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitDdlStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitDdlStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DdlStatementContext ddlStatement() throws RecognitionException {
		DdlStatementContext _localctx = new DdlStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_ddlStatement);
		try {
			setState(57);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case REGISTER:
				enterOuterAlt(_localctx, 1);
				{
				setState(55);
				registerJdbcDataSource();
				}
				break;
			case CREATE:
				enterOuterAlt(_localctx, 2);
				{
				setState(56);
				createTable();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RegisterJdbcDataSourceContext extends ParserRuleContext {
		public TerminalNode REGISTER() { return getToken(SemanticLWHParser.REGISTER, 0); }
		public TerminalNode JDBC() { return getToken(SemanticLWHParser.JDBC, 0); }
		public TerminalNode DATASOURCE() { return getToken(SemanticLWHParser.DATASOURCE, 0); }
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public TerminalNode OPTIONS() { return getToken(SemanticLWHParser.OPTIONS, 0); }
		public TerminalNode LEFT_PAREN() { return getToken(SemanticLWHParser.LEFT_PAREN, 0); }
		public TerminalNode RIGHT_PAREN() { return getToken(SemanticLWHParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public List<TableConstraintContext> tableConstraint() {
			return getRuleContexts(TableConstraintContext.class);
		}
		public TableConstraintContext tableConstraint(int i) {
			return getRuleContext(TableConstraintContext.class,i);
		}
		public RegisterJdbcDataSourceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_registerJdbcDataSource; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterRegisterJdbcDataSource(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitRegisterJdbcDataSource(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitRegisterJdbcDataSource(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RegisterJdbcDataSourceContext registerJdbcDataSource() throws RecognitionException {
		RegisterJdbcDataSourceContext _localctx = new RegisterJdbcDataSourceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_registerJdbcDataSource);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			match(REGISTER);
			setState(60);
			match(JDBC);
			setState(61);
			match(DATASOURCE);
			setState(62);
			errorCapturingIdentifier();
			setState(63);
			match(OPTIONS);
			setState(64);
			match(LEFT_PAREN);
			setState(69);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(65);
				match(COMMA);
				setState(66);
				tableConstraint();
				}
				}
				setState(71);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(72);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateTableContext extends ParserRuleContext {
		public TerminalNode CREATE() { return getToken(SemanticLWHParser.CREATE, 0); }
		public TerminalNode TABLE() { return getToken(SemanticLWHParser.TABLE, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SemanticLWHParser.LEFT_PAREN, 0); }
		public CreateDefinitionsContext createDefinitions() {
			return getRuleContext(CreateDefinitionsContext.class,0);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SemanticLWHParser.RIGHT_PAREN, 0); }
		public TerminalNode IF() { return getToken(SemanticLWHParser.IF, 0); }
		public TerminalNode NOT() { return getToken(SemanticLWHParser.NOT, 0); }
		public TerminalNode EXISTS() { return getToken(SemanticLWHParser.EXISTS, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public List<TableConstraintContext> tableConstraint() {
			return getRuleContexts(TableConstraintContext.class);
		}
		public TableConstraintContext tableConstraint(int i) {
			return getRuleContext(TableConstraintContext.class,i);
		}
		public CreateTableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createTable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterCreateTable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitCreateTable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitCreateTable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateTableContext createTable() throws RecognitionException {
		CreateTableContext _localctx = new CreateTableContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_createTable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			match(CREATE);
			setState(75);
			match(TABLE);
			setState(79);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(76);
				match(IF);
				setState(77);
				match(NOT);
				setState(78);
				match(EXISTS);
				}
				break;
			}
			setState(81);
			multipartIdentifier();
			setState(82);
			match(LEFT_PAREN);
			setState(83);
			createDefinitions();
			setState(88);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(84);
				match(COMMA);
				setState(85);
				tableConstraint();
				}
				}
				setState(90);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(91);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateDefinitionsContext extends ParserRuleContext {
		public List<CreateDefinitionContext> createDefinition() {
			return getRuleContexts(CreateDefinitionContext.class);
		}
		public CreateDefinitionContext createDefinition(int i) {
			return getRuleContext(CreateDefinitionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public CreateDefinitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createDefinitions; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterCreateDefinitions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitCreateDefinitions(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitCreateDefinitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateDefinitionsContext createDefinitions() throws RecognitionException {
		CreateDefinitionsContext _localctx = new CreateDefinitionsContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_createDefinitions);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			createDefinition();
			setState(98);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(94);
					match(COMMA);
					setState(95);
					createDefinition();
					}
					} 
				}
				setState(100);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CreateDefinitionContext extends ParserRuleContext {
		public FullColumnNameContext fullColumnName() {
			return getRuleContext(FullColumnNameContext.class,0);
		}
		public ColumnDefinitionContext columnDefinition() {
			return getRuleContext(ColumnDefinitionContext.class,0);
		}
		public CreateDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_createDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterCreateDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitCreateDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitCreateDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CreateDefinitionContext createDefinition() throws RecognitionException {
		CreateDefinitionContext _localctx = new CreateDefinitionContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_createDefinition);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(101);
			fullColumnName();
			setState(102);
			columnDefinition();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnDefinitionContext extends ParserRuleContext {
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public List<ColumnConstraintContext> columnConstraint() {
			return getRuleContexts(ColumnConstraintContext.class);
		}
		public ColumnConstraintContext columnConstraint(int i) {
			return getRuleContext(ColumnConstraintContext.class,i);
		}
		public ColumnDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterColumnDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitColumnDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitColumnDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnDefinitionContext columnDefinition() throws RecognitionException {
		ColumnDefinitionContext _localctx = new ColumnDefinitionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_columnDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(104);
			dataType();
			setState(108);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FOREIGN || _la==NOT || _la==PRIMARY || _la==UNIQUE) {
				{
				{
				setState(105);
				columnConstraint();
				}
				}
				setState(110);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ColumnConstraintContext extends ParserRuleContext {
		public ColumnConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_columnConstraint; }
	 
		public ColumnConstraintContext() { }
		public void copyFrom(ColumnConstraintContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class PrimaryKeyColumnConstraintContext extends ColumnConstraintContext {
		public TerminalNode PRIMARY() { return getToken(SemanticLWHParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(SemanticLWHParser.KEY, 0); }
		public PrimaryKeyColumnConstraintContext(ColumnConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterPrimaryKeyColumnConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitPrimaryKeyColumnConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitPrimaryKeyColumnConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UniqueKeyColumnConstraintContext extends ColumnConstraintContext {
		public TerminalNode UNIQUE() { return getToken(SemanticLWHParser.UNIQUE, 0); }
		public UniqueKeyColumnConstraintContext(ColumnConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterUniqueKeyColumnConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitUniqueKeyColumnConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitUniqueKeyColumnConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NotNullColumnConstraintContext extends ColumnConstraintContext {
		public TerminalNode NOT() { return getToken(SemanticLWHParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SemanticLWHParser.NULL, 0); }
		public NotNullColumnConstraintContext(ColumnConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterNotNullColumnConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitNotNullColumnConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitNotNullColumnConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ForeignKeyColumnConstraintContext extends ColumnConstraintContext {
		public TerminalNode FOREIGN() { return getToken(SemanticLWHParser.FOREIGN, 0); }
		public TerminalNode KEY() { return getToken(SemanticLWHParser.KEY, 0); }
		public ReferenceDefinitionContext referenceDefinition() {
			return getRuleContext(ReferenceDefinitionContext.class,0);
		}
		public ForeignKeyColumnConstraintContext(ColumnConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterForeignKeyColumnConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitForeignKeyColumnConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitForeignKeyColumnConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ColumnConstraintContext columnConstraint() throws RecognitionException {
		ColumnConstraintContext _localctx = new ColumnConstraintContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_columnConstraint);
		try {
			setState(119);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				_localctx = new NotNullColumnConstraintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				match(NOT);
				setState(112);
				match(NULL);
				}
				break;
			case PRIMARY:
				_localctx = new PrimaryKeyColumnConstraintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(113);
				match(PRIMARY);
				setState(114);
				match(KEY);
				}
				break;
			case UNIQUE:
				_localctx = new UniqueKeyColumnConstraintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(115);
				match(UNIQUE);
				}
				break;
			case FOREIGN:
				_localctx = new ForeignKeyColumnConstraintContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(116);
				match(FOREIGN);
				setState(117);
				match(KEY);
				setState(118);
				referenceDefinition();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TableConstraintContext extends ParserRuleContext {
		public TableConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tableConstraint; }
	 
		public TableConstraintContext() { }
		public void copyFrom(TableConstraintContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class UniqueKeyTableConstraintContext extends TableConstraintContext {
		public IdentifierContext name;
		public TerminalNode UNIQUE() { return getToken(SemanticLWHParser.UNIQUE, 0); }
		public IndexColumnNamesContext indexColumnNames() {
			return getRuleContext(IndexColumnNamesContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(SemanticLWHParser.CONSTRAINT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public UniqueKeyTableConstraintContext(TableConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterUniqueKeyTableConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitUniqueKeyTableConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitUniqueKeyTableConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimaryKeyTableConstraintContext extends TableConstraintContext {
		public IdentifierContext name;
		public TerminalNode PRIMARY() { return getToken(SemanticLWHParser.PRIMARY, 0); }
		public TerminalNode KEY() { return getToken(SemanticLWHParser.KEY, 0); }
		public IndexColumnNamesContext indexColumnNames() {
			return getRuleContext(IndexColumnNamesContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(SemanticLWHParser.CONSTRAINT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public PrimaryKeyTableConstraintContext(TableConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterPrimaryKeyTableConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitPrimaryKeyTableConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitPrimaryKeyTableConstraint(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ForeignKeyTableConstraintContext extends TableConstraintContext {
		public IdentifierContext name;
		public TerminalNode FOREIGN() { return getToken(SemanticLWHParser.FOREIGN, 0); }
		public TerminalNode KEY() { return getToken(SemanticLWHParser.KEY, 0); }
		public IndexColumnNamesContext indexColumnNames() {
			return getRuleContext(IndexColumnNamesContext.class,0);
		}
		public ReferenceDefinitionContext referenceDefinition() {
			return getRuleContext(ReferenceDefinitionContext.class,0);
		}
		public TerminalNode CONSTRAINT() { return getToken(SemanticLWHParser.CONSTRAINT, 0); }
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ForeignKeyTableConstraintContext(TableConstraintContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterForeignKeyTableConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitForeignKeyTableConstraint(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitForeignKeyTableConstraint(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TableConstraintContext tableConstraint() throws RecognitionException {
		TableConstraintContext _localctx = new TableConstraintContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tableConstraint);
		int _la;
		try {
			setState(143);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				_localctx = new PrimaryKeyTableConstraintContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CONSTRAINT) {
					{
					setState(121);
					match(CONSTRAINT);
					setState(122);
					((PrimaryKeyTableConstraintContext)_localctx).name = identifier();
					}
				}

				setState(125);
				match(PRIMARY);
				setState(126);
				match(KEY);
				setState(127);
				indexColumnNames();
				}
				break;
			case 2:
				_localctx = new UniqueKeyTableConstraintContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(130);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CONSTRAINT) {
					{
					setState(128);
					match(CONSTRAINT);
					setState(129);
					((UniqueKeyTableConstraintContext)_localctx).name = identifier();
					}
				}

				setState(132);
				match(UNIQUE);
				setState(133);
				indexColumnNames();
				}
				break;
			case 3:
				_localctx = new ForeignKeyTableConstraintContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(136);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CONSTRAINT) {
					{
					setState(134);
					match(CONSTRAINT);
					setState(135);
					((ForeignKeyTableConstraintContext)_localctx).name = identifier();
					}
				}

				setState(138);
				match(FOREIGN);
				setState(139);
				match(KEY);
				setState(140);
				indexColumnNames();
				setState(141);
				referenceDefinition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IndexColumnNamesContext extends ParserRuleContext {
		public TerminalNode LEFT_PAREN() { return getToken(SemanticLWHParser.LEFT_PAREN, 0); }
		public List<FullColumnNameContext> fullColumnName() {
			return getRuleContexts(FullColumnNameContext.class);
		}
		public FullColumnNameContext fullColumnName(int i) {
			return getRuleContext(FullColumnNameContext.class,i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SemanticLWHParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public IndexColumnNamesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_indexColumnNames; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterIndexColumnNames(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitIndexColumnNames(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitIndexColumnNames(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IndexColumnNamesContext indexColumnNames() throws RecognitionException {
		IndexColumnNamesContext _localctx = new IndexColumnNamesContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_indexColumnNames);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(145);
			match(LEFT_PAREN);
			setState(146);
			fullColumnName();
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(147);
				match(COMMA);
				setState(148);
				fullColumnName();
				}
				}
				setState(153);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(154);
			match(RIGHT_PAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceDefinitionContext extends ParserRuleContext {
		public TerminalNode REFERENCES() { return getToken(SemanticLWHParser.REFERENCES, 0); }
		public MultipartIdentifierContext multipartIdentifier() {
			return getRuleContext(MultipartIdentifierContext.class,0);
		}
		public IndexColumnNamesContext indexColumnNames() {
			return getRuleContext(IndexColumnNamesContext.class,0);
		}
		public ReferenceActionContext referenceAction() {
			return getRuleContext(ReferenceActionContext.class,0);
		}
		public ReferenceDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceDefinition; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterReferenceDefinition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitReferenceDefinition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitReferenceDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceDefinitionContext referenceDefinition() throws RecognitionException {
		ReferenceDefinitionContext _localctx = new ReferenceDefinitionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_referenceDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			match(REFERENCES);
			setState(157);
			multipartIdentifier();
			setState(158);
			indexColumnNames();
			setState(160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ON) {
				{
				setState(159);
				referenceAction();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceActionContext extends ParserRuleContext {
		public ReferenceControlTypeContext onDelete;
		public ReferenceControlTypeContext onUpdate;
		public List<TerminalNode> ON() { return getTokens(SemanticLWHParser.ON); }
		public TerminalNode ON(int i) {
			return getToken(SemanticLWHParser.ON, i);
		}
		public TerminalNode DELETE() { return getToken(SemanticLWHParser.DELETE, 0); }
		public List<ReferenceControlTypeContext> referenceControlType() {
			return getRuleContexts(ReferenceControlTypeContext.class);
		}
		public ReferenceControlTypeContext referenceControlType(int i) {
			return getRuleContext(ReferenceControlTypeContext.class,i);
		}
		public TerminalNode UPDATE() { return getToken(SemanticLWHParser.UPDATE, 0); }
		public ReferenceActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterReferenceAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitReferenceAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitReferenceAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceActionContext referenceAction() throws RecognitionException {
		ReferenceActionContext _localctx = new ReferenceActionContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_referenceAction);
		int _la;
		try {
			setState(178);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				match(ON);
				setState(163);
				match(DELETE);
				setState(164);
				((ReferenceActionContext)_localctx).onDelete = referenceControlType();
				setState(168);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(165);
					match(ON);
					setState(166);
					match(UPDATE);
					setState(167);
					((ReferenceActionContext)_localctx).onUpdate = referenceControlType();
					}
				}

				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(170);
				match(ON);
				setState(171);
				match(UPDATE);
				setState(172);
				((ReferenceActionContext)_localctx).onUpdate = referenceControlType();
				setState(176);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ON) {
					{
					setState(173);
					match(ON);
					setState(174);
					match(DELETE);
					setState(175);
					((ReferenceActionContext)_localctx).onDelete = referenceControlType();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ReferenceControlTypeContext extends ParserRuleContext {
		public TerminalNode RESTRICT() { return getToken(SemanticLWHParser.RESTRICT, 0); }
		public TerminalNode CASCADE() { return getToken(SemanticLWHParser.CASCADE, 0); }
		public TerminalNode SET() { return getToken(SemanticLWHParser.SET, 0); }
		public TerminalNode NULL() { return getToken(SemanticLWHParser.NULL, 0); }
		public TerminalNode NO() { return getToken(SemanticLWHParser.NO, 0); }
		public TerminalNode ACTION() { return getToken(SemanticLWHParser.ACTION, 0); }
		public TerminalNode DEFAULT() { return getToken(SemanticLWHParser.DEFAULT, 0); }
		public ReferenceControlTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_referenceControlType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterReferenceControlType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitReferenceControlType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitReferenceControlType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ReferenceControlTypeContext referenceControlType() throws RecognitionException {
		ReferenceControlTypeContext _localctx = new ReferenceControlTypeContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_referenceControlType);
		try {
			setState(188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(180);
				match(RESTRICT);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(181);
				match(CASCADE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(182);
				match(SET);
				setState(183);
				match(NULL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(184);
				match(NO);
				setState(185);
				match(ACTION);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(186);
				match(SET);
				setState(187);
				match(DEFAULT);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FullColumnNameContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext colName;
		public ErrorCapturingIdentifierContext errorCapturingIdentifier() {
			return getRuleContext(ErrorCapturingIdentifierContext.class,0);
		}
		public FullColumnNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullColumnName; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterFullColumnName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitFullColumnName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitFullColumnName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullColumnNameContext fullColumnName() throws RecognitionException {
		FullColumnNameContext _localctx = new FullColumnNameContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fullColumnName);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(190);
			((FullColumnNameContext)_localctx).colName = errorCapturingIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingIdentifierContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() {
			return getRuleContext(ErrorCapturingIdentifierExtraContext.class,0);
		}
		public ErrorCapturingIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterErrorCapturingIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitErrorCapturingIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitErrorCapturingIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierContext errorCapturingIdentifier() throws RecognitionException {
		ErrorCapturingIdentifierContext _localctx = new ErrorCapturingIdentifierContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_errorCapturingIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			identifier();
			setState(193);
			errorCapturingIdentifierExtra();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ErrorCapturingIdentifierExtraContext extends ParserRuleContext {
		public ErrorCapturingIdentifierExtraContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_errorCapturingIdentifierExtra; }
	 
		public ErrorCapturingIdentifierExtraContext() { }
		public void copyFrom(ErrorCapturingIdentifierExtraContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ErrorIdentContext extends ErrorCapturingIdentifierExtraContext {
		public List<TerminalNode> MINUS() { return getTokens(SemanticLWHParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(SemanticLWHParser.MINUS, i);
		}
		public List<IdentifierContext> identifier() {
			return getRuleContexts(IdentifierContext.class);
		}
		public IdentifierContext identifier(int i) {
			return getRuleContext(IdentifierContext.class,i);
		}
		public ErrorIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterErrorIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitErrorIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitErrorIdent(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class RealIdentContext extends ErrorCapturingIdentifierExtraContext {
		public RealIdentContext(ErrorCapturingIdentifierExtraContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterRealIdent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitRealIdent(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitRealIdent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ErrorCapturingIdentifierExtraContext errorCapturingIdentifierExtra() throws RecognitionException {
		ErrorCapturingIdentifierExtraContext _localctx = new ErrorCapturingIdentifierExtraContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_errorCapturingIdentifierExtra);
		int _la;
		try {
			setState(202);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case MINUS:
				_localctx = new ErrorIdentContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(197); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(195);
					match(MINUS);
					setState(196);
					identifier();
					}
					}
					setState(199); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==MINUS );
				}
				break;
			case LEFT_PAREN:
			case RIGHT_PAREN:
			case COMMA:
			case DOT:
			case ADD:
			case AFTER:
			case ALTER:
			case ANALYZE:
			case ANTI:
			case ARCHIVE:
			case ARRAY:
			case ASC:
			case AT:
			case BETWEEN:
			case BUCKET:
			case BUCKETS:
			case BY:
			case CACHE:
			case CASCADE:
			case CATALOG:
			case CATALOGS:
			case CHANGE:
			case CLEAR:
			case CLUSTER:
			case CLUSTERED:
			case CODEGEN:
			case COLLECTION:
			case COLUMNS:
			case COMMENT:
			case COMMIT:
			case COMPACT:
			case COMPACTIONS:
			case COMPUTE:
			case CONCATENATE:
			case COST:
			case CUBE:
			case CURRENT:
			case DAY:
			case DAYOFYEAR:
			case DATA:
			case DATABASE:
			case DATABASES:
			case DATEADD:
			case DATEDIFF:
			case DBPROPERTIES:
			case DEFINED:
			case DELETE:
			case DELIMITED:
			case DESC:
			case DESCRIBE:
			case DFS:
			case DIRECTORIES:
			case DIRECTORY:
			case DISTRIBUTE:
			case DIV:
			case DROP:
			case ESCAPED:
			case EXCHANGE:
			case EXISTS:
			case EXPLAIN:
			case EXPORT:
			case EXTENDED:
			case EXTERNAL:
			case EXTRACT:
			case FIELDS:
			case FILEFORMAT:
			case FIRST:
			case FOLLOWING:
			case FORMAT:
			case FORMATTED:
			case FUNCTION:
			case FUNCTIONS:
			case GLOBAL:
			case GROUPING:
			case HOUR:
			case IF:
			case IGNORE:
			case IMPORT:
			case INDEX:
			case INDEXES:
			case INPATH:
			case INPUTFORMAT:
			case INSERT:
			case INTERVAL:
			case ITEMS:
			case KEYS:
			case LAST:
			case LAZY:
			case LIKE:
			case ILIKE:
			case LIMIT:
			case LINES:
			case LIST:
			case LOAD:
			case LOCAL:
			case LOCATION:
			case LOCK:
			case LOCKS:
			case LOGICAL:
			case MACRO:
			case MAP:
			case MATCHED:
			case MERGE:
			case MICROSECOND:
			case MILLISECOND:
			case MINUTE:
			case MONTH:
			case MSCK:
			case NAMESPACE:
			case NAMESPACES:
			case NO:
			case NULLS:
			case OF:
			case OPTION:
			case OPTIONS:
			case OUT:
			case OUTPUTFORMAT:
			case OVER:
			case OVERLAY:
			case OVERWRITE:
			case PARTITION:
			case PARTITIONED:
			case PARTITIONS:
			case PERCENTLIT:
			case PIVOT:
			case PLACING:
			case POSITION:
			case PRECEDING:
			case PRINCIPALS:
			case PROPERTIES:
			case PURGE:
			case QUARTER:
			case QUERY:
			case RANGE:
			case RECORDREADER:
			case RECORDWRITER:
			case RECOVER:
			case REDUCE:
			case REFRESH:
			case RENAME:
			case REPAIR:
			case REPEATABLE:
			case REPLACE:
			case RESET:
			case RESPECT:
			case RESTRICT:
			case REVOKE:
			case RLIKE:
			case ROLE:
			case ROLES:
			case ROLLBACK:
			case ROLLUP:
			case ROW:
			case ROWS:
			case SECOND:
			case SCHEMA:
			case SCHEMAS:
			case SEMI:
			case SEPARATED:
			case SERDE:
			case SERDEPROPERTIES:
			case SET:
			case SETMINUS:
			case SETS:
			case SHOW:
			case SKEWED:
			case SORT:
			case SORTED:
			case START:
			case STATISTICS:
			case STORED:
			case STRATIFY:
			case STRUCT:
			case SUBSTR:
			case SUBSTRING:
			case SYNC:
			case SYSTEM_TIME:
			case SYSTEM_VERSION:
			case TABLES:
			case TABLESAMPLE:
			case TBLPROPERTIES:
			case TEMPORARY:
			case TERMINATED:
			case TIMESTAMP:
			case TIMESTAMPADD:
			case TIMESTAMPDIFF:
			case TOUCH:
			case TRANSACTION:
			case TRANSACTIONS:
			case TRANSFORM:
			case TRIM:
			case TRUE:
			case TRUNCATE:
			case TRY_CAST:
			case TYPE:
			case UNARCHIVE:
			case UNBOUNDED:
			case UNCACHE:
			case UNLOCK:
			case UNSET:
			case UPDATE:
			case USE:
			case VALUES:
			case VERSION:
			case VIEW:
			case VIEWS:
			case WEEK:
			case WINDOW:
			case YEAR:
			case ZONE:
			case IDENTIFIER:
			case BACKQUOTED_IDENTIFIER:
				_localctx = new RealIdentContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MultipartIdentifierContext extends ParserRuleContext {
		public ErrorCapturingIdentifierContext errorCapturingIdentifier;
		public List<ErrorCapturingIdentifierContext> parts = new ArrayList<ErrorCapturingIdentifierContext>();
		public List<ErrorCapturingIdentifierContext> errorCapturingIdentifier() {
			return getRuleContexts(ErrorCapturingIdentifierContext.class);
		}
		public ErrorCapturingIdentifierContext errorCapturingIdentifier(int i) {
			return getRuleContext(ErrorCapturingIdentifierContext.class,i);
		}
		public List<TerminalNode> DOT() { return getTokens(SemanticLWHParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(SemanticLWHParser.DOT, i);
		}
		public MultipartIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_multipartIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterMultipartIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitMultipartIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitMultipartIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MultipartIdentifierContext multipartIdentifier() throws RecognitionException {
		MultipartIdentifierContext _localctx = new MultipartIdentifierContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_multipartIdentifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
			((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
			((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
			setState(209);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(205);
				match(DOT);
				setState(206);
				((MultipartIdentifierContext)_localctx).errorCapturingIdentifier = errorCapturingIdentifier();
				((MultipartIdentifierContext)_localctx).parts.add(((MultipartIdentifierContext)_localctx).errorCapturingIdentifier);
				}
				}
				setState(211);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IdentifierContext extends ParserRuleContext {
		public StrictIdentifierContext strictIdentifier() {
			return getRuleContext(StrictIdentifierContext.class,0);
		}
		public IdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_identifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdentifierContext identifier() throws RecognitionException {
		IdentifierContext _localctx = new IdentifierContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_identifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
			strictIdentifier();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StrictIdentifierContext extends ParserRuleContext {
		public StrictIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_strictIdentifier; }
	 
		public StrictIdentifierContext() { }
		public void copyFrom(StrictIdentifierContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class QuotedIdentifierAlternativeContext extends StrictIdentifierContext {
		public QuotedIdentifierContext quotedIdentifier() {
			return getRuleContext(QuotedIdentifierContext.class,0);
		}
		public QuotedIdentifierAlternativeContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterQuotedIdentifierAlternative(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitQuotedIdentifierAlternative(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitQuotedIdentifierAlternative(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class UnquotedIdentifierContext extends StrictIdentifierContext {
		public TerminalNode IDENTIFIER() { return getToken(SemanticLWHParser.IDENTIFIER, 0); }
		public AnsiNonReservedContext ansiNonReserved() {
			return getRuleContext(AnsiNonReservedContext.class,0);
		}
		public UnquotedIdentifierContext(StrictIdentifierContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterUnquotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitUnquotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitUnquotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StrictIdentifierContext strictIdentifier() throws RecognitionException {
		StrictIdentifierContext _localctx = new StrictIdentifierContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_strictIdentifier);
		try {
			setState(217);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(214);
				match(IDENTIFIER);
				}
				break;
			case BACKQUOTED_IDENTIFIER:
				_localctx = new QuotedIdentifierAlternativeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(215);
				quotedIdentifier();
				}
				break;
			case ADD:
			case AFTER:
			case ALTER:
			case ANALYZE:
			case ANTI:
			case ARCHIVE:
			case ARRAY:
			case ASC:
			case AT:
			case BETWEEN:
			case BUCKET:
			case BUCKETS:
			case BY:
			case CACHE:
			case CASCADE:
			case CATALOG:
			case CATALOGS:
			case CHANGE:
			case CLEAR:
			case CLUSTER:
			case CLUSTERED:
			case CODEGEN:
			case COLLECTION:
			case COLUMNS:
			case COMMENT:
			case COMMIT:
			case COMPACT:
			case COMPACTIONS:
			case COMPUTE:
			case CONCATENATE:
			case COST:
			case CUBE:
			case CURRENT:
			case DAY:
			case DAYOFYEAR:
			case DATA:
			case DATABASE:
			case DATABASES:
			case DATEADD:
			case DATEDIFF:
			case DBPROPERTIES:
			case DEFINED:
			case DELETE:
			case DELIMITED:
			case DESC:
			case DESCRIBE:
			case DFS:
			case DIRECTORIES:
			case DIRECTORY:
			case DISTRIBUTE:
			case DIV:
			case DROP:
			case ESCAPED:
			case EXCHANGE:
			case EXISTS:
			case EXPLAIN:
			case EXPORT:
			case EXTENDED:
			case EXTERNAL:
			case EXTRACT:
			case FIELDS:
			case FILEFORMAT:
			case FIRST:
			case FOLLOWING:
			case FORMAT:
			case FORMATTED:
			case FUNCTION:
			case FUNCTIONS:
			case GLOBAL:
			case GROUPING:
			case HOUR:
			case IF:
			case IGNORE:
			case IMPORT:
			case INDEX:
			case INDEXES:
			case INPATH:
			case INPUTFORMAT:
			case INSERT:
			case INTERVAL:
			case ITEMS:
			case KEYS:
			case LAST:
			case LAZY:
			case LIKE:
			case ILIKE:
			case LIMIT:
			case LINES:
			case LIST:
			case LOAD:
			case LOCAL:
			case LOCATION:
			case LOCK:
			case LOCKS:
			case LOGICAL:
			case MACRO:
			case MAP:
			case MATCHED:
			case MERGE:
			case MICROSECOND:
			case MILLISECOND:
			case MINUTE:
			case MONTH:
			case MSCK:
			case NAMESPACE:
			case NAMESPACES:
			case NO:
			case NULLS:
			case OF:
			case OPTION:
			case OPTIONS:
			case OUT:
			case OUTPUTFORMAT:
			case OVER:
			case OVERLAY:
			case OVERWRITE:
			case PARTITION:
			case PARTITIONED:
			case PARTITIONS:
			case PERCENTLIT:
			case PIVOT:
			case PLACING:
			case POSITION:
			case PRECEDING:
			case PRINCIPALS:
			case PROPERTIES:
			case PURGE:
			case QUARTER:
			case QUERY:
			case RANGE:
			case RECORDREADER:
			case RECORDWRITER:
			case RECOVER:
			case REDUCE:
			case REFRESH:
			case RENAME:
			case REPAIR:
			case REPEATABLE:
			case REPLACE:
			case RESET:
			case RESPECT:
			case RESTRICT:
			case REVOKE:
			case RLIKE:
			case ROLE:
			case ROLES:
			case ROLLBACK:
			case ROLLUP:
			case ROW:
			case ROWS:
			case SECOND:
			case SCHEMA:
			case SCHEMAS:
			case SEMI:
			case SEPARATED:
			case SERDE:
			case SERDEPROPERTIES:
			case SET:
			case SETMINUS:
			case SETS:
			case SHOW:
			case SKEWED:
			case SORT:
			case SORTED:
			case START:
			case STATISTICS:
			case STORED:
			case STRATIFY:
			case STRUCT:
			case SUBSTR:
			case SUBSTRING:
			case SYNC:
			case SYSTEM_TIME:
			case SYSTEM_VERSION:
			case TABLES:
			case TABLESAMPLE:
			case TBLPROPERTIES:
			case TEMPORARY:
			case TERMINATED:
			case TIMESTAMP:
			case TIMESTAMPADD:
			case TIMESTAMPDIFF:
			case TOUCH:
			case TRANSACTION:
			case TRANSACTIONS:
			case TRANSFORM:
			case TRIM:
			case TRUE:
			case TRUNCATE:
			case TRY_CAST:
			case TYPE:
			case UNARCHIVE:
			case UNBOUNDED:
			case UNCACHE:
			case UNLOCK:
			case UNSET:
			case UPDATE:
			case USE:
			case VALUES:
			case VERSION:
			case VIEW:
			case VIEWS:
			case WEEK:
			case WINDOW:
			case YEAR:
			case ZONE:
				_localctx = new UnquotedIdentifierContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(216);
				ansiNonReserved();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuotedIdentifierContext extends ParserRuleContext {
		public TerminalNode BACKQUOTED_IDENTIFIER() { return getToken(SemanticLWHParser.BACKQUOTED_IDENTIFIER, 0); }
		public QuotedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterQuotedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitQuotedIdentifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitQuotedIdentifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QuotedIdentifierContext quotedIdentifier() throws RecognitionException {
		QuotedIdentifierContext _localctx = new QuotedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_quotedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			match(BACKQUOTED_IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DataTypeContext extends ParserRuleContext {
		public DataTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_dataType; }
	 
		public DataTypeContext() { }
		public void copyFrom(DataTypeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ComplexDataTypeContext extends DataTypeContext {
		public Token complex;
		public TerminalNode LT() { return getToken(SemanticLWHParser.LT, 0); }
		public List<DataTypeContext> dataType() {
			return getRuleContexts(DataTypeContext.class);
		}
		public DataTypeContext dataType(int i) {
			return getRuleContext(DataTypeContext.class,i);
		}
		public TerminalNode GT() { return getToken(SemanticLWHParser.GT, 0); }
		public TerminalNode ARRAY() { return getToken(SemanticLWHParser.ARRAY, 0); }
		public TerminalNode COMMA() { return getToken(SemanticLWHParser.COMMA, 0); }
		public TerminalNode MAP() { return getToken(SemanticLWHParser.MAP, 0); }
		public TerminalNode STRUCT() { return getToken(SemanticLWHParser.STRUCT, 0); }
		public TerminalNode NEQ() { return getToken(SemanticLWHParser.NEQ, 0); }
		public ComplexColTypeListContext complexColTypeList() {
			return getRuleContext(ComplexColTypeListContext.class,0);
		}
		public ComplexDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterComplexDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitComplexDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitComplexDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class YearMonthIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(SemanticLWHParser.INTERVAL, 0); }
		public TerminalNode YEAR() { return getToken(SemanticLWHParser.YEAR, 0); }
		public List<TerminalNode> MONTH() { return getTokens(SemanticLWHParser.MONTH); }
		public TerminalNode MONTH(int i) {
			return getToken(SemanticLWHParser.MONTH, i);
		}
		public TerminalNode TO() { return getToken(SemanticLWHParser.TO, 0); }
		public YearMonthIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterYearMonthIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitYearMonthIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitYearMonthIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class DayTimeIntervalDataTypeContext extends DataTypeContext {
		public Token from;
		public Token to;
		public TerminalNode INTERVAL() { return getToken(SemanticLWHParser.INTERVAL, 0); }
		public TerminalNode DAY() { return getToken(SemanticLWHParser.DAY, 0); }
		public List<TerminalNode> HOUR() { return getTokens(SemanticLWHParser.HOUR); }
		public TerminalNode HOUR(int i) {
			return getToken(SemanticLWHParser.HOUR, i);
		}
		public List<TerminalNode> MINUTE() { return getTokens(SemanticLWHParser.MINUTE); }
		public TerminalNode MINUTE(int i) {
			return getToken(SemanticLWHParser.MINUTE, i);
		}
		public List<TerminalNode> SECOND() { return getTokens(SemanticLWHParser.SECOND); }
		public TerminalNode SECOND(int i) {
			return getToken(SemanticLWHParser.SECOND, i);
		}
		public TerminalNode TO() { return getToken(SemanticLWHParser.TO, 0); }
		public DayTimeIntervalDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterDayTimeIntervalDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitDayTimeIntervalDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitDayTimeIntervalDataType(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimitiveDataTypeContext extends DataTypeContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public TerminalNode LEFT_PAREN() { return getToken(SemanticLWHParser.LEFT_PAREN, 0); }
		public List<TerminalNode> INTEGER_VALUE() { return getTokens(SemanticLWHParser.INTEGER_VALUE); }
		public TerminalNode INTEGER_VALUE(int i) {
			return getToken(SemanticLWHParser.INTEGER_VALUE, i);
		}
		public TerminalNode RIGHT_PAREN() { return getToken(SemanticLWHParser.RIGHT_PAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public PrimitiveDataTypeContext(DataTypeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterPrimitiveDataType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitPrimitiveDataType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitPrimitiveDataType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DataTypeContext dataType() throws RecognitionException {
		DataTypeContext _localctx = new DataTypeContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_dataType);
		int _la;
		try {
			setState(267);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(221);
				((ComplexDataTypeContext)_localctx).complex = match(ARRAY);
				setState(222);
				match(LT);
				setState(223);
				dataType();
				setState(224);
				match(GT);
				}
				break;
			case 2:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(226);
				((ComplexDataTypeContext)_localctx).complex = match(MAP);
				setState(227);
				match(LT);
				setState(228);
				dataType();
				setState(229);
				match(COMMA);
				setState(230);
				dataType();
				setState(231);
				match(GT);
				}
				break;
			case 3:
				_localctx = new ComplexDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(233);
				((ComplexDataTypeContext)_localctx).complex = match(STRUCT);
				setState(240);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case LT:
					{
					setState(234);
					match(LT);
					setState(236);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (((((_la - 9)) & ~0x3f) == 0 && ((1L << (_la - 9)) & ((1L << (ADD - 9)) | (1L << (AFTER - 9)) | (1L << (ALTER - 9)) | (1L << (ANALYZE - 9)) | (1L << (ANTI - 9)) | (1L << (ARCHIVE - 9)) | (1L << (ARRAY - 9)) | (1L << (ASC - 9)) | (1L << (AT - 9)) | (1L << (BETWEEN - 9)) | (1L << (BUCKET - 9)) | (1L << (BUCKETS - 9)) | (1L << (BY - 9)) | (1L << (CACHE - 9)) | (1L << (CASCADE - 9)) | (1L << (CATALOG - 9)) | (1L << (CATALOGS - 9)) | (1L << (CHANGE - 9)) | (1L << (CLEAR - 9)) | (1L << (CLUSTER - 9)) | (1L << (CLUSTERED - 9)) | (1L << (CODEGEN - 9)) | (1L << (COLLECTION - 9)) | (1L << (COLUMNS - 9)) | (1L << (COMMENT - 9)) | (1L << (COMMIT - 9)) | (1L << (COMPACT - 9)) | (1L << (COMPACTIONS - 9)) | (1L << (COMPUTE - 9)) | (1L << (CONCATENATE - 9)) | (1L << (COST - 9)) | (1L << (CUBE - 9)) | (1L << (CURRENT - 9)) | (1L << (DAY - 9)) | (1L << (DAYOFYEAR - 9)) | (1L << (DATA - 9)) | (1L << (DATABASE - 9)) | (1L << (DATABASES - 9)) | (1L << (DATEADD - 9)) | (1L << (DATEDIFF - 9)) | (1L << (DBPROPERTIES - 9)) | (1L << (DEFINED - 9)) | (1L << (DELETE - 9)) | (1L << (DELIMITED - 9)) | (1L << (DESC - 9)))) != 0) || ((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & ((1L << (DESCRIBE - 73)) | (1L << (DFS - 73)) | (1L << (DIRECTORIES - 73)) | (1L << (DIRECTORY - 73)) | (1L << (DISTRIBUTE - 73)) | (1L << (DIV - 73)) | (1L << (DROP - 73)) | (1L << (ESCAPED - 73)) | (1L << (EXCHANGE - 73)) | (1L << (EXISTS - 73)) | (1L << (EXPLAIN - 73)) | (1L << (EXPORT - 73)) | (1L << (EXTENDED - 73)) | (1L << (EXTERNAL - 73)) | (1L << (EXTRACT - 73)) | (1L << (FIELDS - 73)) | (1L << (FILEFORMAT - 73)) | (1L << (FIRST - 73)) | (1L << (FOLLOWING - 73)) | (1L << (FORMAT - 73)) | (1L << (FORMATTED - 73)) | (1L << (FUNCTION - 73)) | (1L << (FUNCTIONS - 73)) | (1L << (GLOBAL - 73)) | (1L << (GROUPING - 73)) | (1L << (HOUR - 73)) | (1L << (IF - 73)) | (1L << (IGNORE - 73)) | (1L << (IMPORT - 73)) | (1L << (INDEX - 73)) | (1L << (INDEXES - 73)) | (1L << (INPATH - 73)) | (1L << (INPUTFORMAT - 73)) | (1L << (INSERT - 73)) | (1L << (INTERVAL - 73)) | (1L << (ITEMS - 73)) | (1L << (KEYS - 73)) | (1L << (LAST - 73)) | (1L << (LAZY - 73)))) != 0) || ((((_la - 137)) & ~0x3f) == 0 && ((1L << (_la - 137)) & ((1L << (LIKE - 137)) | (1L << (ILIKE - 137)) | (1L << (LIMIT - 137)) | (1L << (LINES - 137)) | (1L << (LIST - 137)) | (1L << (LOAD - 137)) | (1L << (LOCAL - 137)) | (1L << (LOCATION - 137)) | (1L << (LOCK - 137)) | (1L << (LOCKS - 137)) | (1L << (LOGICAL - 137)) | (1L << (MACRO - 137)) | (1L << (MAP - 137)) | (1L << (MATCHED - 137)) | (1L << (MERGE - 137)) | (1L << (MICROSECOND - 137)) | (1L << (MILLISECOND - 137)) | (1L << (MINUTE - 137)) | (1L << (MONTH - 137)) | (1L << (MSCK - 137)) | (1L << (NAMESPACE - 137)) | (1L << (NAMESPACES - 137)) | (1L << (NO - 137)) | (1L << (NULLS - 137)) | (1L << (OF - 137)) | (1L << (OPTION - 137)) | (1L << (OPTIONS - 137)) | (1L << (OUT - 137)) | (1L << (OUTPUTFORMAT - 137)) | (1L << (OVER - 137)) | (1L << (OVERLAY - 137)) | (1L << (OVERWRITE - 137)) | (1L << (PARTITION - 137)) | (1L << (PARTITIONED - 137)) | (1L << (PARTITIONS - 137)) | (1L << (PERCENTLIT - 137)) | (1L << (PIVOT - 137)) | (1L << (PLACING - 137)) | (1L << (POSITION - 137)) | (1L << (PRECEDING - 137)) | (1L << (PRINCIPALS - 137)) | (1L << (PROPERTIES - 137)) | (1L << (PURGE - 137)) | (1L << (QUARTER - 137)) | (1L << (QUERY - 137)) | (1L << (RANGE - 137)) | (1L << (RECORDREADER - 137)) | (1L << (RECORDWRITER - 137)) | (1L << (RECOVER - 137)) | (1L << (REDUCE - 137)) | (1L << (REFRESH - 137)))) != 0) || ((((_la - 201)) & ~0x3f) == 0 && ((1L << (_la - 201)) & ((1L << (RENAME - 201)) | (1L << (REPAIR - 201)) | (1L << (REPEATABLE - 201)) | (1L << (REPLACE - 201)) | (1L << (RESET - 201)) | (1L << (RESPECT - 201)) | (1L << (RESTRICT - 201)) | (1L << (REVOKE - 201)) | (1L << (RLIKE - 201)) | (1L << (ROLE - 201)) | (1L << (ROLES - 201)) | (1L << (ROLLBACK - 201)) | (1L << (ROLLUP - 201)) | (1L << (ROW - 201)) | (1L << (ROWS - 201)) | (1L << (SECOND - 201)) | (1L << (SCHEMA - 201)) | (1L << (SCHEMAS - 201)) | (1L << (SEMI - 201)) | (1L << (SEPARATED - 201)) | (1L << (SERDE - 201)) | (1L << (SERDEPROPERTIES - 201)) | (1L << (SET - 201)) | (1L << (SETMINUS - 201)) | (1L << (SETS - 201)) | (1L << (SHOW - 201)) | (1L << (SKEWED - 201)) | (1L << (SORT - 201)) | (1L << (SORTED - 201)) | (1L << (START - 201)) | (1L << (STATISTICS - 201)) | (1L << (STORED - 201)) | (1L << (STRATIFY - 201)) | (1L << (STRUCT - 201)) | (1L << (SUBSTR - 201)) | (1L << (SUBSTRING - 201)) | (1L << (SYNC - 201)) | (1L << (SYSTEM_TIME - 201)) | (1L << (SYSTEM_VERSION - 201)) | (1L << (TABLES - 201)) | (1L << (TABLESAMPLE - 201)) | (1L << (TBLPROPERTIES - 201)) | (1L << (TEMPORARY - 201)) | (1L << (TERMINATED - 201)) | (1L << (TIMESTAMP - 201)) | (1L << (TIMESTAMPADD - 201)) | (1L << (TIMESTAMPDIFF - 201)) | (1L << (TOUCH - 201)) | (1L << (TRANSACTION - 201)) | (1L << (TRANSACTIONS - 201)) | (1L << (TRANSFORM - 201)) | (1L << (TRIM - 201)) | (1L << (TRUE - 201)) | (1L << (TRUNCATE - 201)) | (1L << (TRY_CAST - 201)) | (1L << (TYPE - 201)))) != 0) || ((((_la - 265)) & ~0x3f) == 0 && ((1L << (_la - 265)) & ((1L << (UNARCHIVE - 265)) | (1L << (UNBOUNDED - 265)) | (1L << (UNCACHE - 265)) | (1L << (UNLOCK - 265)) | (1L << (UNSET - 265)) | (1L << (UPDATE - 265)) | (1L << (USE - 265)) | (1L << (VALUES - 265)) | (1L << (VERSION - 265)) | (1L << (VIEW - 265)) | (1L << (VIEWS - 265)) | (1L << (WEEK - 265)) | (1L << (WINDOW - 265)) | (1L << (YEAR - 265)) | (1L << (ZONE - 265)) | (1L << (IDENTIFIER - 265)) | (1L << (BACKQUOTED_IDENTIFIER - 265)))) != 0)) {
						{
						setState(235);
						complexColTypeList();
						}
					}

					setState(238);
					match(GT);
					}
					break;
				case NEQ:
					{
					setState(239);
					match(NEQ);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 4:
				_localctx = new YearMonthIntervalDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(242);
				match(INTERVAL);
				setState(243);
				((YearMonthIntervalDataTypeContext)_localctx).from = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==MONTH || _la==YEAR) ) {
					((YearMonthIntervalDataTypeContext)_localctx).from = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TO) {
					{
					setState(244);
					match(TO);
					setState(245);
					((YearMonthIntervalDataTypeContext)_localctx).to = match(MONTH);
					}
				}

				}
				break;
			case 5:
				_localctx = new DayTimeIntervalDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(248);
				match(INTERVAL);
				setState(249);
				((DayTimeIntervalDataTypeContext)_localctx).from = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==DAY || _la==HOUR || _la==MINUTE || _la==SECOND) ) {
					((DayTimeIntervalDataTypeContext)_localctx).from = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(252);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==TO) {
					{
					setState(250);
					match(TO);
					setState(251);
					((DayTimeIntervalDataTypeContext)_localctx).to = _input.LT(1);
					_la = _input.LA(1);
					if ( !(_la==HOUR || _la==MINUTE || _la==SECOND) ) {
						((DayTimeIntervalDataTypeContext)_localctx).to = (Token)_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				break;
			case 6:
				_localctx = new PrimitiveDataTypeContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(254);
				identifier();
				setState(265);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==LEFT_PAREN) {
					{
					setState(255);
					match(LEFT_PAREN);
					setState(256);
					match(INTEGER_VALUE);
					setState(261);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(257);
						match(COMMA);
						setState(258);
						match(INTEGER_VALUE);
						}
						}
						setState(263);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(264);
					match(RIGHT_PAREN);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComplexColTypeListContext extends ParserRuleContext {
		public List<ComplexColTypeContext> complexColType() {
			return getRuleContexts(ComplexColTypeContext.class);
		}
		public ComplexColTypeContext complexColType(int i) {
			return getRuleContext(ComplexColTypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(SemanticLWHParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(SemanticLWHParser.COMMA, i);
		}
		public ComplexColTypeListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColTypeList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterComplexColTypeList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitComplexColTypeList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitComplexColTypeList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeListContext complexColTypeList() throws RecognitionException {
		ComplexColTypeListContext _localctx = new ComplexColTypeListContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_complexColTypeList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(269);
			complexColType();
			setState(274);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(270);
				match(COMMA);
				setState(271);
				complexColType();
				}
				}
				setState(276);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ComplexColTypeContext extends ParserRuleContext {
		public IdentifierContext identifier() {
			return getRuleContext(IdentifierContext.class,0);
		}
		public DataTypeContext dataType() {
			return getRuleContext(DataTypeContext.class,0);
		}
		public TerminalNode COLON() { return getToken(SemanticLWHParser.COLON, 0); }
		public TerminalNode NOT() { return getToken(SemanticLWHParser.NOT, 0); }
		public TerminalNode NULL() { return getToken(SemanticLWHParser.NULL, 0); }
		public ComplexColTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_complexColType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterComplexColType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitComplexColType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitComplexColType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ComplexColTypeContext complexColType() throws RecognitionException {
		ComplexColTypeContext _localctx = new ComplexColTypeContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_complexColType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(277);
			identifier();
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(278);
				match(COLON);
				}
			}

			setState(281);
			dataType();
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==NOT) {
				{
				setState(282);
				match(NOT);
				setState(283);
				match(NULL);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnsiNonReservedContext extends ParserRuleContext {
		public TerminalNode ADD() { return getToken(SemanticLWHParser.ADD, 0); }
		public TerminalNode AFTER() { return getToken(SemanticLWHParser.AFTER, 0); }
		public TerminalNode ALTER() { return getToken(SemanticLWHParser.ALTER, 0); }
		public TerminalNode ANALYZE() { return getToken(SemanticLWHParser.ANALYZE, 0); }
		public TerminalNode ANTI() { return getToken(SemanticLWHParser.ANTI, 0); }
		public TerminalNode ARCHIVE() { return getToken(SemanticLWHParser.ARCHIVE, 0); }
		public TerminalNode ARRAY() { return getToken(SemanticLWHParser.ARRAY, 0); }
		public TerminalNode ASC() { return getToken(SemanticLWHParser.ASC, 0); }
		public TerminalNode AT() { return getToken(SemanticLWHParser.AT, 0); }
		public TerminalNode BETWEEN() { return getToken(SemanticLWHParser.BETWEEN, 0); }
		public TerminalNode BUCKET() { return getToken(SemanticLWHParser.BUCKET, 0); }
		public TerminalNode BUCKETS() { return getToken(SemanticLWHParser.BUCKETS, 0); }
		public TerminalNode BY() { return getToken(SemanticLWHParser.BY, 0); }
		public TerminalNode CACHE() { return getToken(SemanticLWHParser.CACHE, 0); }
		public TerminalNode CASCADE() { return getToken(SemanticLWHParser.CASCADE, 0); }
		public TerminalNode CATALOG() { return getToken(SemanticLWHParser.CATALOG, 0); }
		public TerminalNode CATALOGS() { return getToken(SemanticLWHParser.CATALOGS, 0); }
		public TerminalNode CHANGE() { return getToken(SemanticLWHParser.CHANGE, 0); }
		public TerminalNode CLEAR() { return getToken(SemanticLWHParser.CLEAR, 0); }
		public TerminalNode CLUSTER() { return getToken(SemanticLWHParser.CLUSTER, 0); }
		public TerminalNode CLUSTERED() { return getToken(SemanticLWHParser.CLUSTERED, 0); }
		public TerminalNode CODEGEN() { return getToken(SemanticLWHParser.CODEGEN, 0); }
		public TerminalNode COLLECTION() { return getToken(SemanticLWHParser.COLLECTION, 0); }
		public TerminalNode COLUMNS() { return getToken(SemanticLWHParser.COLUMNS, 0); }
		public TerminalNode COMMENT() { return getToken(SemanticLWHParser.COMMENT, 0); }
		public TerminalNode COMMIT() { return getToken(SemanticLWHParser.COMMIT, 0); }
		public TerminalNode COMPACT() { return getToken(SemanticLWHParser.COMPACT, 0); }
		public TerminalNode COMPACTIONS() { return getToken(SemanticLWHParser.COMPACTIONS, 0); }
		public TerminalNode COMPUTE() { return getToken(SemanticLWHParser.COMPUTE, 0); }
		public TerminalNode CONCATENATE() { return getToken(SemanticLWHParser.CONCATENATE, 0); }
		public TerminalNode COST() { return getToken(SemanticLWHParser.COST, 0); }
		public TerminalNode CUBE() { return getToken(SemanticLWHParser.CUBE, 0); }
		public TerminalNode CURRENT() { return getToken(SemanticLWHParser.CURRENT, 0); }
		public TerminalNode DATA() { return getToken(SemanticLWHParser.DATA, 0); }
		public TerminalNode DATABASE() { return getToken(SemanticLWHParser.DATABASE, 0); }
		public TerminalNode DATABASES() { return getToken(SemanticLWHParser.DATABASES, 0); }
		public TerminalNode DATEADD() { return getToken(SemanticLWHParser.DATEADD, 0); }
		public TerminalNode DATEDIFF() { return getToken(SemanticLWHParser.DATEDIFF, 0); }
		public TerminalNode DAY() { return getToken(SemanticLWHParser.DAY, 0); }
		public TerminalNode DAYOFYEAR() { return getToken(SemanticLWHParser.DAYOFYEAR, 0); }
		public TerminalNode DBPROPERTIES() { return getToken(SemanticLWHParser.DBPROPERTIES, 0); }
		public TerminalNode DEFINED() { return getToken(SemanticLWHParser.DEFINED, 0); }
		public TerminalNode DELETE() { return getToken(SemanticLWHParser.DELETE, 0); }
		public TerminalNode DELIMITED() { return getToken(SemanticLWHParser.DELIMITED, 0); }
		public TerminalNode DESC() { return getToken(SemanticLWHParser.DESC, 0); }
		public TerminalNode DESCRIBE() { return getToken(SemanticLWHParser.DESCRIBE, 0); }
		public TerminalNode DFS() { return getToken(SemanticLWHParser.DFS, 0); }
		public TerminalNode DIRECTORIES() { return getToken(SemanticLWHParser.DIRECTORIES, 0); }
		public TerminalNode DIRECTORY() { return getToken(SemanticLWHParser.DIRECTORY, 0); }
		public TerminalNode DISTRIBUTE() { return getToken(SemanticLWHParser.DISTRIBUTE, 0); }
		public TerminalNode DIV() { return getToken(SemanticLWHParser.DIV, 0); }
		public TerminalNode DROP() { return getToken(SemanticLWHParser.DROP, 0); }
		public TerminalNode ESCAPED() { return getToken(SemanticLWHParser.ESCAPED, 0); }
		public TerminalNode EXCHANGE() { return getToken(SemanticLWHParser.EXCHANGE, 0); }
		public TerminalNode EXISTS() { return getToken(SemanticLWHParser.EXISTS, 0); }
		public TerminalNode EXPLAIN() { return getToken(SemanticLWHParser.EXPLAIN, 0); }
		public TerminalNode EXPORT() { return getToken(SemanticLWHParser.EXPORT, 0); }
		public TerminalNode EXTENDED() { return getToken(SemanticLWHParser.EXTENDED, 0); }
		public TerminalNode EXTERNAL() { return getToken(SemanticLWHParser.EXTERNAL, 0); }
		public TerminalNode EXTRACT() { return getToken(SemanticLWHParser.EXTRACT, 0); }
		public TerminalNode FIELDS() { return getToken(SemanticLWHParser.FIELDS, 0); }
		public TerminalNode FILEFORMAT() { return getToken(SemanticLWHParser.FILEFORMAT, 0); }
		public TerminalNode FIRST() { return getToken(SemanticLWHParser.FIRST, 0); }
		public TerminalNode FOLLOWING() { return getToken(SemanticLWHParser.FOLLOWING, 0); }
		public TerminalNode FORMAT() { return getToken(SemanticLWHParser.FORMAT, 0); }
		public TerminalNode FORMATTED() { return getToken(SemanticLWHParser.FORMATTED, 0); }
		public TerminalNode FUNCTION() { return getToken(SemanticLWHParser.FUNCTION, 0); }
		public TerminalNode FUNCTIONS() { return getToken(SemanticLWHParser.FUNCTIONS, 0); }
		public TerminalNode GLOBAL() { return getToken(SemanticLWHParser.GLOBAL, 0); }
		public TerminalNode GROUPING() { return getToken(SemanticLWHParser.GROUPING, 0); }
		public TerminalNode HOUR() { return getToken(SemanticLWHParser.HOUR, 0); }
		public TerminalNode IF() { return getToken(SemanticLWHParser.IF, 0); }
		public TerminalNode IGNORE() { return getToken(SemanticLWHParser.IGNORE, 0); }
		public TerminalNode IMPORT() { return getToken(SemanticLWHParser.IMPORT, 0); }
		public TerminalNode INDEX() { return getToken(SemanticLWHParser.INDEX, 0); }
		public TerminalNode INDEXES() { return getToken(SemanticLWHParser.INDEXES, 0); }
		public TerminalNode INPATH() { return getToken(SemanticLWHParser.INPATH, 0); }
		public TerminalNode INPUTFORMAT() { return getToken(SemanticLWHParser.INPUTFORMAT, 0); }
		public TerminalNode INSERT() { return getToken(SemanticLWHParser.INSERT, 0); }
		public TerminalNode INTERVAL() { return getToken(SemanticLWHParser.INTERVAL, 0); }
		public TerminalNode ITEMS() { return getToken(SemanticLWHParser.ITEMS, 0); }
		public TerminalNode KEYS() { return getToken(SemanticLWHParser.KEYS, 0); }
		public TerminalNode LAST() { return getToken(SemanticLWHParser.LAST, 0); }
		public TerminalNode LAZY() { return getToken(SemanticLWHParser.LAZY, 0); }
		public TerminalNode LIKE() { return getToken(SemanticLWHParser.LIKE, 0); }
		public TerminalNode ILIKE() { return getToken(SemanticLWHParser.ILIKE, 0); }
		public TerminalNode LIMIT() { return getToken(SemanticLWHParser.LIMIT, 0); }
		public TerminalNode LINES() { return getToken(SemanticLWHParser.LINES, 0); }
		public TerminalNode LIST() { return getToken(SemanticLWHParser.LIST, 0); }
		public TerminalNode LOAD() { return getToken(SemanticLWHParser.LOAD, 0); }
		public TerminalNode LOCAL() { return getToken(SemanticLWHParser.LOCAL, 0); }
		public TerminalNode LOCATION() { return getToken(SemanticLWHParser.LOCATION, 0); }
		public TerminalNode LOCK() { return getToken(SemanticLWHParser.LOCK, 0); }
		public TerminalNode LOCKS() { return getToken(SemanticLWHParser.LOCKS, 0); }
		public TerminalNode LOGICAL() { return getToken(SemanticLWHParser.LOGICAL, 0); }
		public TerminalNode MACRO() { return getToken(SemanticLWHParser.MACRO, 0); }
		public TerminalNode MAP() { return getToken(SemanticLWHParser.MAP, 0); }
		public TerminalNode MATCHED() { return getToken(SemanticLWHParser.MATCHED, 0); }
		public TerminalNode MERGE() { return getToken(SemanticLWHParser.MERGE, 0); }
		public TerminalNode MICROSECOND() { return getToken(SemanticLWHParser.MICROSECOND, 0); }
		public TerminalNode MILLISECOND() { return getToken(SemanticLWHParser.MILLISECOND, 0); }
		public TerminalNode MINUTE() { return getToken(SemanticLWHParser.MINUTE, 0); }
		public TerminalNode MONTH() { return getToken(SemanticLWHParser.MONTH, 0); }
		public TerminalNode MSCK() { return getToken(SemanticLWHParser.MSCK, 0); }
		public TerminalNode NAMESPACE() { return getToken(SemanticLWHParser.NAMESPACE, 0); }
		public TerminalNode NAMESPACES() { return getToken(SemanticLWHParser.NAMESPACES, 0); }
		public TerminalNode NO() { return getToken(SemanticLWHParser.NO, 0); }
		public TerminalNode NULLS() { return getToken(SemanticLWHParser.NULLS, 0); }
		public TerminalNode OF() { return getToken(SemanticLWHParser.OF, 0); }
		public TerminalNode OPTION() { return getToken(SemanticLWHParser.OPTION, 0); }
		public TerminalNode OPTIONS() { return getToken(SemanticLWHParser.OPTIONS, 0); }
		public TerminalNode OUT() { return getToken(SemanticLWHParser.OUT, 0); }
		public TerminalNode OUTPUTFORMAT() { return getToken(SemanticLWHParser.OUTPUTFORMAT, 0); }
		public TerminalNode OVER() { return getToken(SemanticLWHParser.OVER, 0); }
		public TerminalNode OVERLAY() { return getToken(SemanticLWHParser.OVERLAY, 0); }
		public TerminalNode OVERWRITE() { return getToken(SemanticLWHParser.OVERWRITE, 0); }
		public TerminalNode PARTITION() { return getToken(SemanticLWHParser.PARTITION, 0); }
		public TerminalNode PARTITIONED() { return getToken(SemanticLWHParser.PARTITIONED, 0); }
		public TerminalNode PARTITIONS() { return getToken(SemanticLWHParser.PARTITIONS, 0); }
		public TerminalNode PERCENTLIT() { return getToken(SemanticLWHParser.PERCENTLIT, 0); }
		public TerminalNode PIVOT() { return getToken(SemanticLWHParser.PIVOT, 0); }
		public TerminalNode PLACING() { return getToken(SemanticLWHParser.PLACING, 0); }
		public TerminalNode POSITION() { return getToken(SemanticLWHParser.POSITION, 0); }
		public TerminalNode PRECEDING() { return getToken(SemanticLWHParser.PRECEDING, 0); }
		public TerminalNode PRINCIPALS() { return getToken(SemanticLWHParser.PRINCIPALS, 0); }
		public TerminalNode PROPERTIES() { return getToken(SemanticLWHParser.PROPERTIES, 0); }
		public TerminalNode PURGE() { return getToken(SemanticLWHParser.PURGE, 0); }
		public TerminalNode QUARTER() { return getToken(SemanticLWHParser.QUARTER, 0); }
		public TerminalNode QUERY() { return getToken(SemanticLWHParser.QUERY, 0); }
		public TerminalNode RANGE() { return getToken(SemanticLWHParser.RANGE, 0); }
		public TerminalNode RECORDREADER() { return getToken(SemanticLWHParser.RECORDREADER, 0); }
		public TerminalNode RECORDWRITER() { return getToken(SemanticLWHParser.RECORDWRITER, 0); }
		public TerminalNode RECOVER() { return getToken(SemanticLWHParser.RECOVER, 0); }
		public TerminalNode REDUCE() { return getToken(SemanticLWHParser.REDUCE, 0); }
		public TerminalNode REFRESH() { return getToken(SemanticLWHParser.REFRESH, 0); }
		public TerminalNode RENAME() { return getToken(SemanticLWHParser.RENAME, 0); }
		public TerminalNode REPAIR() { return getToken(SemanticLWHParser.REPAIR, 0); }
		public TerminalNode REPEATABLE() { return getToken(SemanticLWHParser.REPEATABLE, 0); }
		public TerminalNode REPLACE() { return getToken(SemanticLWHParser.REPLACE, 0); }
		public TerminalNode RESET() { return getToken(SemanticLWHParser.RESET, 0); }
		public TerminalNode RESPECT() { return getToken(SemanticLWHParser.RESPECT, 0); }
		public TerminalNode RESTRICT() { return getToken(SemanticLWHParser.RESTRICT, 0); }
		public TerminalNode REVOKE() { return getToken(SemanticLWHParser.REVOKE, 0); }
		public TerminalNode RLIKE() { return getToken(SemanticLWHParser.RLIKE, 0); }
		public TerminalNode ROLE() { return getToken(SemanticLWHParser.ROLE, 0); }
		public TerminalNode ROLES() { return getToken(SemanticLWHParser.ROLES, 0); }
		public TerminalNode ROLLBACK() { return getToken(SemanticLWHParser.ROLLBACK, 0); }
		public TerminalNode ROLLUP() { return getToken(SemanticLWHParser.ROLLUP, 0); }
		public TerminalNode ROW() { return getToken(SemanticLWHParser.ROW, 0); }
		public TerminalNode ROWS() { return getToken(SemanticLWHParser.ROWS, 0); }
		public TerminalNode SCHEMA() { return getToken(SemanticLWHParser.SCHEMA, 0); }
		public TerminalNode SCHEMAS() { return getToken(SemanticLWHParser.SCHEMAS, 0); }
		public TerminalNode SECOND() { return getToken(SemanticLWHParser.SECOND, 0); }
		public TerminalNode SEMI() { return getToken(SemanticLWHParser.SEMI, 0); }
		public TerminalNode SEPARATED() { return getToken(SemanticLWHParser.SEPARATED, 0); }
		public TerminalNode SERDE() { return getToken(SemanticLWHParser.SERDE, 0); }
		public TerminalNode SERDEPROPERTIES() { return getToken(SemanticLWHParser.SERDEPROPERTIES, 0); }
		public TerminalNode SET() { return getToken(SemanticLWHParser.SET, 0); }
		public TerminalNode SETMINUS() { return getToken(SemanticLWHParser.SETMINUS, 0); }
		public TerminalNode SETS() { return getToken(SemanticLWHParser.SETS, 0); }
		public TerminalNode SHOW() { return getToken(SemanticLWHParser.SHOW, 0); }
		public TerminalNode SKEWED() { return getToken(SemanticLWHParser.SKEWED, 0); }
		public TerminalNode SORT() { return getToken(SemanticLWHParser.SORT, 0); }
		public TerminalNode SORTED() { return getToken(SemanticLWHParser.SORTED, 0); }
		public TerminalNode START() { return getToken(SemanticLWHParser.START, 0); }
		public TerminalNode STATISTICS() { return getToken(SemanticLWHParser.STATISTICS, 0); }
		public TerminalNode STORED() { return getToken(SemanticLWHParser.STORED, 0); }
		public TerminalNode STRATIFY() { return getToken(SemanticLWHParser.STRATIFY, 0); }
		public TerminalNode STRUCT() { return getToken(SemanticLWHParser.STRUCT, 0); }
		public TerminalNode SUBSTR() { return getToken(SemanticLWHParser.SUBSTR, 0); }
		public TerminalNode SUBSTRING() { return getToken(SemanticLWHParser.SUBSTRING, 0); }
		public TerminalNode SYNC() { return getToken(SemanticLWHParser.SYNC, 0); }
		public TerminalNode SYSTEM_TIME() { return getToken(SemanticLWHParser.SYSTEM_TIME, 0); }
		public TerminalNode SYSTEM_VERSION() { return getToken(SemanticLWHParser.SYSTEM_VERSION, 0); }
		public TerminalNode TABLES() { return getToken(SemanticLWHParser.TABLES, 0); }
		public TerminalNode TABLESAMPLE() { return getToken(SemanticLWHParser.TABLESAMPLE, 0); }
		public TerminalNode TBLPROPERTIES() { return getToken(SemanticLWHParser.TBLPROPERTIES, 0); }
		public TerminalNode TEMPORARY() { return getToken(SemanticLWHParser.TEMPORARY, 0); }
		public TerminalNode TERMINATED() { return getToken(SemanticLWHParser.TERMINATED, 0); }
		public TerminalNode TIMESTAMP() { return getToken(SemanticLWHParser.TIMESTAMP, 0); }
		public TerminalNode TIMESTAMPADD() { return getToken(SemanticLWHParser.TIMESTAMPADD, 0); }
		public TerminalNode TIMESTAMPDIFF() { return getToken(SemanticLWHParser.TIMESTAMPDIFF, 0); }
		public TerminalNode TOUCH() { return getToken(SemanticLWHParser.TOUCH, 0); }
		public TerminalNode TRANSACTION() { return getToken(SemanticLWHParser.TRANSACTION, 0); }
		public TerminalNode TRANSACTIONS() { return getToken(SemanticLWHParser.TRANSACTIONS, 0); }
		public TerminalNode TRANSFORM() { return getToken(SemanticLWHParser.TRANSFORM, 0); }
		public TerminalNode TRIM() { return getToken(SemanticLWHParser.TRIM, 0); }
		public TerminalNode TRUE() { return getToken(SemanticLWHParser.TRUE, 0); }
		public TerminalNode TRUNCATE() { return getToken(SemanticLWHParser.TRUNCATE, 0); }
		public TerminalNode TRY_CAST() { return getToken(SemanticLWHParser.TRY_CAST, 0); }
		public TerminalNode TYPE() { return getToken(SemanticLWHParser.TYPE, 0); }
		public TerminalNode UNARCHIVE() { return getToken(SemanticLWHParser.UNARCHIVE, 0); }
		public TerminalNode UNBOUNDED() { return getToken(SemanticLWHParser.UNBOUNDED, 0); }
		public TerminalNode UNCACHE() { return getToken(SemanticLWHParser.UNCACHE, 0); }
		public TerminalNode UNLOCK() { return getToken(SemanticLWHParser.UNLOCK, 0); }
		public TerminalNode UNSET() { return getToken(SemanticLWHParser.UNSET, 0); }
		public TerminalNode UPDATE() { return getToken(SemanticLWHParser.UPDATE, 0); }
		public TerminalNode USE() { return getToken(SemanticLWHParser.USE, 0); }
		public TerminalNode VALUES() { return getToken(SemanticLWHParser.VALUES, 0); }
		public TerminalNode VERSION() { return getToken(SemanticLWHParser.VERSION, 0); }
		public TerminalNode VIEW() { return getToken(SemanticLWHParser.VIEW, 0); }
		public TerminalNode VIEWS() { return getToken(SemanticLWHParser.VIEWS, 0); }
		public TerminalNode WEEK() { return getToken(SemanticLWHParser.WEEK, 0); }
		public TerminalNode WINDOW() { return getToken(SemanticLWHParser.WINDOW, 0); }
		public TerminalNode YEAR() { return getToken(SemanticLWHParser.YEAR, 0); }
		public TerminalNode ZONE() { return getToken(SemanticLWHParser.ZONE, 0); }
		public AnsiNonReservedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ansiNonReserved; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).enterAnsiNonReserved(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SemanticLWHParserListener ) ((SemanticLWHParserListener)listener).exitAnsiNonReserved(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SemanticLWHParserVisitor ) return ((SemanticLWHParserVisitor<? extends T>)visitor).visitAnsiNonReserved(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AnsiNonReservedContext ansiNonReserved() throws RecognitionException {
		AnsiNonReservedContext _localctx = new AnsiNonReservedContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_ansiNonReserved);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ADD) | (1L << AFTER) | (1L << ALTER) | (1L << ANALYZE) | (1L << ANTI) | (1L << ARCHIVE) | (1L << ARRAY) | (1L << ASC) | (1L << AT) | (1L << BETWEEN) | (1L << BUCKET) | (1L << BUCKETS) | (1L << BY) | (1L << CACHE) | (1L << CASCADE) | (1L << CATALOG) | (1L << CATALOGS) | (1L << CHANGE) | (1L << CLEAR) | (1L << CLUSTER) | (1L << CLUSTERED) | (1L << CODEGEN) | (1L << COLLECTION) | (1L << COLUMNS) | (1L << COMMENT) | (1L << COMMIT) | (1L << COMPACT) | (1L << COMPACTIONS) | (1L << COMPUTE) | (1L << CONCATENATE) | (1L << COST) | (1L << CUBE) | (1L << CURRENT) | (1L << DAY) | (1L << DAYOFYEAR) | (1L << DATA) | (1L << DATABASE))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (DATABASES - 64)) | (1L << (DATEADD - 64)) | (1L << (DATEDIFF - 64)) | (1L << (DBPROPERTIES - 64)) | (1L << (DEFINED - 64)) | (1L << (DELETE - 64)) | (1L << (DELIMITED - 64)) | (1L << (DESC - 64)) | (1L << (DESCRIBE - 64)) | (1L << (DFS - 64)) | (1L << (DIRECTORIES - 64)) | (1L << (DIRECTORY - 64)) | (1L << (DISTRIBUTE - 64)) | (1L << (DIV - 64)) | (1L << (DROP - 64)) | (1L << (ESCAPED - 64)) | (1L << (EXCHANGE - 64)) | (1L << (EXISTS - 64)) | (1L << (EXPLAIN - 64)) | (1L << (EXPORT - 64)) | (1L << (EXTENDED - 64)) | (1L << (EXTERNAL - 64)) | (1L << (EXTRACT - 64)) | (1L << (FIELDS - 64)) | (1L << (FILEFORMAT - 64)) | (1L << (FIRST - 64)) | (1L << (FOLLOWING - 64)) | (1L << (FORMAT - 64)) | (1L << (FORMATTED - 64)) | (1L << (FUNCTION - 64)) | (1L << (FUNCTIONS - 64)) | (1L << (GLOBAL - 64)) | (1L << (GROUPING - 64)) | (1L << (HOUR - 64)) | (1L << (IF - 64)) | (1L << (IGNORE - 64)) | (1L << (IMPORT - 64)) | (1L << (INDEX - 64)) | (1L << (INDEXES - 64)) | (1L << (INPATH - 64)) | (1L << (INPUTFORMAT - 64)) | (1L << (INSERT - 64)) | (1L << (INTERVAL - 64)))) != 0) || ((((_la - 128)) & ~0x3f) == 0 && ((1L << (_la - 128)) & ((1L << (ITEMS - 128)) | (1L << (KEYS - 128)) | (1L << (LAST - 128)) | (1L << (LAZY - 128)) | (1L << (LIKE - 128)) | (1L << (ILIKE - 128)) | (1L << (LIMIT - 128)) | (1L << (LINES - 128)) | (1L << (LIST - 128)) | (1L << (LOAD - 128)) | (1L << (LOCAL - 128)) | (1L << (LOCATION - 128)) | (1L << (LOCK - 128)) | (1L << (LOCKS - 128)) | (1L << (LOGICAL - 128)) | (1L << (MACRO - 128)) | (1L << (MAP - 128)) | (1L << (MATCHED - 128)) | (1L << (MERGE - 128)) | (1L << (MICROSECOND - 128)) | (1L << (MILLISECOND - 128)) | (1L << (MINUTE - 128)) | (1L << (MONTH - 128)) | (1L << (MSCK - 128)) | (1L << (NAMESPACE - 128)) | (1L << (NAMESPACES - 128)) | (1L << (NO - 128)) | (1L << (NULLS - 128)) | (1L << (OF - 128)) | (1L << (OPTION - 128)) | (1L << (OPTIONS - 128)) | (1L << (OUT - 128)) | (1L << (OUTPUTFORMAT - 128)) | (1L << (OVER - 128)) | (1L << (OVERLAY - 128)) | (1L << (OVERWRITE - 128)) | (1L << (PARTITION - 128)) | (1L << (PARTITIONED - 128)) | (1L << (PARTITIONS - 128)) | (1L << (PERCENTLIT - 128)) | (1L << (PIVOT - 128)) | (1L << (PLACING - 128)) | (1L << (POSITION - 128)) | (1L << (PRECEDING - 128)) | (1L << (PRINCIPALS - 128)) | (1L << (PROPERTIES - 128)) | (1L << (PURGE - 128)))) != 0) || ((((_la - 192)) & ~0x3f) == 0 && ((1L << (_la - 192)) & ((1L << (QUARTER - 192)) | (1L << (QUERY - 192)) | (1L << (RANGE - 192)) | (1L << (RECORDREADER - 192)) | (1L << (RECORDWRITER - 192)) | (1L << (RECOVER - 192)) | (1L << (REDUCE - 192)) | (1L << (REFRESH - 192)) | (1L << (RENAME - 192)) | (1L << (REPAIR - 192)) | (1L << (REPEATABLE - 192)) | (1L << (REPLACE - 192)) | (1L << (RESET - 192)) | (1L << (RESPECT - 192)) | (1L << (RESTRICT - 192)) | (1L << (REVOKE - 192)) | (1L << (RLIKE - 192)) | (1L << (ROLE - 192)) | (1L << (ROLES - 192)) | (1L << (ROLLBACK - 192)) | (1L << (ROLLUP - 192)) | (1L << (ROW - 192)) | (1L << (ROWS - 192)) | (1L << (SECOND - 192)) | (1L << (SCHEMA - 192)) | (1L << (SCHEMAS - 192)) | (1L << (SEMI - 192)) | (1L << (SEPARATED - 192)) | (1L << (SERDE - 192)) | (1L << (SERDEPROPERTIES - 192)) | (1L << (SET - 192)) | (1L << (SETMINUS - 192)) | (1L << (SETS - 192)) | (1L << (SHOW - 192)) | (1L << (SKEWED - 192)) | (1L << (SORT - 192)) | (1L << (SORTED - 192)) | (1L << (START - 192)) | (1L << (STATISTICS - 192)) | (1L << (STORED - 192)) | (1L << (STRATIFY - 192)) | (1L << (STRUCT - 192)) | (1L << (SUBSTR - 192)) | (1L << (SUBSTRING - 192)) | (1L << (SYNC - 192)) | (1L << (SYSTEM_TIME - 192)) | (1L << (SYSTEM_VERSION - 192)) | (1L << (TABLES - 192)) | (1L << (TABLESAMPLE - 192)) | (1L << (TBLPROPERTIES - 192)) | (1L << (TEMPORARY - 192)) | (1L << (TERMINATED - 192)) | (1L << (TIMESTAMP - 192)) | (1L << (TIMESTAMPADD - 192)) | (1L << (TIMESTAMPDIFF - 192)) | (1L << (TOUCH - 192)))) != 0) || ((((_la - 257)) & ~0x3f) == 0 && ((1L << (_la - 257)) & ((1L << (TRANSACTION - 257)) | (1L << (TRANSACTIONS - 257)) | (1L << (TRANSFORM - 257)) | (1L << (TRIM - 257)) | (1L << (TRUE - 257)) | (1L << (TRUNCATE - 257)) | (1L << (TRY_CAST - 257)) | (1L << (TYPE - 257)) | (1L << (UNARCHIVE - 257)) | (1L << (UNBOUNDED - 257)) | (1L << (UNCACHE - 257)) | (1L << (UNLOCK - 257)) | (1L << (UNSET - 257)) | (1L << (UPDATE - 257)) | (1L << (USE - 257)) | (1L << (VALUES - 257)) | (1L << (VERSION - 257)) | (1L << (VIEW - 257)) | (1L << (VIEWS - 257)) | (1L << (WEEK - 257)) | (1L << (WINDOW - 257)) | (1L << (YEAR - 257)) | (1L << (ZONE - 257)))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u0149\u0121\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001"+
		"\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004"+
		"\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007"+
		"\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b"+
		"\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007"+
		"\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007"+
		"\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007"+
		"\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007"+
		"\u0018\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001"+
		"\u0002\u0001\u0002\u0003\u0002:\b\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0005"+
		"\u0003D\b\u0003\n\u0003\f\u0003G\t\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004P\b"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0005"+
		"\u0004W\b\u0004\n\u0004\f\u0004Z\t\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0005\u0005a\b\u0005\n\u0005\f\u0005d\t"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0005"+
		"\u0007k\b\u0007\n\u0007\f\u0007n\t\u0007\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003\bx\b\b\u0001\t\u0001\t\u0003"+
		"\t|\b\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0003\t\u0083\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0001\t\u0003\t\u0089\b\t\u0001\t\u0001\t\u0001\t\u0001"+
		"\t\u0001\t\u0003\t\u0090\b\t\u0001\n\u0001\n\u0001\n\u0001\n\u0005\n\u0096"+
		"\b\n\n\n\f\n\u0099\t\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u00a1\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f"+
		"\u0001\f\u0001\f\u0003\f\u00a9\b\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\f\u0003\f\u00b1\b\f\u0003\f\u00b3\b\f\u0001\r\u0001\r\u0001\r"+
		"\u0001\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\r\u00bd\b\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"+
		"\u0004\u0010\u00c6\b\u0010\u000b\u0010\f\u0010\u00c7\u0001\u0010\u0003"+
		"\u0010\u00cb\b\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u00d0"+
		"\b\u0011\n\u0011\f\u0011\u00d3\t\u0011\u0001\u0012\u0001\u0012\u0001\u0013"+
		"\u0001\u0013\u0001\u0013\u0003\u0013\u00da\b\u0013\u0001\u0014\u0001\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u00ed\b\u0015\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u00f1\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u00f7\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u00fd\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0005\u0015\u0104\b\u0015\n\u0015\f\u0015\u0107"+
		"\t\u0015\u0001\u0015\u0003\u0015\u010a\b\u0015\u0003\u0015\u010c\b\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016\u0111\b\u0016\n\u0016"+
		"\f\u0016\u0114\t\u0016\u0001\u0017\u0001\u0017\u0003\u0017\u0118\b\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u011d\b\u0017\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0000\u0000\u0019\u0000\u0002\u0004\u0006\b\n"+
		"\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.0\u0000"+
		"\u0004\u0002\u0000\u009b\u009b\u011f\u011f\u0004\u0000<<qq\u009a\u009a"+
		"\u00d9\u00d9\u0003\u0000qq\u009a\u009a\u00d9\u00d94\u0000\t\n\f\r\u000f"+
		"\u000f\u0011\u0012\u0014\u0015\u0017\u0017\u0019\u001d \"$\'))+13367<"+
		"CELNPTTV\\__acfgjlooqtvwy{}}\u0080\u0080\u0083\u0084\u0086\u0086\u0089"+
		"\u009e\u00a0\u00a0\u00a3\u00a4\u00a7\u00a8\u00ab\u00ab\u00ad\u00ae\u00b0"+
		"\u00b4\u00b7\u00bb\u00bd\u00c6\u00c8\u00d0\u00d2\u00db\u00dd\u00e0\u00e2"+
		"\u00e6\u00e8\u00f3\u00f5\u00f9\u00fb\u00fd\u00ff\u00ff\u0101\u010b\u010f"+
		"\u0112\u0115\u0119\u011c\u011c\u011f\u0120\u0131\u00002\u0001\u0000\u0000"+
		"\u0000\u00025\u0001\u0000\u0000\u0000\u00049\u0001\u0000\u0000\u0000\u0006"+
		";\u0001\u0000\u0000\u0000\bJ\u0001\u0000\u0000\u0000\n]\u0001\u0000\u0000"+
		"\u0000\fe\u0001\u0000\u0000\u0000\u000eh\u0001\u0000\u0000\u0000\u0010"+
		"w\u0001\u0000\u0000\u0000\u0012\u008f\u0001\u0000\u0000\u0000\u0014\u0091"+
		"\u0001\u0000\u0000\u0000\u0016\u009c\u0001\u0000\u0000\u0000\u0018\u00b2"+
		"\u0001\u0000\u0000\u0000\u001a\u00bc\u0001\u0000\u0000\u0000\u001c\u00be"+
		"\u0001\u0000\u0000\u0000\u001e\u00c0\u0001\u0000\u0000\u0000 \u00ca\u0001"+
		"\u0000\u0000\u0000\"\u00cc\u0001\u0000\u0000\u0000$\u00d4\u0001\u0000"+
		"\u0000\u0000&\u00d9\u0001\u0000\u0000\u0000(\u00db\u0001\u0000\u0000\u0000"+
		"*\u010b\u0001\u0000\u0000\u0000,\u010d\u0001\u0000\u0000\u0000.\u0115"+
		"\u0001\u0000\u0000\u00000\u011e\u0001\u0000\u0000\u000023\u0003\u0002"+
		"\u0001\u000034\u0005\u0000\u0000\u00014\u0001\u0001\u0000\u0000\u0000"+
		"56\u0003\u0004\u0002\u00006\u0003\u0001\u0000\u0000\u00007:\u0003\u0006"+
		"\u0003\u00008:\u0003\b\u0004\u000097\u0001\u0000\u0000\u000098\u0001\u0000"+
		"\u0000\u0000:\u0005\u0001\u0000\u0000\u0000;<\u0005\u0147\u0000\u0000"+
		"<=\u0005\u0148\u0000\u0000=>\u0005\u0149\u0000\u0000>?\u0003\u001e\u000f"+
		"\u0000?@\u0005\u00a8\u0000\u0000@E\u0005\u0002\u0000\u0000AB\u0005\u0004"+
		"\u0000\u0000BD\u0003\u0012\t\u0000CA\u0001\u0000\u0000\u0000DG\u0001\u0000"+
		"\u0000\u0000EC\u0001\u0000\u0000\u0000EF\u0001\u0000\u0000\u0000FH\u0001"+
		"\u0000\u0000\u0000GE\u0001\u0000\u0000\u0000HI\u0005\u0003\u0000\u0000"+
		"I\u0007\u0001\u0000\u0000\u0000JK\u00054\u0000\u0000KO\u0005\u00f4\u0000"+
		"\u0000LM\u0005r\u0000\u0000MN\u0005\u00a1\u0000\u0000NP\u0005W\u0000\u0000"+
		"OL\u0001\u0000\u0000\u0000OP\u0001\u0000\u0000\u0000PQ\u0001\u0000\u0000"+
		"\u0000QR\u0003\"\u0011\u0000RS\u0005\u0002\u0000\u0000SX\u0003\n\u0005"+
		"\u0000TU\u0005\u0004\u0000\u0000UW\u0003\u0012\t\u0000VT\u0001\u0000\u0000"+
		"\u0000WZ\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XY\u0001\u0000"+
		"\u0000\u0000Y[\u0001\u0000\u0000\u0000ZX\u0001\u0000\u0000\u0000[\\\u0005"+
		"\u0003\u0000\u0000\\\t\u0001\u0000\u0000\u0000]b\u0003\f\u0006\u0000^"+
		"_\u0005\u0004\u0000\u0000_a\u0003\f\u0006\u0000`^\u0001\u0000\u0000\u0000"+
		"ad\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000bc\u0001\u0000\u0000"+
		"\u0000c\u000b\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000ef\u0003"+
		"\u001c\u000e\u0000fg\u0003\u000e\u0007\u0000g\r\u0001\u0000\u0000\u0000"+
		"hl\u0003*\u0015\u0000ik\u0003\u0010\b\u0000ji\u0001\u0000\u0000\u0000"+
		"kn\u0001\u0000\u0000\u0000lj\u0001\u0000\u0000\u0000lm\u0001\u0000\u0000"+
		"\u0000m\u000f\u0001\u0000\u0000\u0000nl\u0001\u0000\u0000\u0000op\u0005"+
		"\u00a1\u0000\u0000px\u0005\u00a2\u0000\u0000qr\u0005\u00bc\u0000\u0000"+
		"rx\u0005\u0082\u0000\u0000sx\u0005\u010d\u0000\u0000tu\u0005e\u0000\u0000"+
		"uv\u0005\u0082\u0000\u0000vx\u0003\u0016\u000b\u0000wo\u0001\u0000\u0000"+
		"\u0000wq\u0001\u0000\u0000\u0000ws\u0001\u0000\u0000\u0000wt\u0001\u0000"+
		"\u0000\u0000x\u0011\u0001\u0000\u0000\u0000yz\u00052\u0000\u0000z|\u0003"+
		"$\u0012\u0000{y\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|}\u0001"+
		"\u0000\u0000\u0000}~\u0005\u00bc\u0000\u0000~\u007f\u0005\u0082\u0000"+
		"\u0000\u007f\u0090\u0003\u0014\n\u0000\u0080\u0081\u00052\u0000\u0000"+
		"\u0081\u0083\u0003$\u0012\u0000\u0082\u0080\u0001\u0000\u0000\u0000\u0082"+
		"\u0083\u0001\u0000\u0000\u0000\u0083\u0084\u0001\u0000\u0000\u0000\u0084"+
		"\u0085\u0005\u010d\u0000\u0000\u0085\u0090\u0003\u0014\n\u0000\u0086\u0087"+
		"\u00052\u0000\u0000\u0087\u0089\u0003$\u0012\u0000\u0088\u0086\u0001\u0000"+
		"\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089\u008a\u0001\u0000"+
		"\u0000\u0000\u008a\u008b\u0005e\u0000\u0000\u008b\u008c\u0005\u0082\u0000"+
		"\u0000\u008c\u008d\u0003\u0014\n\u0000\u008d\u008e\u0003\u0016\u000b\u0000"+
		"\u008e\u0090\u0001\u0000\u0000\u0000\u008f{\u0001\u0000\u0000\u0000\u008f"+
		"\u0082\u0001\u0000\u0000\u0000\u008f\u0088\u0001\u0000\u0000\u0000\u0090"+
		"\u0013\u0001\u0000\u0000\u0000\u0091\u0092\u0005\u0002\u0000\u0000\u0092"+
		"\u0097\u0003\u001c\u000e\u0000\u0093\u0094\u0005\u0004\u0000\u0000\u0094"+
		"\u0096\u0003\u001c\u000e\u0000\u0095\u0093\u0001\u0000\u0000\u0000\u0096"+
		"\u0099\u0001\u0000\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0097"+
		"\u0098\u0001\u0000\u0000\u0000\u0098\u009a\u0001\u0000\u0000\u0000\u0099"+
		"\u0097\u0001\u0000\u0000\u0000\u009a\u009b\u0005\u0003\u0000\u0000\u009b"+
		"\u0015\u0001\u0000\u0000\u0000\u009c\u009d\u0005\u00c7\u0000\u0000\u009d"+
		"\u009e\u0003\"\u0011\u0000\u009e\u00a0\u0003\u0014\n\u0000\u009f\u00a1"+
		"\u0003\u0018\f\u0000\u00a0\u009f\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001"+
		"\u0000\u0000\u0000\u00a1\u0017\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005"+
		"\u00a5\u0000\u0000\u00a3\u00a4\u0005F\u0000\u0000\u00a4\u00a8\u0003\u001a"+
		"\r\u0000\u00a5\u00a6\u0005\u00a5\u0000\u0000\u00a6\u00a7\u0005\u0111\u0000"+
		"\u0000\u00a7\u00a9\u0003\u001a\r\u0000\u00a8\u00a5\u0001\u0000\u0000\u0000"+
		"\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00b3\u0001\u0000\u0000\u0000"+
		"\u00aa\u00ab\u0005\u00a5\u0000\u0000\u00ab\u00ac\u0005\u0111\u0000\u0000"+
		"\u00ac\u00b0\u0003\u001a\r\u0000\u00ad\u00ae\u0005\u00a5\u0000\u0000\u00ae"+
		"\u00af\u0005F\u0000\u0000\u00af\u00b1\u0003\u001a\r\u0000\u00b0\u00ad"+
		"\u0001\u0000\u0000\u0000\u00b0\u00b1\u0001\u0000\u0000\u0000\u00b1\u00b3"+
		"\u0001\u0000\u0000\u0000\u00b2\u00a2\u0001\u0000\u0000\u0000\u00b2\u00aa"+
		"\u0001\u0000\u0000\u0000\u00b3\u0019\u0001\u0000\u0000\u0000\u00b4\u00bd"+
		"\u0005\u00cf\u0000\u0000\u00b5\u00bd\u0005\u001d\u0000\u0000\u00b6\u00b7"+
		"\u0005\u00e2\u0000\u0000\u00b7\u00bd\u0005\u00a2\u0000\u0000\u00b8\u00b9"+
		"\u0005\u00a0\u0000\u0000\u00b9\u00bd\u0005\b\u0000\u0000\u00ba\u00bb\u0005"+
		"\u00e2\u0000\u0000\u00bb\u00bd\u0005D\u0000\u0000\u00bc\u00b4\u0001\u0000"+
		"\u0000\u0000\u00bc\u00b5\u0001\u0000\u0000\u0000\u00bc\u00b6\u0001\u0000"+
		"\u0000\u0000\u00bc\u00b8\u0001\u0000\u0000\u0000\u00bc\u00ba\u0001\u0000"+
		"\u0000\u0000\u00bd\u001b\u0001\u0000\u0000\u0000\u00be\u00bf\u0003\u001e"+
		"\u000f\u0000\u00bf\u001d\u0001\u0000\u0000\u0000\u00c0\u00c1\u0003$\u0012"+
		"\u0000\u00c1\u00c2\u0003 \u0010\u0000\u00c2\u001f\u0001\u0000\u0000\u0000"+
		"\u00c3\u00c4\u0005\u012a\u0000\u0000\u00c4\u00c6\u0003$\u0012\u0000\u00c5"+
		"\u00c3\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000\u0000\u0000\u00c7"+
		"\u00c5\u0001\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8"+
		"\u00cb\u0001\u0000\u0000\u0000\u00c9\u00cb\u0001\u0000\u0000\u0000\u00ca"+
		"\u00c5\u0001\u0000\u0000\u0000\u00ca\u00c9\u0001\u0000\u0000\u0000\u00cb"+
		"!\u0001\u0000\u0000\u0000\u00cc\u00d1\u0003\u001e\u000f\u0000\u00cd\u00ce"+
		"\u0005\u0005\u0000\u0000\u00ce\u00d0\u0003\u001e\u000f\u0000\u00cf\u00cd"+
		"\u0001\u0000\u0000\u0000\u00d0\u00d3\u0001\u0000\u0000\u0000\u00d1\u00cf"+
		"\u0001\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000\u0000\u0000\u00d2#\u0001"+
		"\u0000\u0000\u0000\u00d3\u00d1\u0001\u0000\u0000\u0000\u00d4\u00d5\u0003"+
		"&\u0013\u0000\u00d5%\u0001\u0000\u0000\u0000\u00d6\u00da\u0005\u0141\u0000"+
		"\u0000\u00d7\u00da\u0003(\u0014\u0000\u00d8\u00da\u00030\u0018\u0000\u00d9"+
		"\u00d6\u0001\u0000\u0000\u0000\u00d9\u00d7\u0001\u0000\u0000\u0000\u00d9"+
		"\u00d8\u0001\u0000\u0000\u0000\u00da\'\u0001\u0000\u0000\u0000\u00db\u00dc"+
		"\u0005\u0142\u0000\u0000\u00dc)\u0001\u0000\u0000\u0000\u00dd\u00de\u0005"+
		"\u0012\u0000\u0000\u00de\u00df\u0005\u0125\u0000\u0000\u00df\u00e0\u0003"+
		"*\u0015\u0000\u00e0\u00e1\u0005\u0127\u0000\u0000\u00e1\u010c\u0001\u0000"+
		"\u0000\u0000\u00e2\u00e3\u0005\u0095\u0000\u0000\u00e3\u00e4\u0005\u0125"+
		"\u0000\u0000\u00e4\u00e5\u0003*\u0015\u0000\u00e5\u00e6\u0005\u0004\u0000"+
		"\u0000\u00e6\u00e7\u0003*\u0015\u0000\u00e7\u00e8\u0005\u0127\u0000\u0000"+
		"\u00e8\u010c\u0001\u0000\u0000\u0000\u00e9\u00f0\u0005\u00ee\u0000\u0000"+
		"\u00ea\u00ec\u0005\u0125\u0000\u0000\u00eb\u00ed\u0003,\u0016\u0000\u00ec"+
		"\u00eb\u0001\u0000\u0000\u0000\u00ec\u00ed\u0001\u0000\u0000\u0000\u00ed"+
		"\u00ee\u0001\u0000\u0000\u0000\u00ee\u00f1\u0005\u0127\u0000\u0000\u00ef"+
		"\u00f1\u0005\u0123\u0000\u0000\u00f0\u00ea\u0001\u0000\u0000\u0000\u00f0"+
		"\u00ef\u0001\u0000\u0000\u0000\u00f1\u010c\u0001\u0000\u0000\u0000\u00f2"+
		"\u00f3\u0005}\u0000\u0000\u00f3\u00f6\u0007\u0000\u0000\u0000\u00f4\u00f5"+
		"\u0005\u00fe\u0000\u0000\u00f5\u00f7\u0005\u009b\u0000\u0000\u00f6\u00f4"+
		"\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000\u00f7\u010c"+
		"\u0001\u0000\u0000\u0000\u00f8\u00f9\u0005}\u0000\u0000\u00f9\u00fc\u0007"+
		"\u0001\u0000\u0000\u00fa\u00fb\u0005\u00fe\u0000\u0000\u00fb\u00fd\u0007"+
		"\u0002\u0000\u0000\u00fc\u00fa\u0001\u0000\u0000\u0000\u00fc\u00fd\u0001"+
		"\u0000\u0000\u0000\u00fd\u010c\u0001\u0000\u0000\u0000\u00fe\u0109\u0003"+
		"$\u0012\u0000\u00ff\u0100\u0005\u0002\u0000\u0000\u0100\u0105\u0005\u013b"+
		"\u0000\u0000\u0101\u0102\u0005\u0004\u0000\u0000\u0102\u0104\u0005\u013b"+
		"\u0000\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0104\u0107\u0001\u0000"+
		"\u0000\u0000\u0105\u0103\u0001\u0000\u0000\u0000\u0105\u0106\u0001\u0000"+
		"\u0000\u0000\u0106\u0108\u0001\u0000\u0000\u0000\u0107\u0105\u0001\u0000"+
		"\u0000\u0000\u0108\u010a\u0005\u0003\u0000\u0000\u0109\u00ff\u0001\u0000"+
		"\u0000\u0000\u0109\u010a\u0001\u0000\u0000\u0000\u010a\u010c\u0001\u0000"+
		"\u0000\u0000\u010b\u00dd\u0001\u0000\u0000\u0000\u010b\u00e2\u0001\u0000"+
		"\u0000\u0000\u010b\u00e9\u0001\u0000\u0000\u0000\u010b\u00f2\u0001\u0000"+
		"\u0000\u0000\u010b\u00f8\u0001\u0000\u0000\u0000\u010b\u00fe\u0001\u0000"+
		"\u0000\u0000\u010c+\u0001\u0000\u0000\u0000\u010d\u0112\u0003.\u0017\u0000"+
		"\u010e\u010f\u0005\u0004\u0000\u0000\u010f\u0111\u0003.\u0017\u0000\u0110"+
		"\u010e\u0001\u0000\u0000\u0000\u0111\u0114\u0001\u0000\u0000\u0000\u0112"+
		"\u0110\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000\u0000\u0113"+
		"-\u0001\u0000\u0000\u0000\u0114\u0112\u0001\u0000\u0000\u0000\u0115\u0117"+
		"\u0003$\u0012\u0000\u0116\u0118\u0005\u0133\u0000\u0000\u0117\u0116\u0001"+
		"\u0000\u0000\u0000\u0117\u0118\u0001\u0000\u0000\u0000\u0118\u0119\u0001"+
		"\u0000\u0000\u0000\u0119\u011c\u0003*\u0015\u0000\u011a\u011b\u0005\u00a1"+
		"\u0000\u0000\u011b\u011d\u0005\u00a2\u0000\u0000\u011c\u011a\u0001\u0000"+
		"\u0000\u0000\u011c\u011d\u0001\u0000\u0000\u0000\u011d/\u0001\u0000\u0000"+
		"\u0000\u011e\u011f\u0007\u0003\u0000\u0000\u011f1\u0001\u0000\u0000\u0000"+
		"\u001f9EOXblw{\u0082\u0088\u008f\u0097\u00a0\u00a8\u00b0\u00b2\u00bc\u00c7"+
		"\u00ca\u00d1\u00d9\u00ec\u00f0\u00f6\u00fc\u0105\u0109\u010b\u0112\u0117"+
		"\u011c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}