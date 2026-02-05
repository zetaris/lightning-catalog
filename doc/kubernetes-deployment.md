# Lightning Catalog Kubernetes Deployment Guide

This guide covers deploying Lightning Catalog to Kubernetes using the provided Helm chart.

## Prerequisites

- Kubernetes cluster (1.19+)
- Helm 3.x installed
- kubectl configured to access your cluster
- Container registry access (for pulling images)
- Ingress controller (optional, for external access)

## Quick Start

### 1. Build and Push Docker Image

```bash
# Build frontend, backend, and push to registry
./docker-build.sh
```

This script:
- Builds the React frontend
- Builds the Scala/Spark backend
- Pushes the image to your container registry

### 2. Configure Helm Values

Edit `helm/lightning-catalog/values.yaml` to match your environment:

```yaml
# Image configuration
image:
  repository: your-registry.com/lightning-catalog
  tag: latest
  pullPolicy: Always

# Image pull secret for private registry (optional)
imagePullSecrets:
  - name: registry-secret

# Lightning Catalog configuration
lightning:
  serverPort: 8080
  guiPort: 8081
  # API URL for the GUI frontend (IMPORTANT: set this to your external API URL)
  apiUrl: "https://your-api-url.example.com"
  spark:
    driverMemory: "4g"
    executorMemory: "8g"

# Ingress for GUI
ingress:
  enabled: true
  className: ""  # Set to your ingress class (e.g., nginx, traefik)
  hosts:
    - host: your-gui-url.example.com
      paths:
        - path: /
          pathType: Prefix
          port: 8081

# Ingress for API
ingressApi:
  enabled: true
  className: ""
  hosts:
    - host: your-api-url.example.com
      paths:
        - path: /
          pathType: Prefix
          port: 8080
```

### 3. Deploy with Helm

```bash
# Install
helm install lightning-catalog ./helm/lightning-catalog -n your-namespace

# Or upgrade existing installation
helm upgrade lightning-catalog ./helm/lightning-catalog -n your-namespace
```

## Configuration

### Environment Variables

The following environment variables are passed to the container:

| Variable | Description | Default |
|----------|-------------|---------|
| `LIGHTNING_SERVER_PORT` | API server port | 8080 |
| `LIGHTNING_GUI_PORT` | GUI server port | 8081 |
| `LIGHTNING_API_URL` | External API URL for GUI frontend | http://localhost:8080 |
| `SPARK_DRIVER_MEMORY` | Spark driver memory | 4g |
| `SPARK_EXECUTOR_MEMORY` | Spark executor memory | 8g |

### API URL Configuration

The `lightning.apiUrl` value is critical for the GUI to communicate with the API. This URL is injected at container startup into `/opt/lightning-catalog/web/config.js`.

Set this to your external API URL:

```yaml
lightning:
  apiUrl: "https://api.yourdomain.com"
```

Or override at install time:

```bash
helm install lightning-catalog ./helm/lightning-catalog \
  --set lightning.apiUrl=https://api.yourdomain.com
```

### Persistence

By default, the chart uses an existing PVC for data persistence:

```yaml
persistence:
  enabled: true
  existingClaim: "your-existing-pvc"
  subPath: "lightning-catalog"
  mountPath: /opt/lightning-catalog/model
```

To create a new PVC instead:

```yaml
persistence:
  enabled: true
  existingClaim: ""  # Leave empty to create new PVC
  storageClass: "standard"
  accessMode: ReadWriteOnce
  size: 10Gi
```

To disable persistence (data will be lost on pod restart):

```yaml
persistence:
  enabled: false
```

### Ingress Configuration

The chart supports separate ingress resources for GUI and API:

```yaml
# GUI Ingress
ingress:
  enabled: true
  className: nginx
  annotations:
    # Add your ingress annotations here
  hosts:
    - host: catalog.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
          port: 8081
  tls:
    - secretName: catalog-tls
      hosts:
        - catalog.yourdomain.com

# API Ingress
ingressApi:
  enabled: true
  className: nginx
  hosts:
    - host: catalog-api.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
          port: 8080
  tls:
    - secretName: catalog-api-tls
      hosts:
        - catalog-api.yourdomain.com
```

### Security Context

The container runs as UID 1000 by default:

```yaml
podSecurityContext:
  fsGroup: 1000

securityContext:
  runAsUser: 1000
  runAsNonRoot: true
```

### Resources

Configure resource requests and limits:

```yaml
resources:
  requests:
    memory: "2Gi"
    cpu: "500m"
  limits:
    memory: "4Gi"
    cpu: "2000m"
```

## Operations

### View Logs

```bash
kubectl logs -n your-namespace deployment/lightning-catalog
```

### Restart Deployment

```bash
kubectl rollout restart deployment lightning-catalog -n your-namespace
```

### Check Status

```bash
kubectl get pods -n your-namespace -l app.kubernetes.io/name=lightning-catalog
kubectl get ingress -n your-namespace
```

### Verify API URL Configuration

```bash
# Check the config.js served by GUI
curl https://your-gui-url.example.com/config.js

# Should return:
# window.RUNTIME_CONFIG = { API_URL: "https://your-api-url.example.com" };
```

## Troubleshooting

### GUI shows localhost:8080 in network requests

1. Check that `lightning.apiUrl` is set correctly in values.yaml
2. Verify the env var is passed to the pod:
   ```bash
   kubectl describe pod -n your-namespace <pod-name> | grep LIGHTNING_API_URL
   ```
3. Check container logs for the API URL at startup:
   ```bash
   kubectl logs -n your-namespace <pod-name> | head -10
   ```
4. Verify config.js is being served with correct URL:
   ```bash
   curl https://your-gui-url/config.js
   ```

### Permission denied errors

Ensure the container is running as UID 1000 and file ownership matches:
```bash
kubectl exec -n your-namespace <pod-name> -- ls -la /opt/lightning-catalog/web/
```

### Pod fails to start

1. Check image pull secrets:
   ```bash
   kubectl get secret registry-secret -n your-namespace
   ```
2. Check pod events:
   ```bash
   kubectl describe pod -n your-namespace <pod-name>
   ```

### Persistence issues

1. Verify PVC is bound:
   ```bash
   kubectl get pvc -n your-namespace
   ```
2. Check storage class exists:
   ```bash
   kubectl get storageclass
   ```

## Example Deployment

```bash
# 1. Create namespace
kubectl create namespace lightning

# 2. Create registry pull secret (if using private registry)
kubectl create secret docker-registry registry-secret \
  --docker-server=your-registry.com \
  --docker-username=<username> \
  --docker-password=<password> \
  -n lightning

# 3. Deploy with custom values
helm install lightning-catalog ./helm/lightning-catalog \
  -n lightning \
  --set lightning.apiUrl=https://api.example.com \
  --set ingress.hosts[0].host=catalog.example.com \
  --set ingressApi.hosts[0].host=api.example.com

# 4. Verify deployment
kubectl get pods -n lightning
kubectl get ingress -n lightning
```
