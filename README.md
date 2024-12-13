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

## Lightning Catalog

The Lightning Catalog is an open-source data catalog designed for preparing data at any scale in ad-hoc analytics, data warehousing, lake houses, and ML projects.
It is primarily developed to provide management of various data assets across the enterprise and stitch them together without requiring a centralized repository.
It leverages Apache Spark as the primary compute engine.

## Key Capabilities

* Lightning Catalog manages endpoints of source systems, providing unified access to them through SQL and the Apache Spark API, making data discovery easy.
* Lightning Catalog allows you to run ad-hoc SQL queries over underlying heterogeneous source systems in a federated manner (e.g., joins between multiple source systems without moving data).
* Lightning Catalog simplifies the lifecycle of data engineering pipelines, from build and test to deployment, by leveraging Data Flow Tables. For example, you can load data into a lake house by fetching deltas and transforming data through SQL operations.
* Lightning Catalog alleviates the burden of data preparation for ML engineers, enabling them to focus on building models. It also supports access to unstructured data.
* Lightning Catalog allows you to ingest unstructured files and query both their metadata and actual contents. It leverages Spark's parallel processing capabilities.
* Lightning Catalog enables the creation of a unified semantic layer in a top-down manner, allowing users to upload DDL and map table definitions to underlying data sources.
* Lightning Catalog brings data quality capabilities, including checking database constraints (PK, Unique, FK) over non-RDBMS tables, such as Parquet, Iceberg, Delta, and more.

## Online Documentation

* GitHub online [documentation](https://github.com/zetaris/lightning-catalog/tree/master/doc/doc).

## Collaboration

Lightning tracks issues on GitHub and encourages contributions via pull requests.

### Building - Backend

Lightning is built using Gradle with Java 1.8, Java 11, Java 17, 18, 19.

* To build and run tests: `./gradlew build`
* To skip tests: `./gradlew build -x test -x integrationTest`
* To fix code style for default versions: `./gradlew spotlessApply`
* To fix code style for all versions of Spark/Hive/Flink: `./gradlew spotlessApply -DallVersions`
* To build with a specific Spark version profile: `./gradlew clean build -DdefaultSparkMajorVersion=3.4 -DdefaultSparkVersion=3.4.2`
* The distribution package can be found at `lightning-metastore/spark/spark_version(v3.4, v3.5)/spark-runtime/build/distributions`.

### Building - Frontend & Backend

Lightning provides `build.sh` to build both the frontend and backend.

* `build.sh` takes parameters from the backend build commands listed above.
* The distribution package can be found at `lightning-metastore/build/lightning-metastore-(spark_major_version)-(lightning_version).zip`.

### Execution

* Copy third-party libraries, such as JDBC libraries, into `$LIGHTNING_HOME/3rd-party-lib`.
* Modify the following two parameters in `$LIGHTNING_HOME/bin/start-light.sh`, then run the script.

### Major Features

* Running the catalog on file systems (HDFS, Blob, and local file), allowing version control.
* Support for Apache Spark plug-in architecture.
* Ability to run data pipelines at any scale by leveraging Apache Spark.
* Support for running ANSI SQL and HiveQL queries over underlying source systems.
* Support for multiple namespaces.
* Data quality support via integration with Amazon Deequ.
* Data flow tables, a declarative ETL framework that defines transformations on data.
* Processing unstructured data, recursively accessing all files and their metadata from an endpoint.
* Unified semantic layer (USL) by compiling and deploying DDL.
* Database constraint checks and business rule data quality checks over USL.

### Currently Supported Data Sources (More to be Added)
```
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
PDF
image
avi
txt
```
