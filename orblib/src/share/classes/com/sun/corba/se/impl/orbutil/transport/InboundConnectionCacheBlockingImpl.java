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

package com.sun.corba.se.impl.orbutil.transport;

import java.io.IOException ;

import java.util.logging.Logger ;

import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.transport.Connection ;
import com.sun.corba.se.spi.orbutil.transport.InboundConnectionCache ;

import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueue;
import com.sun.corba.se.spi.orbutil.misc.MethodMonitor;
import com.sun.corba.se.spi.orbutil.misc.MethodMonitorFactory;

/** Manage connections that are initiated from another VM. 
 *
 * @author Ken Cavanaugh
 */
public final class InboundConnectionCacheBlockingImpl<C extends Connection> 
    extends ConnectionCacheBlockingBase<C> 
    implements InboundConnectionCache<C> {

    private static MethodMonitor mm = MethodMonitorFactory.dprintUtil(
        InboundConnectionCacheBlockingImpl.class ) ;

    private final Map<C,ConnectionState<C>> connectionMap ;

    protected String thisClassName() {
	return "InboundConnectionCacheBlockingImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
	final C connection ;		// Connection of the 
					// ConnectionState
	int busyCount ;			// Number of calls to 
					// get without release
	int expectedResponseCount ;	// Number of expected 
					// responses not yet 
					// received

	ConcurrentQueue.Handle reclaimableHandle ;  // non-null iff connection 
						    // is not in use and has no
						    // outstanding requests

	ConnectionState( final C conn ) {
	    this.connection = conn ;

	    busyCount = 0 ;
	    expectedResponseCount = 0 ;
	    reclaimableHandle = null ;
	}
    }

    public InboundConnectionCacheBlockingImpl( final String cacheType, 
	final int highWaterMark, final int numberToReclaim, final long ttl ) {

	super( cacheType, highWaterMark, numberToReclaim, mm, ttl ) ;

	this.connectionMap = new HashMap<C,ConnectionState<C>>() ;
    }

    // We do not need to define equals or hashCode for this class.

    public synchronized void requestReceived( final C conn ) {
        mm.enter( debug(), "requestReceived", conn ) ;

	try {
	    ConnectionState<C> cs = getConnectionState( conn ) ;

	    final int totalConnections = totalBusy + totalIdle ;
	    if (totalConnections > highWaterMark())
		reclaim() ;

	    ConcurrentQueue.Handle<C> reclaimHandle = cs.reclaimableHandle ;
	    if (reclaimHandle != null) {
		reclaimHandle.remove() ;
                mm.info( debug(), "removed from reclaimableQueue", conn ) ;
	    }

	    int count = cs.busyCount++ ;
	    if (count == 0) {
                mm.info(debug(), "moved from idle to busy", conn ) ;

		totalIdle-- ;
		totalBusy++ ;
	    }
	} finally {
            mm.exit( debug()) ;
	}
    }

    public synchronized void requestProcessed( final C conn, 
	final int numResponsesExpected ) {

        mm.enter( debug(), "requestProcessed", conn, numResponsesExpected ) ;
	try {
	    final ConnectionState<C> cs = connectionMap.get( conn ) ;

	    if (cs == null) {
                mm.info( debug(), "connection was closed") ;
		return ; 
	    } else {
		cs.expectedResponseCount += numResponsesExpected ;
		int numResp = cs.expectedResponseCount ;
		int numBusy = --cs.busyCount ;

                mm.info( debug(), "responses expected", numResp,
                    "connection busy count", numBusy ) ;

		if (numBusy == 0) {
		    totalBusy-- ;
		    totalIdle++ ;

		    if (numResp == 0) {
			mm.info( debug(), "queuing reclaimable connection", conn ) ;

			if ((totalBusy+totalIdle) > highWaterMark()) {
			    close( conn ) ;
			} else {
			    cs.reclaimableHandle = 
				reclaimableConnections.offer( conn ) ;
			}
		    }
		}
	    }
	} finally {
           mm.exit( debug() ) ;
	}
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    public synchronized void responseSent( final C conn ) {
        mm.enter( debug(), "responseSent", conn ) ;

	try {
	    final ConnectionState<C> cs = connectionMap.get( conn ) ;
	    final int waitCount = --cs.expectedResponseCount ;
	    if (waitCount == 0) {
                mm.info( debug(), "reclaimable connection", conn ) ;

		if ((totalBusy+totalIdle) > highWaterMark()) {
		    close( conn ) ;
		} else {
		    cs.reclaimableHandle = 
			reclaimableConnections.offer( conn ) ;
		}
	    } else {
                mm.info( debug(), "wait count", waitCount ) ;
	    }
	} finally {
            mm.exit( debug() ) ;
	}
    }

    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    public synchronized void close( final C conn ) {
        mm.enter( debug(), "close", conn ) ;
	
	try {
	    final ConnectionState<C> cs = connectionMap.remove( conn ) ;
            mm.info( debug(), "connection state", cs ) ;

	    int count = cs.busyCount ;

	    if (count == 0)
		totalIdle-- ;
	    else
		totalBusy-- ;

	    final ConcurrentQueue.Handle rh = cs.reclaimableHandle ;
	    if (rh != null) {
                mm.info( debug(), "connection was reclaimable") ;
		rh.remove() ;
	    }

	    try {
		conn.close() ;
	    } catch (IOException exc) {
                mm.info( debug(), "close threw", exc ) ;
	    }
	} finally {
            mm.exit( debug() ) ;
	}
    }

    // Atomically either get the ConnectionState for conn OR 
    // create a new one AND put it in the cache
    private ConnectionState<C> getConnectionState( C conn ) {
	// This should be the only place a CacheEntry is constructed.
        ConnectionState<C> result = connectionMap.get( conn ) ;
        if (result == null) {
            result = new ConnectionState( conn ) ;
            connectionMap.put( conn, result ) ;
            totalIdle++ ;
        }

        return result ;
    }
}

// End of file.
