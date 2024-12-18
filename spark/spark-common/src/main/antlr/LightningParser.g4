
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
    : registerDataSource | registerCatalog | createTable | compileUSL | activateUSLTable | loadUSL | updateUSL |
      removeUSL | registerDQ | listDQ | runDQ | removeDQ | showNamespacesOrTables | showDQResult
    ;

showNamespacesOrTables
    : SHOW NAMESPACES OR TABLES IN namespace=multipartIdentifier
    ;

showDQResult
    : SHOW DQ (VALID | INVALID) RECORD name = identifier TABLE table=multipartIdentifier
    ;

removeDQ
    : REMOVE DQ name = identifier TABLE table=multipartIdentifier
    ;

runDQ
    : RUN DQ (name = identifier)? TABLE table=multipartIdentifier
    ;

listDQ
    : LIST DQ USL usl=multipartIdentifier
    ;

registerDQ
    : REGISTER DQ name = identifier TABLE table=multipartIdentifier AS expression = restOfInput
    ;

loadUSL
    : LOAD USL dbName = identifier NAMESPACE namespace=multipartIdentifier
    ;

updateUSL
    : UPDATE USL dbName = identifier NAMESPACE namespace=multipartIdentifier AS json = restOfInput
    ;

removeUSL
    : REMOVE USL dbName = identifier NAMESPACE namespace=multipartIdentifier
    ;

activateUSLTable
    : ACTIVATE USL TABLE table=multipartIdentifier AS query = restOfInput
    ;

compileUSL
    : COMPILE USL (IF NOT EXISTS)? dbName = identifier (DEPLOY)? NAMESPACE namespace = multipartIdentifier DDL ddls = restOfInput
    ;

registerDataSource
    : REGISTER (OR REPLACE)? dataSourceType = (JDBC | ICEBERG | ORC | PARQUET | DELTA | AVRO | CSV | XML | JSON | PDF | TEXT | IMAGE | VIDEO | AUDIO)
      DATASOURCE identifier (OPTIONS options=propertyList)?
      NAMESPACE multipartIdentifier
      (TAG tagDefinitions)?
     ;

tagDefinitions
    : LEFT_PAREN tagDefinition (COMMA tagDefinition)* RIGHT_PAREN
    ;

tagDefinition
    : identifier dataType
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

createTable
    : (hintAnnotations+=hintAnnotation)* CREATE TABLE (IF NOT EXISTS)?
      (tablename = identifier) LEFT_PAREN createDefinitions (COMMA tableConstraint)* RIGHT_PAREN
      (NAMESPACE namespace = multipartIdentifier)?
    ;

hintAnnotation
    : HENT_START (AT)annotationStatement HENT_END
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

referenceDefinition
    : REFERENCES multipartIdentifier indexColumnNames
      referenceAction?
    ;

indexColumnNames
    : LEFT_PAREN fullColumnName (COMMA fullColumnName)* RIGHT_PAREN
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

nonReserved
    : ACTION | ARRAY  | ARRAY | AVRO | BUILD | CATALOG | CSV | DATASOURCE | DAY | DELTA | DQ | EXISTS  | HOUR |
    | ICEBERG | INTERVAL | INVALID | JDBC | JSON | LAKEHOUSE | MAP | MINUTE | MONTH | NAME | NAMESPACE | NAMESPACES | NO | OPTIONS
    | ORC | PARQUET | RECORD | REGISTER | REMOVE | SECOND | SET | STRUCT | TO | USL | VALID | XML | YEAR
    ;