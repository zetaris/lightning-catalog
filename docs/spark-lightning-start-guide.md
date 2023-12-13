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

## Adding Lightning catalog
Lightning has two different catalog type, hdfs(as of now) and rdbs which will be added later.
SPARK-SQL CLI is kicked off with Lightning catalog on hdfs 

To plugin Lightning catalog, 4 custom parameters need to be provided to SPARK.
* spark.sql.extensions=com.zetaris.lightning.spark.LightningSparkSessionExtension
  Implementation of Spark session for Lightning catalog 
* spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog
  Lightning catalog implementation
* spark.sql.catalog.lightning.type=hadoop
  catalog type, hadoop or rdbms. At the moment, only hadoop is provided
* spark.sql.catalog.lightning.warehouse=/tmp/lightning-model
  hadoop(local file system or any blob storage) endpoint for catalog repository 

Library that need to run
* apache iceberg: ver 1.4.2 with spark 3.3 and scala 2.12 is tested
  org.apache.iceberg:iceberg-spark-3.3_2.12:1.4.2
  org.apache.iceberg:iceberg-spark-extensions-3.3_2.12:1.4.2
  org.apache.iceberg:iceberg-common:1.4.2
  org.apache.iceberg:iceberg-bundled-guava:1.4.2
  org.apache.iceberg:iceberg-api:1.4.2
  org.apache.iceberg:iceberg-core:1.4.2
* delta lake: ver 2.3.0 with scala 2.12 is tested
  io.delta:delta-core_2.12:2.3.0
* caffeine cache
  com.github.ben-manes.caffeine:caffeine:2.9.3
  

```bash
./spark-sql --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/lightning-model \
    --jars /Users/Applications/lightning-metastore/libs/*,/Users/Applications/lightning-metastore/lightning-spark-extensions-3.3_2.12-0.1.jar 
```