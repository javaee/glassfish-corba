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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.LinkedHashMap ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Collections ;

import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventControllerBase ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerGroup ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.NamedBase ;

// TimerFactory is a TimerGroup containing all timers and timer groups 
// that it creates
// 
// Synchronization turns out to be a bit tricky in this module.  The contains
// graph can contain cycles, so locking in recursive calls through 
// TimerGroupImpl instances could result in deadlocks.  We also don't 
// want a single global lock for everything, because Timer.isActivated()
// calls must be quick, and avoid introducing a lot of contention.
//
// The current solution is to use two locks: a global lock on TimerFactoryImpl,
// which is used for all enable/disable/TimerGroup contents changes.
// A lock will also be used in Timer to control access to the
// activation state of a Timer.
public class TimerFactoryImpl extends TimerGroupImpl implements TimerFactory {
    // The string<->int dictionary for timer names
    private Map<Controllable,Integer> conToInt ;
    private Map<Integer,Controllable> intToCon;

    // Next available index in dictionary
    private int nextIndex ;

    private Map<String,TimerImpl> timers ;
    private Map<String,TimerImpl> roTimers ;
    private Map<String,TimerGroupImpl> timerGroups ;
    private Map<String,TimerGroupImpl> roTimerGroups ;
    private Map<String,TimerEventHandler> timerEventHandlers ;
    private Map<String,TimerEventControllerBase> timerEventControllers ;

    public TimerFactoryImpl( String name, String description ) {
	super( 0, null, name, description ) ;
	setFactory( this ) ;
	add( this ) ; // The TimerFactory is a group containing all 
		      // Timers and TimerGroups it creates, so it
		      // should contain itself.
	conToInt = new HashMap<Controllable,Integer>() ;
	intToCon = new HashMap<Integer,Controllable>() ;
	nextIndex = 0 ;
	mapId( this ) ;

	timers = new LinkedHashMap<String,TimerImpl>() ;
	roTimers = Collections.unmodifiableMap( timers ) ;

	timerGroups = new LinkedHashMap<String,TimerGroupImpl>() ;
	timerGroups.put( name(), this ) ;
	roTimerGroups = Collections.unmodifiableMap( timerGroups ) ;

	timerEventHandlers = new HashMap<String,TimerEventHandler>() ;
	timerEventControllers = new HashMap<String,TimerEventControllerBase>() ;
    }

    // con must be a Controllable with an id already set (which is
    // forced by the ControllableBase constructor).  Update both
    // con <-> int maps, and increment nextIndex.  Must be called
    // after any Controllable is constructed.
    private void mapId( Controllable con ) {
	conToInt.put( con, con.id() ) ;
	intToCon.put( con.id(), con ) ;
	nextIndex++ ;
    }
    
    // Check that the name and description are valid, and that
    // the name is not already in use for a Controllable.
    private void checkArgs( Set<String> inUse, String name, 
	String description ) {

	if (name == null)
	    throw new IllegalArgumentException( "name must not be null" ) ;
	if (description == null)
	    throw new IllegalArgumentException( 
		"description must not be null" ) ;
	if (inUse.contains(name)) 
	    throw new IllegalArgumentException( name + " is already used." ) ;
    }

    public synchronized int numberOfIds() {
	return nextIndex ;
    }

    public synchronized Controllable getControllable( int id ) {
	if ((id >= 0) && (id < nextIndex))
	    return intToCon.get( id ) ;
	throw new IllegalArgumentException( "Argument " + id 
	    + " must be between 0 and " + (nextIndex - 1)) ;
    }

    private static class TracingEventHandler 
	extends NamedBase 
	implements TimerEventHandler {

	public TracingEventHandler( TimerFactory factory, String name ) {
	    super( factory, name ) ;
	}

	public void notify( TimerEvent event ) {
	    // Note that we CANNOT use ORBUtility.dprint here, 
	    // because ORBUtility drags in much of the ORB, and 
	    // TimerFactoryImpl MUST be buildable in the
	    // ORB library.
	    //
	    // XXX When we clean up dprint, make sure that the dprint facility
	    // is available in the ORB library.
	    System.out.println( Thread.currentThread().getName() 
		+ " TRACE " + event ) ;
	}
    }

    public synchronized TimerEventHandler makeTracingEventHandler( 
	String name ) {

	if (timerEventHandlers.keySet().contains( name ))
	    throw new IllegalArgumentException( "Name " + name 
		+ " is already in use." ) ;
	TimerEventHandler result = new TracingEventHandler( factory(), name ) ;
	timerEventHandlers.put( name, result ) ;
	return result ;
    }

    public synchronized LogEventHandler makeLogEventHandler( String name ) {
	if (timerEventHandlers.keySet().contains( name ))
	    throw new IllegalArgumentException( "Name " + name 
		+ " is already in use." ) ;
	LogEventHandler result = new LogEventHandlerImpl( factory(), name ) ;
	timerEventHandlers.put( name, result ) ;
	return result ;
    }

    public synchronized StatsEventHandler makeStatsEventHandler( String name ) {
	if (timerEventHandlers.keySet().contains( name ))
	    throw new IllegalArgumentException( "Name " + name 
		+ " is already in use." ) ;
	StatsEventHandler result = new StatsEventHandlerImpl( factory(), 
	    name ) ;
	timerEventHandlers.put( name, result ) ;
	return result ;
    }

    public synchronized StatsEventHandler makeMultiThreadedStatsEventHandler( 
	String name ) {

	if (timerEventHandlers.keySet().contains( name ))
	    throw new IllegalArgumentException( "Name " + name 
		+ " is already in use." ) ;
	StatsEventHandler result = new MultiThreadedStatsEventHandlerImpl( 
	    factory(), name ) ;
	timerEventHandlers.put( name, result ) ;
	return result ;
    }

    public synchronized void removeTimerEventHandler( 
	TimerEventHandler handler ) {

	timerEventHandlers.remove( handler.name() ) ;
    }

    public synchronized Timer makeTimer( String name, String description ) {
	checkArgs( timers.keySet(), name, description ) ;

	TimerImpl result = new TimerImpl( nextIndex, this, name, description ) ;
	mapId( result ) ;
	timers.put( name, result ) ;
	add( result ) ;  // Remember, a TimerFactory is a TimerGroup 
			 // containing all Controllables that it creates!

	return result ;
    }

    public synchronized Map<String,TimerImpl> timers() {
	return roTimers ;
    }

    public synchronized TimerGroup makeTimerGroup( String name, 
	String description ) {

	checkArgs( timerGroups.keySet(), name, description ) ;

	TimerGroupImpl result = new TimerGroupImpl( nextIndex, this, name, 
	    description ) ;

	mapId( result ) ;
	timerGroups.put( result.name(), result ) ;
	add( result ) ;  // Remember, a TimerFactory is a TimerGroup 
			 // containing all
		         // Controllables that it creates!

	return result ;
    }

    public synchronized Map<String,TimerGroupImpl> timerGroups() {
	return roTimerGroups ;
    }

    public void saveTimerEventController( TimerEventControllerBase tec ) {
	if (timerEventControllers.keySet().contains( tec.name() ))
	    throw new IllegalArgumentException( "Name " + tec.name() 
		+ " is already in use." ) ;

	timerEventControllers.put( tec.name(), tec ) ;
    }

    public synchronized TimerEventController makeController( String name ) {
	TimerEventController result = new TimerEventController( this, name ) ;
	return result ;
    }

    public synchronized void removeController( 
	TimerEventControllerBase controller ) {

	timerEventControllers.remove( controller.name() ) ;
    }

    public synchronized Set<? extends Controllable> enabledSet() {
	Set<Controllable> result = new HashSet<Controllable>() ;

	for (Timer t : timers.values()) 
	    if (t.isEnabled()) 
		result.add( t ) ;

	for (TimerGroup tg : timerGroups.values()) 
	    if (tg.isEnabled())
		result.add( tg ) ;

	return result ;    
    }

    public synchronized Set<Timer> activeSet() {
	Set<Timer> result = new HashSet<Timer>() ;

	for (Timer t : timers.values()) 
	    if (t.isActivated()) 
		result.add( t ) ;

	return result ;    
    }

    void updateActivation() {
	// First, set all timers to their enabled state.
	// Enabled timers are always activated, but disabled
	// timers may be enabled later because of enabled
	// timer groups.
	for (TimerImpl timer : timers.values()) {
	    timer.setActivated( timer.isEnabled() ) ;
	}
	
	// Now, iterate over the tcContents of the TimerGroups.
	// Activate each Timer contained in the transitive closure
	// of an enabled TimerGroup.
	for (TimerGroupImpl tg : timerGroups.values()) {
	    if (tg.isEnabled()) {
		Set<ControllableBase> tcc = tg.tcContents() ;
		for (ControllableBase c : tcc) {
		    if (c instanceof Timer) {
			TimerImpl ti = TimerImpl.class.cast( c ) ;
			ti.setActivated( true ) ;
		    }
		}
	    }
	}
    }
}

