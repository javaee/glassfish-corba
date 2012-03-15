/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.transport;

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ConnectionCache;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;

import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.NameValue ;

import org.glassfish.external.statistics.CountStatistic ;
import org.glassfish.external.statistics.impl.CountStatisticImpl ;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;


    ////////////////////////////////////////////////////
    //
    // spi.transport.ConnectionCache
    //
/**
 * @author Harold Carr
 */
@Transport
public abstract class ConnectionCacheBase
    implements
        ConnectionCache
{
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    private static final String STAT_UNIT = "count" ;

    private static final String TOTAL_ID_STD    = "TotalConnections" ;
    private static final String TOTAL_ID        = "totalconnections" ;
    private static final String IDLE_ID_STD     = "ConnectionsIdle" ;
    private static final String IDLE_ID         = "connectionsidle" ;
    private static final String BUSY_ID_STD     = "ConnectionsBusy" ;
    private static final String BUSY_ID         = "connectionsbusy" ;

    private static final String TOTAL_DESC = 
        "Total number of connections in the connection cache" ; 
    private static final String IDLE_DESC = 
        "Number of connections in the connection cache that are idle" ; 
    private static final String BUSY_DESC =
        "Number of connections in the connection cache that are in use" ; 

    protected ORB orb;
    protected long timestamp = 0;
    protected String cacheType;
    protected String monitoringName;

    protected ConnectionCacheBase(ORB orb, String cacheType,
                                       String monitoringName)
    {
        this.orb = orb;
        this.cacheType = cacheType;
        this.monitoringName = monitoringName;
        dprintCreation();
    }
    
    @NameValue
    public String getCacheType()
    {
        return cacheType;
    }

    public synchronized void stampTime(Connection c)
    {
        // _REVISIT_ Need to worry about wrap around some day
        c.setTimeStamp(timestamp++);
    }

    private CountStatistic  makeCountStat( String name, String desc, 
        long value ) {

        CountStatisticImpl result = new CountStatisticImpl( name,
            STAT_UNIT, desc ) ;
        result.setCount( value ) ;
        return result ;
    }

    public void close() {
        synchronized (backingStore()) {
            for (Object obj : values()) {
                ((Connection)obj).closeConnectionResources() ;
            }
        }
    }

    @ManagedAttribute( id=TOTAL_ID ) 
    @Description( TOTAL_DESC ) 
    private CountStatistic numberOfConnectionsAttr()
    {
        return makeCountStat( TOTAL_ID_STD, TOTAL_DESC, 
            numberOfConnections() ) ;
    }

    public long numberOfConnections()
    {
        long count = 0 ;
        synchronized (backingStore()) {
            count = values().size();
        }

        return count ;
    }

    @ManagedAttribute( id=IDLE_ID ) 
    @Description( IDLE_DESC )
    private CountStatistic numberOfIdleConnectionsAttr()
    {
        return makeCountStat( IDLE_ID_STD, IDLE_DESC, 
            numberOfIdleConnections() ) ;
    }

    public long numberOfIdleConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (! ((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }

        return count ;
    }

    @ManagedAttribute( id=BUSY_ID ) 
    @Description( BUSY_DESC )
    private CountStatistic numberOfBusyConnectionsAttr()
    {
        return makeCountStat( BUSY_ID_STD, BUSY_DESC, 
            numberOfBusyConnections() ) ;
    }

    public long numberOfBusyConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }
        
        return count ;
    }


    /**
     * Discarding least recently used Connections that are not busy
     *
     * This method must be synchronized since one WorkerThread could
     * be reclaming connections inside the synchronized backingStore
     * block and a second WorkerThread (or a SelectorThread) could have
     * already executed the if (numberOfConnections <= .... ). As a
     * result the second thread would also attempt to reclaim connections.
     *
     * If connection reclamation becomes a performance issue, the connection
     * reclamation could make its own task and consequently executed in
     * a separate thread.
     * Currently, the accept & reclaim are done in the same thread, WorkerThread
     * by default. It could be changed such that the SelectorThread would do
     * it for SocketChannels and WorkerThreads for Sockets by updating the
     * ParserTable.
     */
    @Transport
    synchronized public boolean reclaim() {
        long numberOfConnections = numberOfConnections() ;

        reclaimInfo( numberOfConnections,
            orb.getORBData().getHighWaterMark(),
            orb.getORBData().getNumberToReclaim() ) ;

        if (numberOfConnections <= orb.getORBData().getHighWaterMark()) {
            return false;
        }

        Object backingStore = backingStore();
        synchronized (backingStore) {

                // REVISIT - A less expensive alternative connection reclaiming
                //           algorithm could be investigated.

            for (int i=0; i < orb.getORBData().getNumberToReclaim(); i++) {
                Connection toClose = null;
                long lru = java.lang.Long.MAX_VALUE;
                Iterator iterator = values().iterator();

                // Find least recently used and not busy connection in cache
                while ( iterator.hasNext() ) {
                    Connection c = (Connection) iterator.next();
                    if ( !c.isBusy() && c.getTimeStamp() < lru ) {
                        toClose = c;
                        lru = c.getTimeStamp();
                    }
                }

                if ( toClose == null ) {
                    return false;
                }

                try {
                    closingInfo( toClose ) ;
                    toClose.close();
                } catch (Exception ex) {
                    // REVISIT - log
                }
            }

            connectionsReclaimedInfo(
                numberOfConnections - numberOfConnections() );
        }

        return true;
    }

    public String getMonitoringName()
    {
        return monitoringName;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    // This is public so folb.Server test can access it.
    public abstract Collection values();

    protected abstract Object backingStore();

    @InfoMethod
    private void creationInfo(String cacheType, String monitoringName) { }

    @Transport
    protected void dprintCreation() {
        creationInfo( getCacheType(), getMonitoringName() ) ;
    }

    @InfoMethod
    private void cacheStatsInfo( long numberOfConnections,
        long numberOfBusyConnections, long numberOfIdleConnections,
        int highWaterMark, int numberToReclaim) { }

    @Transport
    protected void cacheStatisticsInfo() {
        cacheStatsInfo( numberOfConnections(), numberOfBusyConnections(),
            numberOfIdleConnections(), orb.getORBData().getHighWaterMark(),
            orb.getORBData().getNumberToReclaim() ) ;
    }

    @InfoMethod
    private void reclaimInfo(long numberOfConnections, int highWaterMark,
        int numberToReclaim) { }

    @InfoMethod
    private void closingInfo(Connection toClose) { }

    @InfoMethod
    private void connectionsReclaimedInfo(long l) { }
}

// End of file.
