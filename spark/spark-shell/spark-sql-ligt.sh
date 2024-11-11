#
# /*
#  * Copyright 2023 ZETARIS Pty Ltd
#  *
#  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
#  * associated documentation files (the "Software"), to deal in the Software without restriction,
#  * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
#  * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
#  * subject to the following conditions:
#  *
#  * The above copyright notice and this permission notice shall be included in all copies
#  * or substantial portions of the Software.
#  *
#  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
#  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
#  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
#  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#  */
#

#############################################################
# LIGT_HOME var need to be set for the installed directory
# Assuming all 3rd party libraries, for example vendor JDBC jar file, are kept in $LIGT_HOME/jdbc-lib

# $SPARK_HOME/bin/spark-sql --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
#     --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
#     --conf spark.sql.catalog.lightning.type=hadoop \
#     --conf spark.sql.catalog.lightning.warehouse=/tmp/ligt-model \
#     --conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
#     --conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
#     --conf spark.executor.extraClassPath=$LIGT_HOME/lib/* \
#     --conf spark.driver.extraClassPath=$LIGT_HOME/lib/* \
#     --jars $LIGT_HOME/lib/lightning-spark-extensions-3.5_2.12-0.1.jar,$LIGT_HOME/jdbc-lib/*

# Set Spark version information if necessary (optional)
SPARK_VERSION="3.5"

# Set paths
BIN_DIR=$(pwd)  # Assume this script is in the `bin` directory
LIB_DIR="$BIN_DIR/../lib"

# Check if necessary directories and files exist
if [ ! -d "$LIB_DIR" ]; then
    echo "Library directory not found: $LIB_DIR"
    exit 1
fi

# Execute Spark SQL in CLI mode
echo "Starting Spark SQL in CLI mode with necessary JAR files..."
"${SPARK_HOME}/bin/spark-sql" \
    --conf spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension \
    --conf spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog \
    --conf spark.sql.catalog.lightning.type=hadoop \
    --conf spark.sql.catalog.lightning.warehouse=/tmp/ligt-model \
    --conf spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider \
    --conf spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog \
    --conf spark.executor.extraClassPath="$LIB_DIR/*" \
    --conf spark.driver.extraClassPath="$LIB_DIR/*" \
    --jars "$LIB_DIR/lightning-spark-extensions-${SPARK_VERSION}_2.12-0.2.jar,$LIB_DIR/*"
