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

package com.sun.corba.se.impl.naming.cosnaming;

// Import general CORBA classes
import org.omg.CORBA.ORB;

// Import org.omg.CosNaming classes
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingHolder;
import org.omg.CosNaming.BindingIteratorPOA;
import org.omg.CORBA.BAD_PARAM;

/**
 * Class BindingIteratorImpl implements the org.omg.CosNaming::BindingIterator
 * interface, but does not implement the method to retrieve the next
 * binding in the NamingContext for which it was created. This is left
 * to a subclass, which is why this class is abstract; BindingIteratorImpl
 * provides an implementation of the interface operations on top of two
 * subclass methods, allowing multiple implementations of iterators that
 * differ in storage and access to the contents of a NamingContext
 * implementation.
 * <p>
 * The operation next_one() is implemented by the subclass, whereas
 * next_n() is implemented on top of the next_one() implementation.
 * Destroy must also be implemented by the subclass.
 * <p>
 * A subclass must implement NextOne() and Destroy(); these
 * methods are invoked from synchronized methods and need therefore
 * not be synchronized themselves.
 */
public abstract class BindingIteratorImpl extends BindingIteratorPOA
{
    protected ORB orb ;

    /**
     * Create a binding iterator servant.
     * runs the super constructor.
     * @param orb an ORB object.
     * @exception java.lang.Exception a Java exception.   
     */
    public BindingIteratorImpl(ORB orb) 
        throws java.lang.Exception 
    {
	super();
	this.orb = orb ;
    }
  
    /**
     * Return the next binding. It also returns true or false, indicating
     * whether there were more bindings.
     * @param b The Binding as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see NextOne
     */
    public synchronized boolean next_one(org.omg.CosNaming.BindingHolder b)
    {
	// NextOne actually returns the next one
	return nextOneImpl(b);
    }
  
    /**
     * Return the next n bindings. It also returns true or false, indicating
     * whether there were more bindings.
     * @param how_many The number of requested bindings in the BindingList.
     * @param blh The BindingList as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    public synchronized boolean next_n(int how_many, 
        org.omg.CosNaming.BindingListHolder blh)
    {
        if( how_many == 0 ) {
            throw new BAD_PARAM( " 'how_many' parameter is set to 0 which is" +
            " invalid" );
        }  
        return list( how_many, blh );
    }

    /**
     * lists next n bindings. It returns true or false, indicating
     * whether there were more bindings. This method has the package private
     * scope, It will be called from NamingContext.list() operation or
     * this.next_n().
     * @param how_many The number of requested bindings in the BindingList.
     * @param blh The BindingList as an out parameter.
     * @return true if there were more bindings.
     */
    public boolean list( int how_many, org.omg.CosNaming.BindingListHolder blh) 
    {
	// Take the smallest of what's left and what's being asked for
	int numberToGet = Math.min(remainingElementsImpl(),how_many);
    
        // Create a resulting BindingList
	Binding[] bl = new Binding[numberToGet];
	BindingHolder bh = new BindingHolder();
	int i = 0;
	// Keep iterating as long as there are entries
	while (i < numberToGet && this.nextOneImpl(bh) == true) {
	    bl[i] = bh.value;
	    i++;
	}
	// Found any at all?
	if (i == 0) {
	    // No
	    blh.value = new Binding[0];
	    return false;
	}

	// Set into holder
	blh.value = bl;
    
	return true;
    }




    /**
     * Destroy this BindingIterator object. The object corresponding to this
     * object reference is destroyed.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     * @see Destroy
     */ 
    public synchronized void destroy()
    {
	// Destroy actually destroys
	this.destroyImpl();
    }

    /**
     * Abstract method for returning the next binding in the NamingContext
     * for which this BindingIterator was created.
     * @param b The Binding as an out parameter.
     * @return true if there were more bindings.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    protected abstract boolean nextOneImpl(org.omg.CosNaming.BindingHolder b);

    /**
     * Abstract method for destroying this BindingIterator.
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    protected abstract void destroyImpl();

    /**
     * Abstract method for returning the remaining number of elements.
     * @return the remaining number of elements in the iterator.
     */
    protected abstract int remainingElementsImpl();
}
