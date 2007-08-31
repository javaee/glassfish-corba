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

package com.sun.corba.se.impl.orbutil.codegen;

import java.io.PrintStream ;

/** Manages printing of indented source code.
 * Line numbers start at 1 and increase by 1
 * every time nl() is called.  Note that the 
 * proper use of this class requires calling nl()
 * at the START of every line (thanks, Harold!),
 * which make indentation much easier to manage.  For example,
 * an if statement can be printed as
 * 
 * nl().p( "if (expr) {" ).in() ;
 * nl().p( "stmt" ).out() ;
 * nl().p( "} else {" ).in() ;
 * nl().p( "stmt" ).out() ;
 */
public class Printer{
    private static final int DEFAULT_INCREMENT = 4 ;

    static Attribute<Integer> lineNumberAttribute = new Attribute<Integer>( 
	Integer.class, "lineNumber", -1 ) ;

    private PrintStream ps ;
    private int increment ;
    private char padChar ;

    private int lineNumber ;
    private int indent ;
    private char[] pad ;
    private StringBuilder bld ;

    public Printer( PrintStream ps ) {
	this( ps, DEFAULT_INCREMENT, ' ' ) ;
    }

    public Printer( PrintStream ps, int increment, char padChar ) {
	this.ps = ps ;
	this.increment = increment ;
	this.padChar = padChar ;
	this.lineNumber = 1 ;
	this.indent = 0 ;
	this.bld = new StringBuilder() ;
	fill() ;
    }

    public int lineNumber() {
	return lineNumber ;
    }

    public Printer p( String str ) {
	bld.append( str ) ;
	return this ;
    }

    public Printer in() {
	indent += increment ;
	fill() ;
	return this ;
    }

    public Printer out() {
	if (indent < increment)
	    throw new IllegalStateException(
		"Cannot undent past start of line" ) ;

	indent -= increment ;
	fill() ;
	return this ;
    }

    private void fill() {
	pad = new char[indent] ;
	for (int ctr = 0; ctr<pad.length; ctr++)
	    pad[ctr] = padChar ;
    }

    public Printer nl() {
	return this.nl( null ) ;
    }

    public Printer nl( Node node ) {
	lineNumber++ ;
	if (node != null)
	    lineNumberAttribute.set( node, lineNumber ) ;

	ps.println( bld.toString() ) ;
	bld = new StringBuilder() ;
	bld.append( pad ) ; 
	return this ;
    }
}
