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

package com.sun.corba.se.spi.orbutil.generic;

import java.util.List ;
import java.util.LinkedList ;
import java.util.ArrayList ;
import java.util.EmptyStackException ;

/** A stack with additional operations that support recording
 * the current top of stack as a mark, and then later popping
 * all items pushed since the last mark call.
 */
public final class MarkStack<E> {
    private List<E> items ;
    // The int on the marks list points to the first element on items that is part of the mark.
    private List<Integer> marks ; 

    public MarkStack() {
	items = new ArrayList<E>() ;
	marks = new ArrayList<Integer>() ;
    }

    public E push( E elem ) {
	items.add( elem ) ;
	return elem ;
    }

    /** Return the top element of the stack and remove it from the stack.
     * @exception EmptyStackException is thrown if the stack is empty.
     * @exception IllegalStateException if an attempt is made to pop
     * past the top mark.
     */
    public E pop() {
	if (isEmpty())
	    throw new EmptyStackException() ;

	if (marks.size() > 0) {
	    int topMark = marks.get( marks.size() - 1 ) ;
	    if (topMark == items.size())
		throw new IllegalStateException( "Cannot pop item past top mark" ) ;
	}

	E result = items.remove( items.size() - 1 ) ;
	return result ;
    }

    /** Return true iff the stack is empty.
     */
    public boolean isEmpty() {
        if (marks.size() > 0) {
	    int topMark = marks.get( marks.size() - 1 ) ;
            return topMark == items.size() ;
        }

	return items.size() == 0 ;
    }

    /** Return the top element of the stack.  Does not change the stack.
     * @exception EmptyStackException is thrown if the stack is empty.
     */
    public E peek() {
	if (isEmpty())
	    throw new EmptyStackException() ;

	return items.get( items.size() - 1 ) ;	
    }

    /** Record the current position in the stack for a 
     * subsequent popMark call.  This allow marking the 
     * start of a related group of items on the stack
     * that can be popped together later by popMark.
     * Multiple mark calls are supported.
     */
    public void mark() {
	marks.add( items.size() ) ;
    }

    /** Return an ordered list of stack elements starting with
     * the element that was on top of the stack when mark was
     * called.  
     */
    public List<E> popMark() {
	// Use a LinkedList here because addFirst runs in
	// constant time.  The equivalent operation on an
	// ArrayList is linear in the list size.  Removing
	// items from the middle of an ArrayList is also
	// inefficient, because the tail must be copied.
	LinkedList<E> result = new LinkedList<E>() ;	
	int topMark = marks.remove( marks.size() - 1 ) ;
	while (items.size() > topMark) {
	    result.addFirst( items.remove( items.size() - 1 ) ) ;
	}
	return result ;
    }
}
