
/**
 * Lexer from spark, ors/apache/spark/sql/catalyst/parser/SqlBaseLexer.g4
 */
lexer grammar LightningLexer;

@header {package com.zetaris.lightning.parser;}

@members {
  /**
   * When true, parser should throw ParseExcetion for unclosed bracketed comment.
   */
  public boolean has_unclosed_bracketed_comment = false;

  /**
   * Verify whether current token is a valid decimal token (which contains dot).
   * Returns true if the character that follows the token is not a digit or letter or underscore.
   *
   * For example:
   * For char stream "2.3", "2." is not a valid decimal token, because it is followed by digit '3'.
   * For char stream "2.3_", "2.3" is not a valid decimal token, because it is followed by '_'.
   * For char stream "2.3W", "2.3" is not a valid decimal token, because it is followed by 'W'.
   * For char stream "12.0D 34.E2+0.12 "  12.0D is a valid decimal token because it is followed
   * by a space. 34.E2 is a valid decimal token because it is followed by symbol '+'
   * which is not a digit or letter or underscore.
   */
  public boolean isValidDecimal() {
    int nextChar = _input.LA(1);
    if (nextChar >= 'A' && nextChar <= 'Z' || nextChar >= '0' && nextChar <= '9' ||
      nextChar == '_') {
      return false;
    } else {
      return true;
    }
  }

  /**
   * This method will be called when we see '/*' and try to match it as a bracketed comment.
   * If the next character is '+', it should be parsed as hint later, and we cannot match
   * it as a bracketed comment.
   *
   * Returns true if the next character is '+'.
   */
  public boolean isHint() {
    int nextChar = _input.LA(1);
    if (nextChar == '+') {
      return true;
    } else {
      return false;
    }
  }

  /**
   * This method will be called when the character stream ends and try to find out the
   * unclosed bracketed comment.
   * If the method be called, it means the end of the entire character stream match,
   * and we set the flag and fail later.
   */
  public void markUnclosedComment() {
    has_unclosed_bracketed_comment = true;
  }

  /**
     * This method will be called when we see '--' and try to match it as an annotation.
     * If the next character is '@', it should be parsed as annotation later.
     *
     * Returns true if the next character is '@'.
     */
    public boolean isAnnotation() {
      int nextChar = _input.LA(1);
      if (nextChar == '@') {
        return true;
      } else {
        return false;
      }
    }
}

AT: '@';
SEMICOLON: ';';

LEFT_PAREN: '(';
RIGHT_PAREN: ')';
COMMA: ',';
DOT: '.';
LEFT_BRACKET: '[';
RIGHT_BRACKET: ']';


// -- LWH Keyword
ACTION: 'ACTION';
ARRAY: 'ARRAY';
AS: 'AS';
AUDIO: 'AUDIO';
AVRO: 'AVRO';
BUILD: 'BUILD';
CASCADE: 'CASCADE';
CATALOG: 'CATALOG';
COMPILE: 'COMPILE';
CONSTRAINT: 'CONSTRAINT';
CREATE: 'CREATE';
CSV: 'CSV';
DATASOURCE: 'DATASOURCE';
DAY: 'DAY';
DDL: 'DDL';
DEFAULT: 'DEFAULT';
DELETE: 'DELETE';
DELTA: 'DELTA';
DROP: 'DROP';
EXISTS: 'EXISTS';
FALSE: 'FALSE';
FOREIGN: 'FOREIGN';
HOUR: 'HOUR';
IF: 'IF';
ICEBERG: 'ICEBERG';
IMAGE: 'IMAGE';
INTERVAL: 'INTERVAL';
JDBC: 'JDBC';
JSON: 'JSON';
KEY: 'KEY';
LAKEHOUSE: 'LAKEHOUSE';
LIKE: 'LIKE';
LOCATION: 'LOCATION';
MAP: 'MAP';
MINUTE: 'MINUTE';
MONTH: 'MONTH';
NAME: 'NAME';
NAMESPACE: 'NAMESPACE';
NO: 'NO';
NOT: 'NOT';
NULL: 'NULL';
ON: 'ON';
OPTIONS: 'OPTIONS';
OR: 'OR';
ORC: 'ORC';
PARQUET: 'PARQUET';
PDF: 'PDF';
PRIMARY: 'PRIMARY';
REFERENCES: 'REFERENCES';
REGISTER: 'REGISTER';
REPLACE: 'REPLACE';
RESTRICT: 'RESTRICT';
SECOND: 'SECOND';
SET: 'SET';
SOURCE: 'SOURCE';
STRUCT: 'STRUCT';
TABLE: 'TABLE';
TAG: 'TAG';
TEXT: 'TEXT';
TO: 'TO';
TRUE: 'TRUE';
UCL: 'UCL';
UNIQUE: 'UNIQUE';
UPDATE: 'UPDATE';
USING: 'USING';
VIDEO: 'VIDEO';
XML: 'XML';
YEAR: 'YEAR';

// End of the keywords list
//============================

EQ  : '=' | '==';
NSEQ: '<=>';
NEQ : '<>';
NEQJ: '!=';
LT  : '<';
LTE : '<=' | '!>';
GT  : '>';
GTE : '>=' | '!<';

PLUS: '+';
MINUS: '-';
ASTERISK: '*';
SLASH: '/';
PERCENT: '%';
TILDE: '~';
AMPERSAND: '&';
PIPE: '|';
CONCAT_PIPE: '||';
HAT: '^';
COLON: ':';
ARROW: '->';
HENT_START: '/*+';
HENT_END: '*/';

STRING
    : '\'' ( ~('\''|'\\') | ('\\' .) )* '\''
    | '"' ( ~('"'|'\\') | ('\\' .) )* '"'
    | 'R\'' (~'\'')* '\''
    | 'R"'(~'"')* '"'
    ;

BIGINT_LITERAL
    : DIGIT+ 'L'
    ;

SMALLINT_LITERAL
    : DIGIT+ 'S'
    ;

TINYINT_LITERAL
    : DIGIT+ 'Y'
    ;

INTEGER_VALUE
    : DIGIT+
    ;

EXPONENT_VALUE
    : DIGIT+ EXPONENT
    | DECIMAL_DIGITS EXPONENT {isValidDecimal()}?
    ;

DECIMAL_VALUE
    : DECIMAL_DIGITS {isValidDecimal()}?
    ;

FLOAT_LITERAL
    : DIGIT+ EXPONENT? 'F'
    | DECIMAL_DIGITS EXPONENT? 'F' {isValidDecimal()}?
    ;

DOUBLE_LITERAL
    : DIGIT+ EXPONENT? 'D'
    | DECIMAL_DIGITS EXPONENT? 'D' {isValidDecimal()}?
    ;

BIGDECIMAL_LITERAL
    : DIGIT+ EXPONENT? 'BD'
    | DECIMAL_DIGITS EXPONENT? 'BD' {isValidDecimal()}?
    ;

IDENTIFIER
    : (LETTER | DIGIT | '_')+
    ;

BACKQUOTED_IDENTIFIER
    : '`' ( ~'`' | '``' )* '`'
    ;

fragment DECIMAL_DIGITS
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    ;

fragment EXPONENT
    : 'E' [+-]? DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [A-Z]
    ;

SIMPLE_COMMENT
    : '--' ('\\\n' | ~[\r\n])* '\r'? '\n'? -> channel(HIDDEN)
    ;

BRACKETED_COMMENT
    : '/*' {!isHint()}? ( BRACKETED_COMMENT | . )*? ('*/' | {markUnclosedComment();} EOF) -> channel(HIDDEN)
    ;

WS
    : [ \r\n\t]+ -> channel(HIDDEN)
    ;

// Catch-all for anything we can't recognize.
// We use this to be able to ignore and recover all the text
// when splitting statements with DelimiterLexer
UNRECOGNIZED
    : .
    ;


