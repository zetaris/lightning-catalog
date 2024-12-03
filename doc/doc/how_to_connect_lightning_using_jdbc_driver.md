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

This document outlines all the steps for users to run queries over external data sources by ingesting only metadata into the Lightning Catalog. Users can use any BI or JDBC client to run queries.

## 1. Prerequisites
Lightning Catalog has been tested with:
* JRE 1.8, 11, 17, 18, 19.
* Apache Spark (versions 3.4.x, 3.5.x).

## 2. Install Spark
Lightning Catalog leverages Apache Spark as a compute engine. The Spark package can be downloaded from [Apache Spark Downloads](https://spark.apache.org/downloads.html).
The `SPARK_HOME` environment variable must be set.

## 3. Install Lightning Catalog
The Lightning Catalog package can be downloaded from the GitHub release page: [GitHub Releases](https://github.com/zetaris/lightning-catalog/releases) or built from source.

Assume the `LIGHTNING_HOME` variable points to the installation directory.

## 4. Run Hive Thrift Server for JDBC Connectivity
* Run `${LIGHTNING_HOME}/bin/start-thriftserver-light.sh` to start the Hive Thrift server.

## 5. Connect to Hive Thrift Server from JDBC Client
In this example, **DBeaver** is used to connect to the Thrift server.

* Select **Apache Hive** as the database and JDBC driver.
  ![Database Selection](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/dbeaver-database.png)

* Enter `jdbc:hive2://localhost:10000` in the JDBC URL section.
  ![JDBC URL](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/dbeaver-jdbc.png)

* The "default" and "global_temp" schemas will be displayed by default when the connection is made.
  ![SQL Editor](https://github.com/zetaris/lightning-catalog/blob/master/doc/images/dbeaver-query.png)

## 6. Create Namespace
Lightning Catalog has two top-level namespaces:

* **Datasource**:
  - All external source systems are registered in this namespace first.
  - It only stores source endpoints and credentials, such as JDBC URL, username, and password.

* **Metastore**:
  - Once an external source is registered, users can create two types of namespaces:
    - **Normal namespace**: Stores different tables from different data sources within the same namespace.
    - **Unified Semantic Layer (USL)**: Compiles and deploys DDLs and deploys all tables over the underlying data sources.

```bash
-- Spark SQL command to create an RDBMS namespace
CREATE NAMESPACE lightning.datasource.rdbms;
```

##7. Register Data Source & Browse Schema, Tables
```bash   
   REGISTER OR REPLACE JDBC DATASOURCE qa_mssql2 OPTIONS(
   driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
   url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433",
   databaseName "xxx",
   user "xxx",
   password "xxx"
   ) NAMESPACE lightning.datasource.rdbms;

-- Show namespaces in the RDBMS namespace
SHOW NAMESPACES IN lightning.datasource.rdbms.qa_mssql2;

-- This will display:
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

-- Show tables in the "tpch" schema
SHOW TABLES IN lightning.datasource.rdbms.qa_mssql2.tpch;

-- This will display:
namespace   | tableName  | isTemporary
-----------------------------------------
tpch        | customer   | false
tpch        | lineitem   | false
tpch        | nation     | false
tpch        | orders     | false
tpch        | part       | false
tpch        | partsupp   | false
tpch        | region     | false
tpch        | supplier   | false
```

## 8. Run query
Users can run queries using fully qualified namespaces.
```bash
SELECT *
FROM lightning.datasource.rdbms.qa_mssql2.tpch.customer
LIMIT 10;
```

## 9. Joining Other Data Sources
```bash
SELECT dt.d_year,
item.i_brand_id AS brand_id,
item.i_brand AS brand,
SUM(ss_ext_sales_price) AS sum_agg
FROM lightning.datasource.rdbms.postgres_db.HR_schema.TPCDS_DB.date_dim dt
JOIN lightning.datasource.rdbms.postgres_db.finance_schema.TPCDS_DB.store_sales s
ON dt.d_date_sk = s.ss_sold_date_sk
JOIN lightning.datasource.iceberg.icebergdb.TPCDS_DB.item i
ON s.ss_item_sk = i.i_item_sk
WHERE i.i_manufact_id = 128
AND dt.d_moy = 11
GROUP BY dt.d_year, i.i_brand, i.i_brand_id
ORDER BY dt.d_year, sum_agg DESC, brand_id
LIMIT 100;
```
