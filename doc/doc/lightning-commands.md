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
## namespaces.
Lightning has 2 top level namespaces :

* datasource

  All the external source systems is being registered in this namespace first.
  It only keeps source endpoint and credentials, for example, JDBC URL, user and password 


* metastore

  Once the external source is registered then user can register specific tables in this namespace.
  Unlikely datasource namespace, it keeps as much information as possible for source system, 
  for example column name, data type, and all database constraints. These meta information will be used for query optimization


### create name space
```bash
CREATE NAMESPACE lightning.datasource.identifier
```
There is no limitation of multi level of identifier like:
```bash
-- Spark SQL command
CREATE NAMESPACE lightning.datasource.parent1.parent2.parent3...
```

Apache Spark identifier:  
https://spark.apache.org/docs/3.3.1/sql-ref-identifier.html#:~:text=An%20identifier%20is%20a%20string,delimited%20identifiers%20are%20case%2Dinsensitive.

### drop name space
```bash
-- Spark SQL command
DROP NAMESPACE lightning.datasource.identifier
```


### Register rdbms data sources
```bash
-- Spark SQL command to create rdbms namespace
CREATE NAMESPACE lightning.datasource.rdbms

-- Lightning command to register h2 database under rdbms
REGISTER [OR REPLACE] JDBC DATASOURCE h2 OPTIONS(
url "jdbc:h2:mem:dbname;DB_CLOSE_DELAY=-1",
user "admin",
password ""
) NAMESPACE lightning.datasource.rdbms
```

once registered, user can navigate source system:

```bash
-- Spark SQL command
SHOW NAMESPACES IN lightning.datasource.rdbms.h2
```
will display all schemas under that database.

```bash
-- Spark SQL command
SHOW TABLES in lightning.datasource.rdbms.h2.public
```
will display all tables under public schema

```bash
-- Spark SQL command
DESCRIBE TABLE lightning.datasource.rdbms.h2.public.test_users
```
will display column name and data type

```bash
-- Spark SQL command
CREATE TABLE lightning.datasource.rdbms.h2.nyc.taxis (
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
)
```
will create table `taxis` table under `nyc` schema

```bash
-- Spark SQL command
INSERT INTO lightning.datasource.rdbms.h2.nyc.taxis
VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")
```
will insert 2 records into `taxis` table


Like the above, all the SPARK SQL is running on this `datasource` namespace

[Spark SQL Guide](https://spark.apache.org/docs/latest/sql-ref-syntax.html)

### Running federated query over source systems under `datasource` namespace
```bash
SELECT n_name, o_orderdate,  sum(l_extendedprice * (1 - l_discount)) as revenue
FROM slwh.mssql.azure_mssql.tpch1.customer,
 lightning.datasource.rdbms.azure_mssql.tpch1.orders,
 lightning.datasource.rdbms.azure_mssql.tpch1.lineitem,
 lightning.datasource.rdbms.azure_mssql.tpch1.supplier,
 lightning.datasource.rdbms.azure_mssql.tpch1.nation,
 lightning.datasource.rdbms.azure_mssql.tpch1.region
WHERE c_custkey = o_custkey
 and l_orderkey = o_orderkey
 and l_suppkey = s_suppkey
 and c_nationkey = s_nationkey
 and s_nationkey = n_nationkey
 and n_regionkey = r_regionkey
GROUP BY n_name, o_orderdate
```

### Register iceberg lakehouse
```bash
-- Spark SQL command to create iceberg namespace
CREATE NAMESPACE lightning.datasource.iceberg

-- Lightning command
REGISTER [OR REPLACE] ICEBERG DATASOURCE icebergdb OPTIONS(
type "hadoop",
warehouse "/tmp/iceberg-warehouse"
) NAMESPACE lightning.datasource.iceberg
```

once registered, user can navigate source system:

```bash
-- Spark SQL command to create nytaxis dabase
CREATE NAMESPACE lightning.datasource.iceberg.icebergdb.nytaxis
```
will create namespace(database) under the ingested iceberg lakehouse

```bash
CREATE TABLE lightning.datasource.iceberg.icebergdb.nytaxis.taxis (
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
) PARTITIONED BY (vendor_id)
```
will create taxis table under nytaxis database in iceberg, partitioned by vendor_id colum

```bash
INSERT INTO lightning.datasource.iceberg.icebergdb.nytaxis.taxis
VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y")
```
will insert 3 records into taxis table.


### Register data sources to meta store
```bash
-- Spark SQL command to create rdbms namespace
CREATE NAMESPACE lightning.datasource.h2

-- Lightning command to register h2 database under rdbms
REGISTER CATALOG all_schema
SOURCE lightning.datasource.h2.$dbName
NAMESPACE lightning.metastore.h2
```
This will ingest all schema and their tables under lightning.metastore.h2.all_schema

```bash
REGISTER CATALOG $testschema
SOURCE lightning.datasource.h2.$dbName.$testschema
NAMESPACE lightning.metastore.h2
```
This will ingest all tables from $testschema schema under lightning.metastore.h2.$testschema


