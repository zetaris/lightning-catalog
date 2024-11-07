#!/usr/bin/env bash

#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Set the port dynamically
API_ENDPOINT_PORT=8080

# Enter posix mode for bash
set -o posix

CLASS="com.zetaris.lightning.catalog.LightningAPIStarter"

exec "${SPARK_HOME}/bin/spark-daemon.sh submit $CLASS 1 --name "LightningAPI Server" \
    --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf lightning.server.port=$API_ENDPOINT_PORT \
    --conf spark.sql.catalog.lightning.warehouse=/Users/jaesungjun/Application/ligt-model \
    --conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
    --conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
    --conf spark.executor.extraClassPath=/Users/jaesungjun/temp/project/lightning-metastore/spark/v3.5/spark-runtime/build/distributions/lightning-metastore-3.5_2.12-0.2/lib/* \
    --conf spark.driver.extraClassPath=/Users/jaesungjun/temp/project/lightning-metastore/spark/v3.5/spark-runtime/build/distributions/lightning-metastore-3.5_2.12-0.2/lib/* \
    --jars /Users/jaesungjun/temp/project/lightning-metastore/spark/v3.5/spark-runtime/build/distributions/lightning-metastore-3.5_2.12-0.2/lib/lightning-spark-extensions-3.5_2.12-0.2.jar,/Users/jaesungjun/Application/jdbc-lib/*

