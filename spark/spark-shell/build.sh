#!/bin/bash

# Set Spark version information
SPARK_VERSION="3.5" # Replace with actual SPARK_VERSION
OUTPUT_ZIP="lightning-metastore-$SPARK_VERSION-0.2-Ver.zip"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Step 1: Build front-end (React)
echo "Building front-end..."
cd "$SCRIPT_DIR/../../gui" || { echo "Failed to navigate to GUI directory"; exit 1; }
npm install || { echo "Dependency installation failed"; exit 1; }
npm run build || { echo "React build failed"; exit 1; }

# Copy built front-end files to /tmp/tar_build/web
echo "Copying front-end files..."
mkdir -p /tmp/tar_build/web
cp -r build/* /tmp/tar_build/web || { echo "Failed to copy front-end files"; exit 1; }

# Step 2: Build back-end (Gradle, Scala, Spark)
echo "Building back-end..."
cd "$SCRIPT_DIR/../../" || { echo "Failed to navigate to project root"; exit 1; }
./gradlew clean build -DdefaultSparkMajorVersion=$SPARK_VERSION -x test || { echo "Gradle build failed"; exit 1; }

# Step 3: Extract JAR files from distribution package
echo "Extracting JAR files from distribution..."
echo $SCRIPT_DIR
DIST_DIR="$SCRIPT_DIR/../../spark/v${SPARK_VERSION}/spark-runtime/build/distributions"
mkdir -p /tmp/tar_build/lib

# Find tar or zip file in the directory and extract JARs
if ls "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.tar 1> /dev/null 2>&1; then
    echo "Found tar distribution"
    tar -xvf "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.tar -C /tmp/tar_build/lib --strip-components=2 '*.jar' || { echo "Failed to extract JARs from tar"; exit 1; }
elif ls "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.zip 1> /dev/null 2>&1; then
    echo "Found zip distribution"
    unzip "$DIST_DIR/lightning-metastore-${SPARK_VERSION}"*.zip '*.jar' -d /tmp/tar_build/lib || { echo "Failed to extract JARs from zip"; exit 1; }
else
    echo "Distribution package not found"; exit 1;
fi

# Step 4: Copy scripts from spark-common/spark-shell directory to bin
echo "Copying scripts to bin..."
mkdir -p /tmp/tar_build/bin
cp "$SCRIPT_DIR/../../spark/spark-shell"/*.sh /tmp/tar_build/bin || { echo "Failed to copy scripts"; exit 1; }

# Step 5: Package all files into a zip archive in the project root
echo "Packaging files into zip archive..."
cd /tmp/tar_build || { echo "Failed to navigate to /tmp/tar_build"; exit 1; }
zip -r "$OLDPWD/$OUTPUT_ZIP" ./* || { echo "Zip packaging failed"; exit 1; }
cd - || { echo "Failed to return to previous directory"; exit 1; }

# Build completed
echo "Build and packaging completed successfully: $OUTPUT_ZIP"