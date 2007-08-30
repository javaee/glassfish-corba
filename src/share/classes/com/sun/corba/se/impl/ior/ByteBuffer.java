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


public class ByteBuffer {
    /**
     * The array buffer into which the components of the ByteBuffer are
     * stored. The capacity of the ByteBuffer is the length of this array buffer, 
     * and is at least large enough to contain all the ByteBuffer's elements.<p>
     *
     * Any array elements following the last element in the ByteBuffer are 0.
     */
    protected byte elementData[];

    /**
     * The number of valid components in this <tt>ByteBuffer</tt> object. 
     * Components <tt>elementData[0]</tt> through 
     * <tt>elementData[elementCount-1]</tt> are the actual items.
     *
     * @serial
     */
    protected int elementCount;

    /**
     * The amount by which the capacity of the ByteBuffer is automatically 
     * incremented when its size becomes greater than its capacity.  If 
     * the capacity increment is less than or equal to zero, the capacity
     * of the ByteBuffer is doubled each time it needs to grow.
     *
     * @serial
     */
    protected int capacityIncrement;

    /**
     * Constructs an empty ByteBuffer with the specified initial capacity and
     * capacity increment. 
     *
     * @param   initialCapacity     the initial capacity of the ByteBuffer.
     * @param   capacityIncrement   the amount by which the capacity is
     *                              increased when the ByteBuffer overflows.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public ByteBuffer(int initialCapacity, int capacityIncrement) {
	super();
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
	this.elementData = new byte[initialCapacity];
	this.capacityIncrement = capacityIncrement;
    }

    /**
     * Constructs an empty ByteBuffer with the specified initial capacity and 
     * with its capacity increment equal to zero.
     *
     * @param   initialCapacity   the initial capacity of the ByteBuffer.
     * @exception IllegalArgumentException if the specified initial capacity
     *               is negative
     */
    public ByteBuffer(int initialCapacity) {
	this(initialCapacity, 0);
    }

    /**
     * Constructs an empty ByteBuffer so that its internal data array 
     * has size <tt>10</tt> and its standard capacity increment is 
     * zero. 
     */
    public ByteBuffer() {
	this(200);
    }

    /**
     * Trims the capacity of this ByteBuffer to be the ByteBuffer's current 
     * size. If the capacity of this cector is larger than its current 
     * size, then the capacity is changed to equal the size by replacing 
     * its internal data array, kept in the field <tt>elementData</tt>, 
     * with a smaller one. An application can use this operation to 
     * minimize the storage of a ByteBuffer. 
     */
    public void trimToSize() {
	int oldCapacity = elementData.length;
	if (elementCount < oldCapacity) {
	    byte oldData[] = elementData;
	    elementData = new byte[elementCount];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * This implements the unsynchronized semantics of ensureCapacity.
     * Synchronized methods in this class can internally call this 
     * method for ensuring capacity without incurring the cost of an 
     * extra synchronization.
     *
     * @see java.util.ByteBuffer#ensureCapacity(int)
     */ 
    private void ensureCapacityHelper(int minCapacity) {
	int oldCapacity = elementData.length;
	if (minCapacity > oldCapacity) {
	    byte oldData[] = elementData;
	    int newCapacity = (capacityIncrement > 0) ?
		(oldCapacity + capacityIncrement) : (oldCapacity * 2);
    	    if (newCapacity < minCapacity) {
		newCapacity = minCapacity;
	    }
	    elementData = new byte[newCapacity];
	    System.arraycopy(oldData, 0, elementData, 0, elementCount);
	}
    }

    /**
     * Returns the current capacity of this ByteBuffer.
     *
     * @return  the current capacity (the length of its internal 
     *          data arary, kept in the field <tt>elementData</tt> 
     *          of this ByteBuffer.
     */
    public int capacity() {
	return elementData.length;
    }

    /**
     * Returns the number of components in this ByteBuffer.
     *
     * @return  the number of components in this ByteBuffer.
     */
    public int size() {
	return elementCount;
    }

    /**
     * Tests if this ByteBuffer has no components.
     *
     * @return  <code>true</code> if and only if this ByteBuffer has 
     *          no components, that is, its size is zero;
     *          <code>false</code> otherwise.
     */
    public boolean isEmpty() {
	return elementCount == 0;
    }

    public void append(byte value) 
    {
	ensureCapacityHelper(elementCount + 1);
	elementData[elementCount++] = value;
    }

    public void append( int value ) 
    {
	ensureCapacityHelper(elementCount + 4);
	doAppend( value ) ;
    }

    private void doAppend( int value )
    {
	int current = value ;
	for (int ctr=0; ctr<4; ctr++) {
	    elementData[elementCount+ctr] = (byte)(current & 255) ;
	    current = current >> 8 ;
	}
	elementCount += 4 ;
    }

    public void append( String value ) 
    {
	byte[] data = value.getBytes() ;
	ensureCapacityHelper( elementCount + data.length + 4 ) ;
	doAppend( data.length ) ;
	System.arraycopy( data, 0, elementData, elementCount, data.length ) ;
	elementCount += data.length ;
    }

    /**
     * Returns an array containing all of the elements in this ByteBuffer
     * in the correct order.
     *
     * @since 1.2
     */
    public byte[] toArray() {
	return elementData ;
    }
}
