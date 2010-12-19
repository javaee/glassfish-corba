#! /bin/sh
# Note that S1AS_HOME and GFV3_WORK must be defined before this script is called.
# The script installgfv3 must be available, and must install your GF 3.1
# into ${GFV3_WORK}.
# The current directory must be the directory containing this script.

################################################################
# The following setup is environment specific:
################################################################

DAS_HOST="minas"
# Must allow at least 5 instances for the test
# Must be able to access the avilable nodes from the DAS using 
# SSH without password.

# Just test on minas for now until we get things working again
# AVAILABLE_NODES="minas:3,apollo:4"
AVAILABLE_NODES="minas:3"

################################################################
# Do the following to create a new cluster and new GF installation
################################################################

# assume that we are running on minas
killgf
# ssh apollo /volumes/home/ken/bin/killgf
installgfv3
date
SKIP_SETUP="false"

################################################################
# The rest of the script is fixed
################################################################

APS_HOME="/space/ws/mercurial/CORBA/iiop-folb-test/test/OrbFailOver"
APPCLIENT="${S1AS_HOME}/bin/appclient"
PROPS_FILE=${PWD}/logging.properties

EJB_NAME="${APS_HOME}/OrbFailOver-ejb/dist/OrbFailOver-ejb.jar"
CLIENT_NAME="${APS_HOME}/OrbFailOver-app-client/dist/OrbFailOver-app-client.jar"

DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y"
SETUP_ARGS="-Djava.util.logging.config.file=${PROPS_FILE}"
TEST_ARGS="-installDir ${GFV3_WORK}/glassfish3 -dasNode ${DAS_HOST} -availableNodes ${AVAILABLE_NODES} -testEjb ${EJB_NAME} -doCleanup false -skipSetup ${SKIP_SETUP} -exclude listContextTest"

set -x

if [ "${DEBUGGER}" = "1" ];
then ${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
else ${APPCLIENT} ${SETUP_ARGS} -client ${CLIENT_NAME} -name OrbFailOver-app-client ${TEST_ARGS} $@ ;
fi
date
