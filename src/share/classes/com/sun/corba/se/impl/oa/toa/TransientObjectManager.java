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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.oa.toa;

import com.sun.corba.se.impl.misc.ORBUtility ;
import com.sun.corba.se.spi.orb.ORB ;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@com.sun.corba.se.spi.trace.TransientObjectManager
@ManagedData
@Description( "Maintains mapping from Object ID to servant")
public final class TransientObjectManager {
    private ORB orb ;
    private int maxSize = 128;
    private Element[] elementArray; 
    private Element freeList;

    @ManagedAttribute() 
    @Description( "The element array mapping indices into servants" ) 
    private synchronized Element[] getElements() {
        return elementArray.clone() ;
    }

    public TransientObjectManager( ORB orb )
    {
	this.orb = orb ;

        elementArray = new Element[maxSize];
        elementArray[maxSize-1] = new Element(maxSize-1,null);
        for ( int i=maxSize-2; i>=0; i-- ) 
            elementArray[i] = new Element(i,elementArray[i+1]);
        freeList = elementArray[0];
    }

    @com.sun.corba.se.spi.trace.TransientObjectManager
    public synchronized byte[] storeServant(java.lang.Object servant, java.lang.Object servantData)
    {
        if ( freeList == null ) 
            doubleSize();

        Element elem = freeList;
        freeList = (Element)freeList.servant;
        
        byte[] result = elem.getKey(servant, servantData);
	return result ;
    }

    @com.sun.corba.se.spi.trace.TransientObjectManager
    public synchronized java.lang.Object lookupServant(byte transientKey[]) 
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        int counter = ORBUtility.bytesToInt(transientKey,4);

        if (elementArray[index].counter == counter &&
            elementArray[index].valid ) {
            return elementArray[index].servant;
	}

        // servant not found 
        return null;
    }

    @com.sun.corba.se.spi.trace.TransientObjectManager
    public synchronized java.lang.Object lookupServantData(byte transientKey[])
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        int counter = ORBUtility.bytesToInt(transientKey,4);

        if (elementArray[index].counter == counter &&
            elementArray[index].valid ) {
            return elementArray[index].servantData;
	}

        // servant not found 
        return null;
    }

    @InfoMethod
    private void deleteAtIndex( int index ) { }

    @com.sun.corba.se.spi.trace.TransientObjectManager
    public synchronized void deleteServant(byte transientKey[])
    {
        int index = ORBUtility.bytesToInt(transientKey,0);
        deleteAtIndex(index);

        elementArray[index].delete(freeList);
        freeList = elementArray[index];
    }

    public synchronized byte[] getKey(java.lang.Object servant)
    {
        for ( int i=0; i<maxSize; i++ )
            if ( elementArray[i].valid && 
                 elementArray[i].servant == servant )
                return elementArray[i].toBytes();

        // if we come here Object does not exist
	return null;
    }

    private void doubleSize()
    {
        // Assume caller is synchronized

        Element old[] = elementArray;
        int oldSize = maxSize;
        maxSize *= 2;
        elementArray = new Element[maxSize];

        for ( int i=0; i<oldSize; i++ )
            elementArray[i] = old[i];    

        elementArray[maxSize-1] = new Element(maxSize-1,null);
        for ( int i=maxSize-2; i>=oldSize; i-- ) 
            elementArray[i] = new Element(i,elementArray[i+1]);
        freeList = elementArray[oldSize];
    }
}


@ManagedData
@Description( "A single element mapping one ObjectId to a Servant")
final class Element {
    java.lang.Object servant=null;     // also stores "next pointer" in free list
    java.lang.Object servantData=null;    
    int index=-1;
    int counter=0; 
    boolean valid=false; // valid=true if this Element contains
    // a valid servant

    @ManagedAttribute
    @Description( "The servant" )
    private synchronized Object getServant() {
        return servant ;
    }

    @ManagedAttribute
    @Description( "The servant data" )
    private synchronized Object getServantData() {
        return servantData ;
    }

    @ManagedAttribute
    @Description( "The reuse counter")
    private synchronized int getReuseCounter() {
        return counter ;
    }

    @ManagedAttribute
    @Description( "The index of this entry")
    private synchronized int getIndex() {
        return index ;
    }

    Element(int i, java.lang.Object next)
    {
        servant = next;
        index = i;
    }

    byte[] getKey(java.lang.Object servant, java.lang.Object servantData)
    {
        this.servant = servant;
        this.servantData = servantData;
        this.valid = true;

        return toBytes();
    }

    byte[] toBytes()
    {    
        // Convert the index+counter into an 8-byte (big-endian) key.

        byte key[] = new byte[8];
        ORBUtility.intToBytes(index, key, 0);
        ORBUtility.intToBytes(counter, key, 4);

        return key;
    }

    void delete(Element freeList)
    {
        if ( !valid )    // prevent double deletion
            return;
        counter++;
        servantData = null;
        valid = false;

        // add this to freeList
        servant = freeList;
    }

    @Override
    public String toString() 
    {
	return "Element[" + index + ", " + counter + "]" ;
    }
}

