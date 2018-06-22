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

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.encoding.BufferManagerFactory;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;
import com.sun.corba.ee.impl.protocol.SharedCDRClientRequestDispatcherImpl;

public class SharedCDRContactInfoImpl
    extends 
        ContactInfoBase
{
    // This is only necessary for the pi.clientrequestinfo test.
    // It tests that request ids are different.
    // Rather than rewrite the test, just fake it.
    private static int requestId = 0;

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public SharedCDRContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition)
    {
        this.orb = orb;
        this.contactInfoList = contactInfoList;
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }

    public String getType()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public String getHost()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public int getPort()
    {
        throw wrapper.undefinedSocketinfoOperation() ;
    }

    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        // REVISIT - use registry
        return new SharedCDRClientRequestDispatcherImpl();
    }

    public boolean isConnectionBased()
    {
        return false;
    }

    public boolean shouldCacheConnection()
    {
        return false;
    }

    public String getConnectionCacheType()
    {
        throw wrapper.methodShouldNotBeCalled();
    }
    
    public Connection createConnection()
    {
        throw wrapper.methodShouldNotBeCalled();
    }

    // Called when client making an invocation.    
    @Override
    public MessageMediator createMessageMediator(ORB broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay)
    {
        if (connection != null) {
            throw wrapper.connectionNotNullInCreateMessageMediator( connection ) ;
        }

        MessageMediator messageMediator =
            new MessageMediatorImpl(
                (ORB) broker,
                (ContactInfo)contactInfo,
                null, // Connection;
                GIOPVersion.chooseRequestVersion( (ORB)broker,
                     effectiveTargetIOR),
                effectiveTargetIOR,
                requestId++, // Fake RequestId
                getAddressingDisposition(),
                methodName,
                isOneWay);

        return messageMediator;
    }

    public CDROutputObject createOutputObject(MessageMediator messageMediator)
    {
        MessageMediator corbaMessageMediator = (MessageMediator)
            messageMediator;
        // NOTE: GROW.
        CDROutputObject outputObject =
            OutputStreamFactory.newCDROutputObject(orb, messageMediator, 
                                corbaMessageMediator.getRequestHeader(),
                                corbaMessageMediator.getStreamFormatVersion(),
                                BufferManagerFactory.GROW);
        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public String getMonitoringName()
    {
        throw wrapper.methodShouldNotBeCalled();
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    // NOTE: hashCode and equals are CRITICAL to IIOP failover implementation.
    // See SocketOrChannelContactInfoImpl.equals.

    // This calculation must be identical to SocketOrChannelContactInfoImpl.
    private int hashCode = 
        SocketInfo.IIOP_CLEAR_TEXT.hashCode() + "localhost".hashCode() ^ -1;

    public int hashCode()
    {
        return hashCode;
    }

    public boolean equals(Object obj)
    {
        return obj instanceof SharedCDRContactInfoImpl;
    }

    public String toString()
    {
        return
            "SharedCDRContactInfoImpl[" 
            + "]";
    }

    //////////////////////////////////////////////////
    //
    // Implementation
    //
}

// End of file.
