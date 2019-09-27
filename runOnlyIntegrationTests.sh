#!/usr/bin/env bash

#Script that runs, liquibase, deploys wars and runs integration tests

if [[ -z "${VAGRANT_DIR}" ]]; then
  printf '%s\n' "Please export VAGRANT_DIR environment variable to point at atcm-vagrant";
  exit 1;
fi

if [[ ! -d "${VAGRANT_DIR}" ]]; then
  printf '%s\n' "${VAGRANT_DIR} does not exists."
  exit 1;
fi

declare -rx WILDFLY_DEPLOYMENT_DIR="${VAGRANT_DIR}/deployments"
declare -rx CONTEXT_NAME=hearing
declare -rx EVENT_STORE_VERSION=2.1.0
declare -rx FILE_SERVICE_VERSION=1.17.2

#fail script on error
set -e

source functions.sh

integrationTests ${@}

