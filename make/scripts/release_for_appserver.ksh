#!/bin/ksh

#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
#  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
#  The contents of this file are subject to the terms of either the GNU
#  General Public License Version 2 only ("GPL") or the Common Development
#  and Distribution License("CDDL") (collectively, the "License").  You
#  may not use this file except in compliance with the License. You can obtain
#  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
#  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
#  language governing permissions and limitations under the License.
# 
#  When distributing the software, include this License Header Notice in each
#  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
#  Sun designates this particular file as subject to the "Classpath" exception
#  as provided by Sun in the GPL Version 2 section of the License file that
#  accompanied this code.  If applicable, add the following below the License
#  Header, with the fields enclosed by brackets [] replaced by your own
#  identifying information: "Portions Copyrighted [year]
#  [name of copyright owner]"
# 
#  Contributor(s):
# 
#  If you wish your version of this file to be governed by only the CDDL or
#  only the GPL Version 2, indicate your decision by adding "[Contributor]
#  elects to include this software in this distribution under the [CDDL or GPL
#  Version 2] license."  If you don't indicate a single choice of license, a
#  recipient has the option to distribute your version of this file under
#  either the CDDL, the GPL Version 2 or to extend the choice of license to
#  its licensees as provided above.  However, if you add GPL Version 2 code
#  and therefore, elected the GPL Version 2 license, then the option applies
#  only if the new code is made subject to such option by the copyright
#  holder.

function errorExit
{
    echo Release For AppServer Failed!
    exit 1
}

#
# Setup...
#

# the number 10 is arbitrary and can be lowered if problems arise again with the command line length
# Note that this has a BIG effect on how long it takes to make the jars!
# e.g. at 10, make releaseonly took 9.5 minutes, while at 100, it only took 2.5 minutes on an Ultra 10!
# _REVISIT_: Once we take out native code
XARGS_SIZE=10

BUILD_DIR=$1
PLATFORM=$2
JHOME=$3
cd $BUILD_DIR/..
ROOT_DIR=$PWD
# BUILD_PLATFORM=$4
# RELEASE_DIR="$ROOT_DIR/build/$BUILD_PLATFORM/release"
RELEASE_DIR="$ROOT_DIR/build/release"
# export $PLATFORM $BUILD_PLATFORM
export $PLATFORM 

#
# Create variables for our subdirectories...
#

REL_LIB="$RELEASE_DIR/lib"
REL_CLASSES="$RELEASE_DIR/classes"

#
# Make sure we have a zip tool...
#

if test -z "$ZIPTOOL"
then
    ZIPTOOL=$JHOME/bin/jar
    ZIPTOOL=$(echo $ZIPTOOL | sed -e 's#\\#/#g')
fi

#
# Prepare RELEASE_DIR directory...
#

if test -d "$RELEASE_DIR"
then
    echo - Cleaning out release directory $RELEASE_DIR...
    rm -fr "$RELEASE_DIR"
fi

echo - Making release directory $RELEASE_DIR...
mkdir "$RELEASE_DIR" || errorExit
mkdir -p "$REL_LIB" || errorExit
mkdir -p "$REL_CLASSES" || errorExit

#
# Create omgapi.jar...
#

echo - Creating omgapi.jar in release directory...
cd $REL_CLASSES
while read filename; do
    dirname=`dirname $filename`
    mkdir -p $dirname
    # cp $ROOT_DIR/build/$BUILD_PLATFORM/classes/$filename $dirname
    cp $ROOT_DIR/build/classes/$filename $dirname
done < $ROOT_DIR/make/scripts/omgapi-files.list
rm -f $REL_LIB/omgapi.jar

# No need to touch since we will use jar directly
#touch $REL_LIB/omgapi.jar
# No need to do a find, just jar directly
#find . -name "*.class" -print | xargs -n $XARGS_SIZE $ZIPTOOL -ufM $REL_LIB/omgapi.jar

$ZIPTOOL -cfM $REL_LIB/omgapi.jar org/ javax/
rm -rf $REL_CLASSES/org
rm -rf $REL_CLASSES/javax


#
# Create peorb.jar...
#

echo - Creating peorb.jar in release directory...
cd $REL_CLASSES
mkdir -p com/sun
mkdir -p sun
# cp -r $ROOT_DIR/build/$BUILD_PLATFORM/classes/com/sun/corba com/sun
# cp -r $ROOT_DIR/build/$BUILD_PLATFORM/classes/com/sun/org com/sun
# cp -r $ROOT_DIR/build/$BUILD_PLATFORM/classes/sun/rmi sun
# cp -r $ROOT_DIR/build/$BUILD_PLATFORM/classes/sun/corba sun
cp -r $ROOT_DIR/build/classes/com/sun/corba com/sun
cp -r $ROOT_DIR/build/classes/com/sun/org com/sun
cp -r $ROOT_DIR/build/classes/sun/rmi sun
cp -r $ROOT_DIR/build/classes/sun/corba sun
# NOTE: We are filtering  the following set of classes
# 1. com.sun.tools: contains the files for idlj, it is not used by appserver 
# _REVISIT_: THERE IS STILL SCOPE TO REDUCE THE JAR FILE SIZE. Some of the most
# obvious classes not used by Appserver are DynamicAnys, OBD Activation files,
# some parts of POA classes. We should try to filter those files to achieve
# smaller download size goal for SunOneAppServer PE release.
# However, not all of these can be removed without code changes.  DynamicAny
# requires at least a reflective check in the ORBConfigurator code
# when the factory is registered.  The POA classes are all required, as
# they are all statically references.  For example, all of the POAPolicyMediator
# subclasses are statically referenced in the factory.  The ORBD files can
# probably be removed.
rm -f $REL_LIB/peorb.jar

# No need to touch since we will use jar directly
#touch $REL_LIB/peorb.jar
#
# This find just prints all the class files and properties files
# and then the xargs is used along with jar to create the peorb.jar file
# No need for this since we can use a better scheme to do this, besides 
# this is very slow
#find . "(" -name "*.class" -o -name "*.properties" ")" -print | xargs -n $XARGS_SIZE $ZIPTOOL -ufM $REL_LIB/peorb.jar

# This find command finds any thing in the directory which is NOT a class file,
# a properties file or a directory and them just removes those files. Since this
# is a temporary holding location for these files, removal is not a problem (they
# are removed anyway right after jar executes
find . "!" "(" -type d -o -name "*.class" -o -name "*.properties" ")" -exec rm -f {} \;
$ZIPTOOL -cfM $REL_LIB/peorb.jar com/ sun/
rm -rf $REL_CLASSES/com/sun
rm -rf $REL_CLASSES/sun


#
# Create idlj.jar...
#

echo - Creating idlj.jar in release directory...
cd $REL_CLASSES
mkdir -p com/sun
# cp -r $ROOT_DIR/build/$BUILD_PLATFORM/classes/com/sun/tools com/sun
cp -r $ROOT_DIR/build/classes/com/sun/tools com/sun
rm -f $REL_LIB/idlj.jar

# No need to touch since we will use jar directly
#touch $REL_LIB/idlj.jar
#
# This find just prints all the class files and properties files
# and then the xargs is used along with jar to create the peorb.jar file
# No need for this since we can use a better scheme to do this, besides 
# this is very slow
#find .  "(" -name "*.class" -o -name "*.properties"  ")" -print | xargs -n $XARGS_SIZE $ZIPTOOL -ufM $REL_LIB/idlj.jar

# This find command finds any thing in the directory which is NOT a class file,
# a properties file or a directory and them just removes those files. Since this
# is a temporary holding location for these files, removal is not a problem (they
# are removed anyway right after jar executes
find .  "!" "(" -type d -o -name "*.class" -o -name "*.properties"  ")" -exec rm -f {} \;
$ZIPTOOL -cfM $REL_LIB/idlj.jar com/
rm -rf $REL_CLASSES/com/sun
echo - DONE creating release
echo


