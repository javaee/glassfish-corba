/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Set ;
import java.util.HashSet ;

/** This is an implementation of the Set interface that keeps track
 * of its version so that we can tell when it is modified.
 * Each time an element is added to or removed from the set, the
 * version is incremented.
 * This implementation is synchronized so that the version
 * is consistently updated whenever the set is modified.
 */
public class VersionedHashSet<E> extends HashSet<E> {
    private long version = 0 ;

    public synchronized long version() {
	return version ;
    }

    public VersionedHashSet() {
	super() ;
    }

    public VersionedHashSet( Collection<? extends E> c ) {
	super( c ) ;
    }

    public VersionedHashSet( int initialCapacity, float loadFactor ) {
	super( initialCapacity, loadFactor ) ;
    }

    public VersionedHashSet( int initialCapacity ) {
	super( initialCapacity ) ;
    }

    public synchronized boolean add( E e ) {
	boolean result = super.add( e ) ;
	if (result) 
	    version++ ;
	return result ;
    }

    public synchronized boolean remove( Object o ) {
	boolean result = super.remove( o ) ;
	if (result)
	    version++ ;
	return result ;
    }

    public Iterator<E> iterator() {
	final Iterator<E> state = super.iterator() ;

	return new Iterator<E>() {
	    public boolean hasNext() {
		return state.hasNext() ;
	    }

	    public E next() {
		return state.next() ;
	    }

	    public void remove() {
		synchronized (VersionedHashSet.this) {
		    state.remove() ;
		    version++ ;
		}
	    }
	} ;
    }
}

