. ../cluster.props
S1AS_HOME="${instance1_s1as_home}/glassfish"
ASADMIN="${S1AS_HOME}/bin/asadmin"
set -x
PROPS_FILE=${PWD}/logging.properties
${ASADMIN} create-system-properties --target ${cluster_name} "java.util.logging.config.file=${PROPS_FILE}"
${ASADMIN} create-system-properties java.util.logging.config.file=${PROPS_FILE}

