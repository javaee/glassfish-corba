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

package com.sun.corba.se.impl.orbutil.graph ;

import java.util.Collection ;
import java.util.AbstractSet ;
import java.util.ArrayList;
import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

/**
 * Implementation of a simple graph.
 * @author ken
 * @param <T> The type of a Node in the graph, which must extend Node.
 */
public class GraphImpl<T extends Node<T>> extends AbstractSet<T> implements Graph<T>
{
    private Map<T,NodeData> nodeToData ;

    public GraphImpl()
    {
	nodeToData = new HashMap<T,NodeData>() ;
    }

    public GraphImpl( Collection<T> coll )
    {
	this() ;
	addAll( coll ) ;
    }
    
/***********************************************************************************/    
/************ AbstractSet implementation *******************************************/    
/***********************************************************************************/    

    // Required for AbstractSet
    @Override
    public boolean add( T obj ) // obj must be a Node
    {
	boolean found = nodeToData.keySet().contains( obj ) ;

	if (!found) {
	    NodeData nd = new NodeData() ;
	    nodeToData.put( obj, nd ) ;
	}

	return !found ;
    }

    // Required for AbstractSet
    public Iterator<T> iterator()
    {
	return nodeToData.keySet().iterator() ;
    }

    // Required for AbstractSet
    public int size()
    {
	return nodeToData.keySet().size() ;
    }

/***********************************************************************************/    

    public NodeData getNodeData( T node )
    {
	return nodeToData.get(node) ;
    }

    private void clearNodeData()
    {
	// Clear every node
        for (Map.Entry<T,NodeData> entry : nodeToData.entrySet() ) {
            entry.getValue().clear() ;
        }
    }

    interface NodeVisitor<T extends Node>
    {
	void visit( Graph<T> graph, T node, NodeData nd ) ;
    }

    // This visits every node in the graph exactly once.  A
    // visitor is allowed to modify the graph during the
    // traversal.
    void visitAll( NodeVisitor<T> nv )
    {
	boolean done = false ;

	// Repeat the traversal until every node has been visited.  Since
	// it takes one pass to determine whether or not each node has 
	// already been visited, this loop always runs at least once.
	do {
	    done = true ;

	    // Copy entries to array to avoid concurrent modification
	    // problem with iterator if the visitor is updating the graph.
	    Collection<Map.Entry<T,NodeData>> entries =
		new ArrayList<Map.Entry<T,NodeData>>( nodeToData.entrySet() ) ;

	    // Visit each node in the graph that has not already been visited.
	    // If any node is visited in this pass, we must run at least one more
	    // pass.
            for (Map.Entry<T,NodeData> current : entries) {
                T node = current.getKey() ;
                NodeData nd = current.getValue() ;

		if (!nd.isVisited()) {
		    nd.visited() ;
		    done = false ;

		    nv.visit( this, node, nd ) ;
		}
            }
	} while (!done) ;	
    }

    private void markNonRoots()
    {
	visitAll( 
	    new NodeVisitor<T>() {
		public void visit( Graph<T> graph, T node, NodeData nd ) {
                    for (T child : node.getChildren()) {
			// Make sure the child is in the graph so it can be
			// visited later if necessary.
			graph.add( child ) ;

			// Mark the child as a non-root, since a child is never a root.
			NodeData cnd = graph.getNodeData( child ) ;
			cnd.notRoot() ;
                    }
		}
	    } ) ;
    }

    private Set<T> collectRootSet()
    {
	final Set<T> result = new HashSet<T>() ;

        for (Map.Entry<T,NodeData> entry : nodeToData.entrySet()) {
	    T node = entry.getKey() ;
	    NodeData nd = entry.getValue() ;
	    if (nd.isRoot()) {
		result.add( node ) ;
            }
        }

	return result ;
    }

    public Set<T> getRoots()
    {
	clearNodeData() ;
	markNonRoots() ;
	return collectRootSet() ;
    }
}
