#!/bin/bash
# sample local test execution script
# cd here ; unzip glassfish-zip in the current directory ; then:
export GFV3_WORK=`pwd`
export S1AS_HOME=${GFV3_WORK}/glassfish3/glassfish
cd test/OrbFailOver
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  clean
ant -Dlibs.CopyLibs.classpath=$HOME/bin/org-netbeans-modules-java-j2seproject-copylibstask.jar -Dj2ee.server.home=glassfish3/glassfish  
cd ../..
./run.sh -include "15804sfsb,15804sfsb_kill"

