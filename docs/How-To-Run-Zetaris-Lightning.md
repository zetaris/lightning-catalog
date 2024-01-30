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
