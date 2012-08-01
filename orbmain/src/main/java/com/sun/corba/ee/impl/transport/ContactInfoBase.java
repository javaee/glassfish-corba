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

import com.sun.corba.ee.spi.protocol.ClientRequestDispatcher;
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfile;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;
import com.sun.corba.ee.spi.trace.Transport;

/**
 * @author Harold Carr
 */
@Transport
public abstract class ContactInfoBase
    implements
        ContactInfo
{
    protected ORB orb;
    protected ContactInfoList contactInfoList;
    // NOTE: This may be different from same named one in CorbaContactInfoList.
    protected IOR effectiveTargetIOR;
    protected short addressingDisposition;
    protected OutboundConnectionCache connectionCache;

    public ORB getBroker()
    {
        return orb;
    }

    public ContactInfoList getContactInfoList()
    {
        return contactInfoList;
    }

    public ClientRequestDispatcher getClientRequestDispatcher()
    {
        int scid =
            getEffectiveProfile().getObjectKeyTemplate().getSubcontractId() ;
        RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
        return scr.getClientRequestDispatcher( scid ) ;
    }

    // Note: not all derived classes will use a connection cache.
    // These are convenience methods that may not be used.
    public void setConnectionCache(OutboundConnectionCache connectionCache)
    {
        this.connectionCache = connectionCache;
    }

    public OutboundConnectionCache getConnectionCache()
    {
        return connectionCache;
    }

    // Called when client making an invocation.    
    @Transport
    public MessageMediator createMessageMediator(ORB broker,
                                                 ContactInfo contactInfo,
                                                 Connection connection,
                                                 String methodName,
                                                 boolean isOneWay)
    {
        // REVISIT: Would like version, ior, requestid, etc., decisions
        // to be in client subcontract.  Cannot pass these to this
        // factory method because it breaks generic abstraction.
        // Maybe set methods on mediator called from subcontract
        // after creation?
        MessageMediator messageMediator =
            new MessageMediatorImpl(
                (ORB) broker,
                (ContactInfo)contactInfo,
                connection,
                GIOPVersion.chooseRequestVersion( (ORB)broker,
                     effectiveTargetIOR),
                effectiveTargetIOR,
                ((Connection)connection).getNextRequestId(),
                getAddressingDisposition(),
                methodName,
                isOneWay);

        return messageMediator;
    }

    @Transport
    public CDROutputObject createOutputObject(MessageMediator messageMediator) {

        CDROutputObject outputObject =
            new CDROutputObject(orb, messageMediator, 
                                messageMediator.getRequestHeader(),
                                messageMediator.getStreamFormatVersion());

        messageMediator.setOutputObject(outputObject);
        return outputObject;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public short getAddressingDisposition() {
        return addressingDisposition;
    }

    public void setAddressingDisposition(short addressingDisposition) {
        this.addressingDisposition = addressingDisposition;
    }

    // REVISIT - remove this.
    public IOR getTargetIOR() {
        return  contactInfoList.getTargetIOR();
    }

    public IOR getEffectiveTargetIOR() {
        return effectiveTargetIOR ;
    }

    public IIOPProfile getEffectiveProfile() {
        return effectiveTargetIOR.getProfile();
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    public String toString() {
        return "CorbaContactInfoBase[" + "]";
    }
}

// End of file.
