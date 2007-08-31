/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.graph ;

import java.util.Collection ;
import java.util.AbstractSet ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Set ;
import java.util.HashSet ;

public class GraphImpl extends AbstractSet implements Graph 
{
    private Map /* Map<Node,NodeData> */ nodeToData ;

    public GraphImpl()
    {
	nodeToData = new HashMap() ;
    }

    public GraphImpl( Collection coll )
    {
	this() ;
	addAll( coll ) ;
    }
    
/***********************************************************************************/    
/************ AbstractSet implementation *******************************************/    
/***********************************************************************************/    

    // Required for AbstractSet
    public boolean add( Object obj ) // obj must be a Node
    {
	if (!(obj instanceof Node))
	    throw new IllegalArgumentException( "Graphs must contain only Node instances" ) ;

	Node node = (Node)obj ;
	boolean found = nodeToData.keySet().contains( obj ) ;

	if (!found) {
	    NodeData nd = new NodeData() ;
	    nodeToData.put( node, nd ) ;
	}

	return !found ;
    }

    // Required for AbstractSet
    public Iterator iterator()
    {
	return nodeToData.keySet().iterator() ;
    }

    // Required for AbstractSet
    public int size()
    {
	return nodeToData.keySet().size() ;
    }

/***********************************************************************************/    

    public NodeData getNodeData( Node node ) 
    {
	return (NodeData)nodeToData.get( node ) ;
    }

    private void clearNodeData()
    {
	// Clear every node
	Iterator iter = nodeToData.entrySet().iterator() ;
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry)iter.next() ;
	    NodeData nd = (NodeData)(entry.getValue()) ;
	    nd.clear( ) ;
	}
    }

    interface NodeVisitor
    {
	void visit( Graph graph, Node node, NodeData nd ) ;
    }

    // This visits every node in the graph exactly once.  A
    // visitor is allowed to modify the graph during the
    // traversal.
    void visitAll( NodeVisitor nv )
    {
	boolean done = false ;

	// Repeat the traversal until every node has been visited.  Since
	// it takes one pass to determine whether or not each node has 
	// already been visited, this loop always runs at least once.
	do {
	    done = true ;

	    // Copy entries to array to avoid concurrent modification
	    // problem with iterator if the visitor is updating the graph.
	    Map.Entry[] entries = 
		(Map.Entry[])nodeToData.entrySet().toArray( new Map.Entry[0] ) ;

	    // Visit each node in the graph that has not already been visited.
	    // If any node is visited in this pass, we must run at least one more
	    // pass.
	    for (int ctr=0; ctr<entries.length; ctr++) {
		Map.Entry current = entries[ctr] ;
		Node node = (Node)current.getKey() ;
		NodeData nd = (NodeData)current.getValue() ;

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
	    new NodeVisitor() {
		public void visit( Graph graph, Node node, NodeData nd )
		{
		    Iterator iter = node.getChildren().iterator() ; // Iterator<Node>
		    while (iter.hasNext()) {
			Node child = (Node)iter.next() ;

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

    private Set collectRootSet()
    {
	final Set result = new HashSet() ;

	Iterator iter = nodeToData.entrySet().iterator() ;
	while (iter.hasNext()) {
	    Map.Entry entry = (Map.Entry)iter.next() ;
	    Node node = (Node)entry.getKey() ;
	    NodeData nd = (NodeData)entry.getValue() ;
	    if (nd.isRoot())
		result.add( node ) ;
	}

	return result ;
    }

    public Set /* Set<Node> */ getRoots() 
    {
	clearNodeData() ;
	markNonRoots() ;
	return collectRootSet() ;
    }
}
