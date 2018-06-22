/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.naming.cosnaming;

// Import general CORBA classes
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

// Get org.omg.CosNaming Types
import org.omg.CosNaming.Binding;
import org.omg.CosNaming.BindingType;
import org.omg.CosNaming.NameComponent;

// Get base implementation

// Get a hash table
import java.util.Map;
import java.util.Iterator;

/**
 * Class TransientBindingIterator implements the abstract methods
 * defined by BindingIteratorImpl, to use with the TransientNamingContext
 * implementation of the NamingContextImpl. The TransientBindingIterator
 * implementation receives a hash table of InternalBindingValues, and uses
 * an Enumeration to iterate over the contents of the hash table.
 * @see BindingIteratorImpl
 * @see TransientNamingContext
 */
public class TransientBindingIterator extends BindingIteratorImpl
{
    // There is only one POA used for both TransientNamingContext and
    // TransientBindingIteraor servants.
    private POA nsPOA;
    /**
     * Constructs a new TransientBindingIterator object.
     * @param orb a org.omg.CORBA.ORB object.
     * @param aTable A hashtable containing InternalBindingValues which is
     * the content of the TransientNamingContext.
     * @param thePOA the POA to use.
     * @throws java.lang.Exception a Java exception.
   */
    public TransientBindingIterator(ORB orb, 
        Map<InternalBindingKey,InternalBindingValue> aTable,
        POA thePOA )
        throws java.lang.Exception
    {
        super(orb);
        bindingMap = aTable;
        bindingIterator = aTable.values().iterator() ;
        currentSize = this.bindingMap.size();
        this.nsPOA = thePOA;
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
        boolean hasMore = bindingIterator.hasNext();
        if (hasMore) {
            b.value = bindingIterator.next().theBinding;
            currentSize--;
        } else {
            // Return empty but marshalable binding
            b.value = new Binding(new NameComponent[0],BindingType.nobject);
        }
        return hasMore;
    }

    /**
     * Destroys this BindingIterator by disconnecting from the ORB
     * @exception org.omg.CORBA.SystemException One of a fixed set of CORBA 
     * system exceptions.
     */
    final public void destroyImpl()
    {
        // Remove the object from the Active Object Map.
        try {
            byte[] objectId = nsPOA.servant_to_id( this );
            if( objectId != null ) {
                nsPOA.deactivate_object( objectId );
            }
        } 
        catch( Exception e ) {
            NamingUtils.errprint("BindingIterator.Destroy():caught exception:");
            NamingUtils.printException(e);
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
    private Map<InternalBindingKey,InternalBindingValue> bindingMap;
    private Iterator<InternalBindingValue> bindingIterator;
}
