/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2003 Sep 28 (Sun) 09:06:43 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 06:53:22 by Harold Carr.
//

package corba.connections;

import java.util.Collection;
import java.util.Iterator;

import org.omg.CORBA.ORB;

import com.sun.corba.se.spi.monitoring.MonitoredAttribute;
import com.sun.corba.se.spi.monitoring.MonitoringConstants;
import com.sun.corba.se.spi.monitoring.MonitoredObject;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnectionCache;
import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import corba.hcks.U;

public class ConnectionStatistics
{
    public ConnectionStatistics()
    {
    }

    public String outbound(String msg, ORB orb)
    {
	StringBuffer result = new StringBuffer("");
	pac(result, "==================================================");
	pac(result, msg + " OUTBOUND:");

	MonitoredObject orbMO =
	    ((com.sun.corba.se.spi.orb.ORB)orb)
	        .getMonitoringManager().getRootMonitoredObject();

	MonitoredObject connectionMO =
	    orbMO.getChild(MonitoringConstants.CONNECTION_MONITORING_ROOT);
	if (connectionMO == null) {
	    pac(result, "--------------------------------------------------");
	    pac(result, "ERROR: Missing: " 
		+ MonitoringConstants.CONNECTION_MONITORING_ROOT);
	    pac(result, "--------------------------------------------------");
	    System.exit(1);
	}
	MonitoredObject outboundConnectionMO =
	    connectionMO.getChild(MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT);
	if (outboundConnectionMO == null) {
	    pac(result, "--------------------------------------------------");
	    pac(result, "ERROR: Missing: " 
		+ MonitoringConstants.OUTBOUND_CONNECTION_MONITORING_ROOT);
	    pac(result, "--------------------------------------------------");
	    System.exit(1);
	}

	Collection connectionCaches =
	    ((com.sun.corba.se.spi.orb.ORB)orb)
	        .getCorbaTransportManager().getOutboundConnectionCaches();

	Iterator iterator = connectionCaches.iterator();
	while (iterator.hasNext()) {
	    CorbaConnectionCache connectionCache = (CorbaConnectionCache)
		iterator.next();

	    pac(result, connectionCache.getMonitoringName());

	    MonitoredObject xMO =
		outboundConnectionMO.getChild(connectionCache.getMonitoringName());
	    if (xMO == null) {
		pac(result, "--------------------------------------------------");
		pac(result, "ERROR: Missing: " 
		    + connectionCache.getMonitoringName());
		pac(result, "--------------------------------------------------");
		System.exit(1);
	    }
	    MonitoredAttribute attribute;

	    attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
	    if (attribute == null) {
		pac(result, "--------------------------------------------------");
		pac(result, "ERROR: Missing: " 
		    + MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
		pac(result, "--------------------------------------------------");
		System.exit(1);
	    } else {
		pac(result, 
		    MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS
		    + " " + attribute.getValue());
	    }

	    attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
	    if (attribute == null) {
		pac(result, "--------------------------------------------------");
		pac(result, "ERROR: Missing: " 
		    + MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS);
		pac(result, "--------------------------------------------------");
		System.exit(1);
	    } else {
		pac(result, 
		    MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS
		    + " " + attribute.getValue());
	    }

	    attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS);
	    if (attribute == null) {
		pac(result, "--------------------------------------------------");
		pac(result, "ERROR: Missing: " 
		    + MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS);
		pac(result, "--------------------------------------------------");
		System.exit(1);
	    } else {
		pac(result, 
		    MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS
		    + " " + attribute.getValue());
	    }
	}
	return result.toString();
    }

    public String inbound(String msg, ORB orb)
    {
	StringBuffer result = new StringBuffer("");
	pac(result, "==================================================");
	pac(result, msg + " INBOUND:");

	MonitoredObject orbMO =
	    ((com.sun.corba.se.spi.orb.ORB)orb)
	        .getMonitoringManager().getRootMonitoredObject();
	MonitoredObject connectionMO =
	    orbMO.getChild(MonitoringConstants.CONNECTION_MONITORING_ROOT);
	if (connectionMO == null) {
	    pac(result, "Missing " 
		+ MonitoringConstants.CONNECTION_MONITORING_ROOT);
	    System.exit(1);
	}
	MonitoredObject inboundConnectionMO =
	    connectionMO.getChild(MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT);
	if (inboundConnectionMO == null) {
	    pac(result, "Missing " 
		+ MonitoringConstants.INBOUND_CONNECTION_MONITORING_ROOT);
	    System.exit(1);
	}

	CorbaAcceptor acceptor = (CorbaAcceptor)
	    ((com.sun.corba.se.spi.orb.ORB)orb)
	        .getCorbaTransportManager().getAcceptors().iterator().next();

	pac(result, acceptor.getMonitoringName());

	MonitoredObject xMO =
	    inboundConnectionMO.getChild(acceptor.getMonitoringName());
	if (xMO == null) {
	    pac(result, "Missing " 
		+ acceptor.getMonitoringName());
	    System.exit(1);
	}

	MonitoredAttribute attribute;

	attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
	if (attribute == null) {
	    pac(result, "Missing " 
		+ MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
	    System.exit(1);
	} else {
	    pac(result,
		" " + MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS
		+ " " + attribute.getValue());
	}
	
	attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_TOTAL_NUMBER_OF_CONNECTIONS);
	if (attribute == null) {
	    pac(result, "Missing " 
		+ MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS);
	    System.exit(1);
	} else {
	    pac(result,
		" " + MonitoringConstants.CONNECTION_NUMBER_OF_IDLE_CONNECTIONS
		+ " " + attribute.getValue());
	}
    
	attribute = xMO.getAttribute(MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS);
	if (attribute == null) {
	    pac(result, "Missing " 
		+ MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS);
	    System.exit(1);
	} else {
	    pac(result,
		" " + MonitoringConstants.CONNECTION_NUMBER_OF_BUSY_CONNECTIONS
		+ " " + attribute.getValue());
	}
	return result.toString();
    }

    // Print And Collect
    public void pac(StringBuffer result, String append)
    {
	U.sop(append);
	result.append(append).append('\n');
    }
}

// End of file.
