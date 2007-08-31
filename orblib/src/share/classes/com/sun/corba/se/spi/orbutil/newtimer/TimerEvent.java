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

/** Represents a particular observable event.  We are mostly interested
 * in measuring how long an operation takes.  An operation is typically
 * represented by a Timer, and then the timer is used to generate
 * a TimerEvent at the entry to and exit from the operation.
 * <p>
 * Note that this class can also be used as a base class, in case
 * there is a need to attach extra information to a TimerEvent.
 * <p> 
 * All access to this class is unsynchronized.  This class must be
 * used either as an immutable (no calls to update), or access
 * must be restricted to a single thread (as in an iterator).
 */
public class TimerEvent {
    private Timer timer ;
    private TimerEvent.TimerEventType etype ;
    private long time ;
    public String toString() {
	return "TimerEvent[" + etype + " " + timer.name() + "@" + time/1000 + "]" ;
    }

    public TimerEvent( TimerEvent te ) {
	this (te.timer(), te.type()) ;
	this.time = te.time() ;
    }

    /** Create a TimerEvent at the current time.
     */
    public TimerEvent( Timer timer,
	TimerEvent.TimerEventType etype ) {

	long time = System.nanoTime() ;
	internalSetData( timer, etype, time ) ;
    }

    /** Create a TimerEvent at the given time.
     */
    public TimerEvent( Timer timer,
	TimerEvent.TimerEventType etype, long time ) {

	internalSetData( timer, etype, time ) ;
    }

    /** Re-use the same TimerEvent instance with different
     * data.  Used to create flyweight instances for iteration
     * over a collection of TimerEvent instances.
     */
    public void update( Timer timer, 
	TimerEvent.TimerEventType etype, long time ) {

	internalSetData( timer, etype, time ) ;
    }

    private void internalSetData( Timer timer,
	TimerEvent.TimerEventType etype, long time ) {

	this.timer = timer ;
	this.etype = etype ;
	this.time = time ;
    }

    public void incrementTime( long update ) {
	time += update ;
    }

    /** The name of the Timer used to create this entry.
     */
    public Timer timer() {
	return timer ;
    }

    public enum TimerEventType { ENTER, EXIT }

    /** Type of event: ENTER for start of interval for a
     * Timer, EXIT for end of the interval.
     */
    public TimerEvent.TimerEventType type() {
	return etype ;
    }

    /** Time of event in nanoseconds since the TimerLog
     * was created or cleared.
     */
    public long time() {
	return time ;
    }
}
