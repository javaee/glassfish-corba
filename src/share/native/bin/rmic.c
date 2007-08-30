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

/* RMI-IIOP version of rmic compiler which supersedes JRMP rmic.
   Both Win32 and Solaris rmic executables share the same source code.
   All JDKs (1.1.6, 1.2) on the same platform share the same rmic executable.

   The JRMP rmic is a wrapper command which does some initial setup, separates
   JVM options (those start with "-J") from rmic options, then invokes the
   JVM with arguments EXTRA_OPTs, JVM_OPTs, sun.rmi.rmic.Main, APPL_OPTs.
   The initial setup and EXTRA_OPTs are listed as below:

   JDK 1.1.6
	Win32 (main in rmic.c)
	    set appl_home = base/parent directory of rmic.exe
	    set java_home = getenv("JAVA_HOME") or appl_home if NULL
	    append appl_home\classes & appl_home\lib\classes.zip to CLASSPATH
	    EXTRA_OPTs = -Djava.home=java_home
	Solaris (main in rmic.sh)
	    EXTRA_OPTS = ""
   JDK 1.2
	Win32 (main in java.c)
	    set appl_home = base/parent directory of rmic executable
	    EXTRA_OPTs = -ms8m
		-Denv.class.path=getenv("CLASSPATH") if not NULL
		-Dapplication.home=appl_home if not NULL
		-Djava.class.path=appl_home\lib\tools.jar;appl_home\classes
	Solaris
	    same as Win32 except file and path separators

   The RMI-IIOP rmic is also a wrapper command which works like JRMP rmic.
   However, the setup and EXTRA_OPTs are generic such that a single rmic.c
   source can be compiled to run on multiple JDKs and multiple platforms.
   Also the reconstructed options are passed to a java command instead of JVM.
   Note that RMI-IIOP rmic assumes all RMI-IIOP classes except those in
   iioptools.jar can be found from the environment variable CLASSPATH.

   JDK 1.1.6 and JDK 1.2
	Win32 (main in rmic.c)
	    set classpath to CLASSPATH
	    set appl_home = base/parent directory of iioptools.jar
	    if JDK 1.1.6
		prepend appl_home\lib\iioptools11.jar to classpath
	    else
		prepend java_home\lib\tools.jar to classpath
	    prepend appl_home\classes;appl_home\lib\iioptools.jar to classpath
	    update CLASSPATH by classpath
	    EXTRA_OPTS = -ms8m
		-Dapplication.home=appl_home if rmic
		-Denv.class.path=classpath
		(note: -Djava.home is not defined,
		 also, -Djava.class.path will be set by JDK1.2 java command)
	Solaris
	    same as Win32 except file and path separators

   Define the following macros for compilation to:
	_WIN32		compile on Win32 platform, auto set by cl compiler
	DEBUG		build rmic_g, auto set by JDK build makefiles

   Chih-Hsiang Chou, 10/22/98
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/stat.h>
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

/* Return the pathname of java_home/lib/tools.jar */

/* Update the CLASSPATH environment variable and return the new CLASSPATH.
   All passed arguments except subdir must not be NULL.
   Caller owns the returned storage.
 */
static char *updateClasspath(char *classpath, char *appl_home, char *subdir, char *java_home) {
    char *format, *p;
    struct stat sbuf;	/* for checking existance of file */
    int is11;		/* true if the JDK is 1.1.x */

    format = "%s%clib%ctools.jar";
    p = (char *) malloc(strlen(format) + strlen(java_home));
    sprintf(p, format, java_home, file_sep, file_sep);
    is11 = !(stat(p, &sbuf) == 0 && sbuf.st_mode & S_IFREG);	/* true if not exist */

    if (subdir == NULL) {	/* release version */
	/* prepend appl_home/classes, appl_home/lib/iioptools.jar, then
	   appl_home/lib/iioptools11.jar (JDK1.1.x) or
	   java_home/lib/tools.jar (JDK 1.2) to classpath */
	format = "%s/classes%c%s/lib/iioptools.jar%c%s/lib/%s.jar%c%s";
	p = (char *) malloc(strlen(format) + strlen(appl_home)*3 +
	    strlen(java_home) + strlen("iioptools11") + strlen(classpath));
	sprintf(p, format, appl_home, path_sep, appl_home, path_sep,
	    (is11 ? appl_home : java_home), (is11 ? "iioptools11" : "tools"), path_sep, classpath);
    } else {			/* development version */
	/* prepend appl_home/tools/build/platform/classes, then
	   appl_home/tools11/build/platform/classes (JDK1.1.x) or
	   java_home/lib/tools.jar (JDK 1.2) to classpath */
	if (is11) {
	    format = "%s/tools/b%s%c%s/tools11/b%s%c%s";
	    p = (char *) malloc(strlen(format) + strlen(appl_home)*2 +
		strlen(subdir)*2 + strlen(classpath));
	    sprintf(p, format, appl_home, subdir, path_sep,
		appl_home, subdir, path_sep, classpath);
	} else {
	    format = "%s/tools/b%s%c%s/lib/tools.jar%c%s";
	    p = (char *) malloc(strlen(format) + strlen(appl_home) +
		strlen(subdir) + strlen(java_home) + strlen(classpath));
	    sprintf(p, format, appl_home, subdir, path_sep,
		java_home, path_sep, classpath);
	}
    }

    convert(p, '/', file_sep);
    putenv(strjoin("CLASSPATH=", p));
    return p;
}


int main(int argc, char *argv[]) {
    char *jvm_argv[4] = {"-ms8m", NULL, NULL, NULL};	/* extra JVM options */
    char *main_class = "sun.rmi.rmic.Main";	/* main class */

    char *classpath = getenv("CLASSPATH");	/* original CLASSPATH */
    char *appl_home;		/* base directory of RMI-IIOP */
    char *subdir = NULL;	/* for subdir [b]uild/platform/classes */

#ifdef _WIN32
    char *java_file = "JAVA.EXE";	/* the java command */
#else
    char *java_file = "java";
#endif
    char *path = getenv("PATH");	/* PATH environment variable */
    char *java_home;		/* base directory of JDK */

    if (classpath == NULL)
	classpath = "";
    if (path == NULL)
	path = "";
#ifdef _WIN32
    convert(classpath, '/', file_sep);	/* for Win95 */
    convert(path, '/', file_sep);	/* for Win95 */
#endif /*_WIN32*/

    /* Find appl_home from current classpath.
       If the file iiop_fle is found, we assume it is in a development directory
       and appl_home is set to its build root directory.
       Otherwise, if iioprt.jar is found appl_home is set to its base directory.
       When all failed appl_home is set to $RMI_IIOP_HOME.
     */
    appl_home = findDir(classpath, iiop_file);
    if (appl_home != NULL) {
	subdir = strstr(appl_home, "build");	/* find subdir */
	/* note: *(subdir-1) may or may not be a file_sep */
	if (subdir != NULL && subdir[5] == file_sep) {	/* delete substring */
	    *subdir++ = '\0';	/* note: subdir starts with "uild/..." */
	} else {
	    appl_home = NULL;
	    subdir = NULL;
	}
    }
    if (appl_home == NULL)
	appl_home = findBaseDir(classpath, "iioprt.jar", NULL);
    if (appl_home == NULL)
	appl_home = getenv("RMI_IIOP_HOME");
    if (appl_home == NULL) {
	fprintf(stderr, "unable to find installed RMI-IIOP from CLASSPATH\n");
	exit(1);
    }

    /* Find java_home from current PATH.
       If the file java_file is found, we assume it is in the JDK bin directory
       and java_home is set to its root/parent directory.
       Otherwise, java_home is set to $JAVA_HOME.
     */
    java_home = findDir(path, java_file);
    if (java_home != NULL) {
	char parent[4] = "/..";
	*parent = file_sep;
	java_home = strjoin(java_home, parent);
    } else
	java_home = getenv("JAVA_HOME");
    if (java_home == NULL) {
	fprintf(stderr, "unable to find installed JDK from PATH\n");
	exit(2);
    }

    jvm_argv[1] = strjoin("-Dapplication.home=", appl_home);
    classpath = updateClasspath(classpath, appl_home, subdir, java_home);
    jvm_argv[2] = strjoin("-Denv.class.path=", classpath);

    return runExec(newArgv(argv, jvm_argv, main_class, NULL));
}                                       
