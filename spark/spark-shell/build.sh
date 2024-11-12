#!/bin/bash

# Set Spark version information
SPARK_VERSION="3.5" # Replace with actual SPARK_VERSION
OUTPUT_ZIP="lightning-metastore-$SPARK_VERSION-0.2-Ver.zip"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Step 1: Build front-end (React)
echo "Building front-end..."
cd "$SCRIPT_DIR/../../gui"
npm install || { echo "Dependency installation failed"; exit 1; }
npm run build || { echo "React build failed"; exit 1; }

# Copy built front-end files to /tmp/tar_build/web
mkdir -p /tmp/tar_build/web
cp -r build/* /tmp/tar_build/web

# Step 2: Build back-end (Gradle, Scala, Spark)
echo "Building back-end..."
cd "$SCRIPT_DIR/../"
./gradlew clean build -DdefaultSparkMajorVersion=$SPARK_VERSION -x test || { echo "Gradle build failed"; exit 1; }

# Step 3: Extract JAR files from distribution package
echo "Extracting JAR files from distribution..."
DIST_DIR="$SCRIPT_DIR/spark/v${SPARK_VERSION}/spark-runtime/build/distributions"
mkdir -p /tmp/tar_build/lib

# Find tar or zip file in the directory and extract JARs
if ls "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.tar 1> /dev/null 2>&1; then
    tar -xvf "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.tar -C /tmp/tar_build/lib --strip-components=2 '*.jar'
elif ls "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.zip 1> /dev/null 2>&1; then
    unzip "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.zip '*.jar' -d /tmp/tar_build/lib
else
    echo "Distribution package not found"; exit 1;
fi

# Step 4: Copy scripts from spark-common/spark-shell directory to bin
mkdir -p /tmp/tar_build/bin
cp ./spark/spark-shell/*.sh /tmp/tar_build/bin

# Step 5: Package all files into a zip archive in the project root
echo "Packaging files into zip archive..."
cd /tmp/tar_build
zip -r "$OLDPWD/$OUTPUT_ZIP" ./* || { echo "Zip packaging failed"; exit 1; }
cd -  # Return to the previous directory

# Build completed
echo "Build and packaging completed successfully: $OUTPUT_ZIP"
