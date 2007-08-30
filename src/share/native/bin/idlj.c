/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/* idlj is a wrapper command which converts the command
	idlj [-J<jvm_option>] [<idlj_option>]
   into
	java [<jvm_option>] com.sun.tools.corba.se.idl.toJavaPortable.Compile -i idlj_lib [<idlj_option>]
   where idlj_lib = $RMI_IIOP_HOME/lib.

   In addition, the CLASSPATH environment variable is prepended by
	$RMI_IIOP_HOME/lib/idlj.jar
   before the java command is invoked.

   Define the following macros for compilation to:
	_WIN32		compile on Win32 platform, auto set by cl compiler
	DEBUG		build idlj_g, auto set by JDK build makefiles
	MAIN_CLASS	invoke a different main class with simpler runExec

   Chih-Hsiang Chou, 01/29/99
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "utility.h"

static char *copyright[] = {
    "Licensed Materials - Property of IBM and Sun",
    "RMI-IIOP v1.0",
    "Copyright IBM Corp. 1998 1999  All Rights Reserved",
    "US Government Users Restricted Rights - Use, duplication or",
    "disclosure restricted by GSA ADP Schedule Contract with IBM Corp.",
    "Copyright 1998-1999 Sun Microsystems, Inc. 901 San Antonio Road,",
    "Palo Alto, CA  94303, U.S.A.  All rights reserved."
};


char *javaArgs[] = {
	"-Dorg.omg.CORBA.ORBClass=com.sun.corba.se.impl.orb.ORBImpl",
	"-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.se.impl.orb.ORBSingleton",
	NULL
};

#ifdef MAIN_CLASS

int main(int argc, char *argv[]) {
    char *main_class = MAIN_CLASS;	/* main class */
    return runExec(newArgv(argv, javaArgs, main_class, NULL));
}

#else


/* Update the CLASSPATH environment variable and return the new CLASSPATH.
   All passed arguments must not be NULL.
   Caller owns the returned storage.
 */
static char *updateClasspath(char *classpath, char *appl_home, char *lib, char *appl_file) {
    /* prepend "appl_home/lib/appl_file" to current classpath */
    char *format = "%s%s%c%s%c%s";
    char *p = (char *) malloc(strlen(format) + strlen(appl_home) +
	strlen(lib) + strlen(appl_file) + strlen(classpath) );	/* new classpath */

    sprintf(p, format, appl_home, lib, file_sep, appl_file, path_sep, classpath);
    putenv(strjoin("CLASSPATH=", p));
    return p;
}


int main(int argc, char *argv[]) {
    char *main_class = "com.sun.tools.corba.se.idl.toJavaPortable.Compile";	/* main class */
    char *main_argv[3] = {"-i", NULL, NULL};	/* extra main options */
    char *appl_file = "idlj.jar";	/* application class file */
    char lib[5] = "/lib";
    char *classpath = getenv("CLASSPATH");	/* original CLASSPATH */
    char *appl_home;		/* base directory of RMI-IIOP */

    if (classpath == NULL)
	classpath = "";
#ifdef _WIN32
    convert(classpath, '/', file_sep);	/* for Win95 */
    *lib = file_sep;
#endif /*_WIN32*/

    /* Find appl_home from current classpath.
       If the file iiop_fle is found, we assume it is in a development directory
       and appl_home is set to its build root directory.
       Otherwise, if iioprt.jar is found appl_home is set to its base directory.
       When all failed appl_home is set to $RMI_IIOP_HOME.
     */
    appl_home = findDir(classpath, iiop_file);
    if (appl_home != NULL) {
	char *s = strstr(appl_home, "build");	/* find subdir */
	/* note: *(s-1) may or may not be a file_sep */
	if (s != NULL && s[5] == file_sep) {
	    *s = '\0';		/* delete substring */
	    lib[1] = 'L';	/* "Lib" for developement */
	} else
	    appl_home = NULL;
    }
    if (appl_home == NULL)	/* RMI-IIOP is release version */
	appl_home = findBaseDir(classpath, "iioprt.jar", NULL);
    if (appl_home == NULL)
	appl_home = getenv("RMI_IIOP_HOME");
    if (appl_home == NULL) {
	fprintf(stderr, "Error: unable to find installed RMI-IIOP from CLASSPATH\n");
	exit(1);
    }

    main_argv[1] = strjoin(appl_home, lib);	/* update main_argv */
    classpath = updateClasspath(classpath, appl_home, lib, appl_file);

    /* Make sure appl_file is installed in release version */
    if (lib[1] == 'l' && findBaseDir(classpath, appl_file, appl_file) == NULL) {
	fprintf(stderr, "Error: %s not installed\n", appl_file);
	fprintf(stderr, "See RMI-IIOP readme.html to download and install %s\n", appl_file);
	exit(2);
    }

    return runExec(newArgv(argv, javaArgs, main_class, main_argv));
}

#endif /*MAIN_CLASS*/
