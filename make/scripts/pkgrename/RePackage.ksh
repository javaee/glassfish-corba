#! /bin/ksh

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
#

# This script incrementally re-packages the source tree.
#

case $# in
        4) ;;
        *) echo 'Usage: RePackage srcDir destDir originalName newName' 1>&2 ; exit 1;
esac

srcDir=$1
destDir=$2
originalName=$3
newName=$4

typeset -l lc=$newName
typeset -u uc=$lc
   
if [ "$lc" != "$newName" ]
then
    echo "newName must be lower case"
    exit 2
fi

cwdir=`pwd`

echo "**********************************************************************"
echo "<<<<<<<<<<<<<< Starting Repackaging : $originalName $newName >>>>>>>>>>>>>"
echo "**********************************************************************"

# create a timestamp file. This will be used the next time a rename is done
# to figure out what files had been modified since.
touch $srcDir/.rename.begin

# Make sure that the ant scripts have execute permissions
antdir=$srcDir/ant/bin
for f in $antdir/ant $antdir/antRun $antdir/antRun.pl $antdir/complete-ant-cmd.pl $antdir/runant.pl $antdir/runant.py
do
    if [ -e $f ]
    then
	if [ ! -x $f ]
	then
	    chmod +x $f
	fi
    fi
done

# create sed pattern file

cd $srcDir/make/scripts/pkgrename
#rm sed_pattern_file.version
sed -e "s/@version@/$lc/g" -e "s/@VERSION@/$uc/g" sed_pattern_file.data  > sed_pattern_file.version 
cd $cwdir


# if destDir does not exist, do a rename from scratch
# else do an incremental build
# First, get the files that need to be renamed

cd $srcDir
files=

if [ ! -d $destDir ]
then
        # Filter All the deleted files, *.class files etc.,
        files=`find . -type d \( -name .hg -o -name ".snprj" -o -name obj -o -name obj_g -o -name SCCS -o -name CClassHeaders -o -name Codemgr_wsdata -o -name deleted_files -o -name rename -o -name build \) -prune -o -type f \( -name "*.class" -o -name "*.exe" -o -name "*.obj" -o -name "*.map" -o -name "*.pdb" -o -name sed_pattern_file.version \) -prune -o \( -type f -o -type l \) -name "*" -print`
else 
        # compile the TimeCalculator, if it is not available
        if [ $srcDir/make/scripts/pkgrename/TimeCalculator.java -nt $srcDir/make/scripts/pkgrename/TimeCalculator.class ]
        then
                (cd $srcDir/make/scripts/pkgrename/; 
                javac TimeCalculator.java)
        fi

        # compute the elapsed days since the last rename build
        elapsedDays=`java -classpath $srcDir/make/scripts/pkgrename TimeCalculator $destDir` 
        echo $elapsedDays

        # Filter All the deleted files, *.class files etc.,
        suboptimal_files_set=`find . -type d \( -name ".snprj" -o -name obj -o -name obj_g -o -name SCCS -o -name CClassHeaders -o -name Codemgr_wsdata -o -name deleted_files -o -name rename -o -name build \) -prune -o -type f \( -name "*.class" -o -name "*.exe" -o -name "*.obj" -o -name "*.map" -o -name "*.pdb" -o -name sed_pattern_file.version \) -prune -o \( -type f -o -type l \) -name "*" -mtime -$elapsedDays -print`

	# Choose only those files which have been modified since the start of
	# last successful rename
	files=`for file in $suboptimal_files_set
	do
		if [ $file -nt $destDir ]
		then
			echo $file
		fi
	done`
fi

if [ -z '$files' ]
then
        exit 3 # no files to rename
fi


# For Each file 
# Check the type of file, If it is ordinary file then
# Do a Pattern replace using SED else

if [ ! -d $destDir ]
then
        mkdir -p $destDir
fi

cd $destDir

for oldFile in $files
do
    file=`echo $oldFile | sed "s/com\/sun\/corba\/$originalName\//com\/sun\/corba\/$newName\//" | sed "s/com\_sun\_corba\_$originalName\_/com\_sun\_corba\_$newName\_/"`  

    echo ' --> ' $file

    # First Check whether the destination directory exists for
    # the file and then copy the file
    # If the destination directory doesn't exist then create 
    # it under the destDirectory
    dir=`dirname $file`
    if [ ! -d $dir ]
    then
	echo "************************************************"
	echo "Creating dir $destDir/$dir"
	echo "************************************************"
	mkdir -p $dir
    fi

    cp -f $srcDir/$oldFile $file

    # Don't try to run sed on binary files: we skip
    # according to suffix

    file_suffix=`echo $file | awk -F. '{print $NF}'`

    skip_flag=false
    if [ "$file" != "$file_suffix" ] # file has a suffix
    then
        for exclude_suffix in zip jar class o so dll gif pdf clz isj 
        do
                if [ "$file_suffix" = "$exclude_suffix" ] 
                then
                    skip_flag=true
                fi
        done
    fi 

    if [ "$skip_flag" = "true" ] 
    then
        echo "skipping substitution for file $file"
    else
        permset=false
        sed -f $srcDir/make/scripts/pkgrename/sed_pattern_file.version $file > $file.123456
        if [ ! -z $file.123456 ] 
        then

            if [ ! -w $file ] 
            then
                chmod +w $file
                permset=true
            fi

            \mv -f $file.123456 $file

	    # Done for Ant scripts
	    if [ -x $srcDir/$oldFile ]
	    then
		chmod +x $file
	    fi

            if [ $permset = "true" ] 
            then
                permset=false
                chmod -w $file
            fi
		    
        else
            \rm -f $file.123456
        fi
    fi
done

cd $cwdir

# since a rename is successfully completed, timestamp the $destDir
# appropriately (i.e., the beginning of the last successful rename)
touch -r $srcDir/.rename.begin $destDir

echo "**********************************************************************"
echo "<<<<<<<<<<< Completed Repackaging : $originalName $newName >>>>>>>>>>>"
echo "**********************************************************************"
