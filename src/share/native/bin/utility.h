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

#ifndef UTILITY
#define UTILITY

/* declare commonly used global variables and utility functions */

extern char file_sep;	/* file separator such as '/' in "/bin/cmd" */
extern char path_sep;	/* path separator such as ':' in "PATH=/bin:/usr/bin" */
extern char *iiop_file;	/* RMI-IIOP only file, for finding dev base directory */

/* Convert all from_ch character in string s to to_ch */
void convert(char *s, char from_ch, char to_ch);

/* Return a string = s1, s2 cancatenated.
   Caller owns the returned storage.
 */
char *strjoin(char *s1, char *s2);

/* Return a string = all elements in argv cancatenated.
   The list is ended with an element of NULL.
   Caller owns the returned storage.
 */
char *strArgv(char *argv[]);

/* Compare the tail of s1 with s2.  If they are equal return the tail,
   otherwise return NULL.
 */
char *strtail(char *s1, char *s2);

/* Get the base/parent directory of the given path.
   In general, if path = "[..../]file" then return "[..../]..".
   Caller owns the returned storage.
 */
char *getBaseDir(char *path);

/* Find the base directory of file f1 by searching the given path
   (such as CLASSPATH) for an entry whose tail is f1.
   Return NULL if no such entry exists, or when f2 != NULL and f2 is not found
   in the same directory as f1.
   Caller owns the returned storage.
 */
char *findBaseDir(char *path, char *f1, char *f2);

/* Find and return the first directory entry in path containing the file f.
   Caller owns the returned storage.
 */
char *findDir(char *path, char *f);

/* Return number of element in the character string array argv.
   The last element must be NULL and is not counted.
 */
int argvSize(char *argv[]);

/* Parse the given argv = {"cmd", "-Jjvm_option", ..., "option", ...}
   then reconstruct and return a new_argv = 
	{"java", jvm_argv[0], ..., "jvm_option", ..., main_class,
		 main_argv[0], ..., "option", ...}
   The jvm_argv and main_argv contain additional arguments to be passed to
   the JVM and main_class respectively.
   Note: jvm_argv and main_argv can be NULL if no arguments to pass.
   Note: the last element of argv, jvm_argv and main_argv must be NULL.
   Caller owns the returned storage.
 */
char **newArgv(char *argv[], char *jvm_argv[], char *main_class, char *main_argv[]);

/* Overlay the current process by running the command in argv.
   The command *argv is searched through the PATH environment variable.
   Return the error code if failed to run the command.
 */
int runExec(char *argv[]);

#endif /*UTILITY*/
