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

## Data virtualization

Data virtualization is an approach to data management that allows applications to retrieve and manipulate data without requiring technical details about the underlying data sources.
It provides a unified interface for accessing data from multiple disparate sources, such as databases, APIs, cloud services, and more, regardless of their location or format.
**Scenario 1:**
In the scenario, our aim is to join 2 tables from different database produce a joined table.

### 1: Register different datasource for which you want to join the tables
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
In above scenario, we have registered to different database under the name space "ightning.datasource.rdbms" :- Mssql db and postgres db

### 2: Check the tables you want join(Not mandatory step)
```bash
SHOW Tables IN lightning.datasource.rdbms.postgres_db.HR_schema;
SHOW Tables IN lightning.datasource.rdbms.mssql_db.finance_schema;
```

### 3: Another namespace can be created inside database
User can create another namespace inside the registered database as long as the user has right permission to do create schema.
```bash
CREATE NAMESPACE lightning.datasource.rdbms.postgres_db.nytaxis;
````

### 4: Create a table inside a namespace
A new table can be created inside the namespace. 
```bash
CREATE TABLE lightning.datasource.rdbms.postgres_db.nytaxis.taxis (
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
) PARTITIONED BY (vendor_id);
```
you can also create a table using another table from different Db.In below example user is creating
taxi table in postgres db and getting the data from iceberg datalake taxi table.

```bash
CREATE TABLE lightning.datasource.rdbms.postgres_db.nytaxis.taxis 
as
select * from lightning.datasource.iceberg.icebergdb.taxis
```

### 5: Inserting the values in the table in case it is created from scratch
```bash
INSERT INTO lightning.datasource.rdbms.postgres_db.nytaxis.taxis
VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y");
```
### 6: Drop a namespace in a db
In this example, we will drop the namespace created above
```bash
DROP NAMESPACE lightning.datasource.rdbms.postgres_db.nytaxis;
```

### 7: Drop a table in namespace which created inside db
In this example, we will drop the table created above
```bash
DROP table lightning.datasource.rdbms.postgres_db.nytaxis.taxis;
```

### 8: Describe a table to show it metadata
In this example, we will show the column inside a table
```bash
Describe table lightning.datasource.rdbms.postgres_db.nytaxis.taxis;
```

### 9: Join the tables based on business key. some examples are as below;
We can use different type join. example 1 and 2 show inner and left join
```bash
select * from lightning.datasource.rdbms.postgres_db.HR_schema.customers c
inner join
lightning.datasource.rdbms.mssql_db.finance_schema.order o
on c.c_custkey=o.o_custkey
```

```bash
select * from lightning.datasource.rdbms.postgres_db.HR_schema.customers c
Left join
lightning.datasource.rdbms.mssql_db.finance_schema.Nation o
on c.c_custkey=n.n_nationkey
```

In below query we are using table from 3 different database
```bash
select dt.d_year,  item.i_brand_id brand_id,  item.i_brand brand,  sum(ss_ext_sales_price) sum_agg
from 
lightning.datasource.rdbms.postgres_db.HR_schema.TPCDS_DB.date_dim dt
,lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.store_sales s
,lightning.datasource.iceberg.icebergdb.TPCDS_DB.item i
where dt.d_date_sk = s.ss_sold_date_sk
and s.ss_item_sk = i.i_item_sk
and i.i_manufact_id = 128
and dt.d_moy = 11
group by dt.d_year,  i.i_brand,  i.i_brand_id
order by dt.d_year,  sum_agg
desc,  brand_id
limit 100;
```

we can also use different aggregation function and group by. for example:
```bash
select  i_item_id,
avg(ss_quantity) agg1,
sum(ss_list_price) agg2,
count(ss_coupon_amt) agg3,
max(ss_sales_price) agg4
from lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.store_sales
, lightning.datasource.rdbms.postgres_db.fianace_schema.TPCDS_DB.customer_demographics
, lightning.datasource.rdbms.postgres_db.HR_schema.TPCDS_DB.date_dim
, lightning.datasource.iceberg.icebergdb.TPCDS_DB.item
, lightning.datasource.iceberg.icebergdb.TPCDS_DB.promotion
where ss_sold_date_sk = d_date_sk and
ss_item_sk = i_item_sk and
ss_cdemo_sk = cd_demo_sk and
ss_promo_sk = p_promo_sk and
cd_gender = 'M' and
cd_marital_status = 'S' and
cd_education_status = 'College' and
(p_channel_email = 'N' or p_channel_event = 'N') and
d_year = 2000
group by i_item_id
order by i_item_id
limit 100;
```
we can also do union and intersection of tables from different db

```bash
select  i_product_name
             ,i_brand
             ,i_class
             ,i_category
             ,avg(inv_quantity_on_hand) qoh
       from lightning.datasource.rdbms.postgres_db.nytaxis.taxis
       group by        i_product_name
                       ,i_brand
                       ,i_class
                       ,i_category
UNION
select  i_product_name
             ,i_brand
             ,i_class
             ,i_category
             ,avg(inv_quantity_on_hand) qoh
       from lightning.datasource.iceberg.icebergdb.taxis
       group by        i_product_name
                       ,i_brand
                       ,i_class
                       ,i_category
                       

 limit 100;
 ```
### 10: ANSI SQL supports several types of joins, including:

**INNER JOIN**: Returns rows that have matching values in both tables being joined.

**LEFT JOIN (or LEFT OUTER JOIN)**: Returns all rows from the left table and matching rows from the right table. If there is no match, NULL values are returned for the columns from the right table.

**RIGHT JOIN (or RIGHT OUTER JOIN)**: Returns all rows from the right table and matching rows from the left table. If there is no match, NULL values are returned for the columns from the left table.

**FULL JOIN (or FULL OUTER JOIN)**: Returns all rows when there is a match in either the left or right table. If there is no match, NULL values are returned for the columns from the table without a matching row.

**CROSS JOIN**: Returns the Cartesian product of the two tables, i.e., all possible combinations of rows from both tables.

**SELF JOIN**: Joins a table to itself, typically used when a table has a foreign key that references another row in the same table.