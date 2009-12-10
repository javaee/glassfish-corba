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

package com.sun.corba.se.impl.orbutil.transport;

import java.io.IOException ;

import org.glassfish.gmbal.ManagedData ;

import com.sun.corba.se.spi.orbutil.misc.MethodMonitor ;
import com.sun.corba.se.spi.orbutil.misc.MethodMonitorFactory ;

import com.sun.corba.se.spi.orbutil.transport.Connection ;
import com.sun.corba.se.spi.orbutil.transport.ContactInfo ;

import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueue ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.ManagedAttribute ;

@ManagedData
public class OutboundConnectionState<C extends Connection> {
    private static MethodMonitor mm = MethodMonitorFactory.dprintUtil( 
        OutboundConnectionState.class ) ;

    private boolean debug() { return false ; }

// The real state of the connection
    private enum ConnectionStateValue { NEW, BUSY, IDLE }

    private ConnectionStateValue csv ;	// Indicates state of connection

    final ContactInfo<C> cinfo ;	// ContactInfo used to create this 
                                        // Connection
    @ManagedAttribute
    final C connection ;		// Connection of the ConnectionState
                                        //
    final OutboundCacheEntry<C> entry ;	// This Connection's OutboundCacheEntry

    private int busyCount ;	        // Number of calls to get without release
              
    int expectedResponseCount ;	        // Number of expected responses not yet 
                                        // received

    // At all times, a connection is either on the busy or idle queue in 
    // its ConnectionEntry.  If the connection is on the idle queue, 
    // reclaimableHandle may also be non-null if the Connection is also on 
    // the reclaimableConnections queue.
    ConcurrentQueue.Handle<C> reclaimableHandle ;   // non-null iff 
                                                    // connection is not 
                                                    // in use and has no
                                                    // outstanding requests

// State exposed as managed attributes
    @ManagedAttribute
    private synchronized ConnectionStateValue state() { return csv ; }

    @ManagedAttribute
    private synchronized ContactInfo<C> contactInfo() { return cinfo ; }

    @ManagedAttribute
    private synchronized C connection() { return connection ; }

    @ManagedAttribute
    private synchronized OutboundCacheEntry<C> cacheEntry() { return entry ; }
    
    @ManagedAttribute
    private synchronized int busyCount() { return busyCount ; }

    @ManagedAttribute
    private synchronized int expectedResponseCount() {
        return expectedResponseCount ;
    }

    @ManagedAttribute
    public synchronized boolean isReclaimable() {
        return reclaimableHandle != null ;
    }

    public OutboundConnectionState( final ContactInfo<C> cinfo, 
        final OutboundCacheEntry<C> entry, final C conn ) {

        this.csv = ConnectionStateValue.NEW ;
        this.cinfo = cinfo ;
        this.connection = conn ;
        this.entry = entry ;

        busyCount = 0 ;
        expectedResponseCount = 0 ;
        reclaimableHandle = null ;
    }

    public synchronized String toString() {
        return "ConnectionState[" + "cinfo=" + cinfo 
            + " connection=" + connection + " busyCount=" + busyCount 
            + " expectedResponseCount=" + expectedResponseCount 
            + "]" ;
    }

// Methods used in OutboundConnectionCacheBlockingImpl

    public synchronized boolean isBusy() { 
        return csv == ConnectionStateValue.BUSY ; 
    } 

    public synchronized boolean isIdle() { 
        return csv == ConnectionStateValue.IDLE ; 
    } 

    // Mark this connection as being busy, and increment 
    // busyCount.
    public synchronized void acquire() { 
        mm.enter( debug(), "reclaim" ) ;
        try {
            if (busyCount == 0) {
                entry.idleConnections.remove( connection ) ;
                removeFromReclaim() ;
                csv = ConnectionStateValue.BUSY ; 
            } else {
                // Remove from busy queue so we can add it 
                // back to LRU end later.
                entry.busyConnections.remove( connection ) ;
            }

            busyCount++ ;
            entry.busyConnections.offer( connection ) ;
        } finally {
            mm.exit( debug() ) ;
        }
    }

    public synchronized void setReclaimableHandle( 
        ConcurrentQueue.Handle<C> handle ) {
        reclaimableHandle = handle ;
    }

    // Decrement busyCount, and move to IDLE if busyCount is 0.
    // Returns total number of expected responses
    public synchronized int release( int numResponsesExpected ) {
        mm.enter( debug(), "release", numResponsesExpected ) ;
        try {
            expectedResponseCount += numResponsesExpected ;
            busyCount-- ;
            if (busyCount < 0) {
                mm.info( debug(), "ERROR: numBusy is <0!" ) ;
            } 

            if (busyCount == 0) {
                csv = ConnectionStateValue.IDLE ;
                boolean wasOnBusy = entry.busyConnections.remove( connection ) ;
                if (!wasOnBusy) {
                    mm.info( debug(), 
                        "connection not on busy queue, should have been" ) ;
                }
                entry.idleConnections.offer( connection ) ;
            }

            mm.info( debug(), "expectedResponseCount", expectedResponseCount,
                "busyCount", busyCount ) ;
        } finally {
            mm.exit( debug() ) ;
        }

        return expectedResponseCount ;
    }

    // Returns true iff the connection is idle and reclaimable
    public synchronized boolean responseReceived() {
        mm.enter( debug(), "responseReceived" ) ;
        boolean result = false ;
        try {
            --expectedResponseCount ;
            mm.info( debug(), "expectedResponseCount", expectedResponseCount ) ;

            if (expectedResponseCount < 0) {
                mm.info( debug(), "ERROR: expectedResponseCount<0!" ) ;
                expectedResponseCount = 0 ;
            }

            result = (expectedResponseCount == 0) && (busyCount == 0) ;
        } finally {
            mm.exit( debug(), result ) ;
        }

        return result ;
    }

    public synchronized void close() {
        mm.enter( debug(), "close" ) ;
        try {
            removeFromReclaim() ;

            if (csv == ConnectionStateValue.IDLE) {
                entry.idleConnections.remove( connection ) ;
            } else if (csv == ConnectionStateValue.BUSY) {
                entry.busyConnections.remove( connection ) ;
            }

            csv = ConnectionStateValue.NEW ;
            busyCount = 0 ;
            expectedResponseCount = 0  ;

            connection.close() ;
        } catch (IOException exc) {
            mm.info( debug(), "caught IO exception on close", exc ) ;
        } finally {
            mm.exit( debug() ) ;
        }
    }

    private void removeFromReclaim() {
        mm.enter( debug(), "removeFromReclaim" ) ;
        try {
            if (reclaimableHandle != null) {
                if (!reclaimableHandle.remove()) {
                    mm.info( debug(), cinfo, 
                        "result was not on reclaimable Q" ) ;
                }   	
                reclaimableHandle = null ;
            }
        } finally {
            mm.exit( debug() ) ;
        }
    }
}

