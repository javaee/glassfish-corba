/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.timer ;

import java.util.List ;
import java.util.ArrayList ;

import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;

import com.sun.corba.se.spi.orbutil.newtimer.LogEventHandler ;
import com.sun.corba.se.spi.orbutil.newtimer.Timer ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEvent ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventController ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactoryBuilder ;

// import corba.framework.TimerUtils ;

public class LogEventHandlerSuite
{
    private static final int NUM_TIMERS = 15 ;
    private static final String tfName = "LEHTF" ;
    private static final String tfDescription = "The TimerFactorySuite TimerFactory" ;

    private TimerFactory tf ;
    private List<Timer> timers ;
    private TimerEventController controller ;

    // Timer calling order for 1 test cycle
    // Data format is (flag, timerIndex) where flag 0 is exit, 1 is enter
    // 0    called  1
    // 1    called  1
    // 2    called  5
    // 3    called  1
    // 4    called  1
    // 5    called  1
    // 6    called  0
    // 7    called  1
    // 8    called  1
    // 9    called  1
    // 10   called  1
    // 11   called  1
    // 12   called  2
    // 13   called  1
    // 14   called  1
    // total 19 calls
    private static int[][] timerCallData = {
	{ 1, 4 },
	    { 1, 13 },
	    { 0, 13 },
	    { 1, 5 },
		{ 1, 3 },
		{ 0, 3 },
		{ 1, 12 },
		    { 1, 14 },
			{ 1, 9 },
			    { 1, 8 },
				{ 1, 7 },
				    { 1, 2 },
					{ 1, 2 },
					    { 1, 2 },
					    { 0, 2 },
					{ 0, 2 },
				    { 0, 2 },
				    { 1, 2 },
					{ 1, 1 },
					    { 1, 0 },
					    { 0, 0 },
					{ 0, 1 },
				    { 0, 2 },
				    { 1, 2 },
				    { 0, 2 },
				{ 0, 7 },
			    { 0, 8 },
			{ 0, 9 },
		    { 0, 14 },
		{ 0, 12 },
	    { 0, 5 },
	    { 1, 12 },
		{ 1, 11 },
		    { 1, 10 },
		    { 0, 10 },
		{ 0, 11 },
	    { 0, 12 },
	{ 0, 4 },
    } ;

    private void checkLogEventHandler( LogEventHandler leh, int size ) {
	int ctr = 0 ;
	boolean done = false ;
	for (TimerEvent te : leh) {
	    Assert.assertFalse( done ) ;
	    if (ctr >= size)
		done = true ;
	    
	    int[] data = timerCallData[ctr] ;

	    if (data[0] == 0)
		 Assert.assertTrue(te.type() == TimerEvent.TimerEventType.EXIT) ;
	    else
		 Assert.assertTrue(te.type() == TimerEvent.TimerEventType.ENTER) ;
		   
	    Assert.assertTrue( te.timer().equals( timers.get(data[1]) ) ) ;
	    ctr++ ;
	}

	Assert.assertEquals( ctr, size ) ;
    }

    private void callTimers() {
	for (int[] op : timerCallData) {
	    int kind = op[0] ;
	    int timerIndex = op[1] ;
	    Timer timer = timers.get( timerIndex ) ;
	    if (kind == 1) {
		controller.enter( timer ) ;
	    } else {
		controller.exit( timer ) ;
	    }
	}
    }

    @Configuration( beforeTest = true )
    public void setUp() {
	timers = new ArrayList<Timer>() ;
	tf = TimerFactoryBuilder.make( tfName, tfDescription ) ;
	for (int ctr = 0; ctr<NUM_TIMERS; ctr++ ) {
	    Timer timer = tf.makeTimer( "t" + ctr, "Timer " + ctr ) ;
	    timers.add( timer ) ;
	}
	controller = tf.makeController( "Controller" ) ;
    }

    private void enableTimers() {
	for (Timer timer : timers) 
	    timer.enable() ;
    }

    private void disableTimers() {
	for (Timer timer : timers) 
	    timer.disable() ;
    }

    @Configuration( afterTest = true ) 
    public void tearDown() {
	TimerFactoryBuilder.destroy( tf ) ;
    }

    @Test() 
    public void singleThreadedTest() {
	LogEventHandler leh = tf.makeLogEventHandler( "STLEH" ) ;
	controller.register( leh ) ;
	enableTimers() ;
	callTimers() ;
	disableTimers() ;

	checkLogEventHandler( leh, timerCallData.length ) ; 
	leh.clear() ;
	checkLogEventHandler( leh, 0 ) ;
    }
}
