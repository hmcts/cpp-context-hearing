#!/usr/bin/env bash

#Script that runs liquibase, deploys wars and runs integration tests

${VAGRANT_DIR:?"Please export VAGRANT_DIR environment variable to point at atcm-vagrant"}
WILDFLY_DEPLOYMENT_DIR="${VAGRANT_DIR}/deployments"
declare -rx CONTEXT_NAME=hearing
declare -rx FRAMEWORK_VERSION=6.4.0
declare -rx EVENT_STORE_VERSION=2.4.1
declare -rx FILE_SERVICE_VERSION=1.17.12

#fail script on error
set -e

. functions.sh

buildWithSonar

