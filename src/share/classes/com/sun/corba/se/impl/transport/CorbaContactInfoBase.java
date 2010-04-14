/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.nio.ByteBuffer;

import com.sun.corba.se.spi.protocol.CorbaClientRequestDispatcher;
import com.sun.corba.se.spi.transport.CorbaOutboundConnectionCache;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
import com.sun.corba.se.spi.transport.CorbaContactInfo;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.protocol.CorbaMessageMediatorImpl;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.se.spi.trace.Transport;

/**
 * @author Harold Carr
 */
public abstract class CorbaContactInfoBase
    implements
	CorbaContactInfo
{
    protected ORB orb;
    protected CorbaContactInfoList contactInfoList;
    // NOTE: This may be different from same named one in CorbaContactInfoList.
    protected IOR effectiveTargetIOR;
    protected short addressingDisposition;
    protected CorbaOutboundConnectionCache connectionCache;

    public ORB getBroker()
    {
	return orb;
    }

    public CorbaContactInfoList getContactInfoList()
    {
	return contactInfoList;
    }

    public CorbaClientRequestDispatcher getClientRequestDispatcher()
    {
	int scid =
	    getEffectiveProfile().getObjectKeyTemplate().getSubcontractId() ;
	RequestDispatcherRegistry scr = orb.getRequestDispatcherRegistry() ;
	return scr.getClientRequestDispatcher( scid ) ;
    }

    // Note: not all derived classes will use a connection cache.
    // These are convenience methods that may not be used.
    public void setConnectionCache(CorbaOutboundConnectionCache connectionCache)
    {
	this.connectionCache = connectionCache;
    }

    public CorbaOutboundConnectionCache getConnectionCache()
    {
	return connectionCache;
    }

    // Called when client making an invocation.    
    public CorbaMessageMediator createMessageMediator(ORB broker,
						 CorbaContactInfo contactInfo,
						 CorbaConnection connection,
						 String methodName,
						 boolean isOneWay)
    {
	// REVISIT: Would like version, ior, requestid, etc., decisions
	// to be in client subcontract.  Cannot pass these to this
	// factory method because it breaks generic abstraction.
	// Maybe set methods on mediator called from subcontract
	// after creation?
	CorbaMessageMediator messageMediator =
 	    new CorbaMessageMediatorImpl(
	        (ORB) broker,
		(CorbaContactInfo)contactInfo,
 		connection,
 		GIOPVersion.chooseRequestVersion( (ORB)broker,
		     effectiveTargetIOR),
 		effectiveTargetIOR,
 		((CorbaConnection)connection).getNextRequestId(),
 		getAddressingDisposition(),
 		methodName,
 		isOneWay);

	return messageMediator;
    }

    // Called when not using "useNIOToWait" configuration
    @Transport
    public CorbaMessageMediator createMessageMediator(ORB broker,CorbaConnection conn)
    {
	ORB lorb = (ORB) broker;
	CorbaConnection connection = (CorbaConnection) conn;

	// read giop message
	Message msg = MessageBase.readGIOPMessage(lorb, connection);

	ByteBuffer byteBuffer = msg.getByteBuffer();
	msg.setByteBuffer(null);
	CorbaMessageMediator messageMediator =
	    new CorbaMessageMediatorImpl(orb, connection, msg, byteBuffer);

	return messageMediator;
    }

    public CDROutputObject createOutputObject(CorbaMessageMediator messageMediator)
    {
	CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
	    messageMediator;
	
	CDROutputObject outputObject =
	    new CDROutputObject(orb, messageMediator, 
				corbaMessageMediator.getRequestHeader(),
				corbaMessageMediator.getStreamFormatVersion());

	messageMediator.setOutputObject(outputObject);
	return outputObject;
    }

    public CDRInputObject createInputObject(ORB broker, 
        CorbaMessageMediator messageMediator) {

	// REVISIT: Duplicate of acceptor code.
	CorbaMessageMediator corbaMessageMediator = (CorbaMessageMediator)
	    messageMediator;
	return new CDRInputObject((ORB)broker,
				  (CorbaConnection)messageMediator.getConnection(),
				  corbaMessageMediator.getDispatchBuffer(),
				  corbaMessageMediator.getDispatchHeader());
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
