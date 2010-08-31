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
/** Java implementation of the Lock-Free FIFO queue in Ladan-Mozes and Shavit,
 * "An Optimistic Approach to Lock-Free FIFO Queues".
 */
package com.sun.corba.se.impl.orbutil.concurrent ;

import java.util.concurrent.atomic.AtomicReference ;

public class LMSQueue<V> {
    static private class Node<V> {
	private V value ;
	Node<V> next ;
	Node<V> prev ;

	public Node( V value ) {
	    this.value = value ;
	}

	public V getValue() {
	    return value ;
	}
    }

    private AtomicReference<Node<V>> head ;
    private AtomicReference<Node<V>> tail ;

    public final Node<V> dummyNode = new Node<V>( null ) ;

    public void enqueue( V val ) {
	if (val == null)
	    throw new IllegalArgumentException( "Cannot enqueue null value" ) ;

	Node<V> tl ;
	Node<V> nd = new Node<V>( val ) ;
	while (true) {
	    tl = tail.get() ;
	    nd.next = tl ;
	    if (tail.compareAndSet( tl, nd )) {
		tail.get().prev = nd ;
		break ;
	    }
	}
    }

    public V dequeue() {
	Node<V> tl ;
	Node<V> hd ;
	Node<V> firstNodePrev ;
	Node<V> ndDummy ;
	V val ;

	while (true) {						    //D04
	    hd = head.get() ;					    //D05
	    tl = tail.get() ;					    //D06
	    firstNodePrev = hd.prev ;				    //D07
	    val = hd.getValue() ;				    //D08
	    if (hd == head.get()) {				    //D09
		if (val != null) {	    			    //D10  
		    if (tl != hd) {				    //D11
			if (firstNodePrev == null) {		    //D12
			    fixList( tl, hd ) ;			    //D13
			    continue ;				    //D14
			}					    //D15
		    } else {					    //D16,D17
			ndDummy = new Node<V>( null ) ;		    //D18,D19
			ndDummy.next = tl ;			    //D20
			if (tail.compareAndSet( tl, ndDummy )) {    //D21
			    hd.prev = ndDummy ;			    //D22
			} else {				    //D23,D24
			    ndDummy = null ;			    //D25
			}					    //D26
			continue ;				    //D27
		    }						    //D28
		    if (head.compareAndSet( hd, firstNodePrev )) {  //D29
			hd = null ;				    //D30
			return val ;				    //D31
		    }						    //D32
		} else {					    //D33,D34
		    if (tail == head) {				    //D35
			return null ;				    //D36   
		    } else {					    //D37,D38
			if (firstNodePrev == null) {		    //D39
			    fixList( tl, hd ) ;			    //D40
			    continue ;				    //D41
			}					    //D42
			head.compareAndSet( hd, firstNodePrev ) ;   //D43
		    }
		}
	    }
	}
    }

    private void fixList( Node<V> tl, Node<V> hd ) {
	Node<V> curNode = tl ;
	Node<V> curNodeNext = null ;
	Node<V> nextNodePrev = null ;

	while ((hd == head.get()) && (curNode != head.get())) {
	    curNodeNext = curNode.next ;
	    if (curNodeNext == null) {
		return ;
	    }
	    nextNodePrev = curNodeNext.prev ;
	    if (nextNodePrev != curNode) {
		curNodeNext.prev = curNode ;
	    }
	    curNode = curNodeNext ;
	}
    }
}
