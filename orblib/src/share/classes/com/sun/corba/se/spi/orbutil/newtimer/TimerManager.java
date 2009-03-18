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

import com.sun.corba.se.spi.orbutil.newtimer.TimerFactoryBuilder ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerEventController ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedObjectManager ;

/** Provides access to timer facilities.
 * This is intended to make it easy to set up timing,
 * either for performance tests, or for adaptive policy management.
 * Note that the constructor and the initialize method must be called
 * from the same thread in order to safely complete the initilization 
 * of an instance of this class.  After that, multiple threads may
 * access this class for the factory(), points(), and controller() 
 * methods.
 */
public class TimerManager<T> {
    private TimerFactory tf ;
    private T tp ;
    private volatile TimerEventController controller ;
    private boolean isInitialized = false ;

    /** Create a new TimerManager, with a TimerFactory registered under the given name
     * in the TimerFactoryBuilder, and a TimerEventController with the same name.
     */
    public TimerManager( ManagedObjectManager mom, String name ) {
	tf = TimerFactoryBuilder.make( mom, name, name ) ;
	controller = tf.makeController( name ) ;
    }

    public TimerManager( String name ) {
        this( null, name ) ;
    }

    /** Destroy this TimerManager by removing its TimerFactory from the
     * TimerFactoryBuilder.
     */
    public void destroy() {
	TimerFactoryBuilder.destroy( tf ) ;
    }

    // Implementation notes:
    // Performance is crucial here, especially for access to the controller.
    // In particular, enter/exit on a timer must NOT by default acquire any
    // global locks, as this would be likely to result in hot spots for
    // contention.
    //
    // The easiest way (ignoring volatile and AtomicXXX types) to achieve this
    // is simply to make controller() read only, since there is seldom a reason
    // to change the default controller.
    private void checkInitialized() {
	if (!isInitialized)
	    throw new IllegalStateException( "TimerManager is not initialized" ) ;
    }

    // Originally, initialize was synchronized, and this forced the other methods
    // to be synchronized as well, particularly points().  The points() method gets
    // called very frequently, roughly whenever an instance of any major class
    // needed during a request dispatch cycle is called.  Synchronizing on the points()
    // method results in a very bad lock hot spot which severly hampers ORB throughput
    // (Scott measured 15% in GF v2 build 49).
    public void initialize( T tp ) {
	if (isInitialized)
	    throw new IllegalStateException( "TimerManager is already initialized" ) ;

	this.tp = tp ;
	isInitialized = true ;
    }

    /** Get the timing point utility class of type T.
     */
    public T points() {
	// Must NOT be synchronized!
	checkInitialized() ;
	return tp ;
    }

    /** Get the TimerFactory.
     */
    public TimerFactory factory() {
	return tf ;
    }

    /** Return a TimerController.  
     * Returns null if called before initialize( T ).
     */
    public TimerEventController controller() {
	return controller ;
    }
}
