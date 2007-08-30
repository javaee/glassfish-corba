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

import java.util.concurrent.ConcurrentMap ;
import java.util.concurrent.ConcurrentHashMap ;

import java.util.concurrent.atomic.AtomicInteger ;

import java.util.logging.Logger ;

import com.sun.corba.se.spi.orbutil.transport.Connection ;
import com.sun.corba.se.spi.orbutil.transport.ConnectionFinder ;
import com.sun.corba.se.spi.orbutil.transport.ContactInfo ;
import com.sun.corba.se.spi.orbutil.transport.OutboundConnectionCache ;

import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueue;
import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueueFactory;

/** Manage connections that are initiated from this VM. Connections are managed 
 * by a get/release mechanism and cached by the ContactInfo used to create them.
 * For efficiency, multiple connections (referred to as parallel connections)
 * may be created using the same ContactInfo.  Connections are reclaimed when 
 * they are no longer in use and there are too many connections open.
 * <P>
 * A connection is obtained through the get method, and released back to the 
 * cache through the release method.  Note that a connection that is released 
 * may still be expecting a response, in which case the connection is NOT 
 * eligible for reclamation.  If a connection is released to the cache while 
 * expecting a response, the connection must me made available for reclamation 
 * by calling responseReceived.
 *
 * XXX Should a get/release cycle expect at most one response?  
 * Should it support more than one response?  
 * Are there cases where we don't know in advance how many responses
 * are expected?
 * <P>
 * A connection basically represents some sort of communication channel, but 
 * few requirements are placed on the connection.  Basically the ability to 
 * close a connection is required in order for reclamation to work.
 * <P> 
 * Also we need the ContactInfo as a factory for the Connection.
 *
 * @author Ken Cavanaugh
 */
public final class OutboundConnectionCacheImpl<C extends Connection> 
    extends ConnectionCacheNonBlockingBase<C> 
    implements OutboundConnectionCache<C> {
    
    private final int maxParallelConnections ;	// Maximum number of connections 
						// we will open to the same 
						// endpoint

    private ConcurrentMap<ContactInfo<C>,CacheEntry<C>> entryMap ;
    private ConcurrentMap<C,ConnectionState<C>> connectionMap ;

    public int maxParallelConnections() {
	return maxParallelConnections ;
    }

    protected String thisClassName() {
	return "OutboundConnectionCacheImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
	final ContactInfo<C> cinfo ;			// ContactInfo used to 
							// create this Connection
	final C connection ;				// Connection of the 
							// ConnectionState
	final CacheEntry<C> entry ;			// This Connection's 
							// CacheEntry

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
	ConcurrentQueue.Handle idleHandle ;	    // non-null iff connection 
						    // is not in use
	ConcurrentQueue.Handle busyHandle ;	    // non-null iff connection 
						    // is in use

	ConnectionState( final ContactInfo<C> cinfo, final CacheEntry<C> entry, 
	    final C conn ) {

	    this.cinfo = cinfo ;
	    this.connection = conn ;
	    this.entry = entry ;

	    busyCount = new AtomicInteger() ;
	    expectedResponseCount = new AtomicInteger() ;
	    reclaimableHandle = null ;
	    idleHandle = null ;
	    busyHandle = null ;
	}
    }

    // Represents an entry in the outbound connection cache.  
    // This version handles normal shareable ContactInfo 
    // (we also need to handle no share).
    private static final class CacheEntry<C extends Connection> {
	final ConcurrentQueue<C> idleConnections =
	    ConcurrentQueueFactory.<C>makeBlockingConcurrentQueue() ;

	final ConcurrentQueue<C> busyConnections =
	    ConcurrentQueueFactory.<C>makeBlockingConcurrentQueue() ;

	public int totalConnections() {
	    return idleConnections.size() + busyConnections.size() ;
	}
    }

    public OutboundConnectionCacheImpl( final String cacheType, 
	final int highWaterMark, final int numberToReclaim, 
	final int maxParallelConnections, Logger logger ) {

	super( cacheType, highWaterMark, numberToReclaim, logger ) ;
	this.maxParallelConnections = maxParallelConnections ;

	this.entryMap = 
	    new ConcurrentHashMap<ContactInfo<C>,CacheEntry<C>>() ;
	this.connectionMap = 
	    new ConcurrentHashMap<C,ConnectionState<C>>() ;

	if (debug()) {
	    dprint(".constructor completed: " + cacheType );
	}
    }

    // We do not need to define equals or hashCode for this class.

    public C get( final ContactInfo<C> cinfo,
	ConnectionFinder<C> finder ) throws IOException {

	return get( cinfo ) ;
    }

    public C get( final ContactInfo<C> cinfo ) throws IOException {
	final CacheEntry<C> entry = getEntry( cinfo ) ;
	C result = null ;

	final int totalConnections = totalBusy.get() + totalIdle.get() ;
	if (totalConnections >= highWaterMark())
	    reclaim() ;

	do {
	    entry.idleConnections.poll() ;
	    if (result == null) {
		if (canCreateNewConnection( entry )) { 
		    // If this throws an exception just let it
		    // propagate: let a higher layer handle a
		    // connection creation failure.
		    result = cinfo.createConnection() ; 

		    final ConnectionState<C> cs = new ConnectionState<C>( cinfo, 
			entry, result ) ;
		    connectionMap.put( result, cs ) ;
		    if (debug())
			dprint( ".get: created connection " + result ) ;

		    // Make sure this connection is busy: it is
		    // available to other get calls as soon as
		    // it is added to the busy queue.  For this reason we 
		    // must increment busyCount BEFORE we add the result 
		    // to the entry busy queue.
		    cs.busyCount.incrementAndGet() ;
		    entry.busyConnections.offer( result ) ;
		    totalBusy.incrementAndGet() ;
		} else { 
		    // use a busy connection, move to end of busyConnections

		    if (debug())
			dprint( ".get: re-using busy connection " + result ) ;

		    result = entry.busyConnections.poll() ;
		    if (result != null) {
			entry.busyConnections.offer( result ) ;
		    }
		}
	    } else { // got result from idlConnections; update queues and counts
		final ConnectionState<C> cs = connectionMap.get( result ) ;
		final ConcurrentQueue.Handle<C> handle = cs.reclaimableHandle ;
		if (handle != null) {
		    if (handle.remove()) {
			totalIdle.decrementAndGet() ;
			totalBusy.incrementAndGet() ;
			entry.busyConnections.offer( result ) ;
		    } else {
			// another thread reclaimed this connection: try again
			result = null ;
			
			if (debug())
			    dprint( ".get: using idle connection " + result ) ;
		    }   	
		}
	    }
	} while (result != null) ;

	return result ;
    }

    public void release( final C conn, final int numResponsesExpected ) {
	if (debug())
	    dprint( "->release: connection " + conn 
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
		    final ConcurrentQueue.Handle busyHandle = cs.busyHandle ;
		    final CacheEntry<C> entry = cs.entry ;
		    boolean wasOnBusy = false ;
		    if (busyHandle != null)
			wasOnBusy = busyHandle.remove() ;

		    if (wasOnBusy) {
			// At this point, it is possible that we have removed a 
			// busy connection from the busy queue, because the 
			// connection became busy again between the 
			// decrementAndGet call and the remove call.  But, now 
			// that the entry is NOT on the busy or idle queues 
			// (because a connection is 
			// never on both queues at the same time),
			// it cannot again change state.
		    
			if (cs.busyCount.get() > 0) {
			    if (debug()) 
				dprint( 
				    ".release: re-queuing busy connection " 
				    + conn ) ;

			    cs.busyHandle = entry.busyConnections.offer( conn ) ;
			} else {
			    // If the connection does not have waiters, put it on
			    // the global idle queue.
			    //
			    // This is probably unlikely here, because 
			    // release usually requires some response before 
			    // the connection is eligible for reclamation.
			    if (cs.expectedResponseCount.get() == 0) {
				if (debug())
				    dprint( ".release: "
					+ "queuing reclaimable connection "
					+ conn ) ;

				cs.reclaimableHandle = 
				    reclaimableConnections.offer( conn ) ;
				totalBusy.decrementAndGet() ;
			    }

			    if (debug())
				dprint( ".release: queuing idle connection "
				    + conn ) ;

			    cs.idleHandle = entry.idleConnections.offer( conn ) ;
			}
		    }
		}
	    }
	} finally {
	    if (debug())
		dprint( "<-release" ) ;
	}
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    public void responseReceived( final C conn ) {
	final ConnectionState<C> cs = connectionMap.get( conn ) ;
	final ConcurrentQueue.Handle<C> idleHandle = cs.idleHandle ;
	final CacheEntry<C> entry = cs.entry ;
	final int waitCount = cs.expectedResponseCount.decrementAndGet() ;
	if (waitCount == 0) {
	    boolean wasOnIdle = false ;
	    if (cs != null)
		wasOnIdle = cs.idleHandle.remove() ;

	    if (wasOnIdle)
		cs.reclaimableHandle = reclaimableConnections.offer( conn ) ;
	}
    }

    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    public void close( final C conn ) {
	final ConnectionState<C> cs = connectionMap.remove( conn ) ;
	final CacheEntry<C> entry = entryMap.remove( cs.cinfo ) ;

	final ConcurrentQueue.Handle rh = cs.reclaimableHandle ;
	if (rh != null)
	    rh.remove() ;

	final ConcurrentQueue.Handle bh = cs.busyHandle ;
	if (bh != null)
	    bh.remove() ;

	final ConcurrentQueue.Handle ih = cs.idleHandle ;
	if (ih != null)
	    ih.remove() ;

	try { 
	    conn.close() ;
	} catch (IOException exc) {
	    if (debug())
		dprint( ".close: " + conn + " close threw " + exc ) ;
	}
    }

    // Atomically either get the entry for ContactInfo OR 
    // create a new one AND put it in the cache
    private CacheEntry<C> getEntry( ContactInfo<C> cinfo ) {
	// This should be the only place a CacheEntry is constructed.
	CacheEntry<C> entry = new CacheEntry() ;
	CacheEntry<C> result = entryMap.putIfAbsent( cinfo, entry ) ;
	if (result != null)
	    return result ;
	else
	    return entry ;
    }

    // Return true iff the configuration and the current entry support 
    // creating another connection.  Note that it must ALWAYS be 
    // legal to create a new connection if there is currently no connection.
    private boolean canCreateNewConnection( final CacheEntry<C> entry ) {
	final int totalConnections = totalBusy.get() + totalIdle.get() ;
	final int totalConnectionsInEntry = entry.totalConnections() ;
	return (totalConnectionsInEntry == 0) || 
	    ((totalConnections < highWaterMark()) && 
	    (totalConnectionsInEntry < maxParallelConnections)) ;
    }

    public boolean canCreateNewConnection( final ContactInfo<C> cinfo ) {
	final CacheEntry<C> entry = entryMap.get( cinfo ) ;
	if (entry == null)
	    return true ;

	return canCreateNewConnection( entry ) ;
    }
}

// End of file.
