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

package com.sun.corba.se.impl.interceptors;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

import com.sun.corba.se.impl.corba.AnyImpl;

import com.sun.corba.se.impl.logging.InterceptorsSystemException;

import com.sun.corba.se.spi.orb.ORB;

/**
 * SlotTableStack is the container of SlotTable instances for each thread
 */
public class SlotTableStack
{
    // SlotTablePool is the container for reusable SlotTables'
    private class SlotTablePool {

        // Contains a list of reusable SlotTable
        private SlotTable[] pool;

        // High water mark for the pool
        // If the pool size reaches this limit then putSlotTable will
        // not put SlotTable to the pool.
        private final int  HIGH_WATER_MARK = 5;

        // currentIndex points to the last SlotTable in the list
        private int currentIndex;

        SlotTablePool( ) {
            pool = new SlotTable[HIGH_WATER_MARK];
            currentIndex = 0;
        }

        /**
         * Puts SlotTable to the re-usable pool.
         */
        void putSlotTable( SlotTable table ) {
            // If there are enough SlotTables in the pool, then don't add
            // this table to the pool.
            if( currentIndex >= HIGH_WATER_MARK ) {
                // Let the garbage collector collect it.
                return;
            }
            pool[currentIndex] = table;
            currentIndex++;
        }

        /**
         * Gets SlotTable from the re-usable pool.
         */
        SlotTable getSlotTable( ) {
            // If there are no entries in the pool then return null
            if( currentIndex == 0 ) {
                return null;
            }
            // Works like a stack, Gets the last one added first
            currentIndex--;
            return pool[currentIndex];
        }
    }
   
    // Contains all the active SlotTables for each thread.
    // The List is made to behave like a stack.
    private java.util.List<SlotTable> tableContainer;

    // Keeps track of number of PICurrents in the stack.
    private int currentIndex;
 
    // For Every Thread there will be a pool of re-usable SlotTables'
    // stored in SlotTablePool
    private SlotTablePool tablePool;

    // The ORB associated with this slot table stack
    private ORB orb;

    private InterceptorsSystemException wrapper ;

    /**
     * Constructs the stack and and SlotTablePool
     */
    SlotTableStack( ORB orb, SlotTable table ) {
       this.orb = orb;
       wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_Interceptors() ;

       currentIndex = 0;
       tableContainer = new java.util.ArrayList<SlotTable>( );
       tablePool = new SlotTablePool( );
       // SlotTableStack will be created with one SlotTable on the stack.
       // This table is used as the reference to query for number of 
       // allocated slots to create other slottables.
       tableContainer.add( currentIndex, table );
       currentIndex++;
    }
   

    /**
     * pushSlotTable  pushes a fresh Slot Table on to the stack by doing the
     * following,
     * 1: Checks to see if there is any SlotTable in SlotTablePool
     *    If present then use that instance to push into the SlotTableStack
     *
     * 2: If there is no SlotTable in the pool, then creates a new one and 
     *    pushes that into the SlotTableStack
     */
    void pushSlotTable( ) {
        SlotTable table = tablePool.getSlotTable( );
        if( table == null ) {
            // get an existing PICurrent to get the slotSize
            SlotTable tableTemp = peekSlotTable();
            table = new SlotTable( orb, tableTemp.getSize( ));
        }
        // NOTE: Very important not to always "add" - otherwise a memory leak.
        if (currentIndex == tableContainer.size()) {
            // Add will cause the table to grow.
            tableContainer.add( currentIndex, table );
        } else if (currentIndex > tableContainer.size()) {
	    throw wrapper.slotTableInvariant( Integer.valueOf( currentIndex ),
		Integer.valueOf( tableContainer.size() ) ) ;
        } else {
            // Set will override unused slots.
            tableContainer.set( currentIndex, table );
        }
        currentIndex++;
    }

    /**
     * popSlotTable does the following
     * 1: pops the top SlotTable in the SlotTableStack
     *
     * 2: resets the slots in the SlotTable which resets the slotvalues to
     *    null if there are any previous sets. 
     *
     * 3: puts the reset SlotTable into the SlotTablePool to reuse 
     */
    void  popSlotTable( ) {
        if( currentIndex <= 1 ) {
            // Do not pop the SlotTable, If there is only one.
            // This should not happen, But an extra check for safety.
	    throw wrapper.cantPopOnlyPicurrent() ;
        }
        currentIndex--;
        SlotTable table = tableContainer.get( currentIndex );
        tableContainer.set( currentIndex, null ); // Do not leak memory.
        table.resetSlots( );
        tablePool.putSlotTable( table );
    }

    /**
     * peekSlotTable gets the top SlotTable from the SlotTableStack without
     * popping.
     */
    SlotTable peekSlotTable( ) {
       return tableContainer.get( currentIndex - 1);
    }

}
