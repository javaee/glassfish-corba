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
package com.sun.corba.se.spi.orbutil.generic ;

import java.util.List ;
import java.util.Collection ;
import java.util.Collections ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.ArrayList ;

public class Graph<E> {
    /** A Finder finds the immediate successors of an element of the graph.
     */
    public interface Finder<E> extends UnaryFunction<E,List<E>> {} 

    private Set<E> roots ;
    private List<E> preorderTraversal = null ;
    private List<E> postorderTraversal = null ;

    private void traverse( E node, Set<E> visited, Finder<E> finder ) {
	if (!visited.contains( node )) {
	    visited.add( node ) ;

	    preorderTraversal.add( node ) ;

	    for (E child : finder.evaluate(node)) {
		traverse( child, visited, finder ) ;
	    }

	    postorderTraversal.add( node ) ;
	}
    }

    private void init( Collection<E> roots, Finder<E> finder ) {
	this.roots = new HashSet<E>( roots ) ;
	this.roots = Collections.unmodifiableSet( this.roots ) ;
	this.preorderTraversal = new ArrayList<E>() ;
	this.postorderTraversal = new ArrayList<E>() ;
	Set<E> visited = new HashSet<E>() ;
	for (E node : this.roots) {
	    traverse( node, visited, finder ) ;
	}
	this.preorderTraversal = Collections.unmodifiableList( this.preorderTraversal ) ;
	this.postorderTraversal = Collections.unmodifiableList( this.postorderTraversal ) ;
    }

    public Graph( Collection<E> roots, Finder<E> finder ) {
	init( roots, finder ) ;
    }

    public Graph( E root, Finder<E> finder )   {
	Set<E> roots = new HashSet<E>() ;
	roots.add( root ) ;
	init( roots, finder ) ;
    }

    public Set<E> getRoots() {
	return roots ;
    }

    public List<E> getPreorderList() {
	return preorderTraversal ;
    }

    public List<E> getPostorderList() {
	return postorderTraversal ;
    }
}
