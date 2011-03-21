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

package com.sun.corba.se.spi.transport;

import java.util.Collection;

import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.ObjectAdapterId;

import com.sun.corba.se.impl.protocol.giopmsgheaders.Message ;
//
// REVISIT - impl/poa specific:
import com.sun.corba.se.impl.oa.poa.Policies;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.AMXMetadata ;

/**
 * @author Harold Carr
 */
@ManagedObject
@Description( "The Transport Manager for the ORB" )
@AMXMetadata( isSingleton=true ) 
public interface TransportManager {

    public ByteBufferPool getByteBufferPool(int id);

    @ManagedAttribute
    @Description( "The Selector, which listens for all I/O events" )
    public Selector getSelector();

    public Selector getSelector(int id);

    public void close();

    public static final String SOCKET_OR_CHANNEL_CONNECTION_CACHE =
	"SocketOrChannelConnectionCache";

    @ManagedAttribute
    @Description( "List of all Acceptors in this ORB" ) 
    public Collection<Acceptor> getAcceptors() ;

    public Collection<Acceptor> getAcceptors(String objectAdapterManagerId,
				   ObjectAdapterId objectAdapterId);

    // REVISIT - POA specific policies
    public void addToIORTemplate(IORTemplate iorTemplate, 
				 Policies policies,
				 String codebase,
				 String objectAdapterManagerId,
				 ObjectAdapterId objectAdapterId);

    // Methods for GIOP debugging support

    /** Analyze the header of a message.  This provides enough information to
     * classify the message and group related messages together for use in 
     * the getMessageData method.  Also, if data is a GIOP 1.2 message,
     * the result of this call will contain a valid request ID.
     */
    Message getMessage( byte[] data ) ;

    /** Prepare a series of raw GIOP messages for analysis.
     * messages must be a series of GIOP messages that
     * satisfy the following conditions:
     * <OL>
     * <LI>If there is more than one message, the first message must be
     * a request or a reply.
     * <LI>If there is more than one message, all messages after the first
     * must be fragment messages.
     * <LI>If there is more than one message, all messages must share the
     * same request ID (for GIOP 1.2).
     * <LI>The more fragments bit must be set on all messages except the
     * last message.
     * </OL>
     * An instance of MessageData is returned, with all message headers fully
     * unmarshalled, and the CDRInputStream is positioned at the start of the
     * message body (if any).
     */
    MessageData getMessageData( byte[][] messages ) ;

    /** Return a MessageTraceManager for the current thread.
     * Each thread that calls getMessageTraceManager gets its own
     * independent copy.
     */
    MessageTraceManager getMessageTraceManager() ;

    public OutboundConnectionCache getOutboundConnectionCache(
        ContactInfo contactInfo);

    @ManagedAttribute
    @Description( "Outbound Connection Cache (client initiated connections)" )
    public Collection<OutboundConnectionCache> getOutboundConnectionCaches();

    public InboundConnectionCache getInboundConnectionCache(Acceptor acceptor);

    // Only used for MBeans
    @ManagedAttribute
    @Description( "Inbound Connection Cache (server accepted connections)" )
    public Collection<InboundConnectionCache> getInboundConnectionCaches();

    public void registerAcceptor(Acceptor acceptor);

    public void unregisterAcceptor(Acceptor acceptor);
}
    
// End of file.
