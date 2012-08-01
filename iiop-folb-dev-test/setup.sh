#!/bin/bash -x
# sample local test setup script
# cd here ; unzip glassfish-zip in the current directory ; then:
export ORB_BUILD="../corba-40-build-test-orb"
export GF_MODULES="glassfish3/glassfish/modules/"
$HOME/bin/jpskall ASMain
sleep 5
mv glassfish3 glassfish3.old
unzip -qo /java/re/glassfish/4.0/nightly/latest/latest-glassfish.zip
#unzip -qo /java/re/glassfish/4.0/promoted/latest/archive/bundles/latest-glassfish.zip
cp $ORB_BUILD/orbmain/target/glassfish-corba-orb*.jar $GF_MODULES/glassfish-corba-orb.jar
cp $ORB_BUILD/omgapi/target/glassfish-corba-omgapi*.jar $GF_MODULES/glassfish-corba-omgapi.jar
cp $ORB_BUILD/internal-api/target/glassfish-corba-internal-api*.jar $GF_MODULES/glassfish-corba-internal-api.jar
cp $ORB_BUILD/csiv2-idl/target/glassfish-corba-csiv2-idl*.jar $GF_MODULES/glassfish-corba-csiv2-idl.jar
#cp $HOME/repo/gf-corba-v3-mirror-gfv31-master/build/rename/ee/build/release/lib/bundles/glassfish-corba-orb.jar glassfish3/glassfish/modules/
export GFV3_WORK=`pwd`
export S1AS_HOME=${GFV3_WORK}/glassfish3/glassfish
cd test/OrbFailOver
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  clean
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  
cd ../..

