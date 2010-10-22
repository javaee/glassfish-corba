/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

package com.sun.corba.se.spi.orbutil.file ;

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import java.io.File ;
import java.io.File ;
import java.io.File ;
import java.io.IOException ;

import com.sun.corba.se.spi.orbutil.file.Scanner ;
import com.sun.corba.se.spi.orbutil.file.Recognizer ;
import com.sun.corba.se.spi.orbutil.file.FileWrapper ;
import com.sun.corba.se.spi.orbutil.file.ActionFactory ;
import com.sun.corba.se.spi.orbutil.file.Block ;
import com.sun.corba.se.spi.orbutil.file.BlockParser ;

import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.argparser.Help ;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;
import com.sun.corba.se.spi.orbutil.argparser.Separator ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;

public class WorkspaceRename {
    private static final String[] SUBSTITUTE_SUFFIXES = {
	"c", "h", "java", "sjava", "idl", "htm", "html", "xml", "dtd", "bnd",
	"tdesc", "policy", "secure", "mc", "mcd", "scm", "vthought",
	"ksh", "sh", "classlist", "config", "jmk", "properties", "prp", 
	"xjmk", "set", "settings", "data", "txt", "text", "javaref", "idlref" } ;

    private static final String[] SUBSTITUTE_NAMES = {
	"Makefile.corba", "Makefile.example", "ExampleMakefile", "Makefile",
        "manifest", "README", "README.SUN"
    } ;

    private static final String[] COPY_SUFFIXES = {
	"sxc", "sxi", "sxw", "odp", "gif", "png", "jar", "zip", "jpg", "pom",
	"pdf", "doc", "mif", "fm", "book", "zargo", "zuml", "cvsignore", 
	"hgignore", "list", "old", "orig", "rej", "hgtags", "xsl", "bat", "css",
        "icns", "bin", "ico", "init", "ss", "pp", "el", "mail", "lisp", "sch",
        "tst", "xcf"
    } ;

    private static final String[] IGNORE_SUFFIXES = {
	"swo", "swp", "class", "o", "gz", "odt", "pdf", "sxw", "png", "gif", "jpg", "odp", "sxi",
	"fm", "doc", "zargo"
    } ;

    private static final String[] IGNORE_NAMES = {
	"NORENAME", "errorfile", "sed_pattern_file.version", "package-list"
    } ;

    private static final String[] IGNORE_DIRS = {
	".hg", ".snprj", ".cvs", "SCCS", "obj", "obj_g", "Codemgr_wsdata", 
	"deleted_files", "build", "rename", "freezepoint", "test-output",
	"webrev", "javadoc", "felix-cache"
    } ;

    private static final String[][] patterns = {
	{ "com.sun.corba.se", "com.sun.corba.VERSION" },
	{ "com/sun/corba/se", "com/sun/corba/VERSION" }, 
	{ "com.sun.tools.corba.se", "com.sun.tools.corba.VERSION" },
	{ "com/sun/tools/corba/se", "com/sun/tools/corba/VERSION" }, 
	{ "org.objectweb.asm", "com.sun.corba.VERSION.org.objectweb.asm" }, 
	{ "org/objectweb/asm", "com/sun/corba/VERSION/org/objectweb/asm" } } ;

    public static void main(String[] strs) {
	(new WorkspaceRename( strs )).run() ;
    }

    private interface Arguments {
	@Help( "Set to >0 to get information about actions taken for every file.  Larger values give more detail." ) 
	@DefaultValue( "0" ) 
	int verbose() ;

	@Help( "Set to true to avoid modifying any files" ) 
	@DefaultValue( "false" ) 
	boolean dryrun() ;

	@Help( "Source directory for rename" ) 
	@DefaultValue( "" ) 
	File source() ;

	@Help( "Destination directory for rename" ) 
	@DefaultValue( "" ) 
	File destination() ;

	@Help( "The renamed package" ) 
	@DefaultValue( "ee" ) 
	String version() ;

	@Help( "If true, copy all files without renaming anything" )
	@DefaultValue( "false" )
	boolean copyonly() ;

        @Help( "If true, expand all tabs into spaces on files that are renamed (all text file)" ) 
        @DefaultValue ( "true" ) 
        boolean expandtabs() ;
    }

    // Extract these from args so that the methods in Arguments are not
    // repeatedly evaluated during run().  
    //
    // XXX Modify parser to use codegen to generate the implementation
    // class so that the cost is trivial.  The current implementation uses
    // reflection.
    private final int verbose ;
    private final boolean dryrun ;
    private final File source ;
    private final File destination ;
    private final String version ;
    private final boolean copyonly ;
    private final boolean expandtabs ;

    private final List<String> noActionFileNames = new ArrayList<String>() ;

    private void trace( String msg ) {
	System.out.println( msg ) ;
    }

    // Get the FileWrapper representing the destination for the renamed or
    // copied source.  Note that the relative file name must also be renamed!
    private FileWrapper makeTargetFileWrapper( FileWrapper arg ) {
	String rootName = source.getAbsolutePath() ;
	String sourceName = arg.getAbsoluteName() ;
	if (verbose > 1) {
	    trace( "makeTargetFileWrapper: rootName = " + rootName ) ;
	    trace( "makeTargetFileWrapper: sourceName = " + sourceName ) ;
	}

	if (sourceName.startsWith( rootName)) {
            String targetName = sourceName.substring( 
                rootName.length() ) ;

            for (String[] astr : patterns) {
                final String key = astr[0] ;
                final String replacement = astr[1] ;

                if (sourceName.indexOf( key ) >= 0) {
                    targetName = targetName.replace( key, replacement )
                        .replace( "VERSION", version ) ;
                }
            }

	    File result = new File( destination, targetName ) ;
	    File resultDir = result.getParentFile() ;
	    resultDir.mkdirs() ;
	    FileWrapper fwres = new FileWrapper( result ) ;
	    if (verbose > 1) {
		trace( "makeTargetFileWrapper: arg = " + arg ) ;
		trace( "makeTargetFileWrapper: fwres = " + fwres ) ;
	    }
	    return fwres ;
	} else {
	    throw new RuntimeException( "makeTargetFileWrapper: arg file " 
		+ sourceName + " does not start with root name " 
		+ rootName ) ;
	}
    }

    public WorkspaceRename(String[] strs) {
	ArgParser<Arguments> ap = new ArgParser( Arguments.class ) ;
	Arguments args = ap.parse( strs ) ;
	version = args.version() ;
	source = args.source() ;
	destination = args.destination() ;
	verbose = args.verbose() ;
	dryrun = args.dryrun() ;
	copyonly = args.copyonly() ;
        expandtabs = args.expandtabs() ;

	if (verbose > 1) {
	    trace( "Main: args:\n" + args ) ;
	}
    }

    private void run() {
	try {
	    final byte[] copyBuffer = new byte[ 256*1024 ] ;

	    final Scanner.Action copyAction = new Scanner.Action() {
		public String toString() {
		    return "copyAction" ;
		}

		public boolean evaluate( FileWrapper fw ) {
		    try {
			FileWrapper target = makeTargetFileWrapper( fw ) ;
			if (target.isYoungerThan( fw )) {
			    if (verbose > 0) {
				trace( "copyAction: copying " + fw
				    + " to " + target ) ;
			    }
			    fw.copyTo( target, copyBuffer ) ;
			} else {
			    if (verbose > 1) {
				trace( "copyAction: NOT copying " + fw
				    + " to " + target ) ;
			    }
			}
			return true ;
		    } catch (IOException exc ) {
			System.out.println( "Exception while processing file " 
                            + fw + ": " + exc ) ;
			exc.printStackTrace() ;
			return false ;
		    } 
		}
	    } ;

	    final List<Pair<String,String>> substitutions = 
		new ArrayList<Pair<String,String>>() ;

	    for (String[] pstrs : patterns) {
		String pattern = pstrs[0] ;
		String replacement = pstrs[1].replace( "VERSION", version ) ;
		Pair<String,String> pair = new Pair<String,String>( pattern, 
                    replacement ) ;
		substitutions.add( pair ) ;
	    }

	    final Scanner.Action renameAction = new Scanner.Action() {
		public String toString() {
		    return "renameAction" ;
		}

		public boolean evaluate( FileWrapper fw ) {
		    FileWrapper target = makeTargetFileWrapper( fw ) ;

		    try {
			if (target.isYoungerThan( fw )) {
			    if (verbose > 0) {
				trace( "renameAction: renaming " + fw
				    + " to " + target ) ;
			    }

			    Block sourceBlock = BlockParser.getBlock( fw ) ;
			    Block targetBlock = sourceBlock.substitute( 
                                substitutions ) ;

                            if (expandtabs) {
                                targetBlock = targetBlock.expandTabs() ;
                            }

			    target.delete() ;
			    target.open( FileWrapper.OpenMode.WRITE ) ;
			    targetBlock.write( target ) ;
			} else {
			    if (verbose > 1) {
				trace( "renameAction: NOT renaming " + fw
				    + " to " + target ) ;
			    }
			}

			return true ;
		    } catch (IOException exc ) {
			System.out.println( "Exception while processing file " + fw 
			    + ": " + exc ) ;
			exc.printStackTrace() ;
			return false ;
		    } finally {
			target.close() ;
		    }
		}
	    } ;

	    final ActionFactory af = new ActionFactory( verbose, dryrun ) ;

	    // Create the actions we need
	    final Recognizer recognizer = af.getRecognizerAction() ; 

            recognizer.setDefaultAction(
                new Scanner.Action() {
                    public String toString() { return "WorkspaceRename default action" ; } 

                    public boolean evaluate( FileWrapper fw ) {
                        // Ignore irritating tmp* files that show up in Hudson job workspaces 
                        // for some unknown reason.
                        if (!fw.getName().startsWith( "tmp" )) {
                            noActionFileNames.add( fw.getAbsoluteName() ) ;
                        }
                        return true ;
                    }
                } ) ;

	    final Scanner.Action skipAction = af.getSkipAction() ;

	    final Scanner.Action action = copyonly ?
		copyAction : renameAction ;

	    for (String str : SUBSTITUTE_SUFFIXES) {
		recognizer.addKnownSuffix( str, action ) ;
	    }

	    for (String str : SUBSTITUTE_NAMES) {
		recognizer.addKnownName( str, action ) ;
	    }
	    recognizer.setShellScriptAction( action ) ;

	    for (String str : COPY_SUFFIXES) {
		recognizer.addKnownSuffix( str, copyAction ) ;
	    }

	    for (String str : IGNORE_SUFFIXES) {
		recognizer.addKnownSuffix( str, skipAction ) ;
	    }

	    for (String str : IGNORE_NAMES) {
		recognizer.addKnownName( str, skipAction ) ;
	    }

	    if (verbose > 1) {
		trace( "Main: contents of recognizer:" ) ;
		recognizer.dump() ;
	    }

	    final Scanner scanner = new Scanner( verbose, source ) ;
	    for (String str : IGNORE_DIRS )
		scanner.addDirectoryToSkip( str ) ;

	    scanner.scan( recognizer ) ;

            int rc = noActionFileNames.size() ;

            if (rc > 0) {
                System.out.println( "Rename FAILED: no action defined for files:" ) ;
                for (String str : noActionFileNames) {
                    System.out.println( "\t" + str ) ;
                }
                System.exit( rc ) ;
            }
	} catch (IOException exc) {
	    System.out.println( "Exception while processing: " + exc ) ;
	    exc.printStackTrace() ;
	}
    }
}
