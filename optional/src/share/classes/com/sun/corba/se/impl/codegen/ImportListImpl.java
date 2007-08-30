/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.codegen ;

import java.util.Collections ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;
import com.sun.corba.se.spi.orbutil.generic.UnaryVoidFunction ;

import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.ImportList ;

public class ImportListImpl implements ImportList {
    private Map<String,Type> imports ;
    private Node root ;
    private List<Pair<String,String>> sortedImports ;

    public ImportListImpl() {
	imports = new HashMap<String,Type>() ;

	clearRoot() ;
    }

    public ImportList copy() {
	ImportListImpl result = new ImportListImpl() ;
	result.imports = new HashMap<String,Type>( imports ) ;
	return result ;
    }

    private void clearRoot() {
	root = null ;
	sortedImports = null ;
    }

    /** Add a fully-qualified type name to the imports.
     * Returns the Type for the name.
     */
    public Type addImport( final String name ) {
	final Type result = Type._class( name ) ;
	addImport( result ) ;
	return result ;
    }

    public void addImport( final Type type ) {
	final String key = type.className() ;
	if (!imports.entrySet().contains( key ) ) {
	    imports.put( key, type ) ;
	    clearRoot() ;
	}
    }

    /** Return whether or not this Type is in the imports.
     */
    public boolean contains( final String name ) {
	final Type type = Type._class( name ) ;
	return contains( type ) ;
    }

    public boolean contains( final Type type ) {
	final String key = type.className() ;
	final Type importType = imports.get( key ) ;
	if (importType == null)
	    return false ;

	return importType.equals( type ) ;
    }

    /** Lookup just the className, without package name.
     */
    public Type lookup( final String className ) {
	return imports.get( className ) ;
    }

    // A node is a node in a tree.  Each node contains either references to
    // other nodes (children()) or a reference to a Type (type()).
    // Every node has a name
    //
    // The name of a node is either the package name (for a list node)
    // or the class name (for a type node).
    private static abstract class Node implements Comparable<Node>{
	private final String name ;

	private Node( final String name ) {
	    this.name = name ;
	}

	public final String name() {
	    return name ;
	}

	public final int compareTo( final Node node ) {
	    return name.compareTo( node.name() ) ;
	}

	// Only one of type or children returns non-null
	public Type type() {
	    return null ;
	}

	public List<Node> children() {
	    return null ;
	}

	public void sort() {
	}

	public Node find( final String name ) {
	    return null ;
	}

	public void add( final Node node ) {
	}

	public void depthFirst( final UnaryVoidFunction<Node> fn ) {
	    fn.evaluate( this ) ;
	}

	public static Node makeTypeNode( final String name, final Type type ) {
	    return new Node( name ) {
		public Type type() {
		    return type ;
		}
	    } ;
	}

	public static Node makeListNode( final String name ) {
	    return new Node( name ) {
		final List<Node> children = new ArrayList<Node>() ;

		public List<Node> children() {
		    return children ;
		}

		public void sort() {
		    Collections.sort( children ) ;
		    for (Node node : children) {
			node.sort() ;
		    }
		}

		public Node find( String name ) {
		    for (Node n : children) {
			if (n.name().equals( name )) {
			    return n ;
			}
		    }

		    return null ;
		}

		public void add( final Node node ) {
		    children.add( node ) ;
		}

		public void depthFirst( final UnaryVoidFunction<Node> fn ) {
		    for (Node node : children) {
			node.depthFirst( fn ) ;
		    }
		}
	    } ;
	}
    }

    private void insertType( final Type type ) {
	final String packageName = type.packageName() ;
	final String[] packages = packageName.split( "[.]" ) ;
	final String className = type.className() ;

	// current is the List node onto which type is added.
	Node current = root ;
	for (String pkg : packages) {
	    if (current.children() == null)
		return ;

	    Node next = current.find( pkg ) ;
	    if (next == null) {
		next = Node.makeListNode( pkg ) ;
		current.add( next ) ;
	    }

	    current = next ;
	}

	// Current is the correct package, now add the type, if not
	// already present.
	Node classNode = current.find( className ) ;
	if (classNode == null) {
	    classNode = Node.makeTypeNode( className, type ) ;
	    current.add( classNode ) ;
	}
    }

    private void updateRoot() {
	if (root != null)
	    return ;

	root = Node.makeListNode( "" ) ;

	for (Type type : imports.values()) {
	    insertType( type ) ;
	}

	root.sort() ;
    }

    /** Return a list of imports as (packageName,className) pairs.
     * The list is sorted lexicographically.
     */
    public List<Pair<String,String>> getInOrderList() {
	if (sortedImports != null)
	    return sortedImports ;

	updateRoot() ;

	sortedImports = new ArrayList<Pair<String,String>>() ;

	UnaryVoidFunction<Node> fn = new UnaryVoidFunction<Node>() {
	    public void evaluate( Node node ) {
		Type type = node.type() ;
		if (type == null)
		    return ;

		Pair<String,String> pair =
		    new Pair<String,String>( type.packageName(), 
			type.className() ) ;

		sortedImports.add( pair ) ;
	    }
	} ;

	root.depthFirst( fn ) ;

	return sortedImports ;
    }
}
