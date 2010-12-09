#! /bin/sh
# Note that S1AS_HOME must be defined before this script is called.
# The current directory must be the directory containing this script.
APS_HOME="test/OrbFailOver/build"
APPCLIENT="${S1AS_HOME}/bin/appclient"
PROPS_FILE=${PWD}/logging.properties
DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y"
SETUP_ARGS="-Djava.util.logging.config.file=${PROPS_FILE}"

// The following setup is machine specific:
DAS_HOST="minas"
// Must allow at least 5 instances for the test
AVAILABLE_NODES="minas:3,apollo:4,hermes:2"
EJB_NAME="${APS_HOME}/OrbFailOver-ejb.jar"
CLIENT_NAME="${APS_HOME}/OrbFailOver-app-client.jar"

TEST_ARGS="-installDir ${S1AS_HOME} -dasNode ${DAS_HOST} -availableNodes ${AVAILABLE_NODES} -testEjb ${EJB_NAME}"

set -x

if [ "${DEBUGGER}" = "1" ];
then ${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
else ${APPCLIENT} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
fi
