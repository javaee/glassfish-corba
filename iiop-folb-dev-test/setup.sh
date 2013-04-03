#!/bin/bash -x
# sample local test setup script
./scripts/killgf
sleep 5
if [ -d glassfish3 ] 
then
  \rm -rf glassfish3.old
  mv glassfish3 glassfish3.old
fi
unzip -qo ./glassfish.zip
#cp /tmp/orb-iiop.jar glassfish3/glassfish/modules/orb-iiop.jar
export GFV3_WORK=`pwd`
export S1AS_HOME=${GFV3_WORK}/glassfish3/glassfish
cd test/OrbFailOver
ant -Dj2ee.server.home=${S1AS_HOME}  clean
#ant -Dj2ee.server.home=${S1AS_HOME}  
cd OrbFailOver-app-client 
ant -Dj2ee.server.home=${S1AS_HOME}
cd ../OrbFailOver-ejb/
ant -Dj2ee.server.home=${S1AS_HOME}
cd ../../..

