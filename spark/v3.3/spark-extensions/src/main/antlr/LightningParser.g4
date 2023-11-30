
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
    : registerDataSource | createTable | buildLakeHouse | dropLakeHouse | registerTable
    ;

buildLakeHouse
    : BUILD LAKEHOUSE identifier (LOCATION path = STRING)?
    ;

dropLakeHouse
    : DROP LAKEHOUSE identifier
    ;

registerDataSource
    : REGISTER (OR REPLACE)? dataSourceType = (JDBC | ICEBERG | ORC | PARQUET | DELTA | CSV | XML | JSON)
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

createTable
    : (hintAnnotations+=hintAnnotation)* CREATE TABLE (IF NOT EXISTS)?
      multipartIdentifier LEFT_PAREN createDefinitions (COMMA tableConstraint)* RIGHT_PAREN
      (LAKEHOUSE lakehouse = identifier)?
    ;

hintAnnotation
    : HENT_START annotationStatement HENT_END
    ;

annotationStatement
    : annotationName=identifier LEFT_PAREN parameters+=assignment (COMMA parameters+=assignment)* RIGHT_PAREN
    ;

assignment
    : name=identifier EQ value = STRING
    ;


createDefinitions
    : createDefinition (COMMA createDefinition)*
    ;

createDefinition
    : hintAnnotation? fullColumnName columnDefinition
    ;

columnDefinition
    : dataType columnConstraint*
    ;

columnConstraint
    : NOT NULL                                                                 #notNullColumnConstraint
    | PRIMARY KEY                                                              #primaryKeyColumnConstraint
    | UNIQUE                                                                   #uniqueKeyColumnConstraint
    | FOREIGN KEY referenceDefinition                                          #foreignKeyColumnConstraint
    ;

tableConstraint
    : (CONSTRAINT name=identifier)? PRIMARY KEY indexColumnNames                      #primaryKeyTableConstraint
    | (CONSTRAINT name=identifier)? UNIQUE indexColumnNames                           #uniqueKeyTableConstraint
    | (CONSTRAINT name=identifier)? FOREIGN KEY indexColumnNames referenceDefinition  #foreignKeyTableConstraint
    ;

indexColumnNames
    : LEFT_PAREN fullColumnName (COMMA fullColumnName)* RIGHT_PAREN
    ;

referenceDefinition
    : REFERENCES multipartIdentifier indexColumnNames
      referenceAction?
    ;

referenceAction
    : ON DELETE onDelete=referenceControlType
      (
        ON UPDATE onUpdate=referenceControlType
      )?
    | ON UPDATE onUpdate=referenceControlType
      (
        ON DELETE onDelete=referenceControlType
      )?
    ;

referenceControlType
    : RESTRICT | CASCADE | SET NULL | NO ACTION | SET DEFAULT
    ;


fullColumnName
    : colName=errorCapturingIdentifier
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

dataType
    : complex=ARRAY LT dataType GT                         #complexDataType
    | complex=MAP LT dataType COMMA dataType GT            #complexDataType
    | complex=STRUCT (LT complexColTypeList? GT | NEQ)     #complexDataType
    | INTERVAL from=(YEAR | MONTH) (TO to=MONTH)?          #yearMonthIntervalDataType
    | INTERVAL from=(DAY | HOUR | MINUTE | SECOND)
      (TO to=(HOUR | MINUTE | SECOND))?                    #dayTimeIntervalDataType
    | identifier (LEFT_PAREN INTEGER_VALUE
      (COMMA INTEGER_VALUE)* RIGHT_PAREN)?                 #primitiveDataType
    ;

complexColTypeList
    : complexColType (COMMA complexColType)*
    ;

complexColType
    : identifier COLON? dataType (NOT NULL)?
    ;

booleanValue
    : TRUE | FALSE
    ;

registerTable
    : REGISTER TABLE multipartIdentifier AS query = restOfInput
    ;

restOfInput
    : .*?
    ;

nonReserved
    : ACTION | ARRAY  | ARRAY | BUILD | CSV | DATASOURCE | DAY | DELTA | EXISTS  | HOUR |
    | ICEBERG | INTERVAL | JSON | LAKEHOUSE | MAP | MINUTE | MONTH | NO | OPTIONS
    | ORC | PARQUET | REGISTER | SECOND | SET | STRUCT | TO | XML | YEAR
    ;