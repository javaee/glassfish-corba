/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.tools.corba.se.jmk ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;
import java.io.File ;
import java.io.IOException ;
import java.io.FileReader ;
import java.io.BufferedReader ;
import java.io.PrintWriter ;

import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;

/** Utility to compare those irritating .jmk files from 
 * a directory.
 * Input arguments:
 *	-dir (list of dirs): directories (as a comma-separated list)
 *	                   to scan for .java files and other directories (ignores SCCS).
 *			   Each subdirectory of a directory in this list must correspond
 *			   to a Java package.
 *	-jmk (dir name)  : directory containing .jmk files.  These must follow the
 *		           standard naming convention: _ separated list of dir name.jmk.
 *		           Each file consists of macro definitions where each line
 *			   of a definition starts with $(TARGDIR).  Comments are given
 *			   in lines that start with #.  This program ignores all lines
 *			   except those that start with $(TARGDIR).
 *	-del (file)	 : File to receive a list of files that are to be deleted.
 *			   These are the files identified with a .DELETED macro in a
 *			   jmk file.  If -del is included, and the file already exists,
 *			   we do ignore all files after DELETED in the .jmk file.
 *			   
 *  Output format:
 *	Produces no output if jmk files are present and complete
 *	otherwise:
 *	(file name)
 *	    (error: either missing file or extra file)
 *
 */
public class CompareJmkFiles {
    private interface ArgumentData {
	@DefaultValue( "" ) 
	File[] dir() ;

	@DefaultValue( "" ) 
	File jmk() ;

	@DefaultValue( "" ) 
	File del() ;

	@DefaultValue( "false" ) 
	boolean debug() ;
    }

    private CompareJmkFiles( ArgumentData ad ) {
	this.argumentData = ad ;
    }

    private ArgumentData argumentData ;

    private void dprint( String msg ) {
	if (argumentData.debug())
	    System.out.println( msg ) ;
    }

    private boolean filesAlreadyDeleted = false ;
    
    // Map from relative dir names to set of Java files
    private Map<List<String>,Set<String>> dirContents =
	new LinkedHashMap<List<String>,Set<String>>() ;	
    private Set<String> deletedFileNames = 
	new HashSet<String>() ;

    private static int errorCount = 0 ;

    private static void reportError( String msg ) {
	reportError( msg, 1 ) ;
    }

    private static void reportError( String msg, int count ) {
	errorCount += count ;
	System.out.println( msg ) ;
    }

    private static void reportError( String msg, Set<String> names ) {
	reportError( "Error in .jmk file: " + msg, names.size() ) ;
	for (String str : names) 
	    System.out.println( "\t" + str ) ;
    }

    private static final String JMK_FILE_PREFIX = "$(TARGDIR)" ;
    private static final String JAVA_SUFFIX = ".java" ;
    private static final String JMK_SUFFIX = ".jmk" ;
    private static final String SCCS_NAME = "SCCS" ;
    private static final String DELETED_MACRO_FLAG = ".DELETED" ;

    private static void dumpArgs( String[] args ) {
	if (false) {
	    System.out.println( "Arguments to command:" ) ;
	    for (int ctr=0; ctr<args.length; ctr++) {
		System.out.printf( "args[%d]=\"%s\"\n", ctr, args[ctr] ) ;
	    }
	}
    }

    public static void main( String[] args ) {
	dumpArgs( args ) ;
	ArgParser<ArgumentData> ap = new ArgParser( ArgumentData.class ) ;
	ArgumentData ad = null ;

	try {
	    ad = ap.parse( args ) ;
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    System.out.println( "Exception while parsing arguments: " + exc ) ;
	    System.out.println( "Valid arguments:" ) ;
	    System.out.println( ap.getHelpText() ) ;
	}

	if (ad != null) {
	    CompareJmkFiles comp = new CompareJmkFiles( ad ) ;

	    try {
		comp.run() ;

		if (ad.del() != null)
		    comp.writeDeletedFiles() ;
		else if (ad.debug())
		    System.out.println( "No -del file specified" ) ;
	    } catch (Exception exc) {
		exc.printStackTrace() ;
		reportError( "Exception occurred: " + exc ) ;
	    }
	}

	System.exit( errorCount ) ;
    }

    private void writeDeletedFiles() throws IOException {
	if (filesAlreadyDeleted)
	    dprint( "Files have already been deleted: not writing delFile" ) ;	
	else if (deletedFileNames.size() > 0) {
	    PrintWriter pw = new PrintWriter( argumentData.del() ) ;
	    for (String str : deletedFileNames) {
		pw.printf( "%s\n", str ) ;
	    }
	    pw.flush() ;
	    pw.close() ;
	} else
	    dprint( "deletedFileNames is empty" ) ;
    }

    private void run() {
	dprint( "Arguments: " + argumentData ) ;
	filesAlreadyDeleted = (argumentData.del() != null) && argumentData.del().exists() ;
	dprint( "filesAlreadyDeleted = " + filesAlreadyDeleted ) ;

	for (File dir : argumentData.dir()) {
	    analyze( new ArrayList<String>(), dir ) ;
	}

	for (Map.Entry<List<String>,Set<String>> entry : dirContents.entrySet()) {
	    verifyJmkFile( entry.getKey(), entry.getValue() ) ;
	}
    }

    private String makeString( List<String> list, char separator ) {
	StringBuilder sb = new StringBuilder() ;
	for (String str : list) {
	    if (sb.length() > 0)
		sb.append( separator ) ;
	    sb.append( str ) ;
	}

	return sb.toString() ;
    }

    private Set<String> getJavaFileNames( File jmkFile ) {
	FileReader fr = null ;
	BufferedReader br = null ;
	Set<String> result = new HashSet<String>() ;
	boolean fileToBeDeleted = false ;

	try {
	    fr = new FileReader( jmkFile ) ;
	    br = new BufferedReader( fr ) ;

	    String line = br.readLine() ;
	    while (line != null) {
		if (line.contains( DELETED_MACRO_FLAG )) {
		    dprint( "File " + jmkFile + " contains DELETED entries" ) ; 
		    fileToBeDeleted = true ;
		}

		String trimLine = line.trim() ;

		if (trimLine.startsWith( JMK_FILE_PREFIX )) {
		    String str = trimLine.substring(JMK_FILE_PREFIX.length()) ;
		    int index = str.indexOf( JAVA_SUFFIX ) ;
		    str = str.substring( 0, index + JAVA_SUFFIX.length() ) ;

		    if (fileToBeDeleted) {
			if (filesAlreadyDeleted) {
			    dprint( "Skipping already deleted file " + str ) ;
			} else {
			    dprint( "Adding " + str + " to deleted files." ) ;
			    deletedFileNames.add( str ) ;
			    result.add( str ) ;
			}
		    } else {
			result.add( str ) ;
		    }
		}
		    
		line = br.readLine() ;
	    }
	} catch (Exception exc) {
	    reportError( "Exception in getJavaFileNames for " + jmkFile +
		    ": " + exc ) ;
	} finally {
	    try { 
		if (br != null)
		    br.close() ;
		if (fr != null)
		    fr.close() ;
	    } catch (Exception exc) {
		// ignore useless exception on close() call
	    }
	}

	return result ;
    }

    private void verifyJmkFile( List<String> path, Set<String> javaFileNames ) {
	dprint( "Calling verifyJmkFile for path " + path ) ;
	if (path.size() == 0) {
	    reportError( "Files found in anonymous package", javaFileNames ) ;
	} else {
	    // Make a set of all file entries in the jmk file, stripped of the
	    // $(TARGDIR) prefix.
	    String jmkFileName = makeString( path, '_' ) + JMK_SUFFIX ; 
	    File jmkFile = new File( argumentData.jmk(), jmkFileName ) ;
	    Set<String> jmkContents = getJavaFileNames( jmkFile ) ;

	    // Compare javaFileName and jmkContents.  Any differences are
	    // errors: either the .jmk file refers to .java files that do not
	    // exist, or else there are .java files that are not represented
	    // in the .jmk file.
	    Set<String> notInWorkspace = new HashSet<String>( jmkContents );
	    notInWorkspace.removeAll( javaFileNames ) ;

	    Set<String> notInJMKFile = new HashSet<String>( javaFileNames ) ;
	    notInJMKFile.removeAll( jmkContents ) ;

	    if (notInWorkspace.size() > 0)
		reportError( "Files that are in " + jmkFileName 
		    + " but not in the workspace", notInWorkspace ) ;
	    if (notInJMKFile.size() > 0)
		reportError( "Files that are in the workspace but not in " 
		    + jmkFileName, notInJMKFile ) ;
	}
    }

    private Set<String> getJavaFileNameSet( List<String> path ) {
	Set<String> javaFileNames = dirContents.get( path ) ;
	if (javaFileNames == null) {
	    javaFileNames = new HashSet<String>() ;
	    dirContents.put( path, javaFileNames ) ;
	}
	return javaFileNames ;
    }

    private void analyze( List<String> path, File pathDir ) {
	Set<String> javaFileNames = null ;
	String[] files = pathDir.list() ;
	if (files != null) {
	    for (String str : pathDir.list()) {
		dprint( "Analyzing " + str ) ;
		if (str.endsWith( JAVA_SUFFIX )) {
		    String jstr = makeString( path, '/' ) + "/" + str ;
		    if (javaFileNames == null)
			javaFileNames = getJavaFileNameSet( path ) ;
		    javaFileNames.add( jstr ) ;
		} else if (!str.equals( SCCS_NAME )) {
		    File file = new File( pathDir, str ) ;
		    if (file.isDirectory()) {
			List<String> newPath = new ArrayList( path ) ;
			newPath.add( str ) ;
			analyze( newPath, file ) ;
		    }
		}
	    }
	} else {
	    dprint( "No files specified" ) ;
	}
    }
}
