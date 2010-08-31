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

package com.sun.corba.se.impl.transport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.nio.ByteBuffer ;
import java.io.IOException ;

import com.sun.corba.se.spi.transport.Selector;

import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.ByteBufferPool;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.transport.MessageData;
import com.sun.corba.se.spi.transport.MessageTraceManager;

// REVISIT - impl/poa specific:
import com.sun.corba.se.impl.oa.poa.Policies;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.BufferManagerRead;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message_1_2;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.se.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Transport;

import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaInboundConnectionCache;
import com.sun.corba.se.spi.transport.CorbaOutboundConnectionCache;

import org.glassfish.external.probe.provider.StatsProviderManager ;
import org.glassfish.external.probe.provider.PluginPoint ;

/**
 * @author Harold Carr
 */
// Note that no ObjectKeyName attribute is needed, because there is only
// one CorbaTransportManager per ORB.
@Transport
public class CorbaTransportManagerImpl 
    implements
	CorbaTransportManager
{
    protected ORB orb;
    protected List<CorbaAcceptor> acceptors;
    protected final Map<String,CorbaOutboundConnectionCache> outboundConnectionCaches;
    protected final Map<String,CorbaInboundConnectionCache> inboundConnectionCaches;
    protected Selector selector;
    
    public CorbaTransportManagerImpl(ORB orb) 
    {
	this.orb = orb;
	acceptors = new ArrayList<CorbaAcceptor>();
	outboundConnectionCaches = new HashMap<String,CorbaOutboundConnectionCache>();
	inboundConnectionCaches = new HashMap<String,CorbaInboundConnectionCache>();
	selector = new SelectorImpl(orb);
        orb.mom().register( orb, this ) ;
    }

    public ByteBufferPool getByteBufferPool(int id)
    {
	throw new RuntimeException(); 
    }

    public CorbaOutboundConnectionCache getOutboundConnectionCache(
        CorbaContactInfo contactInfo)
    {
	synchronized (contactInfo) {
	    if (contactInfo.getConnectionCache() == null) {
		CorbaOutboundConnectionCache connectionCache = null;
		synchronized (outboundConnectionCaches) {
		    connectionCache = outboundConnectionCaches.get(
                        contactInfo.getConnectionCacheType());
		    if (connectionCache == null) {
			// REVISIT: Would like to be able to configure
			// the connection cache type used.
			connectionCache = 
			    new CorbaOutboundConnectionCacheImpl(orb,
								 contactInfo);

                        // XXX We need to clean up the multi-cache support:
                        // this really only works with a single cache.
                        orb.mom().register( this, connectionCache ) ;
                        StatsProviderManager.register( "orb", PluginPoint.SERVER,
                            "orb/transport/connectioncache/outbound", connectionCache ) ;

			outboundConnectionCaches.put(
                            contactInfo.getConnectionCacheType(),
			    connectionCache);
		    }
		}
		contactInfo.setConnectionCache(connectionCache);
	    }
	    return contactInfo.getConnectionCache();
	}
    }

    public Collection<CorbaOutboundConnectionCache> getOutboundConnectionCaches()
    {
	return outboundConnectionCaches.values();
    }

    public Collection<CorbaInboundConnectionCache> getInboundConnectionCaches()
    {
	return inboundConnectionCaches.values();
    }

    public CorbaInboundConnectionCache getInboundConnectionCache(
        CorbaAcceptor acceptor)
    {
	synchronized (acceptor) {
	    if (acceptor.getConnectionCache() == null) {
		CorbaInboundConnectionCache connectionCache = null;
		synchronized (inboundConnectionCaches) {
		    connectionCache = inboundConnectionCaches.get(
                            acceptor.getConnectionCacheType());
		    if (connectionCache == null) {
			// REVISIT: Would like to be able to configure
			// the connection cache type used.
			connectionCache = 
			    new CorbaInboundConnectionCacheImpl(orb,
								acceptor);
                        orb.mom().register( this, connectionCache ) ;
                        StatsProviderManager.register( "orb", PluginPoint.SERVER,
                            "orb/transport/connectioncache/inbound", connectionCache ) ;

			inboundConnectionCaches.put(
                            acceptor.getConnectionCacheType(),
			    connectionCache);
		    }
		}
		acceptor.setConnectionCache(connectionCache);
	    }
	    return acceptor.getConnectionCache();
	}
    }

    public Selector getSelector() {
        return selector ;
    }

    public Selector getSelector(int id) 
    {
	return selector;
    }

    @Transport
    public synchronized void registerAcceptor(CorbaAcceptor acceptor) {
	acceptors.add(acceptor);
    }

    @Transport
    public synchronized void unregisterAcceptor(CorbaAcceptor acceptor) {
	acceptors.remove(acceptor);
    }

    @Transport
    public void close()
    {
        for (CorbaOutboundConnectionCache cc : outboundConnectionCaches.values()) {
            StatsProviderManager.unregister( cc ) ;
            cc.close() ;
        }
        for (CorbaInboundConnectionCache cc : inboundConnectionCaches.values()) {
            StatsProviderManager.unregister( cc ) ;
            cc.close() ;
        }
        getSelector(0).close();
    }

    ////////////////////////////////////////////////////
    //
    // CorbaTransportManager
    //

    public Collection<CorbaAcceptor> getAcceptors() {
        return getAcceptors( null, null ) ;
    }

    @InfoMethod
    private void display( String msg ) { }

    @Transport
    public Collection<CorbaAcceptor> getAcceptors(String objectAdapterManagerId,
				   ObjectAdapterId objectAdapterId)
    {
	// REVISIT - need to filter based on arguments.

	// REVISIT - initialization will be moved to OA.
	// Lazy initialization of acceptors.
        for (CorbaAcceptor acc : acceptors) {
	    if (acc.initialize()) {
                display( "initializing acceptors" ) ;
		if (acc.shouldRegisterAcceptEvent()) {
		    orb.getTransportManager().getSelector(0)
			.registerForEvent(acc.getEventHandler());
		}
	    }
	}
	return acceptors;
    }

    // REVISIT - POA specific policies
    @Transport
    public void addToIORTemplate(IORTemplate iorTemplate, 
				 Policies policies,
				 String codebase,
				 String objectAdapterManagerId,
				 ObjectAdapterId objectAdapterId)
    {
	Iterator iterator = 
	    getAcceptors(objectAdapterManagerId, objectAdapterId).iterator();
	while (iterator.hasNext()) {
	    CorbaAcceptor acceptor = (CorbaAcceptor) iterator.next();
	    acceptor.addToIORTemplate(iorTemplate, policies, codebase);
	}
    }

    public Message getMessage( byte[] data )
    {
        CorbaConnection connection = new BufferConnectionImpl(orb) ;
	ByteBuffer bb = ByteBuffer.allocate( data.length ) ;
	bb.put( data ) ;
	bb.position( 0 ) ;
	try {
	    connection.write( bb ) ;
	} catch (IOException exc) {
	    // should never happen in this case
	}

	Message msg = MessageBase.readGIOPMessage( orb, connection ) ;
	if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
	    ((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ; 

	return msg ;
    }

    public MessageData getMessageData( byte[][] data ) 
    {
        CorbaConnection connection = new BufferConnectionImpl(orb) ;
	for (int ctr=0; ctr<data.length; ctr++) {
	    byte[] message = data[ctr] ;
	    ByteBuffer bb = ByteBuffer.allocate( message.length ) ;
	    bb.put( message ) ;
	    bb.position( 0 ) ;
	    try {
		connection.write( bb ) ;
	    } catch (IOException exc) {
		// should never happen in this case
	    }
	}

	final Message[] messages = new Message[data.length] ;
	int requestID = 0 ;
	Message firstMessage = null ;
	Message msg = null ;
	CDRInputObject inobj = null ;
	BufferManagerRead buffman = null ;

	for (int ctr=0; ctr<data.length; ctr++) {
	    msg = MessageBase.readGIOPMessage( orb, connection ) ;
	    messages[ctr] = msg ;
	    if (msg.getGIOPVersion().equals( GIOPVersion.V1_2 ))
		((Message_1_2)msg).unmarshalRequestID( msg.getByteBuffer() ) ; 
	    
	    // XXX Check that moreFragments == (ctr < messages.length)

	    if (ctr==0) {
		// XXX Check that we have a request or reply
		// only if Message_1_2: requestID = msg.getRequestID() ;
		firstMessage = msg ;
		inobj = new CDRInputObject( orb, connection,
		    msg.getByteBuffer(), msg ) ;
		buffman = inobj.getBufferManager() ;
		inobj.performORBVersionSpecificInit() ;
	    } else {
		// XXX Check that the request ID is as expected
		buffman.processFragment( msg.getByteBuffer(), (FragmentMessage)msg ) ;
	    }
	}

	// Unmarshal all the data in the first message.  This may 
	// cause other fragments to be read.
	firstMessage.read( inobj ) ;

	final CDRInputObject resultObj = inobj ;

	return new MessageData() {
	   public Message[] getMessages() { return messages ; }
	   public CDRInputObject getStream() { return resultObj ; }
	} ;
    }

    private ThreadLocal currentMessageTraceManager =
	new ThreadLocal() {
	    public Object initialValue() 
	    {
		return new MessageTraceManagerImpl( ) ;
	    }
	} ;

    public MessageTraceManager getMessageTraceManager() 
    {
	return (MessageTraceManager)(currentMessageTraceManager.get()) ;
    }
}

// End of file.
