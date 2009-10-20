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
import java.util.HashMap;
import java.util.Map;

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
    protected Map<CorbaContactInfo, CorbaConnection> connectionCache;
    private CorbaOutboundConnectionCacheProbeProvider pp =
        new CorbaOutboundConnectionCacheProbeProvider() ;

    public CorbaOutboundConnectionCacheImpl(ORB orb, CorbaContactInfo contactInfo)
    {
	super(orb, contactInfo.getConnectionCacheType(),
	      ((CorbaContactInfo)contactInfo).getMonitoringName());
	this.connectionCache = new HashMap<CorbaContactInfo,CorbaConnection>();
    }

    public CorbaConnection get(CorbaContactInfo contactInfo)
    {
	if (orb.transportDebugFlag) {
	    dprint(".get: " + contactInfo + " " + contactInfo.hashCode());
	}
	synchronized (backingStore()) {
	    dprintStatistics();
	    return connectionCache.get(contactInfo);
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
            pp.connectionOpenedEvent( contactInfo.toString(), connection.toString() ) ;
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
		CorbaConnection connection = connectionCache.remove(contactInfo);
                pp.connectionClosedEvent( contactInfo.toString(), connection.toString() ) ;
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
