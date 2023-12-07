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

##Lightning Catalog

Lightning catalog is an open source data catalog for the preparing data at any scale in ad-hoc analytics, data warehouse, lake house and ML project

## Key Capabilities
* Lightning catalog manages an endpoint of source system providing unified access to them  through SQL and Apache SPARK API, which makes data discovery easy.
* Lightning catalog allows to run ad-hoc query(SQL) over underlying heterogenous source systems in federate way, E,g Join between multiple source system without moving data.
* Lightning catalog simplify the life cycle of data engineering pipe line, build, test and deploy by leveraging Data Flow Table, E,g Load data into lake house by fetching delta and transform all the way of SQL operation.
* Lightning catalog can get rid of burden of data preparation workload for ML engineer, and help them focusing on building model. It supports to access to unstructured data.

## Online Documentation
The latest documentation is found on the [project web page](https://lightning.zetaris.com)
[API Documentation](https://scala-doc.lightning.zetaris.com)

## Collaboration

Lightning tracks issues in GitHub and prefers to receive contributions as pull requests.


### Building

Lightning is built using Gradle with Java 1.8 or Java 11.

* To invoke a build and run tests: `./gradlew build`
* To skip tests: `./gradlew build -x test -x integrationTest`
* To fix code style for default versions: `./gradlew spotlessApply`
* To fix code style for all versions of Spark/Hive/Flink:`./gradlew spotlessApply -DallVersions`


### Major Features

* Running catalog in file system( HDFS, Blob, and local file) which allows version control.
* Support Apache Spark Plug-in architecture
* Support running data pipeline at any scale by leveraging Apache Spark.
* Support running ANSI SQL and Hive QL over underlying source systems.
* Support multiple namespace.
* Support data quality by integrating Amazon Deequ
* Support data flow table, declarative ETL framework which define transforms your data.
* Support processing unstructured data, accessing all files and their meta data recursively from an endpoint.
