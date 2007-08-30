/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.tools.corba.se.logutil;

import java.io.PrintWriter ;
import java.io.Writer ;
import java.io.OutputStream ;
import java.io.BufferedWriter ;
import java.io.OutputStreamWriter ;
import jsint.Pair ;
import java.util.StringTokenizer ;

public class IndentingPrintWriter extends PrintWriter {
    private int level = 0 ;
    private int indentWidth = 4 ;
    private String indentString = "" ;

    public void printMsg( String msg, Pair data )
    {
	// System.out.println( "printMsg called with msg=" + msg + " data=" + data ) ;
	StringTokenizer st = new StringTokenizer( msg, "@", true ) ;
	StringBuffer result = new StringBuffer() ;
	Object head = data.first ;
	Pair tail = (Pair)data.rest ;
	String token = null ;

	while (st.hasMoreTokens()) {
	    token = st.nextToken() ;
	    if (token.equals("@")) {
		if (head != null) {
		    result.append( head ) ;
		    head = tail.first ;
		    tail = (Pair)tail.rest ;
		} else {
		    throw new Error( "List too short for message" ) ;
		}
	    } else {
		result.append( token ) ;
	    }
	}

	// System.out.println( "Printing result " + result + " to file" ) ;
	print( result ) ;
	println() ;
    }

    public IndentingPrintWriter (Writer out) {
	super( out, true ) ;
	// System.out.println( "Constructing a new IndentingPrintWriter with Writer " + out ) ;
    }

    public IndentingPrintWriter(Writer out, boolean autoFlush) {
	super( out, autoFlush ) ;
	// System.out.println( "Constructing a new IndentingPrintWriter with Writer " + out ) ;
    }

    public IndentingPrintWriter(OutputStream out) {
	super(out, true);
	// System.out.println( "Constructing a new IndentingPrintWriter with OutputStream " + out ) ;
    }

    public IndentingPrintWriter(OutputStream out, boolean autoFlush) {
	super(new BufferedWriter(new OutputStreamWriter(out)), autoFlush);
	// System.out.println( "Constructing a new IndentingPrintWriter with OutputStream " + out ) ;
    }

    public void setIndentWidth( int indentWidth )
    {
	this.indentWidth = indentWidth ;
	updateIndentString() ;
    }

    public void indent()
    {
	level++ ;
	updateIndentString() ;
    }

    public void undent()
    {
	if (level > 0) {
	    level-- ;
	    updateIndentString() ;
	}
    }

    private void updateIndentString()
    {
	int size = level * indentWidth ;
	StringBuffer sbuf = new StringBuffer( size ) ;
	for (int ctr = 0; ctr<size; ctr++ )
	    sbuf.append( " " ) ;
	indentString = sbuf.toString() ;
    }

    // overridden from PrintWriter
    public void println() 
    {
	super.println() ;

	print( indentString ) ;
    }
}
