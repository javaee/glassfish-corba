#!/bin/bash -x
# sample local test setup script for U*x
./scripts/killgf
sleep 5
if [ -d glassfish4 ] 
then
  \rm -rf glassfish4.old
  mv glassfish4 glassfish4.old
fi
#point to appripriate glassfish.zip bundle
#wget http://hudson.glassfish.org/job/gf-trunk-build-continuous/lastSuccessfulBuild/artifact/bundles/glassfish.zip
unzip -qo ./glassfish.zip
#cp /tmp/orb-iiop.jar glassfish4/glassfish/modules/orb-iiop.jar
export GFV3_WORK=`pwd`
export S1AS_HOME=${GFV3_WORK}/glassfish4/glassfish
cd test/OrbFailOver
ant -Dj2ee.server.home=${S1AS_HOME}  clean
#ant -Dj2ee.server.home=${S1AS_HOME}  
cd OrbFailOver-app-client 
ant -Dj2ee.server.home=${S1AS_HOME}
cd ../OrbFailOver-ejb/
ant -Dj2ee.server.home=${S1AS_HOME}
cd ../../..

