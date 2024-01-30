## Prerequisite 
1.	Spark
2.	Zetaris open-source Jar.
3.	Java 1.8

Step to run Zetaris open-source code.
1.	Create a directory /Home/Zetaris/Zetaris_open_source and go to the directory.
2.	Download Zetaris Open-source code jar from GitHub
3.	Create a directory /Home/Zetaris/Spark and go to the directory.
4.	Download Spark 
a.	Curl https://dlcdn.apache.org/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
5.	Go to folder /home/zetaris/spark/bin
6.	Run the command.
./spark-sql --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/lightning-model \
    --conf spark.jars.packages=org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.4.2,io.delta:delta-core_2.12:2.3.0,com.h2database:h2:2.2.220,net.snowflake:snowflake-jdbc:3.14.4,org.postgresql:postgresql:42.6.0,com.oracle.database.jdbc:ojdbc8:23.3.0.23.09,com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8 \
    --jars /home/zetaris/zetaris_open_source/<Zetaris Open source Jar>
7.	This will establish connection with with Zetaris open source sql editor.Now Sql commands can be run.

Spark Packages for different databases
Zetaris open source supports all the different databases which uses JDBC. Here is the example of how to get database dependency if the database supports JDBC.
For example, Connection to mssql is needed. For that follow the following steps:
1.	Open google chrome and type MSSQL JDBC MAVEN and open the firs link 
2.	Open the first link. And find the suitable jar which support your MSSQL database version.
3.	Once you click on the suitable jar. Further click on SBT tab below:
4.	You will see GroupId, ArtifactId and version.
  a.	In our example it is 
    i.	GroupId = com.microsoft.sqlserver
    ii.	Artifact Id = mssql-jdbc
    iii.	Version = 12.4.2.jre11
5.	You will have to update these 3 values in above provided command in “step 6” of “step to run Zetaris open-source code”. For example GroupID:ArtifactId:Version
./spark-sql --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/lightning-model \
    --conf spark.jars.packages=org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.4.2,io.delta:delta-core_2.12:2.3.0,com.h2database:h2:2.2.220,net.snowflake:snowflake-jdbc:3.14.4,org.postgresql:postgresql:42.6.0,com.oracle.database.jdbc:ojdbc8:23.3.0.23.09,com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8, com.microsoft.sqlserver:mssql-jdbc: 12.4.2.jre11  \
    --jars /home/zetaris/zetaris_open_source/lightning-spark-runtime-3.3_2.12-0.1_v5.jar
6.	Once the dependencies is added run the command.You will now be able to connect to MSSQL database

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
## Prerequisite
* apache iceberg: ver 1.4.2 with spark 3.3 and scala 2.12 is tested
* delta lake: ver 2.3.0 with scala 2.12 is tested
* all the JDBC drivers for datasource
  
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
* spark.jars.packages= Comma-separated list of Maven coordinates of jars to include on the driver and executor classpaths. The coordinates should be groupId:artifactId:version.
* spark.jars=Comma-separated list of jars to include on the driver and executor classpaths. Globs are allowed.



```bash
./spark-sql --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,org.apache.iceberg.spark.extensions.IcebergSparkSessionExtensions,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/lightning-model \
    --conf spark.jars.packages=org.apache.iceberg:iceberg-spark-runtime-3.3_2.12:1.4.2,io.delta:delta-core_2.12:2.3.0,com.h2database:h2:2.2.220,net.snowflake:snowflake-jdbc:3.14.4,org.postgresql:postgresql:42.6.0,com.oracle.database.jdbc:ojdbc8:23.3.0.23.09,com.microsoft.sqlserver:mssql-jdbc:7.0.0.jre8 \
    --jars ${spark-runtime-build-directory}/lightning-spark-extensions-3.3_2.12-0.1.jar,${other-jars-not-present-on-maven-directory}/*
```
### Steps to dependencies/packages from Maven
* Open google chrome and type MSSQL JDBC MAVEN and open the firs link
* Open the first link. And find the suitable jar which support your MSSQL database version.
* You will see GroupId, ArtifactId and version.
  a.	In our example it is 
    i.	GroupId = com.microsoft.sqlserver
    ii.	Artifact Id = mssql-jdbc
    iii.	Version = 12.4.2.jre11
* update these 3 values for spark.jars.packages. For example GroupID:ArtifactId:Version
    * ```bash conf spark.jars.packages=com.microsoft.sqlserver:mssql-jdbc:1.4.2:12.4.2.jre11,io.delta:delta-core_2.12:2.3.0 \ ```
