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

import java.util.ArrayList;
import java.util.Collection;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.transport.Acceptor;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.InboundConnectionCache;

import com.sun.corba.se.spi.monitoring.LongMonitoredAttributeBase;
import com.sun.corba.se.spi.monitoring.MonitoringConstants;
import com.sun.corba.se.spi.monitoring.MonitoringFactories;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaConnectionCache;
import com.sun.corba.se.spi.transport.CorbaAcceptor;

import com.sun.corba.se.impl.orbutil.ORBUtility;

/**
 * @author Harold Carr
 */
public class CorbaInboundConnectionCacheImpl
    extends
	CorbaConnectionCacheBase
    implements
	InboundConnectionCache
{
    protected Collection connectionCache;

    public CorbaInboundConnectionCacheImpl(ORB orb, Acceptor acceptor)
    {
	super(orb, acceptor.getConnectionCacheType(),
	      ((CorbaAcceptor)acceptor).getMonitoringName());
	this.connectionCache = new ArrayList();
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.InboundConnectionCache
    //
    
    public Connection get(Acceptor acceptor) 
    {
	throw wrapper.methodShouldNotBeCalled();
    }
    
    public void put(Acceptor acceptor, Connection connection) 
    {
	if (orb.transportDebugFlag) {
	    dprint(".put: " + acceptor + " " + connection);
	}
	synchronized (backingStore()) {
	    connectionCache.add(connection);
	    connection.setConnectionCache(this);
	    dprintStatistics();
	}
    }

    public void remove(Connection connection)
    {
	if (orb.transportDebugFlag) {
	    dprint(".remove: " +  connection);
	}
	synchronized (backingStore()) {
	    connectionCache.remove(connection);
	    dprintStatistics();
	}
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public Collection values()
    {
	return connectionCache;
    }

    protected Object backingStore()
    {
	return connectionCache;
    }

    protected void registerWithMonitoring()
    {
	// ORB
	MonitoredObject orbMO = 
	    orb.getMonitoringManager().getRootMonitoredObject();

	// REVISIT - add ORBUtil mkdir -p like operation for this.

	// CONNECTION
	MonitoredObject connectionMO = 
	    orbMO.getChild(MonitoringConstants.CONNECTION_MONITORING_ROOT);
	if (connectionMO == null) {
	    connectionMO = 
		MonitoringFactories.getMonitoredObjectFactory()
		    .createMonitoredObject(
		        MonitoringConstants.CONNECTION_MONITORING_ROOT,
			MonitoringConstants.CONNECTION_MONITORING_ROOT_DESCRIPTION);
	    orbMO.addChild(connectionMO);
	}

	// INBOUND CONNECTION
	MonitoredObject inboundConnectionMO = 
	    connectionMO.getChild(
                MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT);
	if (inboundConnectionMO == null) {
	    inboundConnectionMO =
		MonitoringFactories.getMonitoredObjectFactory()
		    .createMonitoredObject(
		        MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT,
			MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION);
	    connectionMO.addChild(inboundConnectionMO);
	}

	// NODE FOR THIS CACHE
	MonitoredObject thisMO = 
	    inboundConnectionMO.getChild(getMonitoringName());
	if (thisMO == null) {
	    thisMO =
		MonitoringFactories.getMonitoredObjectFactory()
		    .createMonitoredObject(
		        getMonitoringName(),
			MonitoringConstants.CONNECTION_MONITORING_DESCRIPTION);
	    inboundConnectionMO.addChild(thisMO);
	}

	LongMonitoredAttributeBase attribute;

	// ATTRIBUTE
	attribute = new 
	    LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS, 
		MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION)
	    {
		public Object getValue() {
		    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfConnections());
		}
	    };
	thisMO.addAttribute(attribute);

	// ATTRIBUTE
	attribute = new 
	    LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS, 
		MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS_DESCRIPTION)
	    {
		public Object getValue() {
		    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfIdleConnections());
		}
	    };
	thisMO.addAttribute(attribute);

	// ATTRIBUTE
	attribute = new 
	    LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS, 
		MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS_DESCRIPTION)
	    {
		public Object getValue() {
		    return new Long(CorbaInboundConnectionCacheImpl.this.numberOfBusyConnections());
		}
	    };
	thisMO.addAttribute(attribute);
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaInboundConnectionCacheImpl", msg);
    }
}

// End of file.
