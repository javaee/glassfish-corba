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

import java.util.Map ;
import java.util.Set ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObject ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedOperation ;

/** Factory class for all Timer-related objects.
 * TimerFactory is also a TimerGroup of all timers and timer groups that it creates.
 */
@ManagedObject( description="The Factory used to create and managed all objects in the Timer framework" ) 
public interface TimerFactory extends TimerGroup {
    /** Returns the maximum id used by this TimerFactory for creating Controllables.
     * The value of con.id() for any Controllable created by this
     * TimerFactory always ranges from 0 inclusive to numberOfIds()
     * exclusive.
     */
    @ManagedAttribute( description="The total number of Controllabled IDs in use" ) 
    int numberOfIds() ;

    /** Returns the Controllable corresponding to id, for 
     * id in the range 0 (inclusive) to numberOfIds() (exclusive). 
     * @throws IndexOutOfBoundsException if id is not in range.
     */
    @ManagedOperation( description="Look up a Timer or TimerGroup by its ID" ) 
    Controllable getControllable( int id ) ;

    /** Create a new LogEventHandler.  All LogEventHandler names
     * must be unique within the same TimerFactory.
     */
    @ManagedOperation( description="Create a new LogEventHandler" )
    LogEventHandler makeLogEventHandler( String name ) ;

    @ManagedOperation( description="Create a new TracingEventHandler" )
    TimerEventHandler makeTracingEventHandler( String name ) ;

    /** Create a new StatsEventHandler.  A StatsEventHandler records 
     * running statistics for all enter/exit pairs until it is cleared,
     * at which point it starts over.  It will keep data separated for
     * each thread, combining information correctly from multiple threads.
     * All StatsEventHandler names
     * must be unique within the same TimerFactory.
     * This StatsEventHandler must be used from a single thread.
     */
    @ManagedOperation( description="Create a new StatsEventHandler" )
    StatsEventHandler makeStatsEventHandler( String name ) ;

    /** Create a new StatsEventHandler.  A StatsEventHandler records 
     * running statistics for all enter/exit pairs until it is cleared,
     * at which point it starts over.  It will keep data separated for
     * each thread, combining information correctly from multiple threads.
     * All StatsEventHandler names
     * must be unique within the same TimerFactory.
     * This StatsEventHandler is multi-thread safe.
     */
    @ManagedOperation( description="Create a new Multi-Threaded StatsEventHandler" )
    StatsEventHandler makeMultiThreadedStatsEventHandler( String name ) ;

    /** Remove the handler from this TimerFactory.  The handler
     * should not be used after this call.
     */
    @ManagedOperation( description="Remove the TimerEventHandler from this factory" )
    void removeTimerEventHandler( TimerEventHandler handler ) ;

    /** Create a new Timer.  Note that Timers cannot be
     * destroyed, other than by garbage collecting the TimerFactory
     * that created them.
     */
    @ManagedOperation( description="Create a new Timer" ) 
    Timer makeTimer( String name, String description )  ;

    /** Returns a read-only map from Timer names to Timers.
     */
    @ManagedAttribute( description="All timers contained in this factory" ) 
    Map<String,? extends Timer> timers() ;

    /** Create a new TimerGroup.  Note that TimerGroups cannot be
     * destroyed, other than by garbage collecting the TimerFactory
     * that created them.
     */
    @ManagedOperation( description="Create a new TimerGroup" ) 
    TimerGroup makeTimerGroup( String name, String description ) ;

    /** Returns a read-only map from TimerGroup names to TimerGroups.
     */
    @ManagedAttribute( description="All timers contained in this factory" ) 
    Map<String,? extends TimerGroup> timerGroups() ;

    /** Create a TimerController, which can create TimerEvents and
     * send them to registered TimerEventHandlers.
     */
    @ManagedOperation( description="Create a new TimerEventController" ) 
    TimerEventController makeController( String name ) ;

    /** Remove the controller from this factory.  The controller 
     * should not be used after this call.
     */
    @ManagedOperation( description="Remote the TimerEventController from this factory" ) 
    void removeController( TimerEventControllerBase controller ) ;

    /** Returns a read-only view of the set of enabled Controllables.
     * These have been explicitly enabled via a call to enable().
     */
    @ManagedAttribute( description="All explicitly enabled Timers and TimerGroups" ) 
    Set<? extends Controllable> enabledSet() ;

    /** Returns a read-only view of the set of Controllables that are 
     * currently active.  An enabled Timer is active.  All Controllables
     * contained in an active or enabled TimerGroup are active.
     */
    @ManagedAttribute( description="All activated Timers" ) 
    Set<Timer> activeSet() ;
}

