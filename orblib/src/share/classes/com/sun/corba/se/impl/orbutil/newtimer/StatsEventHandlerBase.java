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

import java.util.ArrayList ;
import java.util.Stack ;
import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.newtimer.StatisticsAccumulator ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.StatsEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.Statistics ;
import com.sun.corba.se.spi.orbutil.newtimer.NamedBase ;

public abstract class StatsEventHandlerBase extends NamedBase implements StatsEventHandler {
    protected static final String UNITS = "nanoseconds" ;
    
    // indexed by Timer.id()
    protected ArrayList<StatisticsAccumulator> saList ; 
    
    protected StatsEventHandlerBase( TimerFactory factory, String name ) {
	super( factory, name ) ;

	// Note that this implies that no timers or timergroups are created
	// after the StatsEventHandler is created.  We should probably fix this.
	int size = factory.numberOfIds() ;
	saList = new ArrayList<StatisticsAccumulator>( size ) ;
	for (int ctr=0; ctr<size; ctr++) {
	    saList.add( new StatisticsAccumulator(UNITS) ) ;
	}
    } 

    public void clear() {
	for (StatisticsAccumulator sa : saList) 
	    sa.clearState() ;
    }

    // Override this as required to record a duraction for an enter/exit
    // pair.  Called from notify().
    protected abstract void recordDuration( int id, long duration ) ;

    protected final void notify( Stack<TimerEvent> teStack, TimerEvent event ) {
	Timer timer = event.timer() ;
	int id = timer.id() ;

	if (event.type() == TimerEvent.TimerEventType.ENTER) {
	    // push this event onto the Timer stack
	    teStack.push( event ) ;
	} else {
	    // pop off the ENTER event, record duration
	    if (teStack.empty()) {
		throw new IllegalStateException( 
		    "Unexpected empty stack for EXIT event on timer " + timer ) ;
	    } else {
		TimerEvent enter = teStack.pop() ;
		if (!timer.equals( enter.timer() ))
		    throw new IllegalStateException(
			"Expected timer " + timer + " but found timer "
			    + enter.timer() + " on the TimerEvent stack" ) ;

		long duration = event.time() - enter.time() ;

		// Remove the contribution of nested calls from
		// the time for all outer calls.
		for (TimerEvent ev : teStack) {
		    ev.incrementTime( duration ) ;
		}

		recordDuration( id, duration ) ;
	    }
	}
    }

    public Map<Timer,Statistics> stats() {
	Map<Timer,Statistics> result = new HashMap<Timer,Statistics>() ;
	for (int ctr=0; ctr<saList.size(); ctr++) {
	    Controllable con = factory().getControllable( ctr ) ;

	    // ignore IDs of TimerGroups	
	    if (con instanceof Timer) {
		Timer timer = Timer.class.cast( con ) ;
		StatisticsAccumulator sa = saList.get(ctr) ; 
		result.put( timer, sa.getStats() ) ;
	    }
	}

	return result ;
    }
}
