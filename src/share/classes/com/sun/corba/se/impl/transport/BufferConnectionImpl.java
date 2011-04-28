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

package com.sun.corba.se.impl.transport;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import org.omg.CORBA.SystemException;

import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.corba.se.spi.transport.Acceptor;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.threadpool.Work;
import com.sun.corba.se.spi.protocol.RequestId;
import com.sun.corba.se.spi.transport.Connection;
import com.sun.corba.se.spi.transport.ResponseWaitingRoom;

import com.sun.corba.se.impl.encoding.CachedCodeBase;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.misc.ORBConstants;
import com.sun.corba.se.impl.misc.ORBUtility;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.se.spi.protocol.MessageMediator;
import com.sun.corba.se.spi.transport.ConnectionCache;
import com.sun.corba.se.spi.transport.ContactInfo;
import com.sun.corba.se.spi.transport.EventHandler;


/**
 * @author Ken Cavanaugh
 */
public class BufferConnectionImpl
    extends
	EventHandlerBase
    implements
        Connection,
	Work
{
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    //
    // New transport.
    //

    protected long enqueueTime;

    public SocketChannel getSocketChannel()
    {
	return null;
    }

    // REVISIT:
    // protected for test: genericRPCMSGFramework.IIOPConnection constructor.

    //
    // From iiop.Connection.java
    //

    protected long timeStamp = 0;
    protected boolean isServer = false;

    // Start at some value other than zero since this is a magic
    // value in some protocols.
    protected int requestId = 5;
    protected ResponseWaitingRoom responseWaitingRoom;
    protected int state;
    protected final java.lang.Object stateEvent = new java.lang.Object();
    protected final java.lang.Object writeEvent = new java.lang.Object();
    protected boolean writeLocked;
    protected int serverRequestCount = 0;
    
    // Server request map: used on the server side of Connection
    // Maps request ID to IIOPInputStream.
    Map<Integer, MessageMediator> serverRequestMap =
                      new HashMap<Integer, MessageMediator>() ;

    ConcurrentHashMap<RequestId,Queue> fragmentMap =
	new ConcurrentHashMap<RequestId,Queue>();
    
    // This is a flag associated per connection telling us if the
    // initial set of sending contexts were sent to the receiver
    // already...
    protected boolean postInitialContexts = false;
 
    // Remote reference to CodeBase server (supplies
    // FullValueDescription, among other things)
    protected IOR codeBaseServerIOR;

    // CodeBase cache for this connection.  This will cache remote operations,
    // handle connecting, and ensure we don't do any remote operations until
    // necessary.
    protected CachedCodeBase cachedCodeBase = new CachedCodeBase(this);


    List buffers ;

    public BufferConnectionImpl(ORB orb)
    {
	this.orb = orb;
	buffers = new ArrayList() ;
    }

    ////////////////////////////////////////////////////
    //
    // framework.transport.Connection
    //

    public boolean isClosed()
    {
	return false ;
    }

    public boolean shouldRegisterReadEvent()
    {
	return false;
    }

    public boolean shouldRegisterServerReadEvent()
    {
	return false;
    }

    public boolean read()
    {
	return true ;
    }

    protected MessageMediator readBits()
    {
	return null ;
    }

    protected boolean dispatch(MessageMediator messageMediator)
    {
	return false ;
    }

    public boolean shouldUseDirectByteBuffers()
    {
	return false ;
    }

    // Only called from readGIOPMessage with (12, 0, 12) as arguments
    // size is size of buffer to create
    // offset is offset from start of message in buffer
    // length is length to read
    public ByteBuffer read(int size, int offset, int length)
	throws IOException
    {
	byte[] buf = new byte[size];
	readFully( buf, offset, length);
	ByteBuffer byteBuffer = ByteBuffer.wrap(buf);
	byteBuffer.limit(size);
	return byteBuffer;
    }

    // Only called as read( buf, 12, msgsize-12 ) in readGIOPMessage
    // We can ignore the byteBuffer parameter
    // offset is the starting position to place data in the result
    // length is the length of the data to read
    public ByteBuffer read(ByteBuffer byteBuffer, int offset, int length
	) throws IOException
    {
	int size = offset + length;
	byte[] buf = new byte[size];
	readFully(buf, offset, length);
	return ByteBuffer.wrap(buf);
    }

    // Read size bytes from buffer list and place the data
    // starting at offset in buf.
    private void readFully(byte[] buf, int offset, int size) 
	throws IOException
    {
	int remaining = size ;
	int position = offset ;
	int index = 0 ;
	while (remaining > 0) {
	    ByteBuffer buff = (ByteBuffer)buffers.get(0) ; 
	    if (buff == null) {
                throw new IOException("No more data");
            }
	    int dataSize = buff.remaining() ;
	    int xferSize = dataSize ;
	    if (dataSize >= remaining) {
		xferSize = remaining ;
	    } else {
		buffers.remove(0) ;
	    }
	    
	    buff.get( buf, offset, xferSize ) ;

	    offset += xferSize ;
	    remaining -= xferSize ;
	}
    }    

    public void write(ByteBuffer byteBuffer)
	throws IOException
    {
	buffers.add( byteBuffer ) ;
    }

    public void closeConnectionResources() 
    {
    }

    /**
     * Note:it is possible for this to be called more than once
     */
    public synchronized void close() 
    {
    }

    public Acceptor getAcceptor()
    {
	return null;
    }

    public ContactInfo getContactInfo()
    {
	return null;
    }

    public EventHandler getEventHandler()
    {
	return this;
    }

    public CDROutputObject createOutputObject(MessageMediator messageMediator)
    {
	// REVISIT - remove this method from Connection and all it subclasses.
	throw new RuntimeException("*****SocketOrChannelConnectionImpl.createOutputObject - should not be called.");
    }

    // This is used by the GIOPOutputObject in order to
    // throw the correct error when handling code sets.
    // Can we determine if we are on the server side by
    // other means?  XREVISIT
    public boolean isServer()
    {
        return isServer;
    }

    public boolean isBusy()
    {
	return false ;
    }

    public long getTimeStamp()
    {
	return timeStamp;
    }

    public void setTimeStamp(long time)
    {
	timeStamp = time;
    }

    public void setState(String stateString)
    {
	synchronized (stateEvent) {
	    if (stateString.equals("ESTABLISHED")) {
		state =  ESTABLISHED;
		stateEvent.notifyAll();
	    } else {
		// REVISIT: ASSERT
	    }
	}
    }

    public void writeLock()
    {
    }

    public void writeUnlock()
    {
    }

    public void sendWithoutLock(CDROutputObject outputObject)
    {
    }

    public void registerWaiter(MessageMediator messageMediator)
    {
    }

    public void unregisterWaiter(MessageMediator messageMediator)
    {
    }

    public CDRInputObject waitForResponse(MessageMediator messageMediator)
    {
	return null ;
    }

    public void setConnectionCache(ConnectionCache connectionCache)
    {
    }

    public ConnectionCache getConnectionCache()
    {
	return null;	
    }

    ////////////////////////////////////////////////////
    //
    // EventHandler methods
    //

    public SelectableChannel getChannel()
    {
	return null;
    }

    public int getInterestOps()
    {
	return 0;
    }

    //    public Acceptor getAcceptor() - already defined above.

    public Connection getConnection()
    {
	return this;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    public String getName()
    {
	return this.toString();
    }

    public void doWork()
    {
    }

    public void setEnqueueTime(long timeInMillis)
    {
	enqueueTime = timeInMillis;
    }

    public long getEnqueueTime()
    {
	return enqueueTime;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaConnection.
    //

    public ResponseWaitingRoom getResponseWaitingRoom()
    {
	return null ;
    }

    // REVISIT - inteface defines isServer but already defined in 
    // higher interface.


    public void serverRequestMapPut(int requestId, 
				    MessageMediator messageMediator)
    {
	serverRequestMap.put(requestId, messageMediator);
    }

    public MessageMediator serverRequestMapGet(int requestId)
    {
	return serverRequestMap.get(requestId);
    }

    public void serverRequestMapRemove(int requestId)
    {
	serverRequestMap.remove(requestId);
    }

    public Queue<MessageMediator> getFragmentList(RequestId requestId) {
        return fragmentMap.get(requestId);
    }
    
    public void removeFragmentList(RequestId requestId) {
        fragmentMap.remove(requestId);
    }

    // REVISIT: this is also defined in:
    // com.sun.corba.se.spi.legacy.connection.Connection
    public java.net.Socket getSocket()
    {
	return null;
    }

    /** It is possible for a Close Connection to have been
     ** sent here, but we will not check for this. A "lazy"
     ** Exception will be thrown in the Worker thread after the
     ** incoming request has been processed even though the connection
     ** is closed before the request is processed. This is o.k because
     ** it is a boundary condition. To prevent it we would have to add
     ** more locks which would reduce performance in the normal case.
     **/
    public synchronized void serverRequestProcessingBegins()
    {
        serverRequestCount++;
    }

    public synchronized void serverRequestProcessingEnds()
    {
        serverRequestCount--;
    }

    //
    //
    //

    public synchronized int getNextRequestId() 
    {
	return requestId++;
    }

    // Negotiated code sets for char and wchar data
    protected CodeSetComponentInfo.CodeSetContext codeSetContext = null;

    public ORB getBroker() 
    {
        return orb;
    }

    public synchronized CodeSetComponentInfo.CodeSetContext getCodeSetContext() 
    {
        return codeSetContext;
    }

    public synchronized void setCodeSetContext(CodeSetComponentInfo.CodeSetContext csc) {
        if (codeSetContext == null) {
            
            if (OSFCodeSetRegistry.lookupEntry(csc.getCharCodeSet()) == null ||
                OSFCodeSetRegistry.lookupEntry(csc.getWCharCodeSet()) == null) {
                // If the client says it's negotiated a code set that
                // isn't a fallback and we never said we support, then
                // it has a bug.
		throw wrapper.badCodesetsFromClient() ;
            }

            codeSetContext = csc;
        }
    }

    //
    // from iiop.IIOPConnection.java
    //

    // Map request ID to an InputObject.
    // This is so the client thread can start unmarshaling
    // the reply and remove it from the out_calls map while the
    // ReaderThread can still obtain the input stream to give
    // new fragments.  Only the ReaderThread touches the clientReplyMap,
    // so it doesn't incur synchronization overhead.

    public MessageMediator clientRequestMapGet(int requestId)
    {
	return null ;
    }

    protected MessageMediator clientReply_1_1;

    public void clientReply_1_1_Put(MessageMediator x)
    {
	clientReply_1_1 = x;
    }

    public MessageMediator clientReply_1_1_Get()
    {
	return 	clientReply_1_1;
    }

    public void clientReply_1_1_Remove()
    {
	clientReply_1_1 = null;
    }

    protected MessageMediator serverRequest_1_1;

    public void serverRequest_1_1_Put(MessageMediator x)
    {
	serverRequest_1_1 = x;
    }

    public MessageMediator serverRequest_1_1_Get()
    {
	return 	serverRequest_1_1;
    }

    public void serverRequest_1_1_Remove()
    {
	serverRequest_1_1 = null;
    }

    protected String getStateString( int state ) 
    {
        synchronized ( stateEvent ){
            switch (state) {
            case OPENING : return "OPENING" ;
            case ESTABLISHED : return "ESTABLISHED" ;
            case CLOSE_SENT : return "CLOSE_SENT" ;
            case CLOSE_RECVD : return "CLOSE_RECVD" ;
            case ABORT : return "ABORT" ;
            default : return "???" ;
            }
        }
    }
    
    public synchronized boolean isPostInitialContexts() {
        return postInitialContexts;
    }

    // Can never be unset...
    public synchronized void setPostInitialContexts(){
        postInitialContexts = true;
    }
    
    /**
     * Wake up the outstanding requests on the connection, and hand them
     * COMM_FAILURE exception with a given minor code.
     *
     * Also, delete connection from connection table and
     * stop the reader thread.

     * Note that this should only ever be called by the Reader thread for
     * this connection.
     * 
     * @param minor_code The minor code for the COMM_FAILURE major code.
     * @param die Kill the reader thread (this thread) before exiting.
     */
    public void purgeCalls(SystemException systemException,
			   boolean die, boolean lockHeld)
    {
    }

    /*************************************************************************
    * The following methods are for dealing with Connection cleaning for
    * better scalability of servers in high network load conditions.
    **************************************************************************/

    public void sendCloseConnection(GIOPVersion giopVersion)
	throws IOException 
    {
        Message msg = MessageBase.createCloseConnection(giopVersion);
	sendHelper(giopVersion, msg);
    }

    public void sendMessageError(GIOPVersion giopVersion)
	throws IOException 
    {
        Message msg = MessageBase.createMessageError(giopVersion);
	sendHelper(giopVersion, msg);
    }

    /**
     * Send a CancelRequest message. This does not lock the connection, so the
     * caller needs to ensure this method is called appropriately.
     * @exception IOException - could be due to abortive connection closure.
     */
    public void sendCancelRequest(GIOPVersion giopVersion, int requestId)
	throws IOException 
    {

        Message msg = MessageBase.createCancelRequest(giopVersion, requestId);
	sendHelper(giopVersion, msg);
    }

    protected void sendHelper(GIOPVersion giopVersion, Message msg)
	throws IOException
    {
	// REVISIT: See comments in CDROutputObject constructor.
        CDROutputObject outputObject = 
	    new CDROutputObject((ORB)orb, null, giopVersion, this, msg,
				ORBConstants.STREAM_FORMAT_VERSION_1);
        msg.write(outputObject);

	outputObject.writeTo(this);
    }

    public void sendCancelRequestWithLock(GIOPVersion giopVersion,
					  int requestId)
	throws IOException 
    {
	writeLock();
	try {
	    sendCancelRequest(giopVersion, requestId);
	} finally {
	    writeUnlock();
	}
    }

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

    public final void setCodeBaseIOR(IOR ior) {
        codeBaseServerIOR = ior;
    }

    public final IOR getCodeBaseIOR() {
        return codeBaseServerIOR;
    }

    // Get a CodeBase stub to use in unmarshaling.  The CachedCodeBase
    // won't connect to the remote codebase unless it's necessary.
    public final CodeBase getCodeBase() {
        return cachedCodeBase;
    }

    // End Code Base methods -----------------------------------------

    // Can be overridden in subclass for different options.
    protected void setSocketOptions(Socket socket)
    {
    }

    @Override
    public String toString()
    {
        synchronized ( stateEvent ){
            return 
		"BufferConnectionImpl[" + " "
		+ getStateString( state ) + " "
		+ shouldUseSelectThreadToWait() + " "
		+ shouldUseWorkerThreadForEvent()
		+ "]" ;
        }
    }
    
    // Must be public - used in encoding.
    public void dprint(String msg) 
    {
	ORBUtility.dprint("SocketOrChannelConnectionImpl", msg);
    }

    protected void dprint(String msg, Throwable t)
    {
	dprint(msg);
	t.printStackTrace(System.out);
    }
}

// End of file.
