#!/usr/bin/env bash

declare -rx CONTEXT_NAME=hearing
declare -rx FRAMEWORK_VERSION=8.0.5
declare -rx EVENT_STORE_VERSION=8.3.0
declare -rx FRAMEWORK_LIBRARIES_VERSION=8.0.5

function buildWars {
  echo
  echo "Building wars."
  mvn clean install -nsu ${@}
  echo
  echo "Finished building wars"
}


function buildWithSonar {
    echo
    echo "Building with Sonar"
    mvn -C -U verify sonar:sonar -Dsonar.analysis.mode=preview -Dsonar.issuesReport.html.enable=true -Dsonar.exclusions=target/generated-sources/** -Dhttp.proxyHost=10.224.23.8 -Dhttp.proxyPort=3128 -Dsonar.host.url=http://10.124.22.71:9000
    echo "\n"
    echo "Finished building with Sonar. Reports are available at $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/target/sonar/issues-report/issues-report.html"
}

function startVagrant {
  set +e
  vagrant global-status | grep atcm-vagrant | grep running
  if [ "$?" -eq 1 ];
  then
    echo "Starting Vagrant machine from " $VAGRANT_DIR
      export VAGRANT_CWD=$VAGRANT_DIR
      time vagrant up ;
  else
    echo "Vagrant is already running"
  fi
  set -e
}

function deleteWars {
  echo                                    d
  echo "Deleting wars from $WILDFLY_DEPLOYMENT_DIR....."
  rm -rf $WILDFLY_DEPLOYMENT_DIR/*.war
  rm -rf $WILDFLY_DEPLOYMENT_DIR/*.deployed
}

function deployWars {
  rm -rf $WILDFLY_DEPLOYMENT_DIR/*.undeployed
  find . \( -iname "${CONTEXT_NAME}-service-*.war" \) -exec cp {} $WILDFLY_DEPLOYMENT_DIR \;

  echo "Copied wars to $WILDFLY_DEPLOYMENT_DIR"
}

function deployWiremock() {
    mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:copy -DoutputDirectory=$WILDFLY_DEPLOYMENT_DIR -Dartifact=uk.gov.justice.services:wiremock-service:1.1.0:war
}

function checkWiremock(){
    while [ true ]
    do 
      CHECK_STRING="curl -i --connect-timeout 5 -s http://localhost:8080/__admin/"
      echo -n $CHECK_STRING
      CHECK=$( $CHECK_STRING )  >/dev/null 2>&1
      echo $CHECK
      echo $CHECK | grep "200 OK" >/dev/null 2>&1 && break
      sleep $RETRY_DELAY
    done 
}

function healthCheck {
  CONTEXT=("$CONTEXT_NAME-command-api"  "$CONTEXT_NAME-command-handler" "${CONTEXT_NAME}-query-api"  "${CONTEXT_NAME}-query-view" "${CONTEXT_NAME}-event-listener")
  CONTEXT_COUNT=${#CONTEXT[@]}
  TIMEOUT=180
  RETRY_DELAY=5
  START_TIME=$(date +%s)

  echo "Start time is $START_TIME"
  echo "Starting health check on ${CONTEXT[@]}"
  echo "Conducting health check on $CONTEXT_COUNT contexts"
  echo "TIMEOUT is $TIMEOUT Seconds"
  echo "RETRY_DELAY $RETRY_DELAY Seconds"

  while [ true ]
  do
      DEPLOYED=0

      for i in ${CONTEXT[@]}
      do
        CHECK_STRING="curl --connect-timeout 1 -s http://localhost:8080/$i/internal/metrics/ping"
        echo -n $CHECK_STRING
        CHECK=$( $CHECK_STRING )  >/dev/null 2>&1
        echo $CHECK | grep pong >/dev/null 2>&1 && DEPLOYED=$((DEPLOYED + 1))
        echo $CHECK | grep pong >/dev/null 2>&1 && echo " pong" || echo " DOWN"
      done

      echo
      echo RESULT:  ${DEPLOYED} out of  ${CONTEXT_COUNT} wars came back with pong

      [ "${DEPLOYED}" -eq "${CONTEXT_COUNT}" ] && break

      TIME_NOW=$(date +%s)
      TIME_ELAPSED=$(( $TIME_NOW - $START_TIME ))

      echo "Start time is $START_TIME"
      echo "Time Now is $TIME_NOW"
      echo "Time elapsed is $TIME_ELAPSED"


     [ "${TIME_ELAPSED}" -gt "${TIMEOUT}" ] && exit
      sleep $RETRY_DELAY

  done
}

function integrationTests {
  echo
  echo "Running Integration Tests"
  mvn -f ${CONTEXT_NAME}-integration-test/pom.xml clean integration-test -P${CONTEXT_NAME}-integration-test
  echo "Finished running Integration Tests"
}

function runEventLogLiquibase() {
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:event-repository-liquibase:${EVENT_STORE_VERSION}:jar
    java -jar target/event-repository-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}eventstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update

    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:aggregate-snapshot-repository-liquibase:${EVENT_STORE_VERSION}:jar
    java -jar target/aggregate-snapshot-repository-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}eventstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
}

function runEventBufferLiquibase() {
    echo "running event buffer liquibase"
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:event-buffer-liquibase:${EVENT_STORE_VERSION}:jar
    java -jar target/event-buffer-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "finished running event buffer liquibase"
}

function runSystemLiquibase {
    echo "Running system liquibase"
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.services:framework-system-liquibase:${FRAMEWORK_VERSION}:jar
    java -jar target/framework-system-liquibase-${FRAMEWORK_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}system --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished executing system liquibase"
}

function runEventTrackingLiquibase {
    echo "Running event tracking liquibase"
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.event-store:event-tracking-liquibase:${EVENT_STORE_VERSION}:jar
    java -jar target/event-tracking-liquibase-${EVENT_STORE_VERSION}.jar --url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore --username=${CONTEXT_NAME} --password=${CONTEXT_NAME} --logLevel=info update
    echo "Finished executing event tracking liquibase"
}

function runViewStoreLiquibase {
  #run liquibase for context
  mvn -f ${CONTEXT_NAME}-viewstore/${CONTEXT_NAME}-viewstore-liquibase/pom.xml -Dliquibase.url=jdbc:postgresql://localhost:5432/${CONTEXT_NAME}viewstore -Dliquibase.username=${CONTEXT_NAME} -Dliquibase.password=${CONTEXT_NAME} -Dliquibase.logLevel=info resources:resources liquibase:update
  echo "Finished executing liquibase"
}

function runFileServiceLiquibase() {
    echo "running file service liquibase"
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.0.1:copy -DoutputDirectory=target -Dartifact=uk.gov.justice.services:file-service-liquibase:${FRAMEWORK_LIBRARIES_VERSION}:jar
    java -jar target/file-service-liquibase-${FRAMEWORK_LIBRARIES_VERSION}.jar --url=jdbc:postgresql://localhost:5432/fileservice --username=fileservice --password=fileservice --logLevel=info update
    echo "finished file service  liquibase"
}

function runLiquibase {
   runEventLogLiquibase
   runEventBufferLiquibase
   runViewStoreLiquibase
   runSystemLiquibase
   runEventTrackingLiquibase
   runFileServiceLiquibase
   echo "All liquibase update scripts run"
}

function buildDeployAndTest {

  local OPTIND
  local SKIP_UNIT_TESTS
  local SKIP_INTEGRATION_TESTS="false"

  while getopts ":aiu" OPTION; do
    case "${OPTION}" in
      u)
        SKIP_UNIT_TESTS="-DskipTests"
	printf '\e[1;92m%-6s\e[m' "${0##*/}: Skiping the maven unit tests" ;;
      i)
	printf '\e[1;92m%-6s\e[m' "${0##*/}: Skiping the health check and integration tests" ;;
      a)
        SKIP_UNIT_TESTS="-DskipTests"
        SKIP_INTEGRATION_TESTS="skipIntegrationTests"
	printf '\e[1;92m%-6s\e[m' "${0##*/} Skiping all tests" ;;
      *) 
        usage ;;
    esac
  done

  shift $((OPTIND-1))

  buildWars ${SKIP_UNIT_TESTS}
  deployAndTest ${SKIP_INTEGRATION_TESTS}
}

function deployAndTest {
  deleteWars
  deployWiremock
  startVagrant
  runLiquibase
  deployWars
  if [[ "skipIntegrationTests" != "${1}" ]]; then
      healthCheck
      checkWiremock
      integrationTests
  fi

}


function usage() {
  cat <<EOF
Usage: ${0##*/} [OPTION]

  -a	equivalent to -mi
  -u	skip the unit tests
  -i	skip the health check & integration tests
  -h	show this help usage

Examples:
${0##*/} -a	Skip all tests
${0##*/} -u	Skip the unit tests
${0##*/} -i	Skip the health check and integration tests

EOF
  exit 1
}
