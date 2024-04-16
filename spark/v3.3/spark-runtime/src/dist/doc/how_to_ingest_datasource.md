<!--
Copyright 2023 ZETARIS Pty Ltd

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies
or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
-->

## Lightning has 2 top level namespaces :

* datasource

All the external source systems is being registered in this namespace first. It only keeps source endpoint and credentials, for example, JDBC URL, user and password

* metastore

Once the external source is registered then user can register specific tables in this namespace. Unlikely datasource namespace, it keeps as much information as possible for source system, for example column name, data type, and all database constraints. These meta information will be used for query optimization.

In below scenario, our aim to register different data source using lightning query.You have the flexibility to establish connections with any database that supports JDBC connectivity

### Create a namespace.
Having a hierarchical structure is consistently beneficial. In this particular scenario of registering a database,datalake and filesource we will establish new namespaces termed "rdbms","iceberg" and "file" respectively within "datasource" and proceed to register all data sources within those namespace.
```bash
--spark sql to create namespace rdbms where all the database will get registered
CREATE NAMESPACE lightning.datasource.rdbms
```
```bash
--spark sql to create namespace iceberg where all the iceberg datalakes will get registered
CREATE NAMESPACE lightning.datasource.iceberg
```
```bash
--spark sql to create namespace file where all the filesource will get registered
CREATE NAMESPACE lightning.datasource.file
```
### 1. Register datasource under rdbms namespace
You have the flexibility to establish connections with any database that supports JDBC connectivity. Some of the examplesto register different databases are below:
```bash
--lightning sql to register mssql
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCENAME> OPTIONS(
    driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433 ",
    databaseName "DemoXXXXX",
    user "adminXXXXXX" ,
    password "XXXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```
```bash
--lightning sql to register postgres
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
driver "org.postgresql.Driver",
url "jdbc:postgresql://zetarispostgres.postgres.database.azure.com:5432/databasename", 
user "zetXXXXX",
password "XXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```
```bash
--lightning sql to register snowflake
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
  url "jdbc:snowflake://XXXXX.ap-southeast-2.snowflakecomputing.com/?warehouse=WH_SMALL&db=SQL_DBM_IMPORT",
  driver "com.snowflake.client.jdbc.SnowflakeDriver",
  user "XXXXXX",
  password "XXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```
```bash
--lightning sql to register oracle
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
 url "jdbc:oracle:thin:@oracledemozetaris.australiaeast.cloudapp.azure.com:1521/ databasename",
 driver "oracle.jdbc.OracleDriver",
 user "XXXXX" ,
 password "XXXXX"
) NAMESPACE lightning.datasource.rdbms;
```
```bash
--lightning sql to register h2
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS(
     driver "org.h2.Driver", 
     url "jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1",
     user "xxxx",
     password "xxxxx"
     ) NAMESPACE lightning.datasource.rdbms;
```

**driver** : Name of your driver

**url**: JDBC url for your data source

**databaseName**: name of your database

**user**: Username

**password**: Password

**Namespace**: Container where your database will be registered

### 2. Register iceberg datalake under iceberg namespace

```bash
--lightning sql to register iceberg datalake
REGISTER OR REPLACE iceberg DATASOURCE icebergdb OPTIONS(
type "hadoop",
warehouse "/tmp/iceberg-warehouse"
) NAMESPACE lightning.datasource.iceberg
```

**type** : Will be "hadoop"

**warehouse**: File path where you iceberg data lake need to be created

**Namespace**: Container where your data source will be registered

### 3. Register filesource under file namespace
```bash
--lightning sql to register csv file
REGISTER OR REPLACE CSV DATASOURCE customers OPTIONS (
header "true",
inferSchema "true",
path "/home/zetaris/data/csv/customer.csv"
) NAMESPACE lightning.datasource.file.csv
```
```bash
--lightning sql to register orc file
REGISTER OR REPLACE ORC DATASOURCE customers OPTIONS (
header "true",
inferSchema "true",
path "/home/zetaris/data/orc/customer.orc"
) NAMESPACE lightning.datasource.file.orc
```
```bash
--lightning sql to register json file
REGISTER OR REPLACE JSON DATASOURCE customers OPTIONS (
header "true",
inferSchema "true",
path "/home/zetaris/data/json/customer.json"
) NAMESPACE lightning.datasource.file.json
```
```bash
--lightning sql to register parquet file
REGISTER OR REPLACE PARQUET DATASOURCE customers OPTIONS (
header "true",
inferSchema "true",
path "/home/zetaris/data/paquet/customer.parquet"
) NAMESPACE lightning.datasource.file.parquet
```
```bash
--lightning sql to register avro file
REGISTER OR REPLACE AVRO DATASOURCE customers OPTIONS (
header "true",
inferSchema "true",
path "/home/zetaris/data/avro/customer.avro"
) NAMESPACE lightning.datasource.file.avro
```

### 3: Check if data source is registered under different namespaces
This will show all the diffent database, datalake and filesource registered
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms;
SHOW NAMESPACES IN lightning.datasource.iceberg;
SHOW NAMESPACES IN lightning.datasource.file;
```
### 4: Check all the schemas in particular register database,datalake or filesource
```bash
SHOW Tables IN lightning.datasource.rdbms.postgres_db;
```
### 5: Check the tables in particular register database,datalake or filesource
```bash
SHOW Tables IN lightning.datasource.rdbms.postgres_db.HR_schema;
```
### 6: Check the column inside table in particular register database,datalake or filesource
```bash
DESCRIBE TABLE lightning.datasource.rdbms.postgres_db.HR_schema.employee_table
```
#### 7: Run query on particular register database,datalake or filesource table
```bash
select * from lightning.datasource.rdbms.postgres_db.HR_schema.employee_table
```
