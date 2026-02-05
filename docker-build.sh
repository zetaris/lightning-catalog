#!/bin/bash
set -e

# Configuration - customize these for your environment
IMAGE_NAME="${IMAGE_NAME:-lightning-catalog}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
REGISTRY="${REGISTRY:-}"  # e.g., "docker.io/myuser" or "myregistry.azurecr.io"

echo "=== Lightning Catalog Docker Build ==="

# Build frontend
echo ""
echo ">>> Building frontend..."
cd gui
npm install
npm run build
cd ..

# Build backend
echo ""
echo ">>> Building backend..."
./gradlew clean build -x test -x integrationTest \
    -DdefaultSparkMajorVersion=3.5 \
    -DdefaultSparkVersion=3.5.0

# Build Docker image
echo ""
echo ">>> Building Docker image..."

if [ -n "$REGISTRY" ]; then
    FULL_IMAGE="${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
else
    FULL_IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"
fi

docker build -t "$FULL_IMAGE" .

echo ""
echo "=== Build complete ==="
echo "Image: $FULL_IMAGE"
echo ""
echo "To run locally:"
echo "  docker run -p 8080:8080 -p 8081:8081 $FULL_IMAGE"
echo ""
echo "To push to registry:"
echo "  docker push $FULL_IMAGE"
