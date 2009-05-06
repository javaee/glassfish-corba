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

import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import java.io.PrintStream ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;

import com.sun.corba.se.impl.orbutil.codegen.NodeBase ;

public final class MethodGenerator extends MethodInfoBase implements Node {
    private Node nodeImpl ;
    private boolean isConstructor ;
    private boolean argsComplete ;
    private BlockStatement body ;

    public BlockStatement body() {
	return this.body ;
    }

    /** Construct a MethodGenerator that represents a constructor.
     */
    MethodGenerator( ClassGeneratorImpl parent, int modifiers,
	List<Type> exceptions ) {
	super( parent, modifiers ) ;
	nodeImpl = new NodeBase( parent ) ;

	this.isConstructor = true ;
	this.argsComplete = false ;
	this.exceptions.addAll( exceptions ) ;
	body = new BlockStatement( this ) ;
    }

    /** Construct a MethodGenerator that represents a method.
     */
    MethodGenerator( ClassGeneratorImpl parent, int modifiers, Type rtype, String name,
	List<Type> exceptions ) {
	super( parent, modifiers, rtype, name ) ;
	nodeImpl = new NodeBase( parent ) ;

	this.isConstructor = false ;
	this.exceptions.addAll( exceptions ) ;
	body = new BlockStatement( this ) ;
    }
    
    // All Node methods are delegated to nodeImpl.
    public Node parent() {
	return nodeImpl.parent() ;
    }

    public int id() {
	return nodeImpl.id() ;
    }

    public void parent( Node node ) {
	nodeImpl.parent( node ) ;
    }

    public <T extends Node> T getAncestor( Class<T> type ) {
	return nodeImpl.getAncestor( type ) ;
    }

    public <T extends Node> T copy( Class<T> cls ) {
	return nodeImpl.copy( cls ) ;
    }

    public <T extends Node> T copy( Node newParent, Class<T> cls ) {
	return nodeImpl.copy( newParent, cls ) ;
    }

    public Object get( int index ) {
	return nodeImpl.get( index ) ;
    }

    public void set( int index, Object obj ) {
	nodeImpl.set( index, obj ) ;
    }

    public List<Object> attributes() {
	return nodeImpl.attributes() ;
    }

    public Variable addArgument( Type type, String ident ) {
	if (argsComplete)
	    throw new IllegalStateException( 
		"An attempt was made to add an argument after argsComplete was called" ) ;

	Variable var = body.exprFactory().variable( type, ident ) ;

	synchronized (this) {
	    clearHashCode() ;
	    arguments.add( var ) ;
	}

	return var ;
    }

    public boolean isConstructor() {
	return this.isConstructor ;
    }

    public void argsComplete() {
	argsComplete = true ;
    }

    public int hashCode() {
	if (!argsComplete) 
	    throw new IllegalStateException(
		"Trying to call hashCode before argsComplete." ) ;

	return super.hashCode() ;
    }

    public void accept( Visitor visitor ) {
	visitor.visitMethodGenerator( this ) ;
    }
}
