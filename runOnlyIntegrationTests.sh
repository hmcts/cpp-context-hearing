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

#fail script on error
set -e

source functions.sh

integrationTests ${@}

