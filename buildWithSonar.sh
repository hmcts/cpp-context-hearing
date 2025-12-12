#!/usr/bin/env bash

#Script that runs liquibase, deploys wars and runs integration tests

${VAGRANT_DIR:?"Please export VAGRANT_DIR environment variable to point at atcm-vagrant"}
WILDFLY_DEPLOYMENT_DIR="${VAGRANT_DIR}/deployments"
declare -rx CONTEXT_NAME=hearing
declare -rx FRAMEWORK_LIBRARIES_VERSION=17.5.5
declare -rx FRAMEWORK_VERSION=17.5.5
declare -rx EVENT_STORE_VERSION=17.5.4

#fail script on error
set -e

. functions.sh

buildWithSonar

