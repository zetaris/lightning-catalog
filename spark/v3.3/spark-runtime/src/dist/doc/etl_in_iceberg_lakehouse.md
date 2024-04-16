# ETL in iceberg lakegouse
ETL stands for Extract, Transform, Load. It's a process used in data warehousing and data integration to collect data from various sources, transform it into a format that is suitable for analysis, and then load it into a target database, data warehouse, or data lake for storage and further analysis.

**Scenario 1:**
In the scenario, our aim is to generate a snapshot of a PostgreSQL table within an Iceberg structure, followed by the incremental loading of new data from the PostgreSQL database into the Iceberg lakehouse.


### Step 1: Register iceberg datasource.
```bash
REGISTER OR REPLACE iceberg DATASOURCE icebergdb OPTIONS(
type "hadoop",
warehouse "/tmp/iceberg-warehouse"
) NAMESPACE lightning.datasource.iceberg
```

### Step 2: create a snapshot of postgres table into iceberg lake house
```bash
CREATE TABLE lightning.datasource.iceberg.icebergdb.taxis as
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```
### Step 3: To see the snapshot created in iceberg datalake.
```bash
SELECT * from lightning.datasource.iceberg.icebergdb.taxis;
```
```bash
Output 1: 
1       1000371 1.8     15.32   N
1       1000374 8.4     42.13   Y
2       1000372 2.5     22.15   N
2       1000373 0.9     9.01    N
```
### Step 4: To do incremental load from postgres table to the table in iceberg datalake.

```bash
MERGE INTO lightning.datasource.iceberg.icebergdb.taxis t
USING (
    SELECT *
    from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi
) s 
updates ON t.trip_id = s.trip_id
WHEN NOT MATCHED THEN INSERT (
    t.vendor_id,  t.trip_id,  t.trip_distance,  t.fare_amount,  t.store_and_fwd_flag
)
VALUES (
    s.vendor_id,  s.trip_id,  s.trip_distance,  s.fare_amount,  s.store_and_fwd_flag
);
```
### Step 5: To see the incremental load on the table in iceberg lake house.
```bash
select * from lightning.datasource.iceberg.icebergdb.taxis;
```
1 New record is added in the target iceberg datalake table
```bash
Output 2:
2       1000372 2.5     22.15   N
1       1000374 8.4     42.13   Y
2       1000373 0.9     9.01    N
1       1000371 1.8     15.32   N
3       1000375 1.8     15.32   N
```

**Scenario 2:**
In the scenario, our aim is to update records in Iceberg datalake table using orginal postgres table.

### Step 1: To see the current records in target.
```bash
select * from lightning.datasource.iceberg.icebergdb.taxis;
```
```bash
Output 1: 
1       1000371 1.8     15.32   N
1       1000374 8.4     42.13   Y
2       1000372 2.5     22.15   N
2       1000373 0.9     9.01    N
```

### Step 2: To see the current records in source
```bash
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```
```bash
Output 2:
1       1000371 1.8     15.32   N
1       1000374 8.4     422.13   Y
2       1000372 2.5     225.15   N
2       1000373 0.9     299.01    N
```
Now the records are update in source postgres table. same need to be replicated across Iceberg data lake table

### Step 3:  Update all the records in target with new values in source.
```bash
MERGE INTO lightning.datasource.iceberg.icebergdb.taxis t -- target table
USING (
    SELECT *
    from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi
) s --the source
 updates
ON t.trip_id = s.trip_id -- condition to find updates for target rows
WHEN MATCHED THEN
update
set t.vendor_id = s.vendor_id,  t.trip_id = s.trip_id,  t.trip_distance = s.trip_distance,  t.fare_amount = s.fare_amount,  t.store_and_fwd_flag = s.store_and_fwd_flag;
```
#### Step 4: To see if the record update in target
```bash
SELECT * from lightning.datasource.iceberg.icebergdb.taxis;
```
3 records are updated in target iceberg datalake table 
```bash
Output 2:
1       1000371 1.8     15.32   N
1       1000374 8.4     422.13   Y
2       1000372 2.5     225.15   N
2       1000373 0.9     299.01    N
````

