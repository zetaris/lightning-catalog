# Iceberg on AWS s3
This document shows how we create iceberg datalake on AWS s3.

### Create a namespace.
Having a hierarchical structure is consistently beneficial. In this particular scenario of registering a datalake. we will establish new namespaces termed "iceberg" proceed to register.
```bash
--spark sql to create namespace iceberg where all the iceberg datalakes will get registered
CREATE NAMESPACE lightning.datasource.iceberg
```
### Step 1: Register iceberg datasource.
```bash
REGISTER OR REPLACE iceberg DATASOURCE icebergdb_s3 OPTIONS(
type "hadoop",
"fs.s3a.access.key" "AKIAUDI43XXXXXXXXXXX",
"fs.s3a.secret.key" "xfGoSe+mmgXXXXXXXXXXXXX",
warehouse "s3a://zetaris-emr-test/opensourceXXXXXXX/"
) NAMESPACE lightning.datasource.iceberg
```

### Step 2: create a snapshot of table iceberg lake house
```bash
-- Creating snapshot from postgres table intoiceberg lake house
CREATE TABLE lightning.datasource.iceberg.icebergdb_s3.taxis as
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```
```bash
-- Creating new table into iceberg lake house
CREATE TABLE lightning.datasource.iceberg.icebergdb_s3.nytaxis.taxis (
vendor_id bigint,
trip_id bigint,
trip_distance float,
fare_amount double,
store_and_fwd_flag string
) PARTITIONED BY (vendor_id);
```
```bash
-- Inserting value into new table into inceberg lake house
INSERT INTO lightning.datasource.iceberg.icebergdb_s3.nytaxis.taxis
VALUES (1, 1000371, 1.8, 15.32, "N"), (2, 1000372, 2.5, 22.15, "N"), (2, 1000373, 0.9, 9.01, "N"), (1, 1000374, 8.4, 42.13, "Y");
```

### Step 3: To see the snapshot created in iceberg datalake.
```bash
SELECT * from lightning.datasource.iceberg.icebergdb_s3.taxis