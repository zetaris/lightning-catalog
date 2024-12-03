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

## Why is Lightning catalog.
Finding data, also known as data discovery, and preparing data is the first step in any analytics such as Data Warehouse/Lakehouse, ML, AI project.
Lightning catalog plays key role for this data preparation by providing centralized repository for enterprise to make data service easy, simple, fast and cheap.

>Lightning catalog serves as a centralized repository or index where metadata about various data sources, schemas, and virtualized views are stored. 
This catalog helps users discover, understand, and access data assets across different systems without needing to know the specifics of each underlying data source`

## What's Lightning providing
with Zetaris Lightning catalog, organisations can manage all data sources including structured, unstructured data.
Data engineer, scientist, analyst can use Lightning catalog to discover, access and transform in unified way.

* Fully Managed Catalog built in file systems (HDFS, Blob, and local file) which allows version control.
* Support Apache Spark Plug-in architecture, which means all Spark echo system can use it without any changes 
* Support running data pipeline at scale by leveraging Apache Spark and optional NVIDIA GPU
* Support running ANSI SQL and Hive QL over source systems defined in the Catalog
* Support multiple namespace.
* Support unified semantic layer by compiling & deploying DDLs over underlying data sources
* Support business data quality check as well as check database constraints over non rdbms sources.
* Support metadata processing for unstructured data using endpoint declarations.
* Support data flow table, declarative ETL framework which defines and transforms your data(ver 0.3).
