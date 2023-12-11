
parser grammar LightningParser;
@header {package com.zetaris.lightning.parser;}
options { tokenVocab=LightningLexer; }

singleStatement
    : statement SEMICOLON* EOF
    ;

statement
    : ddlStatement
    ;

ddlStatement
    : registerDataSource | registerCatalog
    ;

registerDataSource
    : REGISTER (OR REPLACE)? dataSourceType = (JDBC | ICEBERG | ORC | PARQUET | DELTA | AVRO | CSV | XML | JSON)
      DATASOURCE identifier (OPTIONS options=propertyList)?
      NAMESPACE multipartIdentifier
    ;

propertyList
    : LEFT_PAREN property (COMMA property)* RIGHT_PAREN
    ;

property
    : key=propertyKey (EQ? value=propertyValue)?
    ;

propertyKey
    : identifier (DOT identifier)*
    | STRING
    ;

propertyValue
    : INTEGER_VALUE
    | DECIMAL_VALUE
    | booleanValue
    | STRING
    ;

errorCapturingIdentifier
    : identifier errorCapturingIdentifierExtra
    ;

errorCapturingIdentifierExtra
    : (MINUS identifier)+    #errorIdent
    |                        #realIdent
    ;

multipartIdentifier
    : parts+=errorCapturingIdentifier (DOT parts+=errorCapturingIdentifier)*
    ;

identifier
    : strictIdentifier
    ;

strictIdentifier
    : IDENTIFIER              #unquotedIdentifier
    | quotedIdentifier        #quotedIdentifierAlternative
    | nonReserved             #unquotedIdentifier
    ;

quotedIdentifier
    : BACKQUOTED_IDENTIFIER
    ;

booleanValue
    : TRUE | FALSE
    ;

restOfInput
    : .*?
    ;
registerCatalog
    : REGISTER (OR REPLACE)? CATALOG identifier (OPTIONS options=propertyList)?
      SOURCE source = multipartIdentifier
      (NAME LIKE pattern = STRING)?
      NAMESPACE namespace = multipartIdentifier
    ;

nonReserved
    : ACTION | ARRAY  | ARRAY | BUILD | CSV | DATASOURCE | DAY | DELTA | EXISTS  | HOUR |
    | ICEBERG | INTERVAL | JSON | LAKEHOUSE | MAP | MINUTE | MONTH | NO | OPTIONS
    | ORC | PARQUET | REGISTER | SECOND | SET | STRUCT | TO | XML | YEAR
    ;