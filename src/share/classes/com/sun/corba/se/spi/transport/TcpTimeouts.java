/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package com.sun.corba.ee.spi.transport;

import com.sun.corba.ee.impl.transport.TcpTimeoutsImpl ;

/** This interface defines the ability to wait for a configurable time, 
 * applying an exponential backoff to increase the time.  The maximum
 * single wait time can be bounded, as well as the maximum total wait time.
 */
public interface TcpTimeouts {
    /** Return the initial time to wait on the first getTime or sleepTime
     * call on a new Waiter instance.
     */
    int get_initial_time_to_wait();

    /** Get the maximum total time a Waiter can exist before isExpired returns
     * true.  -1 if not used for this TcpTimeouts instances.
     */
    int get_max_time_to_wait();

    /** Get the maximum time a single sleepTime or getTime can taoke or return
     * in an instance of Waiter. -1 if not used.
     */
    int get_max_single_wait_time() ;

    /** Return the backoff factor, which is the percentage multiplier used
     * to compute the next timeout in the Waiter.advance method.
     */
    int get_backoff_factor();

    /** Interface used to represent a series of timeout values using 
     * exponential backoff.  Supports both a maximum total wait time
     * and a maximum single wait time.  
     * <p>
     * The total wait time starts at
     * 0 and is incremented by each call to getTimeForSleep or sleepTime.
     * Once the total wait time exceeds the maximum total wait time,
     * isExpired returns true.
     * <p>
     * The timer also has a current wait time, which is returned by getTime 
     * and is the interval for which sleep waits.  The initial value 
     * of the current wait time is get_initial_time_to_wait().
     * Each subsequent call to advance increases the current wait time by 
     * a factor of (previous*get_backoff_factor())/100, unless 
     * get_max_single_wait_time is configured and
     * the current wait time exceeds get_max_single_wait_time().  
     * If get_max_single_wait_time() is not used, the current time
     * increases without bound (until it overflows).  Once 
     * get_max_single_wait_time() is reached,
     * every subsequent call to next() returnes get_max_single_wait_time(), 
     * and advance has no effect.
     */
    public interface Waiter {
        /** Advance to the next timeout value.
         */
        void advance() ;

        /** Set the current timeout back to the initial
         * value.  Accumulated time is not affected.
         */
        void reset() ;

        /** Return the current timeout value.
         * Also increments total time.
         */
        int getTimeForSleep() ;

        /** Return the current timeout value,
         * but do not increment total wait time.
         */
        int getTime() ;

        /** Return the accumulated wait time.
         */
        int timeWaiting() ;

        /** Sleep for the current timeout value.
         * Returns true if sleep happened, otherwise false,
         * in the case where the Waiter has expired.
         */
        boolean sleepTime() ;

        /** Returns true if the waiter has expired.  It expires
         * once the total wait time exceeds get_max_wait_time.
         */
        boolean isExpired() ;
    }

    /** Return a Waiter that can be used for computing a series
     * of timeouts.  
     */
    Waiter waiter() ;

    /** Factory used to create TcpTimeouts instances.
     */
    public interface Factory {
        /** Create TcpTimeouts assuming that max_single_wait is 
         * unbounded.
         */
        TcpTimeouts create( int initial_time_to_wait,
            int max_time_to_wait, int backoff_value ) ;

        /** Create TcpTimeouts using all configuration parameters,
         * including a bound on the maximum single wait time.
         */
        TcpTimeouts create( int initial_time_to_wait,
            int max_time_to_wait, int backoff_value, int max_single_wait ) ;

        /** Create TcpTimeouts from a configuration string.  args must
         * be a : separated string, with 3 or 4 args, all of which are
         * positive decimal integers.  The integers are in the same
         * order as the arguments to the other create methods.
         */
        TcpTimeouts create( String args ) ;
    }

    Factory factory = new Factory() {
        public TcpTimeouts create( int initial_time_to_wait,
            int max_time_to_wait, int backoff_value ) {

            return new TcpTimeoutsImpl( initial_time_to_wait,
                max_time_to_wait, backoff_value ) ;
        }

        public TcpTimeouts create( int initial_time_to_wait,
            int max_time_to_wait, int backoff_value, int max_single_wait ) {

            return new TcpTimeoutsImpl( initial_time_to_wait,
                max_time_to_wait, backoff_value, max_single_wait ) ;
        }

        public TcpTimeouts create( String args ) {
            return new TcpTimeoutsImpl( args ) ;
        }
    } ;
}

// End of file.
