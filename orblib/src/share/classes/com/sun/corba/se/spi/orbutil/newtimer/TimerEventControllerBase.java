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

import java.util.Collections ;
import java.util.Set ;
import java.util.HashSet ;

import com.sun.corba.se.impl.orbutil.newtimer.TimerFactoryImpl ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;

/** Supports registration of TimerEventHandlers.  A subclass of this class
 * must also provide some mechanism to create and propagate TimerEvents,
 * which may be subclasses of TimerEvent if needed.  A subclass typically
 * provides methods to indicate when enter and exit.  If additional data
 * is stored in the event, customer enter/exit methods can pass the
 * extra data to the extended event.
 */
public abstract class TimerEventControllerBase extends NamedBase {
    // XXX We will need to explore the efficiency and synchronization
    // here.  Should we use read/write locks around handlers?
    // 
    private Set<TimerEventHandler> handlers ;
    private Set<TimerEventHandler> roHandlers ;

    public TimerEventControllerBase( TimerFactory factory, String name ) {
	super( factory, name ) ;
	handlers = new HashSet<TimerEventHandler>() ;
	roHandlers = Collections.unmodifiableSet( handlers ) ;
	TimerFactoryImpl tfi = TimerFactoryImpl.class.cast( factory ) ;
	tfi.saveTimerEventController( this ) ;
    }

    /** Register the handler to start receiving events from this
     * controller.
     */
    @ManagedOperation( description="Register a TimerEventHandler with this TimerEventController" ) 
    public void register( TimerEventHandler handler ) {
	handlers.add( handler ) ;
    }	

    /** Deregister the handler to stop receiving events from this
     * controller.
     */
    @ManagedOperation( description="Deregister a TimerEventHandler from this TimerEventController" ) 
    public void deregister( TimerEventHandler handler ) {
	handlers.remove( handler ) ;
    }

    /** Read-only image of the set of Handlers.
     */
    @ManagedAttribute( description="TimerEventHandlers registered with this TimerEventController" ) 
    public Set<TimerEventHandler> handlers() {
	return roHandlers ;
    }

    /** Send the event to all registered handlers.
     */
    protected void propagate( TimerEvent ev ) {
	for (TimerEventHandler handler : handlers) {
	    handler.notify( ev ) ;
	}
    }
}

