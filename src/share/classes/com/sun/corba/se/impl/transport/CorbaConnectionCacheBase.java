/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.transport;

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaConnectionCache;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBUtility;


/**
 * @author Harold Carr
 */
public abstract class CorbaConnectionCacheBase
    implements
	CorbaConnectionCache
{
    protected ORB orb;
    protected long timestamp = 0;
    protected String cacheType;
    protected String monitoringName;
    protected ORBUtilSystemException wrapper;

    protected CorbaConnectionCacheBase(ORB orb, String cacheType,
				       String monitoringName)
    {
	this.orb = orb;
	this.cacheType = cacheType;
	this.monitoringName = monitoringName;
	wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
	dprintCreation();
    }
    
    public String getCacheType()
    {
	return cacheType;
    }

    public synchronized void stampTime(CorbaConnection c)
    {
	// _REVISIT_ Need to worry about wrap around some day
        c.setTimeStamp(timestamp++);
    }

    public long numberOfConnections()
    {
	synchronized (backingStore()) {
	    return values().size();
	}
    }

    public void close() {
        synchronized (backingStore()) {
            for (Object obj : values()) {
                ((CorbaConnection)obj).closeConnectionResources() ;
            }
        }
    }

    public long numberOfIdleConnections()
    {
	long count = 0;
	synchronized (backingStore()) {
	    Iterator connections = values().iterator();
	    while (connections.hasNext()) {
		if (! ((CorbaConnection)connections.next()).isBusy()) {
		    count++;
		}
	    }
	}
	return count;
    }

    public long numberOfBusyConnections()
    {
	long count = 0;
	synchronized (backingStore()) {
	    Iterator connections = values().iterator();
	    while (connections.hasNext()) {
		if (((CorbaConnection)connections.next()).isBusy()) {
		    count++;
		}
	    }
	}
	return count;
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
    synchronized public boolean reclaim()
    {
	try {
	    long numberOfConnections = numberOfConnections();

	    if (orb.transportDebugFlag) {
		dprint(".reclaim->: " + numberOfConnections
			+ " ("
			+ orb.getORBData().getHighWaterMark()
			+ "/"
			+ orb.getORBData().getNumberToReclaim()
			+ ")");
	    }

	    if (numberOfConnections <= orb.getORBData().getHighWaterMark()) {
		return false;
	    }
	    
	    Object backingStore = backingStore();
	    synchronized (backingStore) {

	         // REVISIT - A less expensive alternative connection reclaiming 
	         //           algorithm could be investigated.

		for (int i=0; i < orb.getORBData().getNumberToReclaim(); i++) {
		    CorbaConnection toClose = null;
		    long lru = java.lang.Long.MAX_VALUE;
		    Iterator iterator = values().iterator();
		    
		    // Find least recently used and not busy connection in cache
		    while ( iterator.hasNext() ) {
			CorbaConnection c = (CorbaConnection) iterator.next();
			if ( !c.isBusy() && c.getTimeStamp() < lru ) {
			    toClose = c; 
			    lru = c.getTimeStamp();
			}
		    }
		    
		    if ( toClose == null ) {
			return false;
		    }
		    
		    try {
			if (orb.transportDebugFlag) {
			    dprint(".reclaim: closing: " + toClose);
			}
			toClose.close();
		    } catch (Exception ex) {
			// REVISIT - log
		    }
		}

		if (orb.transportDebugFlag) {
		    dprint(".reclaim: connections reclaimed (" 
			    + (numberOfConnections - numberOfConnections()) + ")");
		}
	    }

	    // XXX is necessary to do a GC to reclaim
	    // closed network connections ??
	    // java.lang.System.gc();

	    return true;
	} finally {
	    if (orb.transportDebugFlag) {
		dprint(".reclaim<-: " + numberOfConnections());
	    }
	}
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.ConnectionCache
    //

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

    protected void dprintCreation()
    {
	if (orb.transportDebugFlag) {
	    dprint(".constructor: cacheType: " + getCacheType()
		   + " monitoringName: " + getMonitoringName());
	}
    }

    protected void dprintStatistics()
    {
	if (orb.transportDebugFlag) {
	    dprint(".stats: "
		   + numberOfConnections() + "/total "
		   + numberOfBusyConnections() + "/busy "
		   + numberOfIdleConnections() + "/idle"
		   + " (" 
		   + orb.getORBData().getHighWaterMark() + "/"
		   + orb.getORBData().getNumberToReclaim() 
		   + ")");
	}
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaConnectionCacheBase", msg);
    }
}

// End of file.
