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

import com.sun.corba.se.spi.orb.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.impl.logging.OMGSystemException ;

/**
 * PICurrent is the implementation of Current as specified in the Portable
 * Interceptors Spec orbos/99-12-02.
 * IMPORTANT: PICurrent is implemented with the assumption that get_slot()
 * or set_slot() will not be called in ORBInitializer.pre_init() and 
 * post_init().
 */
public class PICurrent extends org.omg.CORBA.LocalObject
    implements Current
{
    // slotCounter is used to keep track of ORBInitInfo.allocate_slot_id()
    private int slotCounter;

    // The ORB associated with this PICurrent object.
    private ORB myORB;

    private OMGSystemException wrapper ;

    // True if the orb is still initialzing and get_slot and set_slot are not
    // to be called.
    private boolean orbInitializing;

    // ThreadLocal contains a stack of SlotTable which are used
    // for resolve_initial_references( "PICurrent" );
    private ThreadLocal<SlotTableStack> threadLocalSlotTable
        = new ThreadLocal<SlotTableStack>() {
            protected SlotTableStack initialValue( ) {
                return new SlotTableStack( myORB, PICurrent.this );
            }
        };

    /**
     * PICurrent constructor which will be called for every ORB 
     * initialization.
     */
    PICurrent( ORB myORB ) {
	this.myORB = myORB;
	wrapper = myORB.getLogWrapperTable().get_RPC_PROTOCOL_OMG() ;
	this.orbInitializing = true;
        slotCounter = 0;
    }

    synchronized int getTableSize() {
        return slotCounter ;
    }

    /**
     * This method will be called from ORBInitInfo.allocate_slot_id( ).
     * simply returns a slot id by incrementing slotCounter.
     */
    synchronized int allocateSlotId( ) {
        int slotId = slotCounter;
        slotCounter = slotCounter + 1;
        return slotId;
    }

    /**
     * This method gets the SlotTable which is on the top of the
     * ThreadLocalStack.
     */
    SlotTable getSlotTable( ) {
        SlotTable table = threadLocalSlotTable.get().peekSlotTable();
        return table;
    }

    /**
     * This method pushes a SlotTable on the SlotTableStack. When there is
     * a resolve_initial_references("PICurrent") after this call. The new
     * PICurrent will be returned.
     */
    void pushSlotTable( ) {
        SlotTableStack st = threadLocalSlotTable.get();
        st.pushSlotTable( );
    }


    /**
     * This method pops a SlotTable on the SlotTableStack.
     */
    void popSlotTable( ) {
        SlotTableStack st = threadLocalSlotTable.get();
        st.popSlotTable( );
    }

    /**
     * This method sets the slot data at the given slot id (index) in the
     * Slot Table which is on the top of the SlotTableStack.
     */
    public void set_slot( int id, Any data ) throws InvalidSlot 
    {
	if( orbInitializing ) {
	    // As per ptc/00-08-06 if the ORB is still initializing, disallow
	    // calls to get_slot and set_slot.  If an attempt is made to call,
	    // throw a BAD_INV_ORDER.
	    throw wrapper.invalidPiCall3() ;
	}

        getSlotTable().set_slot( id, data );
    }

    /**
     * This method gets the slot data at the given slot id (index) from the
     * Slot Table which is on the top of the SlotTableStack.
     */
    public Any get_slot( int id ) throws InvalidSlot 
    {
	if( orbInitializing ) {
	    // As per ptc/00-08-06 if the ORB is still initializing, disallow
	    // calls to get_slot and set_slot.  If an attempt is made to call,
	    // throw a BAD_INV_ORDER.
	    throw wrapper.invalidPiCall4() ;
	}

        return getSlotTable().get_slot( id );
    }

    /**
     * This method resets all the slot data to null in the  
     * Slot Table which is on the top of SlotTableStack.
     */
    void resetSlotTable( ) {
        getSlotTable().resetSlots();
    }

    /**
     * Called from ORB when the ORBInitializers are about to start 
     * initializing.
     */
    void setORBInitializing( boolean init ) {
	this.orbInitializing = init;
    }
}


    
