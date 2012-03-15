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

package com.sun.corba.ee.impl.interceptors;

import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.orb.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.CORBA.Any;

/**
 * SlotTable is used internally by PICurrent to store the slot information.
 */
public class SlotTable {
    // The vector where all the slot data for the current thread is stored
    private Any[] theSlotData;

    // Required for instantiating Any object.
    private ORB orb;

    // The flag to check whether there are any updates in the current SlotTable.
    // The slots will be reset to null, only if this flag is set.
    private boolean dirtyFlag;

    /**
     * The constructor instantiates an Array of Any[] of size given by slotSize
     * parameter.
     */
    SlotTable( ORB orb, int slotSize ) {
        dirtyFlag = false;
        this.orb = orb;
        theSlotData = new Any[slotSize];
    }

    /**
     * This method sets the slot data at the given slot id (index).
     */
    public void set_slot( int id, Any data ) throws InvalidSlot
    {
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        dirtyFlag = true;
        theSlotData[id] = data;
    }

    /**
     * This method get the slot data for the given slot id (index).
     */
    public Any get_slot( int id ) throws InvalidSlot
    {
        // First check whether the slot is allocated
        // If not, raise the invalid slot exception
        if( id >= theSlotData.length ) {
            throw new InvalidSlot();
        }
        if( theSlotData[id] == null ) {
            theSlotData [id] = new AnyImpl(orb);
        }
        return theSlotData[ id ];
    }


    /**
     * This method resets all the slot data to null if dirtyFlag is set.
     */
    void resetSlots( ) {
        if( dirtyFlag == true ) {
            for( int i = 0; i < theSlotData.length; i++ ) {
                theSlotData[i] = null;
            }
        }
    }

    /**
     * This method returns the size of the allocated slots.
     */
    int getSize( ) {
        return theSlotData.length;
    }

}
    
