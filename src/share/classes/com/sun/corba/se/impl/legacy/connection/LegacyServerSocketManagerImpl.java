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

package com.sun.corba.se.impl.legacy.connection;

import java.util.Collection;
import java.util.Iterator;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;


import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketManager;

import com.sun.corba.se.impl.misc.ORBUtility;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

public class LegacyServerSocketManagerImpl 
    implements
	LegacyServerSocketManager
{
    protected ORB orb;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    
    public LegacyServerSocketManagerImpl(ORB orb) {
	this.orb = orb;
    }

    ////////////////////////////////////////////////////
    //
    // LegacyServerSocketManager
    //

    // Only used in ServerManagerImpl.
    public int legacyGetTransientServerPort(String type)
    {
	return legacyGetServerPort(type, false);
    }

    // Only used by POAPolicyMediatorBase.
    public synchronized int legacyGetPersistentServerPort(String socketType)
    {
	if (orb.getORBData().getServerIsORBActivated()) {
	    // this server is activated by orbd
	    return legacyGetServerPort(socketType, true);
	} else if (orb.getORBData().getPersistentPortInitialized()) {
	    // this is a user-activated server
	    return orb.getORBData().getPersistentServerPort();
	} else {
	    throw wrapper.persistentServerportNotSet();
	}
    }

    // Only used by PI IORInfoImpl.
    public synchronized int legacyGetTransientOrPersistentServerPort(
        String socketType)
    {
	    return legacyGetServerPort(socketType, 
				       orb.getORBData()
				       .getServerIsORBActivated());
    }

    // Used in RepositoryImpl, ServerManagerImpl, POAImpl,
    // POAPolicyMediatorBase, TOAImpl.
    // To get either default or bootnaming endpoint.
    public synchronized LegacyServerSocketEndPointInfo legacyGetEndpoint(
        String name)
    {
	Iterator iterator = getAcceptorIterator();
	while (iterator.hasNext()) {
	    LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
	    if (endPoint != null && name.equals(endPoint.getName())) {
		return endPoint;
	    }
	}
	throw new INTERNAL("No acceptor for: " + name);
    }

    // Check to see if the given port is equal to any of the ORB Server Ports.
    // Used in IIOPProfileImpl, ORBImpl.
    public boolean legacyIsLocalServerPort(int port) 
    {
        // If port is 0 (which signifies in CSIv2 that clear text
        // communication is not allowed), we must return true, because
        // this check is not meaningful.
        if (port == 0) {
            return true ;
        }

	Iterator iterator = getAcceptorIterator();
	while (iterator.hasNext()) { 
	    LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
	    if (endPoint != null && endPoint.getPort() == port) {
		return true;
	    }
	}
        return false;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    private int legacyGetServerPort (String socketType, boolean isPersistent)
    {
	Iterator endpoints = getAcceptorIterator();
	while (endpoints.hasNext()) {
	    LegacyServerSocketEndPointInfo ep = cast(endpoints.next());
	    if (ep != null && ep.getType().equals(socketType)) {
		if (isPersistent) {
		    return ep.getLocatorPort();
		} else {
		    return ep.getPort();
		}
	    }
	}
	return -1;
    }

    private Iterator getAcceptorIterator()
    {
        Collection acceptors = 
	    orb.getCorbaTransportManager().getAcceptors(null, null);
        if (acceptors != null) {
            return acceptors.iterator();
	}

	throw wrapper.getServerPortCalledBeforeEndpointsInitialized() ;
    }

    private LegacyServerSocketEndPointInfo cast(Object o)
    {
	if (o instanceof LegacyServerSocketEndPointInfo) {
	    return (LegacyServerSocketEndPointInfo) o;
	}
	return null;
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("LegacyServerSocketManagerImpl", msg);
    }
}

// End of file.


