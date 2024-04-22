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

This document articulate all the steps for user to create virtual schema keeping tables from different data sources.

## 1. create custom namespace for the virtual schema
```bash
-- spark sql to create namespace under default metastore 
creat namespace lightning.metastore.benchmark;
```

## 2-1. create virtual schema and ingest tables from the ingested data source

```bash
-- this is how to register data source
REGISTER OR REPLACE JDBC DATASOURCE qa_mssql2 OPTIONS(
driver "com.microsoft.sqlserver.jdbc.SQLServerDriver",
url "jdbc:sqlserver://microsoftsqlserver.database.windows.net:1433 ",
databaseName "DemoXXXXX",
user "adminXXXXXX" ,
password "XXXXXXXXXX"
) NAMESPACE lightning.datasource.rdbms;

-- create "tpch" virtual schema by ingesting all tables from the "tpch" schema from the above data source 
register catalog tpch
source lightning.datasource.rdbms.qa_mssql2.tpch
NAMESPACE lightning.metastore.benchmark;

-- show tables
show tables in lightning.metastore.benchmark.tpch
------------
table      |
------------
customer   |
nation     |
orders     |
supplier   |
lineitem   |
region     |
partsupp   |
part       |
```

## 2-2. Ingest tables from other data source
```bash
-- register table named '%person', using wildcard for table name, to the "tpch" schema from opensource schema from postgres 
register catalog tpch
source lightning.datasource.rdbms.qa_postgres.opensource
NAME LIKE "%person"
NAMESPACE lightning.metastore.benchmark;

-- show tables will include '%person' table from postgres
show tables in lightning.metastore.benchmark.tpch
------------
table      |
------------
customer   |
nation     |
orders     |
supplier   |
lineitem   |
region     |
person     |
partsupp   |
part       |
```

## run query
```bash
select * from lightning.metastore.benchmark.tpch.person;

400	Dan	50
300	Mike	80
100	John	30
200	Mary	NULL
Time taken: 3.077 seconds, Fetched 4 row(s)

select * from lightning.metastore.benchmark.tpch.customer limit 10;
1	Customer#000000001	IVhzIApeRb ot,c,E	15	25-989-741-2988	711.56	BUILDING  	to the even, regular platelets. regular, ironic epitaphs nag e
2	Customer#000000002	XSTf4,NCwDVaWNe6tEgvwfmRchLXak	13	23-768-687-3665	121.65	AUTOMOBILE	l accounts. blithely ironic theodolites integrate boldly: caref
3	Customer#000000003	MG9kdTD2WBHm	1	11-719-748-3364	7498.12	AUTOMOBILE	 deposits eat slyly ironic, even instructions. express foxes detect slyly. blithely even accounts abov
4	Customer#000000004	XxVSJsLAGtn	4	14-128-190-5944	2866.83	MACHINERY 	 requests. final, regular ideas sleep final accou
5	Customer#000000005	KvpyuHCplrB84WgAiGV6sYpZq7Tj	3	13-750-942-6364	794.47	HOUSEHOLD 	n accounts will have to unwind. foxes cajole accor
6	Customer#000000006	sKZz0CsnMD7mp4Xd0YrBvx,LREYKUWAh yVn	20	30-114-968-4951	7638.57	AUTOMOBILE	tions. even deposits boost according to the slyly bold packages. final accounts cajole requests. furious
7	Customer#000000007	TcGe5gaZNgVePxU5kRrvXBfkasDTea	18	28-190-982-9759	9561.95	AUTOMOBILE	ainst the ironic, express theodolites. express, even pinto beans among the exp
8	Customer#000000008	I0B10bB0AymmC, 0PrRYBCP1yGJ8xcBPmWhl5	17	27-147-574-9335	6819.74	BUILDING  	among the slyly regular theodolites kindle blithely courts. carefully even theodolites haggle slyly along the ide
9	Customer#000000009	xKiAFTjUsCuxfeleNqefumTrjS	8	18-338-906-3675	8324.07	FURNITURE 	r theodolites according to the requests wake thinly excuses: pending requests haggle furiousl
10	Customer#000000010	6LrEaV6KR6PLVcgl2ArL Q3rqzLzcT1 v2	5	15-741-346-9870	2753.54	HOUSEHOLD 	es regular deposits haggle. fur
Time taken: 6.803 seconds, Fetched 10 row(s)
```