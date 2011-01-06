#! /bin/sh
# Note that S1AS_HOME and GFV3_WORK must be defined before this script is called.
# GFV3_WS must be set in order to use scripts/installgfv3.
# CORBA_WS must be set in order to use scripts/installorb.
# The script installgfv3 must be available, and must install your GF 3.1
# into ${GFV3_WORK}.
# The current directory must be the directory containing this run.sh script.

################################################################
# The following setup is environment specific:
################################################################

DAS_HOST="minas"
# Must allow at least 5 instances for the test
# Must be able to access the avilable nodes from the DAS using 
# SSH without password.

AVAILABLE_NODES="minas:2,apollo:4"
# AVAILABLE_NODES="minas:3"

################################################################
# Do the following to create a new cluster and new GF installation,
# or just set SKIP_SETUP=true.  Different installXXX commands
# can be used to copy updated modules into the GF image, avoiding a
# rebuild of GF (which can take 30+ minutes)
# 
# Install scripts:
#   installgfv3     install GF 3.1 image from a local build
#   installgforb    install GF 3.1 orb glue bundles (orb/orb-iiop, etc.)
#   installgfnaming install GF 3.1 naming code (common/glassfish-naming)
#   installorb      install 3.1 version of ORB from a local build
################################################################

# Clean up old instances
# ${S1AS_HOME}/bin/asadmin stop-cluster c1
# ${S1AS_HOME}/bin/asadmin stop-domain

# Do any needed installs.  installgfv3 MUST be first.
# Only installgfv3 is needed for a fresh install; the others
# are for use if some of the GF or ORB code has been modified
# without rebuilding all of GF.
scripts/installgfv3
# scripts/installgforb
# scripts/installgfnaming
# scripts/installorb
SKIP_SETUP="false"

# Only use this if no installations (see above) is needed.
# SKIP_SETUP="true"

################################################################
# The rest of the script is fixed
################################################################

date

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
