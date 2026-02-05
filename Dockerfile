# Runtime-only Dockerfile for Lightning Catalog
# Requires pre-built artifacts from local build
#
# Build locally first:
#   cd gui && npm install && npm run build && cd ..
#   ./gradlew clean build -x test -x integrationTest -DdefaultSparkMajorVersion=3.5 -DdefaultSparkVersion=3.5.0
#
# Then build Docker image:
#   docker build -t lightning-catalog:latest .

FROM eclipse-temurin:11-jre

USER root

# Install procps
RUN apt-get update && apt-get install -y procps curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Copy local Spark tarball if available, otherwise download
# To use local: place spark-3.5.2-bin-hadoop3.tgz in project root
COPY spark-3.5.2-bin-hadoop3.tgz* /tmp/
RUN if [ -f /tmp/spark-3.5.2-bin-hadoop3.tgz ]; then \
        echo "Using local Spark tarball..." && \
        tar -xzf /tmp/spark-3.5.2-bin-hadoop3.tgz -C /opt && \
        rm /tmp/spark-3.5.2-bin-hadoop3.tgz; \
    else \
        echo "Downloading Spark 3.5.2..." && \
        curl -fL --progress-bar https://archive.apache.org/dist/spark/spark-3.5.2/spark-3.5.2-bin-hadoop3.tgz | tar -xz -C /opt; \
    fi && \
    mv /opt/spark-3.5.2-bin-hadoop3 /opt/spark

# Set environment variables
ENV LIGHTNING_HOME=/opt/lightning-catalog
ENV LIGHTNING_SERVER_PORT=8080
ENV LIGHTNING_GUI_PORT=8081
ENV SPARK_HOME=/opt/spark

# Create directory structure
RUN mkdir -p $LIGHTNING_HOME/web \
             $LIGHTNING_HOME/lib \
             $LIGHTNING_HOME/bin \
             $LIGHTNING_HOME/model \
             $LIGHTNING_HOME/3rd-party-lib \
             $LIGHTNING_HOME/history

# Copy pre-built frontend
COPY gui/build/ $LIGHTNING_HOME/web/

# Copy pre-built JARs (extract from distribution tarball)
COPY spark/v3.5/spark-runtime/build/distributions/lightning-metastore-3.5*.tar /tmp/
RUN tar -xf /tmp/lightning-metastore-3.5*.tar -C /tmp && \
    cp /tmp/lightning-metastore-3.5*/lib/*.jar $LIGHTNING_HOME/lib/ && \
    rm -rf /tmp/lightning-metastore-*

# Copy startup scripts
COPY spark/spark-shell/*.sh $LIGHTNING_HOME/bin/
RUN chmod +x $LIGHTNING_HOME/bin/*.sh

# Create startup script that runs in foreground
RUN echo '#!/bin/bash\n\
set -e\n\
\n\
export LIGHTNING_SERVER_PORT=${LIGHTNING_SERVER_PORT:-8080}\n\
export LIGHTNING_GUI_PORT=${LIGHTNING_GUI_PORT:-8081}\n\
export LIGHTNING_API_URL=${LIGHTNING_API_URL:-http://localhost:8080}\n\
\n\
echo "Starting Lightning Catalog Server..."\n\
echo "API Port: $LIGHTNING_SERVER_PORT"\n\
echo "GUI Port: $LIGHTNING_GUI_PORT"\n\
echo "API URL: $LIGHTNING_API_URL"\n\
\n\
# Inject runtime API URL into the GUI config\n\
echo "window.RUNTIME_CONFIG = { API_URL: \"${LIGHTNING_API_URL}\" };" > ${LIGHTNING_HOME}/web/config.js\n\
\n\
exec ${SPARK_HOME}/bin/spark-submit \\\n\
    --class com.zetaris.lightning.catalog.api.LightningAPIServer \\\n\
    --name "Lightning Server" \\\n\
    --conf "spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension,com.zetaris.lightning.spark.LightningSparkSessionExtension" \\\n\
    --conf "spark.sql.catalog.lightning=com.zetaris.lightning.catalog.LightningCatalog" \\\n\
    --conf "spark.sql.catalog.lightning.type=hadoop" \\\n\
    --conf "spark.sql.catalog.lightning.warehouse=$LIGHTNING_HOME/model" \\\n\
    --conf "spark.sql.catalog.lightning.accessControlProvider=com.zetaris.lightning.analysis.NotAppliedAccessControlProvider" \\\n\
    --conf "spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog" \\\n\
    --conf "spark.driver.memory=2g" \\\n\
    --conf "spark.executor.memory=2g" \\\n\
    --jars "$LIGHTNING_HOME/lib/*,$LIGHTNING_HOME/3rd-party-lib/*" \\\n\
    spark-internal\n\
' > /opt/start.sh && chmod +x /opt/start.sh

# Expose ports
EXPOSE 8080 8081

# Set working directory
WORKDIR $LIGHTNING_HOME

# Set ownership to UID/GID 1000 to match Kubernetes securityContext
# GID 1000 already exists in base image, so just ensure ownership is correct
RUN chown -R 1000:1000 $LIGHTNING_HOME && \
    chown 1000:1000 /opt/start.sh

USER 1000

# Default command
CMD ["/opt/start.sh"]
