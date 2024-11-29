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

#!/bin/bash

#SPARK_VERSION="3.5"  # Spark version if needed to set any paths or environment variables


# Set ports for API and GUI
export LIGHTNING_SERVER_PORT=8080
export LIGHTNING_GUI_PORT=8081

# Set paths
BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export LIGHTNING_HOME="$BIN_DIR/.."

# Set the main class and application parameters
MAIN_CLASS="com.zetaris.lightning.catalog.api.LightningAPIServer"
APP_NAME="Lightning Server"

# Start Spark with necessary JARs and configuration
echo "Starting Lightning Server..."

exec "${SPARK_HOME}/bin/spark-submit" --class $MAIN_CLASS --name "$APP_NAME" \
    --conf "spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension" \
    --conf "spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog" \
    --conf "spark.sql.catalog.lightning.type=hadoop" \
    --conf "spark.sql.catalog.lightning.warehouse=$LIGHTNING_HOME/model" \
    --conf "spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider" \
    --conf "spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog" \
    --jars "$LIGHTNING_HOME/lib/*,$LIGHTNING_HOME/3rd-party-lib/*" spark-internal "$@" &