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

## ver 0.1, April 22, 2024
### Introduction:
Welcome to the latest release of our software! Version 0.1 brings initial featuers for data engineers, scientist and developers to run data catalog on top of Apache Spark.

### Key Highlights:
Introducing data catalog for the preparing data at any scale in ad-hoc analytics, data warehouse, lake house and ML project

###New Features:
* Multi level namespaces: User can create anonymous name space

* Management of source system endpoint : Lightning catalog manages an endpoint of source system providing unified access to them through SQL and Apache SPARK API, which makes data discovery easy.

* Query(SQL) capabilities : Lightning catalog allows to run ad-hoc query(SQL) over underlying heterogenous source systems in federate way, E,g Join between multiple source system without moving data.

* Create virtual db schema : Lightning catalog allows to create virtual DB schema that keeps tables from different data sources

* Supported data sources:
```bash
DeltaLake
Iceberg
H2
Snowflake
Posstgres
Oracle
Mssql
Redshift
Terradata
MySQL
DB2
SQLLite
MariaDB
Derby
HANA
Greenplum
Vertica
Netezza
Csv
Parquet
Orc
Json
Avro
```

## ver 0.2, December xx, 2024
### Introduction:
Welcome to the latest release of our software! Version 0.2 brings more features for data engineers, scientist and developers to run data catalog on top of Apache Spark.

### Key Highlights:
Supporting new UI, more data sources, unified semantic layer, and data quality.

###New Features:
* New UI: UI allows to navigate data sources, running SQL, render ERD and run Data Quality check.

* Unstructured Data source

* Combining unstructured data source with custom schema

* Unified Semantic Layer

* Database constraints over any data sources

* Business Data Quality

* added data sources:
```bash
pdf
image
video
txt
```