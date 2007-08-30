/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.ior ;

import java.util.Collection ;
import java.util.List ;
import java.util.AbstractList ;
import java.util.ListIterator ;
import java.util.Iterator ;

import com.sun.corba.se.spi.ior.MakeImmutable ;

/** Simple class that delegates all List operations to 
* another list.  It also can be frozen, which means that
* a number of operations can be performed on the list,
* and then the list can be made immutable, so that no
* further changes are possible.  A FreezableList is frozen
* using the makeImmutable method.
*/
public class FreezableList<E> extends AbstractList<E> {
    private List<E> delegate = null ;
    private boolean immutable = false ;

    public boolean equals( Object obj )
    {
	if (obj == null)
	    return false ;

	if (!(obj instanceof FreezableList))
	    return false ;

	FreezableList other = FreezableList.class.cast( obj ) ;

	return delegate.equals( other.delegate ) &&
	    (immutable == other.immutable) ;
    }

    public int hashCode()
    {
	return delegate.hashCode() ;
    }

    public FreezableList( List<E> delegate, boolean immutable  )
    {
	this.delegate = delegate ;
	this.immutable = immutable ;
    }

    public FreezableList( List<E> delegate )
    {
	this( delegate, false ) ;
    }

    public void makeImmutable()
    {
	immutable = true ;
    }

    public boolean isImmutable()
    {
	return immutable ;
    }

    public void makeElementsImmutable()
    {
	for (E x : this) {
	    if (x instanceof MakeImmutable) {
		MakeImmutable element = MakeImmutable.class.cast( x ) ;
		element.makeImmutable() ;
	    }
	}
    }

    // Methods overridden from AbstractList

    public int size()
    {
	return delegate.size() ;
    }

    public E get(int index)
    {
	return delegate.get(index) ;
    }

    public E set(int index, E element)
    {
	if (immutable)
	    throw new UnsupportedOperationException() ;

	return delegate.set(index, element) ;
    }

    public void add(int index, E element)
    {
	if (immutable)
	    throw new UnsupportedOperationException() ;

	delegate.add(index, element) ;
    }

    public E remove(int index)
    {
	if (immutable)
	    throw new UnsupportedOperationException() ;

	return delegate.remove(index) ;
    }

    // We also override subList so that the result is a FreezableList.
    public List<E> subList(int fromIndex, int toIndex)
    {
	List<E> list = delegate.subList(fromIndex, toIndex) ;
	List<E> result = new FreezableList<E>( list, immutable ) ;
	return result ;
    }
}
