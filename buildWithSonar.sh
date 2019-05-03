#!/usr/bin/env bash

#Script that runs, liquibase, deploys wars and runs integration tests

${VAGRANT_DIR:?"Please export VAGRANT_DIR environment variable to point at atcm-vagrant"}
WILDFLY_DEPLOYMENT_DIR="${VAGRANT_DIR}/deployments"
CONTEXT_NAME=resultinghmps
EVENT_STORE_VERSION=1.1.8
EVENT_BUFFER_VERSION=1.1.8

#fail script on error
set -e

. functions.sh

buildWithSonar

