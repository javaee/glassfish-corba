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

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaInboundConnectionCache;

import com.sun.corba.se.impl.orbutil.ORBUtility;

import org.glassfish.external.probe.provider.annotations.Probe ;
import org.glassfish.external.probe.provider.annotations.ProbeProvider ;
import org.glassfish.external.probe.provider.annotations.ProbeParam ;

/**
 * @author Harold Carr
 */
@ProbeProvider( providerName="glassfish", moduleName="orb" )
public class CorbaInboundConnectionCacheImpl
    extends
	CorbaConnectionCacheBase
    implements
	CorbaInboundConnectionCache
{
    @Probe( providerName="inboundConnectionOpened" )
    public void connectionOpenedEvent( 
        @ProbeParam( "acceptor" ) String acceptor, 
        @ProbeParam( "connection" ) String connection ) {}

    @Probe( providerName="inboundConnectionClosed" )
    public void connectionClosedEvent( 
        @ProbeParam( "connection" ) String connection ) {}

    protected Collection<CorbaConnection> connectionCache;

    public CorbaInboundConnectionCacheImpl(ORB orb, CorbaAcceptor acceptor)
    {
	super(orb, acceptor.getConnectionCacheType(),
	      ((CorbaAcceptor)acceptor).getMonitoringName());
	this.connectionCache = new ArrayList<CorbaConnection>();
    }

    public CorbaConnection get(CorbaAcceptor acceptor)
    {
	throw wrapper.methodShouldNotBeCalled();
    }
    
    public void put(CorbaAcceptor acceptor, CorbaConnection connection)
    {
	if (orb.transportDebugFlag) {
	    dprint(".put: " + acceptor + " " + connection);
	}
	synchronized (backingStore()) {
	    connectionCache.add(connection);
	    connection.setConnectionCache(this);
	    dprintStatistics();
            connectionOpenedEvent( acceptor.toString(), connection.toString() ) ;
	}
    }

    public void remove(CorbaConnection connection)
    {
	if (orb.transportDebugFlag) {
	    dprint(".remove: " +  connection);
	}
	synchronized (backingStore()) {
	    connectionCache.remove(connection);
	    dprintStatistics();
            connectionClosedEvent( connection.toString() ) ;
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

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaInboundConnectionCacheImpl", msg);
    }
}

// End of file.
