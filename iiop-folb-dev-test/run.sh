#! /bin/sh
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common Development
# and Distribution License("CDDL") (collectively, the "License").  You
# may not use this file except in compliance with the License.  You can
# obtain a copy of the License at
# https://oss.oracle.com/licenses/CDDL+GPL-1.1
# or LICENSE.txt.  See the License for the specific
# language governing permissions and limitations under the License.
#
# When distributing the software, include this License Header Notice in each
# file and include the License file at LICENSE.txt.
#
# GPL Classpath Exception:
# Oracle designates this particular file as subject to the "Classpath"
# exception as provided by Oracle in the GPL Version 2 section of the License
# file that accompanied this code.
#
# Modifications:
# If applicable, add the following below the License Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyright [year] [name of copyright owner]"
#
# Contributor(s):
# If you wish your version of this file to be governed by only the CDDL or
# only the GPL Version 2, indicate your decision by adding "[Contributor]
# elects to include this software in this distribution under the [CDDL or GPL
# Version 2] license."  If you don't indicate a single choice of license, a
# recipient has the option to distribute your version of this file under
# either the CDDL, the GPL Version 2 or to extend the choice of license to
# its licensees as provided above.  However, if you add GPL Version 2 code
# and therefore, elected the GPL Version 2 license, then the option applies
# only if the new code is made subject to such option by the copyright
# holder.
#

# Note that S1AS_HOME and GFV3_WORK must be defined before this script is called.
# GFV3_WS must be set in order to use scripts/installgfv3.
# CORBA_WS must be set in order to use scripts/installorb.
# The script installgfv3 must be available, and must install your GF 3.1
# into ${GFV3_WORK}.
# The current directory must be the directory containing this run.sh script.

################################################################
# The following setup is environment specific:
################################################################
# Use 'localhost' for single node setup. See issue GLASSFISH-15863
DAS_HOST="localhost"
# Must allow at least 5 instances for the test
# Must be able to access the available nodes from the DAS using 
# SSH without password.

AVAILABLE_NODES="localhost:5"

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
# scripts/installgfv3
# scripts/installgforb
# scripts/installgfnaming
# scripts/installgfejbsec
# scripts/installorb
SKIP_SETUP="false"

# Only use this if no installations (see above) is needed.
# SKIP_SETUP="true"

################################################################
# The rest of the script is fixed
################################################################

date

APS_HOME="`pwd`/test/OrbFailOver"
APPCLIENT="${S1AS_HOME}/bin/appclient"
PROPS_FILE=${PWD}/logging.properties
echo ${PROPS_FILE}

EJB_NAME="${APS_HOME}/OrbFailOver-ejb/dist/OrbFailOver-ejb.jar"
CLIENT_NAME="${APS_HOME}/OrbFailOver-app-client/dist/OrbFailOver-app-client.jar"

ENDPOINTS="localhost:9037,localhost:10037,localhost:11037"
TARGET_ARGS="-targetserver ${ENDPOINTS}"
TARGET_DEF="-Dcom.sun.appserv.iiop.endpoints=${ENDPOINTS}"
DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8228,server=y,suspend=y"
SETUP_ARGS="-Djava.util.logging.config.file=${PROPS_FILE}"
TEST_ARGS="-installDir ${GFV3_WORK}/glassfish4 -dasNode ${DAS_HOST} -availableNodes ${AVAILABLE_NODES} -testEjb ${EJB_NAME} -doCleanup false -skipSetup ${SKIP_SETUP}"
CLIENT_ARGS="-client ${CLIENT_NAME} -name OrbFailOver-app-client -serverORBDebug folb"

set -x

# echo ${APPCLIENT} ${SETUP_ARGS} ${CLIENT_ARGS} ${TEST_ARGS} $@ ;

if [ "${DEBUGGER}" = "1" ];
then 
    CMD="${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} ${CLIENT_ARGS} ${TEST_ARGS} $@ "    
else 
#    CMD="${APPCLIENT} ${SETUP_ARGS} ${CLIENT_ARGS} ${TEST_ARGS} $@ "
    CMD="${APPCLIENT} ${SETUP_ARGS} -Djava.rmi.server.useCodebaseOnly=true ${CLIENT_ARGS} ${TEST_ARGS} $@ "
fi
echo $CMD
$CMD
exitStatus=$?

# For testing with externally supplied endpoints
# if [ "${DEBUGGER}" = "1" ];
# then ${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} ${TARGET_DEF} ${CLIENT_ARGS} ${TEST_ARGS} -useExternalEndpoints true $@
# else ${APPCLIENT} ${SETUP_ARGS} ${CLIENT_ARGS} ${TEST_ARGS} -useExternalEndpoints true $@
# fi 

date
exit $exitStatus

