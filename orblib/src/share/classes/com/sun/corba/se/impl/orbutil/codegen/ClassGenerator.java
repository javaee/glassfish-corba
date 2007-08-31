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

import java.util.Set ;
import java.util.HashSet ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Properties ;

import java.io.PrintStream ;

import com.sun.corba.se.impl.orbutil.codegen.NodeBase ;
import com.sun.corba.se.impl.orbutil.codegen.FieldGenerator ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.MethodInfo ;
import com.sun.corba.se.spi.orbutil.codegen.FieldInfo ;

import static java.lang.reflect.Modifier.* ;

/** Class used to generate a description of a class or interface.
 * An interface is an abstract class, all of whose methods are 
 * abstract.  Interfaces do not have a super class, an initializer,
 * or constructors.  Interfaces also do not have variables.
 * <p>
 * Note: the hashCode of a ClassGenerator changes whenever a
 * method, constructor, or field is added, so do not put
 * ClassGenerators into sets or maps unless they are fully
 * populated.
 */
public final class ClassGenerator extends ClassInfoBase implements Node {
    private Node nodeImpl ;
    private BlockStatement initializer ;
    private List<MethodGenerator> methods ;
    private List<MethodGenerator> constructors ;
    private List<FieldGenerator> fields ;

    /** Construct a ClassGenerator representing an interface.
     */
    ClassGenerator( int modifiers, String name, List<Type> impls )  {
	// Note that all interfaces must have the ABSTRACT and INTERFACE 
	// modifiers.
	super( modifiers | ABSTRACT | INTERFACE, Type._class(name) ) ;

	nodeImpl = new NodeBase( null ) ; 
	initializeInterface( impls ) ;

	initializer = null ;
	methods = new ArrayList<MethodGenerator>() ;
	constructors = null ;
	fields = null ;
    }

    /** Construct a ClassGenerator representing a class.
     */
    ClassGenerator( int modifiers, String name, Type superType, 
	List<Type> impls ) {
	super( modifiers, Type._class( name ) ) ;
	nodeImpl = new NodeBase( null ) ; 

	// We need the Type._class( name ) form of the class
	// type in order for Type._classGenerator to function
	// correctly.  Later we will need the _classGenerator form
	// to avoid attempts to load the class for the class that
	// has not yet been completely generated, so we override
	// the value of thisType here.
	initializeClass( Type._classGenerator( this ), superType, impls ) ;

	initializer = new BlockStatement( this ) ;
	methods = new ArrayList<MethodGenerator>() ;
	constructors = new ArrayList<MethodGenerator>() ;
	fields = new ArrayList<FieldGenerator>() ;
    }

    // All node methods are delegated to nodeImpl.
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
    // End of delegation

    public BlockStatement initializer() {
	if (isInterface())
	    throw new IllegalStateException( 
		"An Interface does not have an initializer" ) ;
	return initializer ;
    }

    public List<FieldGenerator> fields() {
	return fields ;
    }

    public List<MethodGenerator> methods() {
	return methods ;
    }

    public List<MethodGenerator> constructors() {
	if (isInterface())
	    throw new IllegalStateException( 
		"An Interface does not have constructors" ) ;
	return constructors ;
    }

    public Set<MethodInfo> constructorInfo() {
	return new HashSet<MethodInfo>( constructors ) ;
    }

    // Every method must be added to methodInfoByName (defined in ClassInfoBase)
    // AFTER it is completed.  This cannot be done here in startMethod, so
    // we do it in methodComplete.
    public MethodGenerator startMethod( int modifiers, Type rtype, String name, 
	List<Type> exceptions ) {
	if (isInterface() && !isAbstract(modifiers))
	    // XXX should check for other modifiers as well?
	    throw new IllegalArgumentException(
		"All methods in an interface must be abstract" ) ;

	MethodGenerator result = new MethodGenerator( this, modifiers, rtype, 
	    name, exceptions ) ;

	return result ;
    }

    // Since methods and constructors are handled largely the same way, we
    // have a startConstructor method that requires a call to methodComplete
    // after the constructor has been defined.
    public MethodGenerator startConstructor( int modifiers, 
	List<Type> exceptions ) {

	if (isInterface()) 
	    throw new IllegalStateException(
		"Interfaces may not define constructors" ) ;

	MethodGenerator result = new MethodGenerator( this, modifiers,
	    exceptions ) ;

	return result ;
    }

    public void methodComplete( MethodGenerator mg ) {
	mg.argsComplete() ;

	if (mg.isConstructor()) {
	    constructors.add( mg ) ;
	    addConstructorInfo( mg ) ;
	} else {
	    // Add method to the list of MethodGenerators maintained
	    // in the ClassGenerator API (not the same as 
	    // methodInfoByName).
	    methods.add( mg ) ;

	    // Add method to methodInfoByName in ClassInfoBase
	    // after the method has been defined.
	    // This is required so that the hashCode value of 
	    // the MethodGenerator does not
	    // change after the MethodGenerator is added to the
	    // Set<MethodInfo> in methodInfoByName.
	    addMethodInfo( mg ) ;
	}
    }

    public FieldGenerator addField( int modifiers, Type type, String name ) {
	if (isInterface())
	    throw new IllegalStateException(
		"Interfaces may not contain data members" ) ;

	if (fieldInfo().keySet().contains( name ))
	    throw new IllegalArgumentException( "Fields for class " + name +
		" already contains field " + name ) ;

	FieldGenerator var = new FieldGenerator( this, modifiers, type, name ) ;

	fields.add( var ) ;
	addFieldInfo( var ) ;

	return var ;
    }

    public void accept( Visitor visitor ) {
	visitor.visitClassGenerator( this ) ;
    }
}
