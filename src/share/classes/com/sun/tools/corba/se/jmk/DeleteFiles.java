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

import java.io.File ;
import java.io.IOException ;
import java.io.FileReader ;
import java.io.BufferedReader ;

import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;

/** Utility to delete files from a list of files. 
 * Input arguments:
 *	-dir (directory) : Name of the directory containing the files to be deleted.
 *	-file (file name): Name of the file containing the files to be deleted.
 *			   File names in file must be one per line, with / as file
 *			   separator.
 */
public class DeleteFiles {
    private interface ArgumentData {
	@DefaultValue( "" ) 
	File dir() ;

	@DefaultValue( "" ) 
	File file() ;
    }

    private ArgumentData argumentData ;

    private DeleteFiles( ArgumentData ad ) {
	argumentData = ad ;
    }

    private static int errorCount = 0 ;

    private static void reportError( String msg ) {
	errorCount++ ;
	System.out.println( msg ) ;
    }

    public static void main( String[] args ) {
	ArgParser<ArgumentData> ap = new ArgParser( ArgumentData.class ) ;
	ArgumentData ad = null ;

	try {
	    ad = ap.parse( args ) ;
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    System.out.println( "Exception while parsing arguments: " + exc ) ;
	    System.out.println( ap.getHelpText() ) ;
	}

	DeleteFiles df = new DeleteFiles( ad ) ;

	try {
	    df.run() ;
	} catch (Exception exc) {
	    exc.printStackTrace() ;
	    reportError( "Exception occurred: " + exc ) ;
	}

	System.exit( errorCount ) ;
    }

    private File relativeFile( String line ) {
	File result = argumentData.dir() ;
	String[] strs = line.split( "/" ) ;
	for (String str : strs ) 
	    result = new File( result, str ) ;
	return result ;
    }

    private void run() {
	FileReader fr = null ;
	BufferedReader br = null ;

	try {
	    fr = new FileReader( argumentData.file() ) ;
	    br = new BufferedReader( fr ) ;

	    String line = br.readLine() ;
	    while (line != null) {
		File file = relativeFile( line ) ;
		file.delete() ; // don't care whether delete succeeds or fails,
			        // since this is called when file has already been
				// deleted in some cases.
		line = br.readLine() ;
	    }
	} catch (Exception exc) {
	    reportError( "Exception in run: " 
		    + exc ) ;
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
    }
}
