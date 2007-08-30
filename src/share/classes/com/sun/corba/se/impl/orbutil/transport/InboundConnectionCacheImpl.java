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

import java.util.logging.Logger ;

import java.io.IOException ;

import java.util.concurrent.ConcurrentMap ;
import java.util.concurrent.ConcurrentHashMap ;

import java.util.concurrent.atomic.AtomicInteger ;

import com.sun.corba.se.spi.orbutil.transport.Connection ;
import com.sun.corba.se.spi.orbutil.transport.ContactInfo ;
import com.sun.corba.se.spi.orbutil.transport.InboundConnectionCache ;

import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueueFactory;
import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueue;

/** Manage connections that are initiated from another VM. 
 * Connections are reclaimed when 
 * they are no longer in use and there are too many connections open.
 * <P>
 * A connection basically represents some sort of communication channel, but 
 * few requirements are placed on the connection.  Basically the ability to 
 * close a connection is required in order for reclamation to work.
 * <P> 
 *
 * @author Ken Cavanaugh
 */
public final class InboundConnectionCacheImpl<C extends Connection> 
    extends ConnectionCacheNonBlockingBase<C> 
    implements InboundConnectionCache<C> {
    
    private final ConcurrentMap<C,ConnectionState<C>> connectionMap ;

    protected String thisClassName() {
	return "InboundConnectionCacheImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
	final C connection ;				// Connection of the 
							// ConnectionState
	final AtomicInteger busyCount ;			// Number of calls to 
							// get without release
	final AtomicInteger expectedResponseCount ;	// Number of expected 
							// responses not yet 
							// received

	// At all times, a connection is either on the busy or idle queue in 
	// its ConnectionEntry, and so only the corresponding handle is 
	// non-null.  If idleHandle is non-null, reclaimableHandle may also 
	// be non-null if the Connection is also on the 
	// reclaimableConnections queue.
	ConcurrentQueue.Handle reclaimableHandle ;  // non-null iff connection 
						    // is not in use and has no
						    // outstanding requests

	ConnectionState( final C conn ) {
	    this.connection = conn ;

	    busyCount = new AtomicInteger() ;
	    expectedResponseCount = new AtomicInteger() ;
	    reclaimableHandle = null ;
	}
    }

    public InboundConnectionCacheImpl( final String cacheType, 
	final int highWaterMark, final int numberToReclaim, 
	Logger logger ) {

	super( cacheType, highWaterMark, numberToReclaim, logger ) ;

	this.connectionMap = 
	    new ConcurrentHashMap<C,ConnectionState<C>>() ;

	if (debug()) {
	    dprint(".constructor completed: " + getCacheType() );
	}
    }

    // We do not need to define equals or hashCode for this class.

    public void requestReceived( final C conn ) {
	ConnectionState<C> cs = getConnectionState( conn ) ;

	final int totalConnections = totalBusy.get() + totalIdle.get() ;
	if (totalConnections > highWaterMark())
	    reclaim() ;

	ConcurrentQueue.Handle<C> reclaimHandle = cs.reclaimableHandle ;
	if (reclaimHandle != null) 
	    reclaimHandle.remove() ;

	int count = cs.busyCount.getAndIncrement() ;
	if (count == 0) {
	    totalIdle.decrementAndGet() ;
	    totalBusy.incrementAndGet() ;
	}
    }

    public void requestProcessed( final C conn, 
	final int numResponsesExpected ) {

	if (debug())
	    dprint( "->requestProcessed: connection " + conn 
		+ " expecting " + numResponsesExpected + " responses" ) ;

	try {
	    final ConnectionState<C> cs = connectionMap.get( conn ) ;

	    if (cs == null) {
		if (debug())
		    dprint( ".release: connection " + conn + " was closed" ) ;

		return ; 
	    } else {
		int numResp = cs.expectedResponseCount.addAndGet( 
		    numResponsesExpected ) ;
		int numBusy = cs.busyCount.decrementAndGet() ;

		if (debug()) {
		    dprint( ".release: " + numResp + " responses expected" ) ;
		    dprint( ".release: " + numBusy + " responses expected" ) ;
		}

		if (numBusy == 0) {
		    totalBusy.decrementAndGet() ;
		    totalIdle.incrementAndGet() ;

		    if (numResp == 0) {
			if (debug())
			    dprint( ".release: "
				+ "queuing reclaimable connection "
				+ conn ) ;

			cs.reclaimableHandle = 
			    reclaimableConnections.offer( conn ) ;
		    }
		}
	    }
	} finally {
	    if (debug())
		dprint( "<-requestProcessed" ) ;
	}
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    public void responseSent( final C conn ) {
	final ConnectionState<C> cs = connectionMap.get( conn ) ;
	final int waitCount = cs.expectedResponseCount.decrementAndGet() ;
	if (waitCount == 0) {
	    cs.reclaimableHandle = reclaimableConnections.offer( conn ) ;
	}
    }

    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    public void close( final C conn ) {
	final ConnectionState<C> cs = connectionMap.remove( conn ) ;
	int count = cs.busyCount.get() ;
	if (count == 0)
	    totalIdle.decrementAndGet() ;
	else
	    totalBusy.decrementAndGet() ;

	final ConcurrentQueue.Handle rh = cs.reclaimableHandle ;
	if (rh != null)
	    rh.remove() ;

	try {
	    conn.close() ;
	} catch (IOException exc) {
	    if (debug())
		dprint( ".close: " + conn + " close threw " + exc ) ;
	}
    }

    // Atomically either get the ConnectionState for conn OR 
    // create a new one AND put it in the cache
    private ConnectionState<C> getConnectionState( C conn ) {
	// This should be the only place a ConnectionState is constructed.
	ConnectionState<C> cs = new ConnectionState( conn ) ;
	ConnectionState<C> result = connectionMap.putIfAbsent( conn, cs ) ;
	if (result != null) {
	    totalIdle.incrementAndGet() ;
	    return result ;
	} else {
	    return cs ;
	}
    }
}

// End of file.
