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

package com.sun.corba.ee.impl.interceptors;

import com.sun.corba.ee.spi.logging.InterceptorsSystemException;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * SlotTableStack is the container of SlotTable instances for each thread
 */
public class SlotTableStack
{
    private static final InterceptorsSystemException wrapper =
        InterceptorsSystemException.self ;

    // Contains all the active SlotTables for each thread.
    // The List is made to behave like a stack.
    private java.util.List<SlotTable> tableContainer;

    // Keeps track of number of PICurrents in the stack.
    private int currentIndex;
 
    // The ORB associated with this slot table stack
    private ORB orb;

    private PICurrent current ;

    /**
     * Constructs the stack.  This stack must always contain at least
     * one element so that peek never failes.
     */
    SlotTableStack( ORB orb, PICurrent current ) {
       this.current = current ;
       this.orb = orb;

       currentIndex = 0;
       tableContainer = new java.util.ArrayList<SlotTable>( );
       pushSlotTable() ;
    }

    /**
     * pushSlotTable  pushes a fresh Slot Table on to the stack by 
     * creating a new SlotTable and pushing that into the SlotTableStack.
     */
    void pushSlotTable( ) {
        SlotTable table = new SlotTable( orb, current.getTableSize() );
        
        // NOTE: Very important not to always "add" - otherwise a memory leak.
        if (currentIndex == tableContainer.size()) {
            // Add will cause the table to grow.
            tableContainer.add( currentIndex, table );
        } else if (currentIndex > tableContainer.size()) {
            throw wrapper.slotTableInvariant( currentIndex,
                tableContainer.size() ) ;
        } else {
            // Set will override unused slots.
            tableContainer.set( currentIndex, table );
        }
        currentIndex++;
    }

    /**
     * popSlotTable does the following
     * 1: pops the top SlotTable in the SlotTableStack (if there is more than one)
     *
     * 2: resets the slots in the SlotTable which resets the slotvalues to
     *    null if there are any previous sets. 
     */
    void  popSlotTable( ) {
        if(currentIndex == 1) {
            // Do not pop the SlotTable, If there is only one.
            // This should not happen, But an extra check for safety.
            throw wrapper.cantPopOnlyPicurrent() ;
        }
        currentIndex--;
        SlotTable table = tableContainer.get( currentIndex );
        tableContainer.set( currentIndex, null ); // Do not leak memory.
        table.resetSlots( );
    }

    /**
     * peekSlotTable gets the top SlotTable from the SlotTableStack without
     * popping.
     */
    SlotTable peekSlotTable( ) {
        SlotTable result = tableContainer.get( currentIndex - 1 ) ;
        if (result.getSize() != current.getTableSize()) {
            // stale table, so throw it away
            result = new SlotTable( orb, current.getTableSize() ) ;
            tableContainer.set( currentIndex - 1, result ) ;
        }

        return result ;
    }
}
