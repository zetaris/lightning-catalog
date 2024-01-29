Prerequisite 
1.	Successful connection to Zetaris open-source jars. Follow the document “How to run Zetaris Open-Source Code”.
Connecting to different Datasource
Mssql

Step 1:  Create a namespace.
Create a namespace lightning.datasource.rdbms

Step 2: Register data source in the namespace created. Run data source register command.
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS(
    driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433 ",
    databaseName "DemoXXXXX",
    user "adminXXXXXX" ,
    password "XXXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;

driver: Name of your driver
url: JDBC url for your data source
databaseName: name of your database
user: Username
password: Password
Namespace: Container where your data source will be registered

Step 3: Check if data source is registered. The command will show schema in MSSQL database.
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;

Step 4: Run queries on registered data source.
select * from lightning.datasource.rdbms. .<datasourcename>.<Schemaname>.<Tablename>;

Postgres
Step 1:  Create a namespace.
Create a namespace lightning.datasource.rdbms

Step 2: Register data source in the namespace created. Run data source register command.
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
driver "org.postgresql.Driver",
url "jdbc:postgresql://zetarispostgres.postgres.database.azure.com:5432/databasename", 
user "zetXXXXX” 
password "XXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
driver: Name of your driver
url: JDBC url for your data source
databaseName: name of your database
user: Username
password: Password
Namespace: Container where your data source will be registered

Step 3: Check if data source is registered. The command will show schema in MSSQL database.
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;

Step 4: Run queries on registered data source.
select * from lightning.datasource.rdbms. .<datasourcename>.<Schemaname>.<Tablename>;

Snowflake

Step 1:  Create a namespace.
Create a namespace lightning.datasource.rdbms

Step 2: Register data source in the namespace created. Run data source register command.
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
  url "jdbc:snowflake://XXXXX.ap-southeast-2.snowflakecomputing.com/?warehouse=WH_SMALL&db=SQL_DBM_IMPORT",
  driver "com.snowflake.client.jdbc.SnowflakeDriver",
  user "XXXXXX",
  password "XXXXXX"
) NAMESPACE lightning.datasource.rdbms

driver: Name of your driver
url: JDBC url for your data source
user: Username
password: Password
Namespace: Container where your data source will be registered

Step 3: Check if data source is registered. The command will show schema in MSSQL database.
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;

Step 4: Run queries on registered data source.
select * from lightning.datasource.rdbms. .<datasourcename>.<Schemaname>.<Tablename>;


Oracle

Step 1:  Create a namespace.
Create a namespace lightning.datasource.rdbms

Step 2: Register data source in the namespace created. Run data source register command.
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
 url "jdbc:oracle:thin:@oracledemozetaris.australiaeast.cloudapp.azure.com:1521/ databasename",
 driver "oracle.jdbc.OracleDriver",
 user "XXXXX" ,
 password "XXXXX"
) NAMESPACE lightning.datasource.rdbms;
driver: Name of your driver
url: JDBC url for your data source
databaseName: name of your database
user: Username
password: Password
Namespace: Container where your data source will be registered

Step 3: Check if data source is registered. The command will show schema in MSSQL database.
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;

Step 4: Run queries on registered data source.
select * from lightning.datasource.rdbms. .<datasourcename>.<Schemaname>.<Tablename>;


H2

Step 1:  Create a namespace.
Create a namespace lightning.datasource.rdbms

Step 2: Register data source in the namespace created. Run data source register command.
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS(
     driver "org.h2.Driver", 
     url "jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1",
     user "xxxx",
     password "xxxxx"
     ) NAMESPACE lightning.datasource.rdbms;
driver: Name of your driver
url: JDBC url for your data source
user: Username
password: Password
Namespace: Container where your data source will be registered

Step 3: Check if data source is registered. The command will show schema in MSSQL database.
SHOW NAMESPACES IN lightning.datasource.rdbms.<datasourcename>;

SHOW Tables IN lightning.datasource.rdbms.<datasourcename>.<Schemaname>;

Step 4: Run queries on registered data source.
select * from lightning.datasource.rdbms. .<datasourcename>.<Schemaname>.<Tablename>;

