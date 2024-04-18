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

# Data virtualization

Data virtualization is an approach to data management that allows applications to retrieve and manipulate data without requiring technical details about the underlying data sources.
It provides a unified interface for accessing data from multiple disparate sources, such as databases, APIs, cloud services, and more, regardless of their location or format.

## (**Browse Schema & tables)**
User can browse registered schema and its tables using Spark SQL command

### 1. Register datasource
User can register external datasource into Lightning catalog by providing JDBC details.

```bash
-- lightning command to register mssql
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS(
    driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
    url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433 ",
    databaseName "DemoXXXXX",
    user "adminXXXXXX" ,
    password "XXXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```
```bash
-- lightning command to register postgres
REGISTER OR REPLACE JDBC DATASOURCE <DATASOURCE NAME> OPTIONS (
driver "org.postgresql.Driver",
url "jdbc:postgresql://zetarispostgres.postgres.database.azure.com:5432/databasename", 
user "zetXXXXX",
password "XXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;
```

<DATASOURCE NAME> must follow identifier format in spark:  

https://spark.apache.org/docs/3.3.1/sql-ref-identifier.html#:~:text=An%20identifier%20is%20a%20string,delimited%20identifiers%20are%20case%2Dinsensitive.

In above scenario, we have registered to different database under the name space "ightning.datasource.rdbms",  mssql_db, prstsgres_db for instance.

### 2. Browse schema & tables
```bash
-- spark sql command
SHOW NAMESPACES IN lightning.datasource.rdbms.postgres_db;
```

This will display all the schema under the registered database.

### 3: Check the tables you want join(Not mandatory step)
```bash
-- Spark SQL command to show tables
SHOW Tables IN lightning.datasource.rdbms.postgres_db.HR_schema;
SHOW Tables IN lightning.datasource.rdbms.mssql_db.finance_schema;

-- Spark SQL command to browse schema
DESCRIBE TABLE lightning.datasource.rdbms.postgres_db.HR_schema.CUSTOMER
```

### 4: Another namespace can be created inside database
User can create another namespace inside the registered database as long as the user has right permission to do create schema.

```bash
CREATE NAMESPACE lightning.datasource.rdbms.postgres_db.nytaxis;
````

### 5: Create a new table inside a namespace
A new table can be created inside the namespace. 
```bash
CREATE TABLE lightning.datasource.rdbms.postgres_db.nytaxis.taxis (
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
);
```
In this example, taxis table is created under nytaxis schema for the registered PostgreSQL db.

you can also create a table using another table from different Db.In below example user is creating
taxi table in postgres db and getting the data from iceberg datalake taxi table.

```bash
CREATE TABLE lightning.datasource.rdbms.postgres_db.nytaxis.taxis 
AS
SELECT * FROM lightning.datasource.iceberg.icebergdb.taxis
```

### 6: Inserting the values in the table in case it is created from scratch
```bash
INSERT INTO lightning.datasource.rdbms.postgres_db.nytaxis.taxis
VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y");
```
### 7: Drop a namespace in a db
In this example, we will drop the namespace created above
```bash
DROP NAMESPACE lightning.datasource.rdbms.postgres_db.nytaxis;
```

### 8: Drop a table in namespace which created inside db
In this example, we will drop the table created above
```bash
DROP table lightning.datasource.rdbms.postgres_db.nytaxis.taxis;
```

### 9: Describe a table to show it metadata
In this example, we will show the column inside a table
```bash
DESCRIBE table lightning.datasource.rdbms.postgres_db.nytaxis.taxis;
```

### 10: Join the tables based on business key. some examples are as below;
We can use different type join. example 1 and 2 show inner and left join
```bash
SELECT * 
FROM lightning.datasource.rdbms.postgres_db.HR_schema.customers c
INNER JOIN lightning.datasource.rdbms.mssql_db.finance_schema.order o
ON c.c_custkey=o.o_custkey
```

```bash
SELECT * 
FROM lightning.datasource.rdbms.postgres_db.HR_schema.customers c
LEFT JOIN lightning.datasource.rdbms.mssql_db.finance_schema.Nation o
ON c.c_custkey=n.n_nationkey
```

In below query we are using table from 3 different database
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

we can also use different aggregation function and group by. for example:
```bash
SELECT  i_item_id,
   avg(ss_quantity) agg1,
   sum(ss_list_price) agg2,
   count(ss_coupon_amt) agg3,
   max(ss_sales_price) agg4
FROM lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.store_sales,
     lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.customer_demographics,
     lightning.datasource.rdbms.postgres_db.HR_schema.TPCDS_DB.date_dim,
     lightning.datasource.iceberg.icebergdb.TPCDS_DB.item,
     lightning.datasource.iceberg.icebergdb.TPCDS_DB.promotion
WHERE ss_sold_date_sk = d_date_sk
AND ss_item_sk = i_item_sk 
AND ss_cdemo_sk = cd_demo_sk
AND ss_promo_sk = p_promo_sk
AND cd_gender = 'M'
AND cd_marital_status = 'S'
AND cd_education_status = 'College'
AND (p_channel_email = 'N' OR p_channel_event = 'N')
AND d_year = 2000
BROUP BY i_item_id
ORDER BY i_item_id
LIMIT 100;
```

# Spark SQL Reference
https://spark.apache.org/docs/3.3.1/sql-programming-guide.html
