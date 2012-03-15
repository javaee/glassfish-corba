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

import java.util.ArrayList;
import java.util.Collection;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.InboundConnectionCache;

import com.sun.corba.ee.spi.trace.Transport;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.AMXMetadata ;
import org.glassfish.gmbal.Description ;

/**
 * @author Harold Carr
 */
@Transport
@ManagedObject
@Description( "Cache of connections accepted by the ORB" ) 
@AMXMetadata( type="corba-inbound-connection-cache-mon", group="monitoring" )
public class InboundConnectionCacheImpl
    extends
        ConnectionCacheBase
    implements
        InboundConnectionCache
{
    protected Collection<Connection> connectionCache;
    private InboundConnectionCacheProbeProvider pp =
        new InboundConnectionCacheProbeProvider() ;

    public InboundConnectionCacheImpl(ORB orb, Acceptor acceptor)
    {
        super(orb, acceptor.getConnectionCacheType(),
              ((Acceptor)acceptor).getMonitoringName());
        this.connectionCache = new ArrayList<Connection>();
    }

    public Connection get(Acceptor acceptor)
    {
        throw wrapper.methodShouldNotBeCalled();
    }
    
    @Transport
    public void put(Acceptor acceptor, Connection connection)
    {
        synchronized (backingStore()) {
            connectionCache.add(connection);
            connection.setConnectionCache(this);
            cacheStatisticsInfo();
            pp.connectionOpenedEvent( acceptor.toString(), connection.toString() ) ;
        }
    }

    @Transport
    public void remove(Connection connection)
    {
        synchronized (backingStore()) {
            connectionCache.remove(connection);
            cacheStatisticsInfo();
            pp.connectionClosedEvent( connection.toString() ) ;
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
}

// End of file.
