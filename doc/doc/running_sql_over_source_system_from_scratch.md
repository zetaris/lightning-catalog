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

This document articulate all the steps for user to run query over external data sources by ingesting only metata data into Lightning catalog.
User can use any BI or JDBC client to run query.

## 1. prerequisites
Lighting Catalog needs:  
* JRE 1.8 or above
* Apache Spark(ver3.3.x, ver3.4.x, ver3.5.x) : https://spark.apache.org/

## 2. Install Spark
Lightning catalog leverages Apache Spark as main query engine. Spark package can be downloaded https://spark.apache.org/downloads.html

## 3. run hive thrift server providing jdbc connectivity.
Assuming SPARK_HOME environment was set to unzipped directory from the downloaded spark, and LIGT_HOME was set for the directory of Lighting Catalog

open $SPARK_HOME/sbin/start-thriftserver.sh and replace the existing spark submit command with the following:

exec "${SPARK_HOME}"/sbin/spark-daemon.sh submit $CLASS 1 --name "Thrift JDBC/ODBC Server" \
--conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
--conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
--conf spark.sql.catalog.lightning.type=hadoop \
--conf spark.sql.catalog.lightning.warehouse=/tmp/ligt-model \
--conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
--conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
--conf spark.executor.extraClassPath=$LIGT_HOME/lib/* \
--conf spark.driver.extraClassPath=$LIGT_HOME/lib/* \
--jars $LIGT_HOME/lib/lightning-spark-extensions-3.5_2.12-0.1.jar,$LIGT_HOME/jdbc-lib/* "$@"

## 4. Connect to hive thrift server from jdbc client.
In this example, DBeaver is being used to connect to thrift server.

* Select apache hive for the database and jdbc driver.  
  ![database selection](https://drive.google.com/file/d/1gxiWqFRbvJB8_GLVeprx63J8RDqqscjN/view?usp=sharing)
  
* Enter jdbc:hive2://localhost:10000 for JDBC url section.  
  ![jdbc url](https://drive.google.com/file/d/1w_yNRHimV1ZhEZ1y21It-HKrKV2CoBCB/view?usp=sharing)
  
* "default" and "global_temp" schema will be displayed by default when connection is made.  
  ![sql editor](https://drive.google.com/file/d/1EgNW4jQHynTlXlgQkRYQqUhoKYbHW7cG/view?usp=sharing)
  
## 5. Create namespace.
Lightning has 2 top level namespaces :

* datasource

  All the external source systems is being registered in this namespace first.
  It only keeps source endpoint and credentials, for example, JDBC URL, user and password


* metastore

  Once the external source is registered then user can register specific tables in this namespace.
  Unlikely datasource namespace, it keeps as much information as possible for source system,
  for example column name, data type, and all database constraints. These meta information will be used for query optimization
  
```bash
-- Spark SQL command to create rdbms namespace
CREATE NAMESPACE lightning.datasource.rdbms 
```
## 6. Register data source & browse schema, tables

```bash
REGISTER OR REPLACE JDBC DATASOURCE qa_mssql2 OPTIONS(
driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433",
databaseName "xxx",
user "xxx" ,
password "xxx"
) NAMESPACE lightning.datasource.rdbms;

show namespaces in lightning.datasource.rdbms.qa_mssql2;

will display :
namespace
---------------
db_accessadmin
db_backupoperator
db_datareader
db_datawriter
db_ddladmin
db_denydatareader
db_denydatawriter
db_owner
db_securityadmin
dbo
guest
INFORMATION_SCHEMA
sys
mcri
MCRI_REDCAP
MCRI_Sales
mcri_test
nkop
source_data1
tdx
tenzing
tenzing1
tenzing2
tpch
tpch1

show tables in lightning.datasource.rdbms.qa_mssql2.tpch;

will display :
namespace tableName  isTemporary
--------------------------------- 
tpch	  customer	 false
tpch	  lineitem	 false
tpch	  nation	 false
tpch	  orders	 false
tpch	  part	     false
tpch	  partsupp	 false
tpch	  region	 false
tpch	  supplier	 false
```
## 7. run query
User can run query along with namespace.

```bash
SELECT *
FROM lightning.datasource.rdbms.qa_mssql2.tpch.customer
LIMIT 10
```

## 8. joining other data source.
Once data source registered, they can be joined along with registered namespaces.

```bash
SELECT dt.d_year,  item.i_brand_id brand_id,  item.i_brand brand,  sum(ss_ext_sales_price) sum_agg
FROM lightning.datasource.rdbms.postgres_db.HR_schema.TPCDS_DB.date_dim dt,
     lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.store_sales s,
     lightning.datasource.iceberg.icebergdb.TPCDS_DB.item i
WHERE dt.d_date_sk = s.ss_sold_date_sk
AND s.ss_item_sk = i.i_item_sk
AND i.i_manufact_id = 128
AND dt.d_moy = 11
GROUP BY dt.d_year,  i.i_brand,  i.i_brand_id
ORDER BY dt.d_year,  sum_agg DESC,  brand_id
LIMIT 100;
```