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

package com.sun.corba.se.spi.orbutil.misc ;

import java.util.Arrays ;

public abstract class ObjectWriter {
    public static ObjectWriter make( boolean isIndenting, 
	int initialLevel, int increment )
    {
	if (isIndenting)
	    return new IndentingObjectWriter( initialLevel, increment ) ;
	else
	    return new SimpleObjectWriter() ;
    }

    public abstract void startObject( Object obj ) ;

    public abstract void startElement() ;

    public abstract void endElement() ;

    public abstract void endObject( String str ) ;

    public abstract void endObject() ;

    public String toString() { return result.toString() ; }

    public void append( boolean arg ) { result.append( arg ) ; } 

    public void append( char arg ) { result.append( arg ) ; } 

    public void append( short arg ) { result.append( arg ) ; } 

    public void append( int arg ) { result.append( arg ) ; } 

    public void append( long arg ) { result.append( arg ) ; } 

    public void append( float arg ) { result.append( arg ) ; } 

    public void append( double arg ) { result.append( arg ) ; } 

    public void append( String arg ) { result.append( arg ) ; } 

//=================================================================================================
// Implementation
//=================================================================================================

    protected StringBuffer result ;

    protected ObjectWriter()
    {
	result = new StringBuffer() ;
    }

    protected void appendObjectHeader( Object obj ) 
    {
	result.append( obj.getClass().getName() ) ;
	result.append( "<" ) ;
	result.append( System.identityHashCode( obj ) ) ;
	result.append( ">" ) ;
	Class compClass = obj.getClass().getComponentType() ;

	if (compClass != null) {
	    result.append( "[" ) ;
	    if (compClass == boolean.class) {
		boolean[] arr = (boolean[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == byte.class) {
		byte[] arr = (byte[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == short.class) {
		short[] arr = (short[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == int.class) {
		int[] arr = (int[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == long.class) {
		long[] arr = (long[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == char.class) {
		char[] arr = (char[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == float.class) {
		float[] arr = (float[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else if (compClass == double.class) {
		double[] arr = (double[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    } else { // array of object
		java.lang.Object[] arr = (java.lang.Object[])obj ;
		result.append( arr.length ) ;
		result.append( "]" ) ;
	    }
	}

	result.append( "(" ) ;
    }

    /** Expected patterns:
    * startObject endObject( str )
    *	header( elem )\n
    * startObject ( startElement append* endElement ) * endObject
    *	header(\n
    *	    append*\n *
    *	)\n
    */
    private static class IndentingObjectWriter extends ObjectWriter {
	private int level ;
	private int increment ;

	public IndentingObjectWriter( int initialLevel, int increment )
	{
	    this.level = initialLevel ;
	    this.increment = increment ;
	    startLine() ;
	}

	private void startLine() 
	{
	    char[] fill = new char[ level * increment ] ;
	    Arrays.fill( fill, ' ' ) ;
	    result.append( fill ) ;
	}

	public void startObject( java.lang.Object obj ) 
	{
	    appendObjectHeader( obj ) ;
	    level++ ;
	}

	public void startElement() 
	{
	    result.append( "\n" ) ;
	    startLine() ;
	}

	public void endElement() 
	{
	}

	public void endObject( String str ) 
	{
	    level-- ;
	    result.append( str ) ;
	    result.append( ")" ) ;
	}

	public void endObject( ) 
	{
	    level-- ;
	    result.append( "\n" ) ;
	    startLine() ;
	    result.append( ")" ) ;
	}
    }
    
    private static class SimpleObjectWriter extends ObjectWriter {
	public void startObject( java.lang.Object obj ) 
	{
	    appendObjectHeader( obj ) ;
	    result.append( " " ) ;
	}

	public void startElement() 
	{
	    result.append( " " ) ;
	}

	public void endObject( String str )
	{
	    result.append( str ) ;
	    result.append( ")" ) ;
	}

	public void endElement() 
	{
	}

	public void endObject() 
	{
	    result.append( ")" ) ;
	}
    }
}
