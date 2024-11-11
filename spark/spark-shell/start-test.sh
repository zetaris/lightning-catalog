#!/bin/bash

# Set Spark version information if necessary (optional)
SPARK_VERSION="3.5" # Spark version if needed to set any paths or environment variables

# Set paths
BIN_DIR=$(pwd)  # Extracted project root directory
LIB_DIR="$BIN_DIR/../lib"

# Check if necessary directories and files exist
if [ ! -d "$LIB_DIR" ]; then
    echo "Library directory not found: $LIB_DIR"
    exit 1
fi

# Set the main class and application parameters
MAIN_CLASS="com.zetaris.lightning.catalog.api.LightningAPIServer"
APP_NAME="LightningAPI Server"

# Determine mode (CLI or GUI) based on input argument
MODE="cli"  # default mode is CLI
if [[ "$1" == "--mode=gui" ]]; then
  MODE="gui"
  shift  # Remove the argument from the list
fi

# Start Spark with necessary JARs and configuration
echo "Starting Spark in $MODE mode with necessary JAR files..."
exec "${SPARK_HOME}/bin/spark-submit" \
    --class $MAIN_CLASS \
    --name "$APP_NAME" \
    --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/ligt-model \
    --conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
    --conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
    --driver-class-path "$LIB_DIR/*:$SPARK_HOME/jdbc-libs/*" \
    --jars "$LIB_DIR/lightning-spark-extensions-${SPARK_VERSION}_2.12-0.2.jar,$LIB_DIR/*" \
    --num-executors 2 \
    --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:$SPARK_HOME/conf/log4j.properties" \
    --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=file:$SPARK_HOME/conf/log4j.properties" \
    "$LIB_DIR/lightning-spark-common_2.12-0.2.jar" \
    "$@" "$MODE"
