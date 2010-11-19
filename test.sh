#! /bin/sh
. ../cluster.props
APS_HOME="test/OrbFailOver/build"
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
APPCLIENT="${S1AS_HOME}/bin/appclient"
PROPS_FILE=${PWD}/logging.properties
DEBUG_ARGS="-agentlib:jdwp=transport=dt_socket,address=8118,server=y,suspend=y"

# HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT},${instance3_node}:${instance3_IIOP_LISTENER_PORT} 
HOST_PORTS=${instance1_node}:${instance1_IIOP_LISTENER_PORT},${instance2_node}:${instance2_IIOP_LISTENER_PORT}

SETUP_ARGS="-Djava.util.logging.config.file=${PROPS_FILE} -Dcom.sun.appserv.iiop.endpoints=${HOST_PORTS}"
TEST_ARGS="-Dtest.folb.asadmin.command=${ASADMIN} -Dtest.folb.instances=in1,in2,in3"

set -x
${ASADMIN} deploy --target ${cluster_name} --force ${APS_HOME}/OrbFailOver-ejb.jar

if [ "${DEBUGGER}" = "1" ];
then ${APPCLIENT} ${DEBUG_ARGS} ${SETUP_ARGS} ${TEST_ARGS} -client  ${APS_HOME}/OrbFailOver-app-client.jar -targetserver ${HOST_PORTS} -name OrbFailOver-app-client $@ ;
else
${APPCLIENT} ${SETUP_ARGS} ${TEST_ARGS} -client  ${APS_HOME}/OrbFailOver-app-client.jar -targetserver ${HOST_PORTS} -name OrbFailOver-app-client $@ ;
fi
