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
import java.util.Hashtable;

import com.sun.corba.se.spi.monitoring.LongMonitoredAttributeBase;
import com.sun.corba.se.spi.monitoring.MonitoringConstants;
import com.sun.corba.se.spi.monitoring.MonitoringFactories;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaContactInfo;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaOutboundConnectionCache;

/**
 * @author Harold Carr
 */
public class CorbaOutboundConnectionCacheImpl
    extends
	CorbaConnectionCacheBase
    implements
	CorbaOutboundConnectionCache
{
    protected Hashtable connectionCache;

    public CorbaOutboundConnectionCacheImpl(ORB orb, CorbaContactInfo contactInfo)
    {
	super(orb, contactInfo.getConnectionCacheType(),
	      ((CorbaContactInfo)contactInfo).getMonitoringName());
	this.connectionCache = new Hashtable();
    }

    public CorbaConnection get(CorbaContactInfo contactInfo)
    {
	if (orb.transportDebugFlag) {
	    dprint(".get: " + contactInfo + " " + contactInfo.hashCode());
	}
	synchronized (backingStore()) {
	    dprintStatistics();
	    return (CorbaConnection) connectionCache.get(contactInfo);
	}
    }
    
    public void put(CorbaContactInfo contactInfo, CorbaConnection connection)
    {
	if (orb.transportDebugFlag) {
	    dprint(".put: " + contactInfo + " " + contactInfo.hashCode() + " "
		   + connection);
	}
	synchronized (backingStore()) {
	    connectionCache.put(contactInfo, connection);
	    connection.setConnectionCache(this);
	    dprintStatistics();
	}
    }

    public void remove(CorbaContactInfo contactInfo)
    {
	if (orb.transportDebugFlag) {
	    dprint(".remove: " + contactInfo + " " + contactInfo.hashCode());
	}
	synchronized (backingStore()) {
	    if (contactInfo != null) {
		connectionCache.remove(contactInfo);
	    }
	    dprintStatistics();
	}
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public Collection values()
    {
	return connectionCache.values();
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

	// OUTBOUND CONNECTION
	MonitoredObject outboundConnectionMO = 
	    connectionMO.getChild(
                MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT);
	if (outboundConnectionMO == null) {
	    outboundConnectionMO =
		MonitoringFactories.getMonitoredObjectFactory()
		    .createMonitoredObject(
		        MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT,
			MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT_DESCRIPTION);
	    connectionMO.addChild(outboundConnectionMO);
	}

	// NODE FOR THIS CACHE
	MonitoredObject thisMO = 
	    outboundConnectionMO.getChild(getMonitoringName());
	if (thisMO == null) {
	    thisMO =
		MonitoringFactories.getMonitoredObjectFactory()
		    .createMonitoredObject(
			getMonitoringName(),
			MonitoringConstants.CONNECTION_MONITORING_DESCRIPTION);
	    outboundConnectionMO.addChild(thisMO);
	}

	LongMonitoredAttributeBase attribute;

	// ATTRIBUTE
	attribute = new 
	    LongMonitoredAttributeBase(
                MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS, 
		MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS_DESCRIPTION)
	    {
		public Object getValue() {
		    return Long.valueOf(CorbaOutboundConnectionCacheImpl.this.numberOfConnections());
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
		    return Long.valueOf(CorbaOutboundConnectionCacheImpl.this.numberOfIdleConnections());
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
		    return Long.valueOf(CorbaOutboundConnectionCacheImpl.this.numberOfBusyConnections());
		}
	    };
	thisMO.addAttribute(attribute);
    }

    @Override
    public String toString()
    {
	return "CorbaOutboundConnectionCacheImpl["
	    + connectionCache
	    + "]";
    }

    @Override
    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaOutboundConnectionCacheImpl", msg);
    }
}

// End of file.
