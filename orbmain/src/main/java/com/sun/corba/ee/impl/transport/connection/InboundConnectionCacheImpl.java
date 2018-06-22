/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.transport.connection;

import java.io.IOException ;

import java.util.concurrent.ConcurrentMap ;
import java.util.concurrent.ConcurrentHashMap ;

import java.util.concurrent.atomic.AtomicInteger ;

import com.sun.corba.ee.spi.transport.connection.Connection ;
import com.sun.corba.ee.spi.transport.connection.InboundConnectionCache ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

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
@Transport
public final class InboundConnectionCacheImpl<C extends Connection> 
    extends ConnectionCacheNonBlockingBase<C> 
    implements InboundConnectionCache<C> {

    private final ConcurrentMap<C,ConnectionState<C>> connectionMap ;

    protected String thisClassName() {
        return "InboundConnectionCacheImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
        final C connection ;                            // Connection of the 
                                                        // ConnectionState
        final AtomicInteger busyCount ;                 // Number of calls to 
                                                        // get without release
        final AtomicInteger expectedResponseCount ;     // Number of expected 
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
        final int highWaterMark, final int numberToReclaim, long ttl ) {

        super( cacheType, highWaterMark, numberToReclaim, ttl ) ;

        this.connectionMap = 
            new ConcurrentHashMap<C,ConnectionState<C>>() ;
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

    @InfoMethod
    private void msg( String m ) {}

    @InfoMethod
    private void display( String m, Object value ) {}

    @Transport
    public void requestProcessed( final C conn, 
        final int numResponsesExpected ) {

        final ConnectionState<C> cs = connectionMap.get( conn ) ;

        if (cs == null) {
            msg( "connection was closed");
            return ;
        } else {
            int numResp = cs.expectedResponseCount.addAndGet(
                numResponsesExpected ) ;
            int numBusy = cs.busyCount.decrementAndGet() ;

            display( "numResp", numResp ) ;
            display( "numBusy", numBusy ) ;

            if (numBusy == 0) {
                totalBusy.decrementAndGet() ;
                totalIdle.incrementAndGet() ;

                if (numResp == 0) {
                    display( "queing reclaimalbe connection", conn ) ;
                    cs.reclaimableHandle =
                        reclaimableConnections.offer( conn ) ;
                }
            }
        }
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    @Transport
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
            // XXX log this
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
