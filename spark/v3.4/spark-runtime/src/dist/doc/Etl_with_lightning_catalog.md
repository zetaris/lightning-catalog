
## Prerequisite 
1.	Source should be registered.
2.	Target (preferably data lake) should be registered.

## Step to do increments from target to source.

### Step 1:Merge when not matched.
1. Command is used to insert the data from source to target when no data is matched in target.

#### Query 1: To see the record in target.
```bash
select * from lightning.datasource.iceberg.icebergdb.nytaxis.taxis;
```
```bash
Output 1: 
1       1000371 1.8     15.32   N
1       1000374 8.4     42.13   Y
2       1000372 2.5     22.15   N
2       1000373 0.9     9.01    N
```

#### Query 2: To see the record in source
```bash
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```
```bash
Output 2:
2       1000372 2.5     22.15   N
1       1000374 8.4     42.13   Y
2       1000373 0.9     9.01    N
1       1000371 1.8     15.32   N
3       1000375 1.8     15.32   N
```

#### Query3: To add the records which are not present in Target
```bash
MERGE INTO lightning.datasource.iceberg.icebergdb.nytaxis.taxis t - - a target table USING (
    SELECT *
    from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi
) s - - the source updates ON t.trip_id = s.trip_id - - condition to find updates for target rows WHEN NOT MATCHED THEN INSERT (
    t.vendor_id,  t.trip_id,  t.trip_distance,  t.fare_amount,  t.store_and_fwd_flag
)
VALUES (
    s.vendor_id,  s.trip_id,  s.trip_distance,  s.fare_amount,  s.store_and_fwd_flag
);
```

### Step 2:Merge when matched.
#### When records are matched between source and target. We can have 2 actions:
1.	Delete: Delete the matching record from target
2.	Update: Update the matching record from target


#### Update
#### Query 1: To see the record in target
```bash
select * from lightning.datasource.iceberg.icebergdb.nytaxis.taxis;
```
```bash
Output 1: 
1       1000371 1.8     15.32   N
1       1000374 8.4     42.13   Y
2       1000372 2.5     22.15   N
2       1000373 0.9     9.01    N
```
#### Query 2: To see the record in source
```bash
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```

#### Query 3: Will update all the records in target with new values in source.
```bash
MERGE INTO lightning.datasource.iceberg.icebergdb.nytaxis.taxis t -- a target table
USING (
    SELECT *
    from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi
) s -- the source updates
ON t.trip_id = s.trip_id -- condition to find updates for target rows
WHEN MATCHED THEN
update
set t.vendor_id = s.vendor_id,  t.trip_id = s.trip_id,  t.trip_distance = s.trip_distance,  t.fare_amount = s.fare_amount,  t.store_and_fwd_flag = s.store_and_fwd_flag;
```

#### Delete
#### Query 1: To see the record in target.
```bash
select * from lightning.datasource.iceberg.icebergdb.nytaxis.taxis;
```
```bash
Output 1: 
1       1000371 1.8     15.32   N
1       1000374 8.4     42.13   Y
2       1000372 2.5     22.15   N
2       1000373 0.9     9.01    N
```

#### Query 2: To see the record in source.
```bash
SELECT * from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi;
```
```bash
Output 2:
2       1000372 2.5     22.15   N
1       1000374 8.4     42.13   Y
2       1000373 0.9     9.01    N
1       1000371 1.8     15.32   N
3       1000375 1.8     15.32   N
```
```bash
Query 3:
MERGE INTO lightning.datasource.iceberg.icebergdb.nytaxis.taxis t -- a target table
USING (
    SELECT *
    from lightning.datasource.rdbms.qa_postgres.opensource.postgres_taxi
) s -- the source updates
ON t.trip_id = s.trip_id -- condition to find updates for target rows
WHEN MATCHED
And T.vendor_id = 2 THEN Delete;
```
