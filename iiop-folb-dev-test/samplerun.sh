#!/bin/bash -x
# sample local test execution script
source ./setup.sh
############################
glassfish4/glassfish/bin/asadmin start-domain
glassfish4/glassfish/bin/asadmin  create-jvm-options -Djava.rmi.server.useCodebaseOnly=true
glassfish4/glassfish/bin/asadmin stop-domain
pause
############################
#./run.sh 
#./run.sh -include "lbfail"
./run.sh -exclude "15804sfsb,15804sfsb_kill,15804sfsb_kill_delete"

