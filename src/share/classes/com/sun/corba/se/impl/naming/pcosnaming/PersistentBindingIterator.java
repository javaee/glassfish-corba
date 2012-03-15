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

package com.sun.corba.ee.impl.naming.pcosnaming;

// Import general CORBA classes
import org.omg.CORBA.INTERNAL;

import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;
import org.omg.PortableServer.POA;

import com.sun.corba.ee.impl.naming.cosnaming.BindingIteratorImpl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
     * initialize.
     * @param thePOA the POA to use.
     * @throws java.lang.Exception can throw many exceptions?
     */
    public PersistentBindingIterator(org.omg.CORBA.ORB orb, 
        Map<InternalBindingKey,InternalBindingValue> aTable,
        POA thePOA ) throws java.lang.Exception
    {
        super(orb);
        map = new HashMap<InternalBindingKey,InternalBindingValue>( aTable ) ;
        iterator = this.map.keySet().iterator() ;
        currentSize = this.map.size();
        biPOA = thePOA;
    }

    /**
   * Returns the next binding in the NamingContext. Uses the enumeration
   * object to determine if there are more bindings and if so, returns
   * the next binding from the InternalBindingValue.
   * @param b The Binding as an out parameter.
   * @return true if there were more bindings.
   */
    final public boolean nextOneImpl(org.omg.CosNaming.BindingHolder b)
    {
        // If there are more elements get the next element
        boolean hasMore = iterator.hasNext();
        if (hasMore) {
            InternalBindingKey theBindingKey = iterator.next();
            InternalBindingValue theElement = map.get( theBindingKey );
            NameComponent n = new NameComponent( theBindingKey.id,
                theBindingKey.kind );
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
    final public void destroyImpl()
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
    public final int remainingElementsImpl() {
        return currentSize;
    }

    private int currentSize;
    private HashMap<InternalBindingKey,InternalBindingValue> map ;
    private Iterator<InternalBindingKey> iterator;
}
