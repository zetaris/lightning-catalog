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

## Prerequisite 
Successful connection to Zetaris Lightning. Follow the document “Run-Zetaris-Lightning.md”.
Connecting to different Datasource
## Step to ingest a datasource
### Mssql


#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.rdbms
```
#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS(
    driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433 ",
    databaseName "DemoXXXXX",
    user "adminXXXXXX" ,
    password "XXXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```

**driver** : Name of your driver

**url**: JDBC url for your data source

**databaseName**: name of your database

**user**: Username

**password**: Password

**Namespace**: Container where your data source will be registered


#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;
```
Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.rdbms.<datasourcename>.<Schemaname>.<Tablename>;
```
### Postgres
#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.rdbms
```

#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
driver "org.postgresql.Driver",
url "jdbc:postgresql://zetarispostgres.postgres.database.azure.com:5432/databasename", 
user "zetXXXXX” 
password "XXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```

**driver** : Name of your driver

**url**: JDBC url for your data source

**databaseName**: name of your database

**user**: Username

**password**: Password

**Namespace**: Container where your data source will be registered

#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;
```
#### Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.rdbms.<datasourcename>.<Schemaname>.<Tablename>;
```
### Snowflake

#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.rdbms
```

#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
  url "jdbc:snowflake://XXXXX.ap-southeast-2.snowflakecomputing.com/?warehouse=WH_SMALL&db=SQL_DBM_IMPORT",
  driver "com.snowflake.client.jdbc.SnowflakeDriver",
  user "XXXXXX",
  password "XXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```

**driver** : Name of your driver

**url**: JDBC url for your data source

**databaseName**: name of your database

**user**: Username

**password**: Password

**Namespace**: Container where your data source will be registered

#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;
```

#### Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.rdbms.<datasourcename>.<Schemaname>.<Tablename>;
```

### Oracle

#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.rdbms
```

#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
 url "jdbc:oracle:thin:@oracledemozetaris.australiaeast.cloudapp.azure.com:1521/ databasename",
 driver "oracle.jdbc.OracleDriver",
 user "XXXXX" ,
 password "XXXXX"
) NAMESPACE lightning.datasource.rdbms;
```

**driver** : Name of your driver

**url**: JDBC url for your data source

**databaseName**: name of your database

**user**: Username

**password**: Password

**Namespace**: Container where your data source will be registered

#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;
```

#### Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.rdbms.<datasourcename>.<Schemaname>.<Tablename>;
```

### H2

#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.rdbms
```

#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
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

**Namespace**: Container where your data source will be registered

#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;
```

#### Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.rdbms.<datasourcename>.<Schemaname>.<Tablename>;
```


### Iceberg

#### Step 1:  Create a namespace.
```bash
Create a namespace lightning.datasource.iceberg
```

#### Step 2: Register data source in the namespace created. Run data source register command.
```bash
REGISTER OR REPLACE iceberg DATASOURCE icebergdb OPTIONS(
type "hadoop",
warehouse "/tmp/iceberg-warehouse"
) NAMESPACE lightning.datasource.iceberg
```

**type** : Will be "hadoop"

**warehouse**: File path where you iceberg data lake need to be created

**databaseName**: name of your database


**Namespace**: Container where your data source will be registered

#### Step 3: Check if data source is registered. The command will show schema in MSSQL database.
```bash
SHOW NAMESPACES IN lightning.datasource.iceberg.<datasourcename>;

SHOW Tables IN lightning.datasource.iceberg.<datasourcename>.<Schemaname>;
```

#### Step 4: Run queries on registered data source.
```bash
select * from lightning.datasource.iceberg.<datasourcename>.<Schemaname>.<Tablename>;
```
