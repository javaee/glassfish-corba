/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
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

import java.util.Map ;
import java.util.HashMap ;

import java.util.concurrent.locks.ReentrantLock ;

import com.sun.corba.se.spi.orbutil.transport.Connection ;
import com.sun.corba.se.spi.orbutil.transport.ConnectionFinder ;
import com.sun.corba.se.spi.orbutil.transport.ContactInfo ;
import com.sun.corba.se.spi.orbutil.transport.OutboundConnectionCache ;

import com.sun.corba.se.spi.orbutil.concurrent.ConcurrentQueueFactory;

import com.sun.corba.se.spi.orbutil.misc.MethodMonitor ;
import com.sun.corba.se.spi.orbutil.misc.MethodMonitorFactory ;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.ManagedAttribute ;

@ManagedObject
@Description( "Outbound connection cache for connections opened by the client" ) 
public final class OutboundConnectionCacheBlockingImpl<C extends Connection> 
    extends ConnectionCacheBlockingBase<C> 
    implements OutboundConnectionCache<C> {
   
    private static MethodMonitor mm = MethodMonitorFactory.dprintUtil( 
        OutboundConnectionCacheBlockingImpl.class ) ;

    private ReentrantLock lock = new ReentrantLock() ;

    // Configuration data
    // XXX we may want this data to be dynamically re-configurable
    private final int maxParallelConnections ;	// Maximum number of 
						// connections we will open 
						// to the same endpoint

    @ManagedAttribute
    public int maxParallelConnections() { return maxParallelConnections ; }
    
    private Map<ContactInfo<C>,OutboundCacheEntry<C>> entryMap ;

    @ManagedAttribute( id="cacheEntries" ) 
    private Map<ContactInfo<C>,OutboundCacheEntry<C>> entryMap() {
        return new HashMap<ContactInfo<C>,OutboundCacheEntry<C>>( entryMap ) ;
    }
    
    private Map<C,OutboundConnectionState<C>> connectionMap ;

    @ManagedAttribute( id="connections" ) 
    private Map<C,OutboundConnectionState<C>> connectionMap() {
        return new HashMap<C,OutboundConnectionState<C>>( connectionMap ) ;
    }

    protected String thisClassName() {
	return "OutboundConnectionCacheBlockingImpl" ;
    }

    public OutboundConnectionCacheBlockingImpl( final String cacheType, 
	final int highWaterMark, final int numberToReclaim, 
	final int maxParallelConnections, final long ttl ) {

	super( cacheType, highWaterMark, numberToReclaim, mm, ttl ) ;

        mm.enter( debug(), "<init>", cacheType, highWaterMark, 
            numberToReclaim, maxParallelConnections ) ;
            
	if (maxParallelConnections < 1) 
	    throw new IllegalArgumentException( 
		"maxParallelConnections must be > 0" ) ;

	this.maxParallelConnections = maxParallelConnections ;

	this.entryMap = 
            new HashMap<ContactInfo<C>,OutboundCacheEntry<C>>() ;
	this.connectionMap = new HashMap<C,OutboundConnectionState<C>>() ;
        this.reclaimableConnections = 
            ConcurrentQueueFactory.<C>makeConcurrentQueue( ttl ) ;

        mm.exit( debug() ) ;
    }

    public boolean canCreateNewConnection( ContactInfo<C> cinfo ) {
        lock.lock() ;
        try {
            OutboundCacheEntry<C> entry = entryMap.get( cinfo ) ;
            if (entry == null)
                return true ;

            return internalCanCreateNewConnection( entry ) ;
        } finally {
            lock.unlock() ;
        }
    }

    private boolean internalCanCreateNewConnection( 
        final OutboundCacheEntry<C> entry ) {
        lock.lock() ;
        try {
            final boolean createNewConnection = (entry.totalConnections() == 0) ||
                ((numberOfConnections() < highWaterMark()) &&
                (entry.totalConnections() < maxParallelConnections)) ;

            return createNewConnection ;
        } finally {
            lock.unlock() ;
        }
    }

    public C get( final ContactInfo<C> cinfo) throws IOException {
        return get( cinfo, null ) ;
    }

    public C get( final ContactInfo<C> cinfo,
	final ConnectionFinder<C> finder ) throws IOException {
        lock.lock() ;
        mm.enter( debug(), "get", cinfo ) ;
        C result = null ;

	try {
            while (true) {
                final OutboundCacheEntry<C> entry = getEntry( cinfo ) ;

                if (finder != null) {
                    mm.info( debug(), "calling finder to get a connection" ) ;
                        
                    entry.startConnect() ; 
                    // Finder may block, especially on opening a new 
                    // connection, so we can't hold the lock during the
                    // finder call.
                    lock.unlock() ;
                    try {
                        result = finder.find( cinfo, 
                            entry.idleConnectionsView,
                            entry.busyConnectionsView ) ;
                    } finally {
                        lock.lock() ;
                        entry.finishConnect() ;
                    }

                    if (result != null) {
                        mm.info( debug(), "finder got connection", result ) ;
                    }
                }

                if (result == null) {
                    result = entry.idleConnections.poll() ;
                }
                if (result == null) {
                    result = tryNewConnection( entry, cinfo ) ;
                }
                if (result == null) {
                    result = entry.busyConnections.poll() ;
                }

                if (result == null)  {
                    mm.info( debug(), "No connection available: "
                        + "awaiting a pending connection" ) ;
                    entry.waitForConnection() ;
                    continue ;
                } else {
                    OutboundConnectionState<C> cs = getConnectionState( 
                        cinfo, entry, result ) ;

                    if (cs.isBusy()) {
                        // Nothing to do in this case
                    } else if (cs.isIdle()) {
                        totalBusy++ ;
                        decrementTotalIdle() ;
                    } else { // state is NEW
                        totalBusy++ ;
                    }

                    cs.acquire() ;
                    break ;
                }
            }
	} finally {
            mm.info( debug(), "totalIdle", totalIdle,
                "totalBusy", totalBusy ) ;
            mm.exit( debug(), result ) ;
            lock.unlock() ;
	}

        return result ;
    }

    private OutboundCacheEntry<C> getEntry( final ContactInfo<C> cinfo 
	) throws IOException {

        mm.enter( debug(), "getEntry", cinfo ) ;
        OutboundCacheEntry<C> result = null ;
	try {
	    // This is the only place a OutboundCacheEntry is constructed.
	    result = entryMap.get( cinfo ) ;
	    if (result == null) {
		result = new OutboundCacheEntry<C>( lock ) ;
                mm.info( debug(), "creating new OutboundCacheEntry", result ) ;
		entryMap.put( cinfo, result ) ;
	    } else {
                mm.info( debug(), "re-using existing OutboundCacheEntry", result ) ;
	    }
	} finally {
            mm.exit( debug(), result ) ;
	}

        return result ;
    }

    // Note that tryNewConnection will ALWAYS create a new connection if
    // no connection currently exists.
    private C tryNewConnection( final OutboundCacheEntry<C> entry, 
	final ContactInfo<C> cinfo ) throws IOException {
	
        mm.enter( debug(), "tryNewConnection", cinfo ) ;
        C conn = null ;
	try {
	    if (internalCanCreateNewConnection(entry)) {
		// If this throws an exception just let it
		// propagate: let a higher layer handle a
		// connection creation failure.
                entry.startConnect() ;
                lock.unlock() ;
                try {
                    conn = cinfo.createConnection() ; 
                } finally {
                    lock.lock() ;
                    entry.finishConnect() ;
                }
	    }
	} finally {
            mm.exit( debug(), conn ) ;
	}

        return conn ;
    }

    private OutboundConnectionState<C> getConnectionState( 
	ContactInfo<C> cinfo, OutboundCacheEntry<C> entry, C conn ) {
        lock.lock() ;
        mm.enter( debug(), "getConnectionState", "cinfo", cinfo,
            "entry", entry, "conn", conn ) ;
	
	try {
	    OutboundConnectionState<C> cs = connectionMap.get( conn ) ;
	    if (cs == null) {
		cs = new OutboundConnectionState<C>( cinfo, entry, conn ) ;
                mm.info( debug(), "creating new OutboundConnectionState ", cs ) ;
		connectionMap.put( conn, cs ) ;
	    } else {
                mm.info( debug(), "found OutboundConnectionState ", cs ) ;
	    }

	    return cs ;
	} finally {
            mm.exit( debug() ) ;
            lock.unlock() ;
	}
    }
    
    public void release( final C conn, 
	final int numResponsesExpected ) {
        lock.lock() ;
        mm.enter( debug(), "release", "conn", conn, "numResponsesExpected",
            numResponsesExpected ) ;

        OutboundConnectionState<C> cs = null ;

	try {
            cs = connectionMap.get( conn ) ;
	    if (cs == null) {
                mm.info( debug(), "connection was already closed" ) ;
		return ; 
	    } else {
                int numResp = cs.release( numResponsesExpected ) ;
                mm.info( debug(), "numResponsesExpected", numResponsesExpected ) ;

		if (!cs.isBusy()) {
		    boolean connectionClosed = false ;
		    if (numResp == 0) {
			connectionClosed = reclaimOrClose( cs, conn ) ;
		    }

		    decrementTotalBusy() ;

		    if (!connectionClosed) {
                        mm.info( debug(), "idle connection queued" ) ;
			totalIdle++ ;
		    }
		}
	    }
	} finally {
            mm.info( debug(), "cs", cs, "totalIdle", totalIdle,
                "totalBusy", totalBusy ) ;
            mm.exit( debug() ) ;
            lock.unlock() ;
	}
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    public void responseReceived( final C conn ) {
        lock.lock() ;
        mm.enter( debug(), "responseReceived", conn ) ;
	try {
	    final OutboundConnectionState<C> cs = connectionMap.get( conn ) ;
	    if (cs == null) {
                mm.info( debug(), "response received on closed connection" ) ;
		return ;
	    }

            if (cs.responseReceived()) {
		reclaimOrClose( cs, conn ) ;
            }
	} finally {
            mm.exit( debug() ) ;
            lock.unlock() ;
	}
    }
    
    // If overflow, close conn and return true,
    // otherwise enqueue on reclaimable queue and return false.
    private boolean reclaimOrClose( OutboundConnectionState<C> cs, 
        final C conn ) {

        mm.enter( debug(), "reclaimOrClose", "cs", cs, "conn", conn ) ;

	try {
	    final boolean isOverflow = numberOfConnections() > 
		highWaterMark() ;

	    if (isOverflow) {
                mm.info( debug(), "closing overflow connection" ) ;
		close( conn ) ;
	    } else {
                mm.info( debug(), "queuing reclaimable connection" ) ;
		cs.setReclaimableHandle( 
		    reclaimableConnections.offer( conn ) ) ;
	    }

	    return isOverflow ;
	} finally {
            mm.exit( debug() ) ;
	}
    }


    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    public void close( final C conn ) {
        lock.lock() ;
        mm.enter( debug(), "close", conn ) ;
	try {
	    final OutboundConnectionState<C> cs = connectionMap.remove( conn ) ;
	    if (cs == null) {
                mm.info( debug(), "connection was already closed" ) ;
		return ;
	    }
            mm.info( debug(), "cs", cs ) ;

            if (cs.isBusy()) {
                mm.info( debug(), "connection removed from busy connections" ) ;
		decrementTotalBusy() ;
            } else if (cs.isIdle()) {
                mm.info( debug(), "connection removed from idle connections" ) ;
		decrementTotalIdle() ;
            }

            cs.close() ;
	} finally {
            mm.exit( debug() ) ;
            lock.unlock() ;
	}
    }

    private void decrementTotalIdle() {
        mm.enter( debug(), "decrementTotalIdle", totalIdle ) ;
	try {
	    if (totalIdle > 0) {
		totalIdle-- ;
	    } else {
                mm.info( debug(), "ERROR: was already 0!" ) ;
	    }
	} finally {
            mm.exit( debug(), totalIdle ) ;
	}
    }

    private void decrementTotalBusy() {
        mm.enter( debug(), "decrementTotalBusy", totalBusy ) ;
	try {
	    if (totalBusy > 0) {
		totalBusy-- ;
	    } else {
                mm.info( debug(), "ERROR: count was already 0!" ) ;
	    }
	} finally {
            mm.exit( debug() ) ;
	}
    }

}

// End of file.
