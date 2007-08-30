/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1993-2007 Sun Microsystems, Inc. All rights reserved.
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
/*
 * @(#)TransientBindingIterator.java	1.36 99/07/16
 * 
 * Copyright 1993-1997 Sun Microsystems, Inc. 901 San Antonio Road, 
 * Palo Alto, California, 94303, U.S.A.  All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 * 
 * CopyrightVersion 1.2
 * 
 */

package com.sun.corba.se.impl.naming.pcosnaming;

// Import general CORBA classes
import org.omg.CORBA.SystemException;
import org.omg.CORBA.ORB;
import org.omg.CORBA.INTERNAL;

// Get org.omg.CosNaming Types
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.BindingTypeHolder;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;

// Get base implementation
import com.sun.corba.se.impl.naming.pcosnaming.NamingContextImpl;
import com.sun.corba.se.impl.naming.pcosnaming.InternalBindingValue;

import com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl;

// Get a hash table
import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Class TransientBindingIterator implements the abstract methods
 * defined by BindingIteratorImpl, to use with the TransientNamingContext
 * implementation of the NamingContextImpl. The TransientBindingIterator
 * implementation receives a hash table of InternalBindingValues, and uses
 * an Enumeration to iterate over the contents of the hash table.
 * @see BindingIteratorImpl
 * @see TransientNamingContext
 */
public class PersistentBindingIterator extends BindingIteratorImpl
{
    private POA biPOA;
    /**
     * Constructs a new PersistentBindingIterator object.
     * @param orb a org.omg.CORBA.ORB object.
     * @param aTable A hashtable containing InternalBindingValues which is
     * the content of the PersistentNamingContext.
     * @param java.lang.Exception a Java exception.
     * @exception Exception a Java exception thrown of the base class cannot
     * initialize.
   */
    public PersistentBindingIterator(org.omg.CORBA.ORB orb, Hashtable aTable,
        POA thePOA ) throws java.lang.Exception
    {
	super(orb);
	this.orb = orb;
	theHashtable = aTable;
	theEnumeration = this.theHashtable.keys();
	currentSize = this.theHashtable.size();
        biPOA = thePOA;
    }

    /**
   * Returns the next binding in the NamingContext. Uses the enumeration
   * object to determine if there are more bindings and if so, returns
   * the next binding from the InternalBindingValue.
   * @param b The Binding as an out parameter.
   * @return true if there were more bindings.
   */
    final public boolean NextOne(org.omg.CosNaming.BindingHolder b)
    {
	// If there are more elements get the next element
	boolean hasMore = theEnumeration.hasMoreElements();
	if (hasMore) {
            InternalBindingKey theBindingKey =
		 ((InternalBindingKey)theEnumeration.nextElement());
            InternalBindingValue theElement =
		(InternalBindingValue)theHashtable.get( theBindingKey );
	    NameComponent n = new NameComponent( theBindingKey.id, theBindingKey.kind ); 
	    NameComponent[] nlist = new NameComponent[1];
	    nlist[0] = n;
            BindingType theType = theElement.theBindingType;
	    
	    b.value =
	        new Binding( nlist, theType );	
	} else {
	    // Return empty but marshalable binding
	    b.value = new Binding(new NameComponent[0],BindingType.nobject);
	}
	return hasMore;
    }

    /**
   * Destroys this BindingIterator by disconnecting from the ORB
   * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA system exceptions.
   */
    final public void Destroy()
    {
        // Remove the object from the Active Object Map.
        try {
            byte[] objectId = biPOA.servant_to_id( this );
            if( objectId != null ) {
                biPOA.deactivate_object( objectId );
            }
        }
        catch( Exception e ) {
            throw new INTERNAL( "Exception in BindingIterator.Destroy " + e );
        }
    }

    /**
   * Returns the remaining number of elements in the iterator.
   * @return the remaining number of elements in the iterator.   
   */
    public final int RemainingElements() {
	return currentSize;
    }

    private int currentSize;
    private Hashtable theHashtable;
    private Enumeration theEnumeration;
    private org.omg.CORBA.ORB orb;
}
