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

# Function to download Spark if Spark-demon doesn't exist
download_spark() {
  local spark_version=$1

  # If version is 3.5, set it to 3.5.2
  if [[ "$spark_version" == "3.5" ]]; then
    spark_version="3.5.2"
  fi
  if [[ "$spark_version" == "3.4" ]]; then
    spark_version="3.4.3"
  fi

  local download_url="https://archive.apache.org/dist/spark/spark-${spark_version}/spark-${spark_version}-bin-hadoop3.tgz"
  local download_dir="../spark-${spark_version}-bin-hadoop3"

  # Asking for user consent before downloading
  read -p "Do you want to download Spark ${spark_version}? (y/n): " consent
  if [[ "$consent" != "y" ]]; then
    echo "Download has been canceled."
    exit 1
  fi

  echo "Downloading Spark ${spark_version} from ${download_url}..."

  # Download and extract
  curl -O "${download_url}"
  tar -xvzf "spark-${spark_version}-bin-hadoop3.tgz" -C ../
  rm "spark-${spark_version}-bin-hadoop3.tgz"

  echo "Spark ${spark_version} downloaded and extracted to ${download_dir}"

  # Set SPARK_HOME to the downloaded Spark version
  export SPARK_HOME="$(cd "${download_dir}" && pwd)"
}

# Function to read version values from ../versions.txt
load_versions() {
  local versions_file="../versions.txt"
  if [[ -f "$versions_file" ]]; then
    source "$versions_file"
  else
    # If versions.txt does not exist, create it with default values
    echo "versions.txt not found. Creating a new versions.txt file with default values."
    echo "SPARK_VERSION=3.5.2" > "$versions_file"
    echo "SCALA_VERSION=2.12" >> "$versions_file"
    echo "SPARK_HOME=$(pwd)/../spark-3.5.2-bin-hadoop3" >> "$versions_file"
    
    # Re-load the created file
    source "$versions_file"
  fi
}

# Call the function to load SPARK_VERSION and SCALA_VERSION
load_versions

# Check if an existing HiveThriftServer2 is running
# check_existing_thriftserver

# Set Spark and Scala version environment variables
sparkVersion=${SPARK_VERSION}
scalaVersion=${SCALA_VERSION}
sparkHome=${SPARK_HOME}

# Check if the Spark directory already exists
if [[ -d "../spark-${sparkVersion}-bin-hadoop3" ]]; then
  echo "Spark ${sparkVersion} is already downloaded. Initializing SPARK_HOME."
  export SPARK_HOME="$(cd "../spark-${sparkVersion}-bin-hadoop3" && pwd)"
else
  # Try to find any version starting with "spark-${sparkVersion}-bin-hadoop3" (for example, spark-3.5.2-bin-hadoop3)
  spark_dir=$(find ../ -maxdepth 1 -type d -name "spark-${sparkVersion}*-bin-hadoop3" | head -n 1)

  if [[ -n "$spark_dir" ]]; then
    echo "Found Spark directory: $spark_dir"
    export SPARK_HOME="$(cd "$spark_dir" && pwd)"
  else
    echo "Spark directory not found. Proceeding to download Spark version ${sparkVersion}."
    download_spark "${sparkVersion}"
  fi
fi

# Check if Spark Daemon exists
if [[ ! -f "${SPARK_HOME}/sbin/spark-daemon.sh" ]]; then
  echo "Spark Daemon not found. Downloading Spark version ${sparkVersion}..."
  download_spark "${sparkVersion}"
fi

export SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# LIGT_HOME paths based on the versions
zipFile="../../spark/v${sparkVersion}/spark-runtime/build/distributions/lightning-metastore-${sparkVersion}_${scalaVersion}-0.2.zip"
# ligt_home="../../spark/v${sparkVersion}/spark-runtime/build/distributions/lightning-metastore-${sparkVersion}_${scalaVersion}-0.2"
export LIGT_HOME="../../spark/v${sparkVersion}/spark-runtime/build/distributions/lightning-metastore-${sparkVersion}_${scalaVersion}-0.2"
export COMMON_HOME="../../spark/spark-common/build"

# Unzip the distribution if it's not already unzipped
if [ ! -d "$LIGT_HOME" ]; then
  echo "Unzipping $zipFile..."
  unzip $zipFile -d "../../spark/v${sparkVersion}/spark-runtime/build/distributions/"
  echo "Unzipped $zipFile to $LIGT_HOME"
else
  echo "LIGT_HOME is already unzipped at $LIGT_HOME"
fi

# Function to find an available port
find_available_port() {
  local port=8000
  while : ; do
    if ! lsof -i:$port > /dev/null; then
      echo $port
      return
    fi
    port=$((port+1))
  done
}

# Set the port dynamically
PORT=$(find_available_port)

# Start serving the React UI (Python HTTP server) on the available port
echo "Starting React UI server on port $PORT..."
cd ../../gui/build
python3 -m http.server $PORT &  # UI will be available at http://localhost:$PORT
cd -  # Return to the previous directory

# Inform the user about the UI URL
echo "UI is available at http://localhost:$PORT"

# Enter posix mode for bash
set -o posix

# NOTE: This exact class name is matched downstream by SparkSubmit.
# Any changes need to be reflected there.
# CLASS="org.apache.spark.sql.hive.thriftserver.HiveThriftServer2"

# Define the class for the API server
CLASS="com.zetaris.lightning.catalog.LightningAPIStarter"

function usage {
  echo "Usage: ./sbin/start-thriftserver [options] [thrift server options]"
  pattern="usage"
  pattern+="\|Spark assembly has been built with Hive"
  pattern+="\|NOTE: SPARK_PREPEND_CLASSES is set"
  pattern+="\|Spark Command: "
  pattern+="\|======="
  pattern+="\|--help"
  pattern+="\|Using Spark's default log4j profile:"
  pattern+="\|^log4j:"
  pattern+="\|Started daemon with process name"
  pattern+="\|Registered signal handler for"

  "${SPARK_HOME}"/bin/spark-submit --help 2>&1 | grep -v Usage 1>&2
  echo
  echo "Thrift server options:"
  "${SPARK_HOME}"/bin/spark-class $CLASS --help 2>&1 | grep -v "$pattern" 1>&2
}

if [[ "$@" = *--help ]] || [[ "$@" = *-h ]]; then
  usage
  exit 1
fi

export SUBMIT_USAGE_FUNCTION=usage

#############################################################
# Add a slight delay to ensure the server is up before opening the browser
sleep 2

# Open the default browser to access the UI
if which open > /dev/null; then
  open http://localhost:$PORT
elif which xdg-open > /dev/null; then
  xdg-open http://localhost:$PORT
else
  echo "Please manually open http://localhost:$PORT in your browser."
fi

#############################################################

exec "${SPARK_HOME}/bin/spark-submit" \
    --class com.zetaris.lightning.catalog.LightningAPIStarter \
    --name "API Server" \
    --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/ligt-model \
    --conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
    --conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
    --driver-class-path $LIGT_HOME/lib/*:$COMMON_HOME/libs/lightning-spark-common_2.12-0.2.jar:$SPARK_HOME/jdbc-libs/* \
    --jars $LIGT_HOME/lib/lightning-spark-extensions-${sparkVersion}_${scalaVersion}-0.2.jar,$COMMON_HOME/libs/lightning-spark-common_2.12-0.2.jar,$LIGT_HOME/lib/* \
    --num-executors 2 \
    --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:$SPARK_HOME/conf/log4j.properties" \
    --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:$SPARK_HOME/conf/log4j.properties" \
    $COMMON_HOME/libs/lightning-spark-common_2.12-0.2.jar \
    "$@"