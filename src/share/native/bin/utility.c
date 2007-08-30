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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

/* definitions and implementations of commonly used global variables and
   utility functions

   Chih-Hsiang Chou, 1/29/99
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <sys/stat.h>
#include "utility.h"

#ifdef _WIN32
#include <process.h>
char file_sep = '\\';		/* separator for file pathname */
char path_sep = ';';		/* separator for PATH and CLASSPATH */
/* iiop_file should only exist in development version of RMI-IIOP.
   If found from CLASSPATH, RMI-IIOP is assumed to be a development version.
 */
char *iiop_file = "javax\\rmi\\CORBA\\Tie.class";
#else
char file_sep = '/';
char path_sep = ':';
char *iiop_file = "javax/rmi/CORBA/Tie.class";
#endif /*_WIN32*/


/* Convert all from_ch character in string s to to_ch */
void convert(char *s, char from_ch, char to_ch) {
    if (s != NULL)
	for (; *s; ++s)
	    if (*s == from_ch)
		*s = to_ch;
}


/* Return a string = s1, s2 cancatenated.
   Caller owns the returned storage.
 */
char *strjoin(char *s1, char *s2) {
    char *s = (char *) malloc(strlen(s1) + strlen(s2) + 1);
    strcpy(s, s1);
    strcat(s, s2);
    return s;
}


/* Return a string = all elements in argv cancatenated.
   The list is ended with an element of NULL.
   Caller owns the returned storage.
 */
char *strjoinArgv(char *argv[]) {
    int size = 1;
    char **p, *q, *s, *sp;
    for (p = argv; *p != NULL; ++p)
	for (q = *p; *q++;)
	    ++size;
    s = sp = (char *) malloc(sizeof(char) * size);
    for (p = argv; *p != NULL; ++p)
	for (q = *p; *q;)
	    *sp++ = *q++;
    *sp = '\0';
    return s;
}


/* Compare the tail of s1 with s2.  If they are equal return the tail,
   otherwise return NULL.
 */
char *strtail(char *s1, char *s2) {
    int len1, len2;
    if (s1 == NULL || s2 == NULL)
	return NULL;
    len1 = strlen(s1);
    len2 = strlen(s2);
    if (len1 < len2)
	return NULL;
    if (strcmp(s1 + len1 - len2, s2) == 0)
	return s1 + len1 - len2;
    return NULL;
}


/* Get the base/parent directory of the given path.
   In general, if path = "[..../]file" then return "[..../]..".
   For examples:
	file		->	..
	/file		->	/.. => /
	./file		->	./.. => ..
	../file		->	../..
	dir/file	->	dir/.. => .
	/./file		->	/./.. => /.. => /
	/../file	->	/../.. => /
	/dir/file	->	/dir/.. => /
	..../dir/file	->	..../dir/.. => ....
	for Win32, every path above can optionally start with drive:
   Caller owns the returned storage.
 */
char *getBaseDir(char *path) {
    char *base = (char *) malloc(strlen(path) + 3);
    char *p;
    strcpy(base, path);
    if ((p = strrchr(base, file_sep)) == NULL) { /* no file_sep */
	free(base);
	return "..";	/* path == "file" */
    }
    strcpy(p + 1, "..");	/* use the general solution */
    return base;
}


/* Find the base directory of file f1 by searching the given path
   (such as CLASSPATH) for an entry whose tail is f1.
   Return NULL if no such entry exists, or when f2 != NULL and f2 is not found
   in the same directory as f1.
   Caller owns the returned storage.
 */
char *findBaseDir(char *path, char *f1, char *f2) {
    char *buf;
    if (path == NULL || f1 == NULL)
	return NULL;
    buf = (char *) malloc(strlen(path) + (f2 == NULL ? 1 : strlen(f2)));
    while (path != NULL) {
	if (*path == '\0')	/* path_sep at the end */
	    path = NULL;
	else if (*path == path_sep) {	/* path_sep at the start or repeated */
	    ++path;
	    continue;
	} else {
	    char *p = strchr(path, path_sep);	/* extract first element */
	    char *tail;
	    if (p != NULL)
		*p = '\0';	/* temporarily make a substring */
	    tail = strtail(path, f1);	/* ended with f1? */
	    if (tail != NULL && (tail == path || *(tail-1) == file_sep)) {
		if (f2 == NULL)
		    strcpy(buf, path);	/* path == fullpath of f1 */
		else {
		    struct stat sbuf;	/* for checking existance of file */
		    char ch = *tail;
		    *tail = '\0';
		    strcpy(buf, path);
		    *tail = ch;
		    strcat(buf, f2);
		    if (!(stat(buf, &sbuf) == 0 && sbuf.st_mode & S_IFREG))
			tail = NULL;	/* file not exist */
		}
	    } else
		tail = NULL;
	    if (p != NULL)
		*p++ = path_sep;	/* restore */
	    if (tail != NULL) {
		path = getBaseDir(buf);
		break;
	    }
	    path = p;
	}
    }
    free(buf);
    return path;
}


/* Find and return the first directory entry in path containing the file f.
   Caller owns the returned storage.
 */
char *findDir(char *path, char *f) {
    char *buf;
    if (path == NULL || f == NULL)
	return NULL;
    buf = (char *) malloc(strlen(path) + strlen(f) + 2);
    while (path != NULL) {
	if (*path == '\0')	/* path_sep at the end */
	    path = NULL;
	else if (*path == path_sep) {	/* path_sep at the start or repeated */
	    ++path;
	    continue;
	} else {
	    struct stat sbuf;	/* for checking existance of file */
	    char *p = strchr(path, path_sep);	/* extract first element */
	    int len;
	    if (p != NULL)
		*p = '\0';	/* temporarily make a substring */
	    len = strlen(path);	/* length of substring */
	    sprintf(buf, "%s%c%s", path, file_sep, f);
	    if (p != NULL)
		*p++ = path_sep;	/* restore */
	    if (stat(buf, &sbuf) == 0 && sbuf.st_mode & S_IFREG) {
		buf[len] = '\0';	/* return only the directory */
		return buf;
	    }
	    path = p;
	}
    }
    free(buf);
    return path;
}


/* Return number of element in the character string array argv.
   The last element must be NULL and is not counted.
 */
int argvSize(char *argv[]) {
    int i;
    if (argv == NULL)
	return 0;
    for (i = 0; *argv != NULL; ++argv, ++i);
    return i;
}


/* Parse the given argv = {"cmd", "-Jjvm_option", ..., "option", ...}
   then reconstruct and return a new_argv = 
	{"java", jvm_argv[0], ..., "jvm_option", ..., main_class,
		 main_argv[0], ..., "option", ...}
   The jvm_argv and main_argv contain additional arguments to be passed to
   the JVM and main class respectively.
   Note: jvm_argv and main_argv can be NULL if no arguments to pass.
   Note: the last element of argv, jvm_argv and main_argv must be NULL.
   Caller owns the returned storage.
 */
char **newArgv(char *argv[], char *jvm_argv[], char *main_class, char *main_argv[]) {
    int argc = argvSize(argv);
    int i = argc + argvSize(jvm_argv) + argvSize(main_argv) + 2;
    char *mark = (char *) malloc(sizeof(char) * argc);
    char **new_argv = (char **) malloc(sizeof(char *) * i);
    char **p = new_argv;

#ifdef _WIN32
#ifdef DEBUG
    *p++ = "java_g.exe";
#else
    *p++ = "java.exe";
#endif /*DEBUG*/
#else
#ifdef DEBUG
    *p++ = "java_g";
#else
    *p++ = "java";
#endif /*DEBUG*/
#endif /*_WIN32*/

    if (jvm_argv != NULL)		/* copy jvm_argv */
	while (*jvm_argv != NULL)
	    *p++ = *jvm_argv++;
    for (i = 1; i < argc; ++i) {	/* copy -Jxxx arguments */
	if (strncmp(argv[i], "-J", 2) == 0) {
	    *p++ = argv[i] + 2;
	    mark[i] = 1;		/* mark as parsed */
        } else
	    mark[i] = 0;
    }
    *p++ = main_class;			/* the fully qualified main class */
    if (main_argv != NULL)		/* copy main_argv */
	while (*main_argv != NULL)
	    *p++ = *main_argv++;
    for (i = 1; i < argc; ++i)		/* copy unmarked argunments */
	if (mark[i] == 0)
	    *p++ = argv[i];
    *p = NULL;
    free(mark);
    return new_argv;
}


/* Overlay the current process by running the command in argv.
   The command *argv is searched through the PATH environment variable.
   Return the error code if failed to run the command.
 */
int runExec(char *argv[]) {
    char *path = getenv("PATH");
    int debug = getenv("RMI_IIOP_EXE_DEBUG") != NULL;
    int i;

    if (debug)	/* for debugging only */
	for (i = 0; argv[i] != NULL; ++i)
	    printf("argv[%d] = %s\n", i, argv[i]);
#ifdef _WIN32
    convert(path, '/', file_sep);	/* for Win95 */
    /* In Win32, elements of argv are concatenated into one command string
       then passed to the child process.  As a result, any element with blanks
       must be quoted first */
    for (i = 0; argv[i] != NULL; ++i)
	if (strchr(argv[i], ' ') != NULL) {	/* found blanks in argument */
	    char *arg = (char *) malloc(strlen(argv[i]) + 3);
	    sprintf(arg, "\"%s\"", argv[i]);
	    argv[i] = arg;	/* replace with a quoted argument */
	    if (debug)
		printf("new argv[%d] = %s\n", i, argv[i]);
	}
    /* In Win32, execvp terminates (instead of overlaying) the calling process.
       So we have to spawn and wait until the child process is done. */
    i = spawnvp(_P_WAIT, *argv, argv);
#else
    i = execvp(*argv, argv);
#endif /*_WIN32*/
    if (i == -1)
	fprintf(stderr, "failed to run the command %s, check the PATH.\n", *argv);
    return i;
}
