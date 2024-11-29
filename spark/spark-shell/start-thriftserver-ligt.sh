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

# Enter posix mode for bash
set -o posix

BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export LIGHTNING_HOME="$BIN_DIR/.."
CLASS="org.apache.spark.sql.hive.thriftserver.HiveThriftServer2"

exec "${SPARK_HOME}"/sbin/spark-daemon.sh submit $CLASS 1 --name "Thrift JDBC/ODBC Server" \
    --conf "spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension" \
    --conf "spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog" \
    --conf "spark.sql.catalog.lightning.type=hadoop" \
    --conf "spark.sql.catalog.lightning.warehouse=$LIGHTNING_HOME/model" \
    --conf "spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider" \
    --conf "spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog" \
    --jars "$LIGHTNING_HOME/lib/*,$LIGHTNING_HOME/3rd-party-lib/*" spark-internal "$@"