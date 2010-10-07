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
import java.util.Iterator ;
import java.util.ArrayList ;
import java.util.Set ;
import java.util.Map ;
import java.util.HashSet ;
import java.util.StringTokenizer ;

import java.io.IOException ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

/** Represents a range of Strings, typically read from a file, that are in some sense
 * related and contiguous.  Blocks may also be tagged as an aid in transforming
 * a series of blocks.
 */
public class Block {
    private List<String> data ;
    private final Set<String> tags ;

    private Block( final List<String> data, final Set<String> tags ) {
	this.data = data ;
	this.tags = tags ;
    }

    /** Create a new Block from a list of strings.
     */
    public Block( final List<String> data ) {
	this.data = data ;
	this.tags = new HashSet<String>() ;
    }

    public String toString() {
	final StringBuilder sb = new StringBuilder() ;
	sb.append( "Block[" ) ;
	boolean first = true ;
	for (String tag : tags) {
	    if (first) {
		first = false ;
	    } else {
		sb.append( " " ) ;
	    }

	    sb.append( tag ) ;
	}
	sb.append( "]" ) ;
	return sb.toString() ;
    }

    public boolean equals( Object obj ) {
	if (obj == this)
	    return true ;

	if (!(obj instanceof Block))
	    return false ;

	Block block = (Block)obj ;

	// Equal if contents are equal; we ignore the tags
	Iterator<String> iter1 = data.iterator() ;
	Iterator<String> iter2 = block.data.iterator() ;
	while (iter1.hasNext() && iter2.hasNext()) {
	    String str1 = iter1.next() ;
	    String str2 = iter2.next() ;
	    if (!str1.equals( str2 ))
		return false ;
	}

	return iter1.hasNext() == iter2.hasNext() ;
    }

    public int hashCode() {
	int hash = 0 ;
	for (String str : data) 
	    hash ^= data.hashCode() ;
	return hash ;
    }

    /** Create a new Block which is a copy of block.
     */
    public Block( final Block block ) {
	this.data = new ArrayList<String>( block.data ) ;
	this.tags = new HashSet<String>( block.tags ) ;
    }

    /** Add a tag to the block.  Useful for classifying blocks.
     */
    public void addTag( final String tag ) {
	tags.add( tag ) ;
    }

    /** Return whether or not a block has a particular tag.
     */
    public boolean hasTag( final String tag ) {
	return tags.contains( tag ) ;
    }

    /** Return whether or not a block has ALL of the listed tags.
     */
    public boolean hasTags( final String... tags ) {
	for (String tag : tags) {
	    if (!hasTag( tag ))
		return false ;
	}

	return true ;
    }

    /** Get the contents of the block.
     */
    public List<String> contents() {
	return data ;
    }

    /** Add String before the start of the block.
     */
    public void addBeforeFirst( final String str ) {
	data.add( 0, str ) ;
    }

    /** Add String after the end of the block.
     */
    public void addAfterLast( final String str ) {
	data.add( data.size(), str ) ;
    }

    /** Add the prefix to each string in the block.
     */
    public void addPrefixToAll( String prefix ) {
	final List<String> newData = new ArrayList<String>() ;
	for (String str : data) {
	    newData.add( prefix + str ) ; 
	}
	data = newData ;
    }

    /** Return the first string in the block that contains the search string.
     */
    public String find( final String search ) {
	for (String str : data) {
	    if (str.contains( search ))
		return str ;
	}

	return null ;
    }

    /** Write block to FileWrapper.  FileWrapper must be open for writing.
     */
    public void write( final FileWrapper fw ) throws IOException {
	for (String str : data ) {
	    fw.writeLine( str ) ;
	}
    }

    /** replace all occurrences of @KEY@ with parameters.get( KEY ).
     * This is very simple: only one scan is made, so @...@ patterns
     * in the parameters values are ignored.
     */
    public Block instantiateTemplate( Map<String,String> parameters ) {
	final List<String> result = new ArrayList<String>( data.size() ) ;
	for (String str : data) {
	    final StringBuilder sb = new StringBuilder() ;
	    final StringTokenizer st = new StringTokenizer( str, "@" ) ;

	    // Note that the pattern is always TEXT@KEY@TEXT@KEY@TEXT,
	    // so the the first token is not a keyword, and then the tokens
	    // alternate.
	    boolean isKeyword = false ;
	    while (st.hasMoreTokens()) {
		final String token = st.nextToken() ;
		final String replacement = 
		    isKeyword ? parameters.get( token ) : token ;
		sb.append( replacement ) ;
		isKeyword = !isKeyword ;
	    }

	    result.add( sb.toString() ) ;
	}

	return new Block( result ) ;
    }

    private String expandTabs( String src ) {
        int outCtr = 0 ;
        StringBuilder result = new StringBuilder() ;
        for (int ctr=0; ctr<src.length(); ctr++) {
            char ch = src.charAt( ctr ) ;
            if (ch == '\t') {
                int nextTab = ((outCtr >> 3) + 1) << 3 ;
                while (outCtr < nextTab) {
                    result.append( ' ' ) ;
                    outCtr++ ;
                }
            } else {
                result.append( ch ) ;
                outCtr++ ;
            }
        }

        return result.toString() ;
    }

    /** Replace tabs with spaces, assuming tab stops are located as usual at n*8 + 1
     */
    public Block expandTabs() {
	List<String> result = new ArrayList<String>() ;
	for (String line : data) {
            String exp = expandTabs( line ) ;
	    result.add( exp ) ;
	}
	return new Block( result ) ;
    }

    public Block substitute( List<Pair<String,String>> substitutions ) {
	List<String> result = new ArrayList<String>() ;
	for (String line : data) {
	    String newLine = line ;
	    for (Pair<String,String> pair : substitutions) {
		String pattern = pair.first() ;
		String replacement = pair.second() ;
		newLine = newLine.replace( pattern, replacement ) ;
	    }
	    result.add( newLine ) ;
	}
	return new Block( result ) ;
    }

    /** Split block into two blocks, with only the
     * first line of the original Block in result.first().
     */
    public Pair<Block,Block> splitFirst() {
	List<String> first = new ArrayList<String>() ;
	List<String> rest = new ArrayList<String>() ;
	for (String str : data) {
	    if (first.size() == 0) {
		first.add( str ) ;
	    } else {
		rest.add( str ) ;
	    }
	}

	Block block1 = new Block( first, new HashSet<String>(tags) ) ;
	Block block2 = new Block( rest, new HashSet<String>(tags) ) ;
	Pair<Block,Block> result = new Pair<Block,Block>( block1, block2 ) ;

	return result ;
    }
}
