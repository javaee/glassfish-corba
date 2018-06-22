/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.spi.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;

import org.omg.CORBA.SystemException;

import com.sun.org.omg.SendingContext.CodeBase;


import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.RequestId;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;

/**
 * @author Harold Carr
 */
public interface Connection
    extends
        com.sun.corba.ee.spi.legacy.connection.Connection
{
    /**
     * Used to determine if the <code>Connection</code> should register
     * with the CorbaTransportManager Selector
     * to handle read events.
     *
     * For example, an HTTP transport would not register since the requesting
     * thread would just block on read when waiting for the reply.
     *
     * @return <code>true</code> if it should be registered.
     */
    public boolean shouldRegisterReadEvent();

    /**
     * Used to determine if the <code>Connection</code> should register
     * with the 
     * CorbaTransportManager Selector
     * to handle read events.
     *
     * For example, an HTTP transport would not register since the requesting
     * thread would just block on read when waiting for the reply.
     *
     * @return <code>true</code> if it should be registered.
     */
    public boolean shouldRegisterServerReadEvent(); // REVISIT - why special?

    /**
     * Called to read incoming messages.
     *
     * @return <code>true</code> if the thread calling read can be released.
     */
    public boolean read();

    public void close();

    // REVISIT: replace next two with PlugInFactory (implemented by ContactInfo
    // and Acceptor).

    public Acceptor getAcceptor();

    public ContactInfo getContactInfo();

    public EventHandler getEventHandler();

    /**
     * Indicates whether a CorbaContactInfo or CorbaAcceptor
     * created the
     * <code>Connection</code>.
     *
     * @return <code>true</code> if a CorbaAcceptor
     * created the <code>Connection</code>.
     */
    public boolean isServer();

    /**
     * Indicates if the <code>Connection</code> is closed.
     *
     * @return <code>true</code> if the <code>Connection</code> is closed.
     */
    public boolean isClosed();

    /**
     * Indicates if the <code>Connection</code> is in the process of
     * sending or receiving a message.
     *
     * @return <code>true</code> if the <code>Connection</code> is busy.
     */
    public boolean isBusy();

    /**
     * Timestamps are used for connection management, in particular, for
     * reclaiming idle <code>Connection</code>s.
     *
     * @return the "time" the <code>Connection</code> was last used.
     */
    public long getTimeStamp();

    /**
     * Timestamps are used for connection management, in particular, for
     * reclaiming idle <code>Connection</code>s.
     *
     * @param time - the "time" the <code>Connection</code> was last used.
     */
    public void setTimeStamp(long time);

    /**
     * The "state" of the <code>Connection</code>.
     *
     * param state
     */
    public void setState(String state);

    /**
     * Grab a write lock on the <code>Connection</code>.
     *
     * If another thread already has a write lock then the calling
     * thread will block until the lock is released.  The calling
     * thread must call
     * {@link #writeUnlock}
     * when it is done.
     */
    public void writeLock();

    /**
     * Release a write lock on the <code>Connection</code>.
     */
    public void writeUnlock();

    /*
     * Send the data encoded in
     * {@link com.sun.corba.ee.impl.encoding.CDROutputObject CDROutputObject}
     * on the <code>Connection</code>.
     *
     * @param outputObject
     */
    public void sendWithoutLock(CDROutputObject outputObject);

    /**
     * Register an invocation's CorbaMessageMediator
     * with the <code>Connection</code>.
     *
     * This is useful in protocols which support fragmentation.
     *
     * @param messageMediator
     */
    public void registerWaiter(MessageMediator messageMediator);

    /**
     * If a message expect's a response then this method is called.
     *
     * This method might block on a read (e.g., HTTP), put the calling
     * thread to sleep while another thread read's the response (e.g., GIOP),
     * or it may use the calling thread to perform the server-side work
     * (e.g., Solaris Doors).
     *
     * @param messageMediator
     */
    public CDRInputObject waitForResponse(MessageMediator messageMediator);

    /**
     * Unregister an invocation's * CorbaMessageMediator
     * with the <code>Connection</code>.
     *
     * @param messageMediator
     */
    public void unregisterWaiter(MessageMediator messageMediator);

    public void setConnectionCache(ConnectionCache connectionCache);

    public ConnectionCache getConnectionCache();
    public boolean hasSocketChannel();

    public void write(ByteBuffer byteBuffer)
        throws IOException;

    public int getNextRequestId();
    public ORB getBroker();
    public CodeSetComponentInfo.CodeSetContext getCodeSetContext();
    public void setCodeSetContext(CodeSetComponentInfo.CodeSetContext csc);

    // Facade to ResponseWaitingRoom.
    public MessageMediator clientRequestMapGet(int requestId);

    public void clientReply_1_1_Put(MessageMediator x);
    public MessageMediator clientReply_1_1_Get();
    public void clientReply_1_1_Remove();

    public void serverRequest_1_1_Put(MessageMediator x);
    public MessageMediator serverRequest_1_1_Get();
    public void serverRequest_1_1_Remove();

    public boolean isPostInitialContexts();

    // Can never be unset...
    public void setPostInitialContexts();

    public void purgeCalls(SystemException systemException,
                           boolean die, boolean lockHeld);

    //
    // Connection status
    //
    public static final int OPENING = 1;
    public static final int ESTABLISHED = 2;
    public static final int CLOSE_SENT = 3;
    public static final int CLOSE_RECVD = 4;
    public static final int ABORT = 5;

    // Begin Code Base methods ---------------------------------------
    //
    // Set this connection's code base IOR.  The IOR comes from the
    // SendingContext.  This is an optional service context, but all
    // JavaSoft ORBs send it.
    //
    // The set and get methods don't need to be synchronized since the
    // first possible get would occur during reading a valuetype, and
    // that would be after the set.

    // Sets this connection's code base IOR.  This is done after
    // getting the IOR out of the SendingContext service context.
    // Our ORBs always send this, but it's optional in CORBA.

    void setCodeBaseIOR(IOR ior);

    IOR getCodeBaseIOR();

    // Get a CodeBase stub to use in unmarshaling.  The CachedCodeBase
    // won't connect to the remote codebase unless it's necessary.
    CodeBase getCodeBase();

    // End Code Base methods -----------------------------------------

    public void sendCloseConnection(GIOPVersion giopVersion)
        throws IOException;

    public void sendMessageError(GIOPVersion giopVersion)
        throws IOException;

    public void sendCancelRequest(GIOPVersion giopVersion, int requestId)
        throws
            IOException;

    // NOTE: This method can throw a connection rebind SystemException.
    public void sendCancelRequestWithLock(GIOPVersion giopVersion,
                                          int requestId)
        throws 
            IOException;

    public ResponseWaitingRoom getResponseWaitingRoom();

    public void serverRequestMapPut(int requestId,
                                    MessageMediator messageMediator);
    public MessageMediator serverRequestMapGet(int requestId);
    public void serverRequestMapRemove(int requestId);

    public Queue<MessageMediator> getFragmentList(RequestId corbaRequestId);
    public void removeFragmentList(RequestId corbaRequestId);

    // REVISIT: WRONG: should not expose sockets here.
    public SocketChannel getSocketChannel();

    // REVISIT - CorbaMessageMediator parameter?
    public void serverRequestProcessingBegins();
    public void serverRequestProcessingEnds();

    /** Clean up all connection resources.  Used when shutting down an ORB.
     */
    public void closeConnectionResources() ;
}

// End of file.

