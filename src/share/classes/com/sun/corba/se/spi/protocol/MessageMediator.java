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

package com.sun.corba.se.spi.protocol;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.Connection;
import java.nio.ByteBuffer;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.transport.ContactInfo;

import com.sun.corba.se.impl.protocol.giopmsgheaders.LocateReplyMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.LocateReplyOrReplyMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.RequestMessage;

/**
 * @author Harold Carr
 */
public abstract interface MessageMediator
    extends
	ResponseHandler
{
    public void setReplyHeader(LocateReplyOrReplyMessage header);
    public LocateReplyMessage getLocateReplyHeader();
    public ReplyMessage getReplyHeader();
    public void setReplyExceptionDetailMessage(String message);
    public RequestMessage getRequestHeader();
    public GIOPVersion getGIOPVersion();
    public byte getEncodingVersion();
    public int getRequestId();
    public Integer getRequestIdInteger();
    public boolean isOneWay();
    public short getAddrDisposition();
    public String getOperationName();
    public ServiceContexts getRequestServiceContexts();
    public void setRequestServiceContexts(ServiceContexts sc);
    public ServiceContexts getReplyServiceContexts();
    public Message getDispatchHeader();
    public void setDispatchHeader(Message msg);
    public ByteBuffer getDispatchBuffer();
    public void setDispatchBuffer(ByteBuffer byteBuffer);
    public int getThreadPoolToUse();
    public boolean dispatch();
    public byte getStreamFormatVersion(); // REVIST name ForRequest?
    public byte getStreamFormatVersionForReply();

    // REVISIT - not sure if the final fragment and DII stuff should
    // go here.

    public void sendCancelRequestIfFinalFragmentNotSent();

    public void setDIIInfo(org.omg.CORBA.Request request);
    public boolean isDIIRequest();
    public Exception unmarshalDIIUserException(String repoId,
					       InputStream inputStream);
    public void setDIIException(Exception exception);
    public void handleDIIReply(InputStream inputStream);

    public boolean isSystemExceptionReply();
    public boolean isUserExceptionReply();
    public boolean isLocationForwardReply();
    public boolean isDifferentAddrDispositionRequestedReply();
    public short getAddrDispositionReply();
    public IOR getForwardedIOR();
    public SystemException getSystemExceptionReply();
    public void cancelRequest();

    ////////////////////////////////////////////////////
    //
    // Server side
    //

    public ObjectKeyCacheEntry getObjectKeyCacheEntry();
    public void setProtocolHandler(ProtocolHandler protocolHandler);
    public ProtocolHandler getProtocolHandler();

    ////////////////////////////////////////////////////
    //
    // ResponseHandler
    //

    public org.omg.CORBA.portable.OutputStream createReply();
    public org.omg.CORBA.portable.OutputStream createExceptionReply();

    ////////////////////////////////////////////////////
    //
    // from core.ServerRequest
    //

    public boolean executeReturnServantInResponseConstructor();

    public void setExecuteReturnServantInResponseConstructor(boolean b);

    public boolean executeRemoveThreadInfoInResponseConstructor();

    public void setExecuteRemoveThreadInfoInResponseConstructor(boolean b);

    public boolean executePIInResponseConstructor();

    public void setExecutePIInResponseConstructor( boolean b );

    public ORB getBroker();

    public ContactInfo getContactInfo();

    public Connection getConnection();

    /**
     * Used to initialize message headers.
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void initializeMessage();

    /**
     * Used to send the message (or its last fragment).
     *
     * Note: this should be moved to a <code>RequestDispatcher</code>.
     */
    public void finishSendingRequest();

    public CDRInputObject waitForResponse();

    public void setOutputObject(CDROutputObject outputObject);

    public CDROutputObject getOutputObject();

    public void setInputObject(CDRInputObject inputObject);

    public CDRInputObject getInputObject();
}

// End of file.

