#!/bin/bash
set -e

# ─── Usage ──────────────────────────────────────────────────────────
# Build a Docker image from a local WAR and deploy to a K8s namespace.
#
#   customimage.sh <namespace> [repo_path]
#
# Examples:
#   cd ~/gitrepos/cpp-context-listing && customimage.sh ns-ste-ccm-86
#   customimage.sh ns-ste-ccm-86 ~/gitrepos/cpp-context-hearing
# ────────────────────────────────────────────────────────────────────

REGISTRY="${CPP_ACR_REGISTRY:?ERROR: CPP_ACR_REGISTRY not set. Example: export CPP_ACR_REGISTRY=yourregistry.azurecr.io}"
REGISTRY_PATH="${CPP_ACR_REGISTRY_PATH:?ERROR: CPP_ACR_REGISTRY_PATH not set. Example: export CPP_ACR_REGISTRY_PATH=your-org}"
BASE_IMAGE_TAG="${CPP_WILDFLY_BASE_IMAGE_TAG:-26.1.3.Finaljdk17_latest}"
CONTAINER_NAME="wildfly-app"
WILDFLY_HOME="/opt/jboss/wildfly"

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <namespace> [repo_path]"
    echo "  namespace  : K8s namespace (e.g. ns-ste-ccm-86)"
    echo "  repo_path  : path to cpp-context-* repo (default: current directory)"
    exit 1
fi

NAMESPACE=$1
REPO_PATH="${2:-$(pwd)}"

BACKUP_DIR="$HOME/.kube-deploy-backups"
mkdir -p "$BACKUP_DIR"

# Resolve to absolute path
REPO_PATH="$(cd "$REPO_PATH" && pwd)"

# ─── Detect context name from repo directory ────────────────────────
REPO_NAME="$(basename "$REPO_PATH")"
if [[ ! "$REPO_NAME" =~ ^cpp-context- ]]; then
    echo "ERROR: $REPO_PATH does not look like a cpp-context-* repo (got: $REPO_NAME)"
    exit 1
fi

# Derive context: cpp-context-listing → listing, cpp-context-listing-courtscheduler → listingcourtscheduler
CONTEXT_RAW="${REPO_NAME#cpp-context-}"
# The service module and Docker image use a collapsed name (no hyphens between context parts)
# e.g. listing-courtscheduler → listingcourtscheduler for the service name
# but listing stays as listing

# ─── Find the service WAR ──────────────────────────────────────────
# Some repos (e.g. listing) use a *-service module as the deployable WAR.
# Others (e.g. courtscheduler) use a *-api module with a different context root.
# Strategy: if the K8s probe path is known, pick the WAR whose jboss-web.xml
# context-root matches the probe. Fall back to *-service/target/*.war.
SERVICE_DIR=""
WAR_FILE=""

# Collect all candidate WARs (service first, then api)
CANDIDATE_WARS=()
for candidate in "${REPO_PATH}/"*-service/target/*.war; do
    [ -f "$candidate" ] && CANDIDATE_WARS+=("$candidate")
done
for candidate in "${REPO_PATH}/"*-api/target/*.war; do
    [ -f "$candidate" ] && CANDIDATE_WARS+=("$candidate")
done

if [ "${#CANDIDATE_WARS[@]}" -eq 0 ]; then
    echo "ERROR: No WAR file found. Have you built the project? (mvn clean install -DskipTests)"
    echo "Searched: ${REPO_PATH}/*-service/target/*.war and ${REPO_PATH}/*-api/target/*.war"
    exit 1
fi

# If only one candidate, use it
if [ "${#CANDIDATE_WARS[@]}" -eq 1 ]; then
    WAR_FILE="${CANDIDATE_WARS[0]}"
else
    # Multiple WARs found — try to match context-root to the K8s probe path.
    # Derive expected deployment name to query the probe path.
    _TMP_SERVICE_DIR="$(dirname "$(dirname "${CANDIDATE_WARS[0]}")")"
    _TMP_SERVICE_MODULE="$(basename "$_TMP_SERVICE_DIR")"
    _TMP_DEPLOYMENT="${_TMP_SERVICE_MODULE}-wildfly-app"
    _PROBE_PATH="$(kubectl get deployment "$_TMP_DEPLOYMENT" -n "$NAMESPACE" \
        -o jsonpath='{.spec.template.spec.containers[0].livenessProbe.httpGet.path}' 2>/dev/null || true)"
    # Extract the first path segment: /listingcourtscheduler-api/... → listingcourtscheduler-api
    _EXPECTED_CTX="$(echo "$_PROBE_PATH" | sed 's|^/||' | cut -d/ -f1)"

    if [ -n "$_EXPECTED_CTX" ]; then
        for candidate in "${CANDIDATE_WARS[@]}"; do
            _CTX="$(unzip -p "$candidate" WEB-INF/jboss-web.xml 2>/dev/null \
                | grep -oP '(?<=<context-root>)/?\K[^<]+' || true)"
            _CTX="${_CTX#/}"  # strip leading slash
            if [ "$_CTX" = "$_EXPECTED_CTX" ]; then
                WAR_FILE="$candidate"
                echo "Matched WAR context-root /${_CTX} to probe path ${_PROBE_PATH}"
                break
            fi
        done
    fi

    # Fall back to first candidate (service WAR) if no match
    if [ -z "$WAR_FILE" ]; then
        WAR_FILE="${CANDIDATE_WARS[0]}"
        echo "WARNING: Could not match WAR to probe path. Using: $(basename "$WAR_FILE")"
    fi
fi

# SERVICE_DIR always points to the *-service module (for deployment name derivation)
SERVICE_DIR=""
for candidate in "${REPO_PATH}/"*-service; do
    [ -d "$candidate/target" ] && SERVICE_DIR="$candidate" && break
done
[ -z "$SERVICE_DIR" ] && SERVICE_DIR="$(dirname "$(dirname "$WAR_FILE")")"

WAR_FILENAME="$(basename "$WAR_FILE")"
SERVICE_MODULE="$(basename "$SERVICE_DIR")"
# e.g. listing-service, listingcourtscheduler-service, hearing-service
SERVICE_NAME="${SERVICE_MODULE}"
IMAGE_NAME="${SERVICE_NAME}"
DEPLOYMENT_NAME="${SERVICE_NAME}-wildfly-app"

echo "─────────────────────────────────────────"
echo "Repo:        $REPO_NAME"
echo "Service:     $SERVICE_NAME"
echo "WAR:         $WAR_FILENAME"
echo "Deployment:  $DEPLOYMENT_NAME"
echo "Namespace:   $NAMESPACE"
echo "─────────────────────────────────────────"

# ─── Get version and commit for image tag ──────────────────────────
POM_VERSION="$(cd "$REPO_PATH" && mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
SHORT_COMMIT="$(cd "$REPO_PATH" && git rev-parse --short=10 HEAD)"
BRANCH="$(cd "$REPO_PATH" && git branch --show-current)"

# Build tag: sanitise branch name (take last segment, uppercase, max 12 chars)
BRANCH_PREFIX="$(echo "$BRANCH" | sed 's|.*/||' | tr '[:lower:]' '[:upper:]' | tr -cd '[:alnum:]' | cut -c1-12)"
BUILD_TS="$(date -u +%Y%m%d%H%M%S)"
IMAGE_TAG="${BRANCH_PREFIX}_${SHORT_COMMIT}_${BUILD_TS}"

FULL_IMAGE="${REGISTRY}/${REGISTRY_PATH}/${IMAGE_NAME}:${IMAGE_TAG}"

echo "Image tag:   $IMAGE_TAG"
echo "Full image:  $FULL_IMAGE"
echo "─────────────────────────────────────────"

# ─── Verify deployment exists ──────────────────────────────────────
echo "Checking deployment ${DEPLOYMENT_NAME} in ${NAMESPACE}..."
if ! kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" > /dev/null 2>&1; then
    echo "ERROR: Deployment $DEPLOYMENT_NAME not found in namespace $NAMESPACE"
    echo "Available deployments:"
    kubectl get deployments -n "$NAMESPACE" -o name 2>/dev/null | grep wildfly | sed 's|deployment.apps/||'
    exit 1
fi

# ─── Backup deployment before changes ─────────────────────────────
BACKUP_FILE="$BACKUP_DIR/${NAMESPACE}_${DEPLOYMENT_NAME}.json"
echo "Backing up deployment to: $BACKUP_FILE"
kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" -o json > "$BACKUP_FILE"
CURRENT_REVISION=$(kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" \
    -o jsonpath='{.metadata.annotations.deployment\.kubernetes\.io/revision}')
if [ -n "$CURRENT_REVISION" ]; then
    echo "$CURRENT_REVISION" > "$BACKUP_DIR/${NAMESPACE}_${DEPLOYMENT_NAME}.revision"
    echo "Saved pre-change revision: $CURRENT_REVISION"
fi

# ─── Build Docker image ────────────────────────────────────────────
TMPDIR="$(mktemp -d)"
trap "rm -rf $TMPDIR" EXIT

cp "$WAR_FILE" "$TMPDIR/$WAR_FILENAME"

cat > "$TMPDIR/Dockerfile" <<DOCKERFILE
FROM ${REGISTRY}/${REGISTRY_PATH}/wildfly:${BASE_IMAGE_TAG}
ARG wildfly_home=${WILDFLY_HOME}
ARG wildfly_user=jboss
COPY ${WAR_FILENAME} \${wildfly_home}/standalone/deployments/
USER root
RUN chown \${wildfly_user}:\${wildfly_user} \${wildfly_home}/standalone/deployments/${WAR_FILENAME}
USER \${wildfly_user}
LABEL custom_build="true"
LABEL source_branch="${BRANCH}"
LABEL source_commit="${SHORT_COMMIT}"
LABEL pom_version="${POM_VERSION}"
DOCKERFILE

echo "Building Docker image..."
docker build -t "$FULL_IMAGE" "$TMPDIR"

# ─── Push to ACR ───────────────────────────────────────────────────
echo "Logging in to ACR..."
az acr login --name "${REGISTRY%%.*}" 2>/dev/null || {
    echo "ERROR: ACR login failed. Run: az login"
    exit 1
}

echo "Pushing image..."
docker push "$FULL_IMAGE"

# ─── Add startup probe to survive slow Wildfly boot ───────────────
# Wildfly apps often take >150s to start, exceeding the default liveness
# initialDelaySeconds (150). A startup probe disables liveness/readiness
# checks until the app has actually started, preventing premature kills.
# Also reset any stedebug.sh patches (relaxed liveness) to defaults.
PROBE_PATH=$(kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" \
    -o jsonpath='{.spec.template.spec.containers[0].livenessProbe.httpGet.path}')
PROBE_PORT=$(kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" \
    -o jsonpath='{.spec.template.spec.containers[0].livenessProbe.httpGet.port}')

echo "Adding startup probe (${PROBE_PATH}, 10s × 30 = 300s budget)..."
PATCH='[
    {"op": "add", "path": "/spec/template/spec/containers/0/startupProbe", "value": {
        "httpGet": {
            "path": "'"$PROBE_PATH"'",
            "port": "'"$PROBE_PORT"'",
            "scheme": "HTTP",
            "httpHeaders": [{"name": "Host", "value": "api"}]
        },
        "periodSeconds": 10,
        "failureThreshold": 30,
        "timeoutSeconds": 15
    }},
    {"op": "replace", "path": "/spec/template/spec/containers/0/image", "value": "'"$FULL_IMAGE"'"}'

# Reset liveness to defaults if stedebug.sh had relaxed them
LIVE_PERIOD=$(kubectl get deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" \
    -o jsonpath='{.spec.template.spec.containers[0].livenessProbe.periodSeconds}')
if [ "$LIVE_PERIOD" -gt 60 ] 2>/dev/null; then
    echo "Resetting relaxed liveness probe (periodSeconds=${LIVE_PERIOD}) to defaults..."
    PATCH="${PATCH}"',
    {"op": "replace", "path": "/spec/template/spec/containers/0/livenessProbe/periodSeconds", "value": 3},
    {"op": "replace", "path": "/spec/template/spec/containers/0/livenessProbe/failureThreshold", "value": 3}'
fi

PATCH="${PATCH}]"

# ─── Deploy (single patch = single rollout) ───────────────────────
echo "Deploying to ${NAMESPACE}..."
kubectl patch deployment "$DEPLOYMENT_NAME" -n "$NAMESPACE" --type='json' -p="$PATCH"

echo "Waiting for rollout..."
kubectl rollout status "deployment/${DEPLOYMENT_NAME}" -n "$NAMESPACE" --timeout=600s

echo ""
echo "═════════════════════════════════════════"
echo " Deployed successfully!"
echo " Image:      $FULL_IMAGE"
echo " Deployment: $DEPLOYMENT_NAME"
echo " Namespace:  $NAMESPACE"
echo ""
echo " Rollback:   kubectl rollout undo deployment/${DEPLOYMENT_NAME} -n ${NAMESPACE}"
echo " Backup:     $BACKUP_FILE"
echo "═════════════════════════════════════════"
