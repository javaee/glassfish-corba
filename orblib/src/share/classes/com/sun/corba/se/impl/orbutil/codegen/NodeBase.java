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

import com.sun.corba.se.impl.orbutil.codegen.Node ;

import com.sun.corba.se.spi.orbutil.copyobject.DefaultCopier ;
import com.sun.corba.se.spi.orbutil.copyobject.Copy ;
import com.sun.corba.se.spi.orbutil.copyobject.CopyType ;
import com.sun.corba.se.spi.orbutil.copyobject.CopyInterceptor ;

/** Base class for implementing various kinds of Nodes in the AST.
 * This mainly supports dynamic attributes.  It also supports
 * dynamic delegation to another node.  Dynamic attributes not
 * found in the current node will automatically be searched for
 * in the delegate.
 *
 * @author Ken Cavanaugh
 */
public class NodeBase extends AttributedObjectBase implements Node, CopyInterceptor {
    // Copying of tree nodes deserves some discussion here.
    // The basic issue is that we must always make sure that the
    // AST is really a tree, and not a DAG.  This is necessary
    // because each Node in the AST has dynamic attributes, and we
    // cannot allow the situation where a Visitor traversing the
    // tree would attempt to set conflicting dynamic attribute values
    // when visiting a Node from different paths.
    //
    // To avoid this problem, we make sure that every Node that is 
    // added to the AST as an ordinary java field is first copied
    // before it is added to the tree.  Being a lazy programmer,
    // I do not want to write clone methods or copy constructors on
    // every different subclass of Node in the AST representation.
    // Since we already have a reflective copier in the ORB, I want to
    // re-use that.  But it does not quite do the right thing.  In particular,
    // the default copier would traverse the parent of each Node, which means
    // that effectively every time we copy a node, we copy the entire AST,
    // leading to extreme memory consumption (perhaps quadratic or worse in AST size).
    //
    // To avoid this, we need some way of controlling exactly how AST nodes are 
    // copied.  The CopyInterceptor interface and Copy annotation provide a lot
    // of flexibility for the object copier.  Here we just need to make sure that the
    // parent is NOT traversed, so that the copy and the original just share
    // the same parent.
    @Copy(CopyType.IDENTITY) // parent is set to the same reference in the source and the copy
    private Node parent ;

    private static int nextId = 0 ;
    private int myId ;

    public void preCopy() {
    }

    public void postCopy() {
	// Get a new ID for the new node
	myId = getNewId() ;
    }

    private synchronized static int getNewId() {
	return nextId++ ;
    }

    public final <T extends Node> T getAncestor( Class<T> type ) {
    	Node current = this ;
	while (current != null && current.getClass() != type)
	    current = current.parent() ;

	if (current == null)
	    return null ;

	return type.cast( current ) ;
    }

    public NodeBase( Node parent ) {
        this.parent = parent ;
	myId = getNewId() ;
    }
    
    public int id() {
	return myId ;
    }

    public final Node parent() {
        return parent ;
    }

    public final void parent( Node node ) {
	this.parent = node ;
    }

    // May be overridden in subclass to control copy behavior.
    public <T extends Node> T copy( Class<T> cls ) {
	return cls.cast( DefaultCopier.copy(this) ) ;
    }

    // May be overridden in subclass to control copy behavior.
    public <T extends Node> T copy( Node newParent, Class<T> cls ) {
	T result = cls.cast( DefaultCopier.copy(this) ) ;
	result.parent( newParent ) ;
	return result ;
    }

    public String toString() {
	String cname = this.getClass().getName() ;
	final int lastDot = cname.lastIndexOf( '.' ) ;
	cname = cname.substring( lastDot+1 ) ;
	return cname + "@" + myId ;
    }

    // Usually overridden in subclass
    public void accept( Visitor visitor ) {
	visitor.visitNode( this ) ;
    }
}
