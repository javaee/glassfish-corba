/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.plugin.hwlb ;

import java.util.Collection ;
import java.util.Map ;
import java.util.HashMap ;

import org.omg.CORBA.LocalObject ;

import com.sun.corba.se.spi.protocol.CorbaClientRequestDispatcher ;

import com.sun.corba.se.spi.transport.CorbaConnection ;
import com.sun.corba.se.spi.transport.CorbaOutboundConnectionCache ;

import com.sun.corba.se.impl.encoding.CDRInputObject ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBConfigurator ;
import com.sun.corba.se.spi.orb.DataCollector ;

import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfoList ;
import com.sun.corba.se.spi.transport.CorbaContactInfoListFactory ;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry ;

import com.sun.corba.se.spi.ior.IOR ;

import com.sun.corba.se.spi.orbutil.ORBConstants ;

import com.sun.corba.se.impl.transport.CorbaConnectionCacheBase ;
import com.sun.corba.se.impl.transport.SocketOrChannelConnectionImpl ;

// The following 3 implementation classes are needed as base 
// classes.  This needs some architectural changes, perhaps
// adding a codegen-based proxy layer for dynamic inheritance.
import com.sun.corba.se.impl.protocol.CorbaClientRequestDispatcherImpl ;

import com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl ;
import com.sun.corba.se.impl.transport.CorbaContactInfoListImpl ;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Transport;


/** Install this in an ORB using the property 
 * ORBConstants.USER_CONFIGURATOR_PREFIX + "corba.lb.NoConnectionCacheImpl" = "dummy"
 */
@Transport
public class NoConnectionCacheImpl
    extends LocalObject
    implements ORBConfigurator
{
    @Transport
    private static class NCCConnectionCacheImpl extends CorbaConnectionCacheBase
	implements CorbaOutboundConnectionCache {
	// store is a dummy variable
	private Map store = new HashMap() ;

	// holds only one connection
	private CorbaConnection connection = null ;

	public NCCConnectionCacheImpl( ORB orb ) {
	    super( orb, "Dummy", "Dummy" ) ;
	}

	public Collection values() {
	    return store.values() ;
	}

	protected Object backingStore() {
	    return store ;
	}

	public CorbaConnection get(CorbaContactInfo contactInfo) {
	    return connection ;
	}

        @Transport
	public void put(CorbaContactInfo contactInfo, CorbaConnection conn ) {
            remove( contactInfo ) ;
            connection = conn ;
	}

        @InfoMethod
        private void removeConnectionInfo( CorbaConnection conn ) { }

        @InfoMethod
        private void connectionIsNull() { }

        @Transport
	public void remove(CorbaContactInfo contactInfo) {
            if (connection != null) {
                removeConnectionInfo(connection);
                connection.close() ;
                connection = null ;
            } else {
                connectionIsNull();
            }
	}
    }

    private static ThreadLocal connectionCache = new ThreadLocal() ;

    private static NCCConnectionCacheImpl getConnectionCache( ORB orb ) {
	NCCConnectionCacheImpl result = (NCCConnectionCacheImpl)connectionCache.get() ;
	if (result == null) {
	    result = new NCCConnectionCacheImpl( orb ) ;
	    connectionCache.set( result ) ;
	}

	return result ;
    }

    @Transport
    private static class NCCConnectionImpl extends SocketOrChannelConnectionImpl {
	private static int count = 0 ;
	private int myCount ;

        @Transport
        private void constructedNCCConnectionImpl( String str ) {
        }

        public NCCConnectionImpl(ORB orb, CorbaContactInfo contactInfo, 
		String socketType, String hostname, int port) {

	    super(orb,contactInfo, socketType, hostname, port);
	    myCount = count++ ;
            constructedNCCConnectionImpl(toString());
	}

        @Override
	public String toString() {
	    return "NCCConnectionImpl(" + myCount + ")["
		+ super.toString() + "]" ;
	}

        @Transport
        @Override
        public synchronized void close() {  
            super.closeConnectionResources() ;
	}
    }

    @Transport
    private static class NCCContactInfoImpl extends SocketOrChannelContactInfoImpl {
	public NCCContactInfoImpl( ORB orb,
	    CorbaContactInfoList contactInfoList, IOR effectiveTargetIOR,
	    short addressingDisposition, String socketType, String hostname,
	    int port) {

	    super( orb, contactInfoList, effectiveTargetIOR, addressingDisposition,
		    socketType, hostname, port ) ;
	}

        @Transport
        @Override
	public boolean shouldCacheConnection() {
	    return false ;
	}

        @InfoMethod
        private void createdConnection( CorbaConnection conn ) { }

        @Transport
        @Override
	public CorbaConnection createConnection() {
            CorbaConnection connection = new NCCConnectionImpl( orb, this,
                socketType, hostname, port ) ;
            createdConnection(connection);
            NCCConnectionCacheImpl cc = NoConnectionCacheImpl.getConnectionCache( orb ) ;
            cc.put( this, connection ) ;
            connection.setConnectionCache( cc ) ;

            return connection ;
	}
    }

    public static class NCCContactInfoListImpl extends CorbaContactInfoListImpl {
	public NCCContactInfoListImpl( ORB orb, IOR ior ) {
	    super( orb, ior ) ;
	}

        @Override
	public CorbaContactInfo createContactInfo( String type, String hostname, 
	    int port ) {

	    return new NCCContactInfoImpl( orb, this, effectiveTargetIOR,
		orb.getORBData().getGIOPAddressDisposition(), type, hostname, port ) ;
	}
    }

    private static class NCCClientRequestDispatcherImpl extends CorbaClientRequestDispatcherImpl {
        @Override
	public void endRequest( ORB broker, Object self, CDRInputObject inputObject ) {
	    super.endRequest( broker, self, inputObject) ;
	    getConnectionCache( broker ).remove( null ) ;
	}
    }

    public void configure( DataCollector dc, final ORB orb ) {
	CorbaContactInfoListFactory factory = new CorbaContactInfoListFactory() {
	    public void setORB(ORB orb) {} 
	    public CorbaContactInfoList create( IOR ior ) {
		return new NCCContactInfoListImpl( orb, ior ) ;
	    }
	} ;

        // Disable a read optimization that forces a blocking read on all
        // OP_READ events.  It makes little sense to enable this optimization
        // when using a NCCConnectionCache because clients will close a
        // Connection when a response to a request has been received.  Hence,
        // if the client knows when it receives a response to a request and
        // then closes the Connection, there's no reason to enable an 
        // optimization that will force a blocking read which will wait for
        // more data to arrive after the client's response has been received.
        orb.getORBData().alwaysEnterBlockingRead( false ) ;

	orb.setCorbaContactInfoListFactory( factory ) ;

	CorbaClientRequestDispatcher crd = new NCCClientRequestDispatcherImpl() ;
	RequestDispatcherRegistry rdr = orb.getRequestDispatcherRegistry() ;
	// Need to register crd with all scids.  Assume range is 0 to MAX_POA_SCID.
	for (int ctr=0; ctr<ORBConstants.MAX_POA_SCID; ctr++) {
	    CorbaClientRequestDispatcher disp = rdr.getClientRequestDispatcher( ctr ) ;
	    if (disp != null) {
		rdr.registerClientRequestDispatcher( crd, ctr ) ;
	    }
	}
    }
}
