/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.corba.se.pept.transport.Connection;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.transport.CorbaContactInfoBase;

/**
 * @author Harold Carr
 */
public class SocketOrChannelContactInfoImpl
    extends CorbaContactInfoBase
    implements SocketInfo
{
    protected boolean isHashCodeCached = false;
    protected int cachedHashCode;

    protected String socketType;
    protected String hostname;
    protected int    port;

    // XREVISIT 
    // See SocketOrChannelAcceptorImpl.createMessageMediator
    // See SocketFactoryContactInfoImpl.constructor()
    // See SocketOrChannelContactInfoImpl.constructor()
    protected SocketOrChannelContactInfoImpl()
    {
    }

    protected SocketOrChannelContactInfoImpl(
        ORB orb,
	CorbaContactInfoList contactInfoList)
    {
	this.orb = orb;
	this.contactInfoList = contactInfoList;
    }

    public SocketOrChannelContactInfoImpl(
        ORB orb,
	CorbaContactInfoList contactInfoList,
	String socketType,
	String hostname,
	int port)
    {
	this(orb, contactInfoList);
	this.socketType = socketType;
	this.hostname = hostname;
	this.port     = port;
    }

    // XREVISIT
    public SocketOrChannelContactInfoImpl(
        ORB orb,
	CorbaContactInfoList contactInfoList,
	IOR effectiveTargetIOR,
	short addressingDisposition,
	String socketType,
	String hostname,
	int port)
    {
	this(orb, contactInfoList, socketType, hostname, port);
	this.effectiveTargetIOR = effectiveTargetIOR;
	this.addressingDisposition = addressingDisposition;
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.ContactInfo
    //

    public boolean isConnectionBased()
    {
	return true;
    }

    public boolean shouldCacheConnection()
    {
	return true;
    }

    public String getConnectionCacheType()
    {
	return CorbaTransportManager.SOCKET_OR_CHANNEL_CONNECTION_CACHE;
    }

    public Connection createConnection()
    {
	Connection connection =
	    new SocketOrChannelConnectionImpl(orb, this, 
					      socketType, hostname, port);
	return connection;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public String getMonitoringName()
    {
	return "SocketConnections";
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.ContactInfo
    //

    public String getType()
    {
	return socketType;
    }

    public String getHost()
    {
	return hostname;
    }

    public int getPort()
    {
	return port;
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    // NOTE: hashCode should only check type/host/port, otherwise
    // RMI-IIOP Failover will break.  See IIOPPrimaryToContactInfoImpl.java
    // in the app server or in the Corba unit tests.
    
    @Override
    public int hashCode() 
    {
	if (! isHashCodeCached) {
	    cachedHashCode = socketType.hashCode() ^ hostname.hashCode() ^ port;
	    isHashCodeCached = true;
	}
	return cachedHashCode;
    }

    // NOTE: equals should only check type/host/port, otherwise
    // RMI-IIOP Failover will break.  See IIOPPrimaryToContactInfoImpl.java
    // in the app server or in the Corba unit tests.
    
    @Override
    public boolean equals(Object obj) 
    {
	if (obj == null) {
	    return false;
	} else if (!(obj instanceof SocketOrChannelContactInfoImpl)) {
	    return false;
	}

	SocketOrChannelContactInfoImpl other =
	    (SocketOrChannelContactInfoImpl) obj;

        if (port != other.port) {
            return false;
	}
        if (!hostname.equals(other.hostname)) {
            return false;
        }
	if (socketType == null) {
	    if (other.socketType != null) {
		return false;
	    }
	} else if (!socketType.equals(other.socketType)) {
	    return false;
	}
        return true;
    }

    public String toString()
    {
	return
	    "SocketOrChannelContactInfoImpl[" 
	    + socketType + " "
	    + hostname + " "
	    + port
	    + "]";
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    protected void dprint(String msg) 
    {
	ORBUtility.dprint("SocketOrChannelContactInfoImpl", msg);
    }
}

// End of file.
