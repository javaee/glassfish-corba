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

package com.sun.corba.se.impl.codegen;

import java.util.Map ;
import java.util.LinkedHashMap ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Variable ;

import com.sun.corba.se.impl.codegen.StatementBase ;

/**
 *
 * @author Ken Cavanaugh
 */
public final class TryStatement extends StatementBase {
    private BlockStatement bodyPart ;
    private BlockStatement finalPart ;
    private Map<Type,Pair<Variable,BlockStatement>> catches ;

    TryStatement( Node parent ) {
	super( parent ) ;
	bodyPart = new BlockStatement( this ) ;
	finalPart = new BlockStatement( this ) ;
	catches = new LinkedHashMap<Type,Pair<Variable,BlockStatement>>() ;
    }

    public BlockStatement bodyPart() {
	return this.bodyPart ;
    }
    
    public BlockStatement finalPart() {
	return this.finalPart ;
    }
    
    public Map<Type,Pair<Variable,BlockStatement>> catches() {
	return this.catches ;
    }

    /** Add a new Catch block to this try statement.  type must be
     * a non-primitive, non-array type, and may occur only once per
     * try statement.
     */
    public Pair<Variable,BlockStatement> addCatch( Type type, String ident ) {
	if (type.isPrimitive())
	    throw new IllegalArgumentException( "Primitive type " + type +
		" not allowed in catch block" ) ;

	if (type.isArray())
	    throw new IllegalArgumentException( "Array type " + type +
		" not allowed in catch block" ) ;

	if (catches.containsKey( type )) 
	    throw new IllegalArgumentException( "Type " + type + 
		" is already used as a catch block in this try statement" ) ;

	// XXX iterate over list and make sure that type is not a subclass of
	// any previous types in the list
	
	BlockStatement stmt = new BlockStatement( this ) ;
	Variable var = stmt.exprFactory().variable( type, ident ) ;
	Pair<Variable,BlockStatement> result = new Pair<Variable,BlockStatement>(
	    var, stmt ) ;
	catches.put( type, result ) ;
	return result ;
    }
	
    public void accept( Visitor visitor ) {
	visitor.visitTryStatement( this ) ;
    }
}
