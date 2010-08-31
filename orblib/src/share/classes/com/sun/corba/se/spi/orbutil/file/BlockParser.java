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

import java.io.IOException ;

import com.sun.corba.se.spi.orbutil.generic.BinaryFunction ;

/** Utility class used to convert FileWrappers into (lists of) Blocks.
 */
public class BlockParser {
    private BlockParser() {}

    /** Return the contents of the text file as a Block.
     */
    public static Block getBlock( final FileWrapper fw ) throws IOException {
	fw.open( FileWrapper.OpenMode.READ ) ;

	try {
	    final List<String> data = new ArrayList<String>() ;

	    String line = fw.readLine() ;
	    while (line != null) {
		data.add( line ) ;
		line = fw.readLine() ;
	    }
	    
	    return new Block( data ) ;
	} finally {
	    fw.close() ;
	}
    }

    public static final String COMMENT_BLOCK_TAG = "CommentBlock" ;

    /** Transform fw into a list of blocks.  There are two types of blocks in this
     * list, and they always alternate:
     * <ul>
     * <li>Blocks in which every line starts with prefix, 
     * Such blocks are given the tag COMMENT_BLOCK_TAG.  
     * <li>Blocks in which no line starts with prefix.
     * Such blocks are not tagged.
     * <ul>
     */
    public static List<Block> parseBlocks( final FileWrapper fw, 
	final String prefix ) throws IOException {

	boolean inComment = false ;
	final List<Block> result = new ArrayList<Block>() ;
	fw.open( FileWrapper.OpenMode.READ ) ;

	try {
	    List<String> data = new ArrayList<String>() ;

	    BinaryFunction<List<String>,String,List<String>> newBlock = 
		new BinaryFunction<List<String>,String,List<String>>() {
		    public List<String> evaluate( List<String> data, String tag ) {
			if (data.size() == 0)
			    return data ;

			final Block bl = new Block( data ) ;
			if (tag != null)
			    bl.addTag( tag ) ;
			result.add( bl ) ;
			return new ArrayList<String>() ;
		    }
		} ;

	    String line = fw.readLine() ;
	    while (line != null) {
		if (inComment) {
		    if (!line.startsWith( prefix )) {
			inComment = false ;
			data = newBlock.evaluate( data, COMMENT_BLOCK_TAG ) ;
		    }
		} else {
		    if (line.startsWith( prefix )) {
			inComment = true ;
			data = newBlock.evaluate( data, null ) ;
		    }
		}
		data.add( line ) ;

		line = fw.readLine() ;
	    }

	    // Create last block!
	    Block bl = new Block( data ) ;
	    if (inComment)
		bl.addTag( COMMENT_BLOCK_TAG ) ;
	    result.add( bl ) ;
	    
	    return result ;
	} finally {
	    fw.close() ;
	}
    }

    /** Transform fw into a list of blocks.  There are two types of blocks in this
     * list, and they always alternate:
     * <ul>
     * <li>Blocks that start with a String containing start, 
     * and end with a String containing end.  Such blocks are given the
     * tag COMMENT_BLOCK_TAG.  
     * <li>Blocks that do not contain start or end anywhere
     * <ul>
     */
    public static List<Block> parseBlocks( final FileWrapper fw, 
	final String start, final String end ) throws IOException {

	boolean inComment = false ;
	final List<Block> result = new ArrayList<Block>() ;
	fw.open( FileWrapper.OpenMode.READ ) ;

	try {
	    List<String> data = new ArrayList<String>() ;

	    BinaryFunction<List<String>,String,List<String>> newBlock = 
		new BinaryFunction<List<String>,String,List<String>>() {
		    public List<String> evaluate( List<String> data, String tag ) {
			if (data.size() == 0)
			    return data ;

			final Block bl = new Block( data ) ;
			if (tag != null)
			    bl.addTag( tag ) ;
			result.add( bl ) ;
			return new ArrayList<String>() ;
		    }
		} ;

	    String line = fw.readLine() ;
	    while (line != null) {
		if (inComment) {
		    data.add( line ) ;

		    if (line.contains( end )) {
			inComment = false ;
			data = newBlock.evaluate( data, COMMENT_BLOCK_TAG ) ;
		    }
		} else {
		    if (line.contains( start )) {
			inComment = true ;
			data = newBlock.evaluate( data, null ) ;
		    }

		    data.add( line ) ;

		    if (line.contains( end)) {
			// Comment was a single line!
			inComment = false ;
			data = newBlock.evaluate( data, COMMENT_BLOCK_TAG ) ;
		    }
		}

		line = fw.readLine() ;
	    }

	    // Create last block!
	    Block bl = new Block( data ) ;
	    if (inComment)
		bl.addTag( COMMENT_BLOCK_TAG ) ;
	    result.add( bl ) ;
	    
	    return result ;
	} finally {
	    fw.close() ;
	}
    }
}
