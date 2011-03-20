/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Map ;
import java.util.HashMap ;

import java.io.File ;
import java.io.IOException ;

import com.sun.corba.se.spi.orbutil.argparser.ArgParser ;
import com.sun.corba.se.spi.orbutil.argparser.Help ;
import com.sun.corba.se.spi.orbutil.argparser.DefaultValue ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction ;
import com.sun.corba.se.spi.orbutil.generic.Pair ;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class CopyrightProcessor {
    private CopyrightProcessor() {} 

    private interface Arguments {
	@DefaultValue( "true" ) 
	@Help( "Set to true to validate copyright header; if false, generate/update/insert copyright headers as needed" ) 
	boolean validate() ;

	@DefaultValue( "0" ) 
	@Help( "Set to >0 to get information about actions taken for every file.  Larger values give more detail." ) 
	int verbose() ;

	@DefaultValue( "true" ) 
	@Help( "Set to true to avoid modifying any files" ) 
	boolean dryrun() ;

	@Help( "Root of mercurial repository for workspace" )
	@DefaultValue( "" ) 
	File root() ;

	@Help( "List of directory names that should be skipped" ) 
	@DefaultValue( "" ) 
	List<String> skipdirs() ;

	@Help( "File containing text of copyright header.  This must not include any comment characters" ) 
	@DefaultValue( "" ) 
	FileWrapper copyright() ;

	@DefaultValue( "1997" )
	@Help( "Default copyright start year, if not otherwise specified" ) 
	String startyear() ;

        @DefaultValue( "true" )
        @Help( "If true, use the year the file was last modified, otherwise use the current year" ) 
        boolean useFileVersion() ;
    }

    private static boolean validate ;
    private static int verbose ;
    private static boolean useFileVersion ;
    private static ModificationAnalyzer modificationAnalyzer = null ;

    private static final String[] JAVA_LIKE_SUFFIXES = {
	"c", "h", "java", "sjava", "idl" } ;

    private static final String[] XML_LIKE_SUFFIXES = {
	"htm", "html", "xml", "dtd" } ;

    private static final String[] JAVA_LINE_LIKE_SUFFIXES = {
	"tdesc", "policy", "secure" } ;

    private static final String[] SCHEME_LIKE_SUFFIXES = {
	"mc", "mcd", "scm", "vthought" } ;

    // Shell scripts must always start with #! ..., others need not.
    private static final String[] SHELL_SCRIPT_LIKE_SUFFIXES = {
	"ksh", "sh" } ;
    private static final String[] SHELL_LIKE_SUFFIXES = {
	"classlist", "config", "jmk", "properties", "prp", "xjmk", "set",
	"data", "txt", "text" } ;

    // Files whose names match these also use the SHELL_PREFIX style line comment.
    private static final String[] MAKEFILE_NAMES = {
	"Makefile.corba", "Makefile.example", "ExampleMakefile", "Makefile" } ;

    private static final String[] BINARY_LIKE_SUFFIXES = {
	"bnd", "sxc", "sxi", "sxw", "odp", "gif", "png", "jar", "zip", "jpg", "pom",
	"pdf", "doc", "mif", "fm", "book", "zargo", "zuml", "cvsignore", 
	"hgignore", "list", "odt", "old", "orig", "rej", "swp", "swo", "class", "o",
	"javaref", "idlref", "css", "bin", "settings" } ;

    // Special file names to ignore
    private static final String[] IGNORE_FILE_NAMES = {
	"NORENAME", "errorfile", "sed_pattern_file.version", "build.properties",
        ".DS_Store", ".hgtags", ".hgignore"
    } ;

    // Block tags
    private static final String COPYRIGHT_BLOCK_TAG = "CopyrightBlock" ;
    private static final String ORACLE_COPYRIGHT_TAG = "OracleCopyright" ;
    private static final String CORRECT_COPYRIGHT_TAG = "CorrectCopyright" ;

    private static void trace( String msg ) {
	System.out.println( msg ) ;
    }

    private static Block makeCopyrightLineCommentBlock( final Block copyrightText, 
	final String prefix, final String tag ) {

	final Block result = new Block( copyrightText ) ;
	result.addPrefixToAll( prefix ) ;

	result.addTag( tag ) ;
	result.addTag( CORRECT_COPYRIGHT_TAG ) ;

	return result ;
    }

    private static Block makeCopyrightBlockCommentBlock( Block copyrightText, 
	String start, String prefix, String end, String tag ) {

	final Block result = new Block( copyrightText ) ;
	result.addPrefixToAll( prefix ) ;
	result.addBeforeFirst( start ) ;
	result.addAfterLast( end ) ;
	
	result.addTag( tag ) ;
	result.addTag( CORRECT_COPYRIGHT_TAG ) ;

	return result ;
    }

    private static final String COPYRIGHT = "Copyright" ;

    // Search for COPYRIGHT followed by white space, then [0-9]*-[0-9]*
    private static Pair<String,String> getCopyrightPair( String str ) {
        StringParser sp = new StringParser( str ) ;
        if (!sp.skipToString( COPYRIGHT )) {
            return null;
        }

        if (!sp.skipString( COPYRIGHT )) {
            return null;
        }

        if (!sp.skipWhitespace()) {
            return null;
        }

        String start = sp.parseInt() ;
        if (start == null) {
            return null;
        }

        if (!sp.skipString( "-" )) {
            return null;
        }

        String end = sp.parseInt() ;
        if (end == null) {
            return null;
        }

        return new Pair<String,String>( start, end ) ;
    }

    private static final String START_YEAR = "StartYear" ;
    private static final String LAST_YEAR = "LastYear" ;

    private static Block makeCopyrightBlock( Pair<String,String> years, 
	Block copyrightText) throws IOException {

	if (verbose > 1) {
	    trace( "makeCopyrightBlock: years = " + years ) ;
	    trace( "makeCopyrightBlock: copyrightText = " + copyrightText ) ;

	    trace( "Contents of copyrightText block:" ) ;
	    for (String str : copyrightText.contents()) {
		trace( "\t" + str ) ;
	    }
	}

	Map<String,String> map = new HashMap<String,String>() ;
	map.put( START_YEAR, years.first() ) ;
	map.put( LAST_YEAR, years.second() ) ;
	Block withStart = copyrightText.instantiateTemplate( map ) ;

	if (verbose > 1) {
	    trace( "Contents of copyrightText block withStart date:" ) ;
	    for (String str : withStart.contents()) {
		trace( "\t" + str ) ;
	    }
	}   

	return withStart ;
    }

    private interface BlockParserCall extends UnaryFunction<FileWrapper,List<Block>> {}

    private static BlockParserCall makeBlockCommentParser( final String start, 
	final String end ) {

	return new BlockParserCall() {
            @Override
	    public String toString() {
		return "BlockCommentBlockParserCall[start=," + start
		    + ",end=" + end + "]" ;
	    }

	    public List<Block> evaluate( FileWrapper fw ) {
		try {
		    return BlockParser.parseBlocks( fw, start, end ) ;
		} catch (IOException exc) {
		    throw new RuntimeException( exc ) ;
		}
	    }
	} ;
    }

    private static BlockParserCall makeLineCommentParser( final String prefix ) {

	return new BlockParserCall() {
            @Override
	    public String toString() {
		return "LineCommentBlockParserCall[prefix=," + prefix + "]" ;
	    }

	    public List<Block> evaluate( FileWrapper fw ) {
		try {
		    return BlockParser.parseBlocks( fw, prefix ) ;
		} catch (IOException exc) {
		    throw new RuntimeException( exc ) ;
		}
	    } 
	} ;
    }

    private static void validationError( Block block, String msg, 
        FileWrapper fw, Pair<String,String> firstDifference ) {
	trace( "Copyright validation error: " + msg + " for " + fw ) ;
        trace( "\tfirst block line = " + firstDifference.first() ) ;
        trace( "\tsecond block line = " + firstDifference.second() ) ;
	if ((verbose > 0) && (block != null)) {
	    trace( "Block=" + block ) ;
	    trace( "Block contents:" ) ;
	    for (String str : block.contents()) {
		trace( "\"" + str + "\"" ) ;
	    }
	}
    }

    // Strip out old Oracle copyright block.  Prepend new copyrightText.
    // copyrightText is a Block containing a copyright template in the correct comment format.
    // parseCall is the correct block parser for splitting the file into Blocks.
    // defaultStartYear is the default year to use in copyright comments if not
    // otherwise specified in an old copyright block.
    // afterFirstBlock is true if the copyright needs to start after the first block in the
    // file.
    private static Scanner.Action makeCopyrightBlockAction( final Block copyrightText, 
	final BlockParserCall parserCall, final String defaultStartYear, 
	final boolean afterFirstBlock ) {

	if (verbose > 0) {
	    trace( "makeCopyrightBlockAction: copyrightText = " + copyrightText ) ;
	    trace( "makeCopyrightBlockAction: parserCall = " + parserCall ) ;
	    trace( "makeCopyrightBlockAction: defaultStartYear = " + defaultStartYear ) ;
	    trace( "makeCopyrightBlockAction: afterFirstBlock = " + afterFirstBlock ) ;
	}

	return new Scanner.Action() {
            @Override
	    public String toString() {
		return "CopyrightBlockAction[copyrightText=" + copyrightText
		    + ",parserCall=" + parserCall 
		    + ",defaultStartYear=" + defaultStartYear 
		    + ",afterFirstBlock=" + afterFirstBlock + "]" ;
	    }

            private Pair<String,String> getFileVersionYear( FileWrapper fw ) throws IOException {
                /* Old, slow version
                final String fname = fw.getAbsoluteName() ;
                final Block block = new Block( "hg log " + fname ) ;
                final String dateString = block.find( "date:" ) ;
                // Format: date: weekday month day time year timezone
                // we just need the year.
                if (dateString != null) {
                    final String[] tokens = dateString.split( " +" ) ;
                    return tokens[5] ;
                } else {
                    return "" ;
                }
                */
                final String fname = fw.getAbsoluteName() ;
                final String first = modificationAnalyzer.firstModification(fname) ;
                final String last = modificationAnalyzer.lastModification(fname) ;
                return new Pair<String,String>( first, last ) ;
            }

            private Pair<String,String> doBlockDiff( Block block,
                Block cb, Block altCb ) {
                final Pair<String,String> cbDiff = cb.firstDifference( block )  ;
                if ((altCb == null) || cbDiff.equals( Block.NO_DIFFERENCE )) {
                    return cbDiff ;
                } else {
                    return altCb.firstDifference(block) ;
                }
            }

            private void checkBlockCopyright( FileWrapper fw, Block block,
                Block cb, Block altCb ) {
                if (block.hasTags( ORACLE_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG,
                    BlockParser.COMMENT_BLOCK_TAG)) {
                    Pair<String,String> fdiff =
                        doBlockDiff( block, cb, altCb ) ;
                    if (!Block.NO_DIFFERENCE.equals( fdiff )) {
                        validationError( block,
                            "Second block has incorrect copyright text",
                            fw, fdiff ) ;
                    }
                } else {
                    validationError( block,
                        "Second block should be copyright but isn't", fw,
                        Block.NO_DIFFERENCE ) ;
                }
            }

	    public boolean evaluate( FileWrapper fw ) {
		try {
                    Pair<String,String> years = null ;
                    Pair<String,String> altYears = null ;
                    Pair<String,String> updateYears = null ;

                    if (useFileVersion) {
                        years = getFileVersionYear( fw ) ;
                        altYears = new Pair<String,String>( defaultStartYear,
                            years.second() ) ;
                    } else {
                        final String cy = "" + (new GregorianCalendar()).get(
                            Calendar.YEAR ) ;
                        years = new Pair<String,String>( defaultStartYear, cy ) ;
                        altYears = null ;
                    }

		    boolean hadAnOldOracleCopyright = false ;
		    
		    // Convert file into blocks
		    final List<Block> fileBlocks = parserCall.evaluate( fw ) ;

		    // Tag blocks
		    for (Block block : fileBlocks) {
			String str = block.find( COPYRIGHT ) ;
			if (str != null) {
			    block.addTag( COPYRIGHT_BLOCK_TAG ) ;
			    if (str.contains( "Oracle" )) {
				Pair<String,String> scp = getCopyrightPair( str ) ;
                                if (scp != null) {
                                    if (scp.first().equals( defaultStartYear)) {
                                        updateYears = altYears ;
                                    } else {
                                        updateYears = years ;
                                    }
                                }
				block.addTag( ORACLE_COPYRIGHT_TAG ) ;
				hadAnOldOracleCopyright = true ;
			    }
			}
		    }

		    if (verbose > 1) {
			trace( "copyrightBlockAction: blocks in file " + fw ) ;
			for (Block block : fileBlocks) {
			    trace( "\t" + block ) ;
			    for (String str : block.contents()) {
				trace( "\t\t" + str ) ;
			    }
			}
		    }

		    if (validate) {
                        final Block cb = makeCopyrightBlock( years, copyrightText ) ;
                        final Block altCb = (altYears == null)
                            ? null : makeCopyrightBlock( altYears, copyrightText);

			// There should be a Oracle copyright block in the 
                        // first block (if afterFirstBlock is false), otherwise
                        // in the second block.  It should entirely match
                        // copyrightText.
			int count = 0 ;
			for (Block block : fileBlocks) {
			    // Generally always return true, because we want
                            // to see ALL validation errors.
			    if (!afterFirstBlock && (count == 0)) {
                                checkBlockCopyright( fw, block, cb, altCb ) ;
				return true ;
			    } else if (afterFirstBlock && (count == 1)) {
                                checkBlockCopyright( fw, block, cb, altCb ) ;
				return true ;
			    } 
			    
			    if (count > 1) {
				// should not get here!  Return false only in 
                                // this case, because this is an internal error
                                // in the validator.
				validationError( null,
                                    "Neither first nor second block checked", 
                                    fw, Block.NO_DIFFERENCE ) ;
				return false ;
			    }

			    count++ ;
			}
		    } else {
                        final Block updateCb = makeCopyrightBlock( updateYears,
                            copyrightText ) ;

			// Re-write file, replacing the first block tagged
			// ORACLE_COPYRIGHT_TAG, COPYRIGHT_BLOCK_TAG, and 
                        // commentBlock with the copyrightText block.
			
			if (fw.canWrite()) {
			    trace( "Updating copyright/license header on file "
                                + fw ) ;

			    // XXX this is dangerous: a crash before close
                            // will destroy the file!
			    fw.delete() ; 
			    fw.open( FileWrapper.OpenMode.WRITE ) ;

			    boolean firstMatch = true ;
			    boolean firstBlock = true ;
			    for (Block block : fileBlocks) {
				if (!hadAnOldOracleCopyright && firstBlock) {
				    if (afterFirstBlock) {
					block.write( fw ) ;
					updateCb.write( fw ) ;
				    } else {
					updateCb.write( fw ) ;
					block.write( fw ) ;
				    }
				    firstBlock = false ;
				} else if (block.hasTags( ORACLE_COPYRIGHT_TAG,
                                    COPYRIGHT_BLOCK_TAG,
				    BlockParser.COMMENT_BLOCK_TAG) && firstMatch)  {
				    firstMatch = false ;
				    if (hadAnOldOracleCopyright) {
					updateCb.write( fw ) ;
				    }
				} else {
				    block.write( fw ) ;
				}
			    }
			} else {
			    if (verbose > 1) {
				trace( "Skipping file " + fw
                                    + " because is is not writable" ) ;
			    }
			}
		    }
		} catch (IOException exc ) {
		    trace( "Exception while processing file " + fw + ": "
                        + exc ) ;
		    exc.printStackTrace() ;
		    return false ;
		} finally {
		    fw.close() ;
		}

		return true ;
	    }
	} ;
    }

    // Note: we could also make the block and line comment processors configurable,
    // but that seems like overkill.
    private static final String JAVA_COMMENT_START = "/*" ;
    private static final String JAVA_COMMENT_PREFIX = " *" ;
    // Note that the display form of JAVA_COMMENT_END adds a space to line up the *'s
    private static final String JAVA_COMMENT_END = "*/" ; 

    private static final boolean JAVA_AFTER_FIRST_BLOCK = false ;
    private static final String JAVA_FORMAT_TAG = "JavaFormat" ;

    private static final String XML_COMMENT_START = "<!--" ;
    private static final String XML_COMMENT_PREFIX = " " ;
    private static final String XML_COMMENT_END = "-->" ;
    private static final boolean XML_AFTER_FIRST_BLOCK = true ;
    private static final String XML_FORMAT_TAG = "XmlFormat" ;

    private static final String JAVA_LINE_PREFIX = "// " ;
    private static final boolean JAVA_LINE_AFTER_FIRST_BLOCK = false ;
    private static final String JAVA_LINE_FORMAT_TAG = "JavaLineFormat" ;

    private static final String SCHEME_PREFIX = "; " ;
    private static final boolean SCHEME_AFTER_FIRST_BLOCK = false ;
    private static final String SCHEME_FORMAT_TAG = "SchemeFormat" ;

    private static final String SHELL_PREFIX = "# " ;
    private static final boolean SHELL_AFTER_FIRST_BLOCK = true ;
    private static final String SHELL_FORMAT_TAG = "ShellFormat" ;
    // Note that there are actually 2 SHELL parsers for SHELL and SHELL_SCRIPT.
    // The difference is that the SHELL_SCRIPT always starts with #!.
    
    public static void main(String[] strs) {
	ArgParser ap = new ArgParser( Arguments.class ) ;
	Arguments args = ap.parse( strs, Arguments.class ) ;

	String startYear = args.startyear() ;
	verbose = args.verbose() ;
	validate = args.validate() ;
        useFileVersion = args.useFileVersion() ;
        if (useFileVersion) {
            modificationAnalyzer = new ModificationAnalyzer(
                args.root().getAbsolutePath() ) ;
        }

	if (verbose > 0) {
	    trace( "Main: args:\n" + args ) ;
	}

	try {
	    // Create the blocks needed for different forms of the 
	    // copyright comment template
	    final Block copyrightText = BlockParser.getBlock( args.copyright() ) ;

	    final Block javaCopyrightText = 
		makeCopyrightBlockCommentBlock( copyrightText, 
		    JAVA_COMMENT_START, JAVA_COMMENT_PREFIX, " " + JAVA_COMMENT_END,
		    JAVA_FORMAT_TAG ) ;
	    final Block xmlCopyrightText = 
		makeCopyrightBlockCommentBlock( copyrightText, 
		    XML_COMMENT_START, XML_COMMENT_PREFIX, XML_COMMENT_END, 
		    XML_FORMAT_TAG ) ;

	    final Block javaLineCopyrightText =
		makeCopyrightLineCommentBlock( copyrightText, JAVA_LINE_PREFIX,
		JAVA_LINE_FORMAT_TAG ) ;
	    final Block schemeCopyrightText = 
		makeCopyrightLineCommentBlock( copyrightText, SCHEME_PREFIX,
		SCHEME_FORMAT_TAG ) ;
	    final Block shellCopyrightText = 
		makeCopyrightLineCommentBlock( copyrightText, SHELL_PREFIX,
		SHELL_FORMAT_TAG ) ;

	    if (verbose > 0) {
		trace( "Main: copyrightText = " + copyrightText ) ;
		trace( "Main: javaCopyrightText = " + javaCopyrightText ) ;
		trace( "Main: xmlCopyrightText = " + xmlCopyrightText ) ;
		trace( "Main: javaLineCopyrightText = " + javaLineCopyrightText ) ;
		trace( "Main: schemeCopyrightText = " + schemeCopyrightText ) ;
		trace( "Main: shellCopyrightText = " + shellCopyrightText ) ;
	    }

	    ActionFactory af = new ActionFactory( args.verbose(), args.dryrun() ) ;

	    // Create the actions we need
	    Recognizer recognizer = af.getRecognizerAction() ; // recognizer is the scanner action

	    // Create the BlockParserCalls needed for the actions
	    BlockParserCall javaBlockParserCall = makeBlockCommentParser( 
		JAVA_COMMENT_START, JAVA_COMMENT_END ) ;
	    BlockParserCall xmlBlockParserCall = makeBlockCommentParser(
		XML_COMMENT_START, XML_COMMENT_END ) ;

	    BlockParserCall javaLineParserCall = makeLineCommentParser( JAVA_LINE_PREFIX ) ;
	    BlockParserCall schemeLineParserCall = makeLineCommentParser( SCHEME_PREFIX ) ;
	    BlockParserCall shellLineParserCall = makeLineCommentParser( SHELL_PREFIX ) ;

	    // Now, create the actions needed in the recognizer
	    Scanner.Action skipAction = af.getSkipAction() ;

	    Scanner.Action javaAction = makeCopyrightBlockAction( javaCopyrightText,
		javaBlockParserCall, startYear, JAVA_AFTER_FIRST_BLOCK ) ;
	    Scanner.Action xmlAction = makeCopyrightBlockAction( xmlCopyrightText,
		xmlBlockParserCall, startYear, XML_AFTER_FIRST_BLOCK ) ;
	    Scanner.Action schemeAction = makeCopyrightBlockAction( schemeCopyrightText, 
		schemeLineParserCall, startYear, SCHEME_AFTER_FIRST_BLOCK ) ;
	    Scanner.Action javaLineAction = makeCopyrightBlockAction( javaLineCopyrightText, 
		javaLineParserCall, startYear, JAVA_LINE_AFTER_FIRST_BLOCK ) ;
	    Scanner.Action shellAction = makeCopyrightBlockAction( shellCopyrightText, 
		shellLineParserCall, startYear, false ) ;
	    Scanner.Action shellScriptAction = makeCopyrightBlockAction( shellCopyrightText, 
		shellLineParserCall, startYear, true ) ;

	    // Configure the recognizer
	    // Java
	    for (String str : JAVA_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, javaAction ) ;
	    }

	    // Java line 
	    for (String str : JAVA_LINE_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, javaLineAction ) ;
	    }

	    // XML
	    for (String str : XML_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, xmlAction ) ;
	    }

	    // Scheme
	    for (String str : SCHEME_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, schemeAction ) ;
	    }

	    // Shell
	    for (String str : SHELL_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, shellAction ) ;
	    }

	    for (String str : MAKEFILE_NAMES) {
		recognizer.addKnownName( str, shellAction ) ;
	    }

	    for (String str : SHELL_SCRIPT_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, shellScriptAction ) ;
	    }
	    recognizer.setShellScriptAction( shellScriptAction ) ;
	    
	    // Binary
	    for (String str : BINARY_LIKE_SUFFIXES) {
		recognizer.addKnownSuffix( str, skipAction ) ;
	    }

	    for (String str : IGNORE_FILE_NAMES) {
		recognizer.addKnownName( str, skipAction ) ;
	    }

	    if (verbose > 0) {
		trace( "Main: contents of recognizer:" ) ;
		recognizer.dump() ;
	    }

	    Scanner scanner = new Scanner( verbose, args.root() ) ;
	    for (String str : args.skipdirs() ) {
                scanner.addDirectoryToSkip(str);
            }

	    // Finally, we process all files
	    scanner.scan( recognizer ) ;
	} catch (IOException exc) {
	    System.out.println( "Exception while processing: " + exc ) ;
	    exc.printStackTrace() ;
	}
    }
}

