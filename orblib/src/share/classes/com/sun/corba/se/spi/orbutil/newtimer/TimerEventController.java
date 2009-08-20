/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.newtimer ;

import java.util.Set ;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.ManagedOperation ;
import org.glassfish.gmbal.Description ;

/** Creates timer events and sends them to all registered event
 * handlers.  Multiple controllers may be created from the
 * same TimerFactory.
 */
@ManagedObject
@Description( "Controls entering and exiting Timers" ) 
public class TimerEventController extends TimerEventControllerBase {

    public TimerEventController( TimerFactory factory, String name ) {
	super( factory, name ) ;
    }

    /** Generate a TimerEvent representing the entry to a 
     * particular timer.  This event is sent to all registered
     * TimerEventHandlers.  An event is only generated in
     * case timer.isActivated() is true.
     */
    @ManagedOperation
    @Description( "Enter a particular Timer" ) 
    public void enter( Timer timer ) {
	handle( timer, TimerEvent.TimerEventType.ENTER ) ;
    }

    /** Generate a TimerEvent representing the exit from a 
     * particular timer.  This event is sent to all registered
     * TimerEventHandlers.  An event is only generated in
     * case timer.isActivated() is true.
     */
    @ManagedOperation
    @Description( "Exit a particular Timer" ) 
    public void exit( Timer timer ) {
	handle( timer, TimerEvent.TimerEventType.EXIT ) ;
    }
   
    private void handle( Timer timer, TimerEvent.TimerEventType type ) {
	if (timer.isActivated()) {
	    TimerEvent te = new TimerEvent( timer, type ) ;
	    propagate( te ) ;
	}
    }
}
