#!/bin/bash -x
# sample local test execution script
# cd here ; unzip glassfish-zip in the current directory ; then:
$HOME/bin/jpskall ASMain
sleep 5
mv glassfish3 glassfish3.old
unzip -q /java/re/glassfish/4.0/promoted/latest/archive/bundles/latest-glassfish.zip
#unzip -q /java/re/glassfish/3.1.2/promoted/latest/archive/bundles/latest-glassfish.zip
#cp $HOME/repo/gf-corba-v3-mirror-gfv31-master/build/rename/ee/build/release/lib/bundles/glassfish-corba-orb.jar glassfish3/glassfish/modules/
export GFV3_WORK=`pwd`
export S1AS_HOME=${GFV3_WORK}/glassfish3/glassfish
cd test/OrbFailOver
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  clean
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  
cd ../..
./run.sh -include "15804sfsb,15804sfsb_kill,15804sfsb_kill_delete"

