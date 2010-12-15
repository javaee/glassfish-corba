#! /bin/sh
# Note that S1AS_HOME and GFV3_WORK must be defined before this script is called.
# The current directory must be the directory containing this script.

# assume that we are running on minas
# killgf
# ssh apollo /volumes/home/ken/bin/killgf
# installgfv3
APS_HOME="/space/ws/mercurial/CORBA/iiop-folb-test/test/OrbFailOver"
APPCLIENT="${S1AS_HOME}/bin/appclient"
PROPS_FILE=${PWD}/logging.properties

# The following setup is machine specific:
DAS_HOST="minas"
# Must allow at least 5 instances for the test
# AVAILABLE_NODES="minas:3,apollo:4"
# Just test on minas for now until we get things working again
AVAILABLE_NODES="minas:3"
EJB_NAME="${APS_HOME}/OrbFailOver-ejb/dist/OrbFailOver-ejb.jar"
CLIENT_NAME="${APS_HOME}/OrbFailOver-app-client/dist/OrbFailOver-app-client.jar"

DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y"
SETUP_ARGS="-Djava.util.logging.config.file=${PROPS_FILE}"
TEST_ARGS="-installDir ${GFV3_WORK}/glassfish3 -dasNode ${DAS_HOST} -availableNodes ${AVAILABLE_NODES} -testEjb ${EJB_NAME} -doCleanup false -skipSetup true -exclude listContextTest"

set -x

if [ "${DEBUGGER}" = "1" ];
then ${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
else ${APPCLIENT} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
fi
