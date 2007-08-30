/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.genericRPCMSGFramework;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ConnectionCache;
import com.sun.corba.se.pept.transport.OutboundConnectionCache;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.spi.servicecontext.ORBVersionServiceContext;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.protocol.giopmsgheaders.KeyAddr;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.portable.OutputStream;

/**
 * @author
 */
public class TestClientRequestDispatcher
    implements
	ClientRequestDispatcher
{
    public TestClientRequestDispatcher()
    {
    }
    
    public OutputObject beginRequest(Object self, String methodName,
				     boolean isOneWay,
				     ContactInfo contactInfo) 
    {
	Broker broker = contactInfo.getBroker();
	Connection connection = null;
	if (contactInfo.shouldCacheConnection()) {
	    connection =
		broker.getTransportManager()
		    .getOutboundConnectionCache(contactInfo).get(contactInfo);
	}
	if (connection == null) {
	    connection = contactInfo.createConnection();
	    if (connection.shouldRegisterReadEvent()) {
		broker.getTransportManager().getSelector(0)
		    .registerForEvent(connection.getEventHandler());
		connection.setState("ESTABLISHED");
	    }
	    if (contactInfo.shouldCacheConnection()) {
		OutboundConnectionCache connectionCache =
		    broker.getTransportManager()
		        .getOutboundConnectionCache(contactInfo);
		connectionCache.stampTime(connection);
		connectionCache.put(contactInfo, connection);
		connectionCache.reclaim();
	    }
	}
	MessageMediator messageMediator =
	    contactInfo.createMessageMediator(
	        broker, contactInfo, connection, methodName, isOneWay);

        broker.getInvocationInfo().setMessageMediator(messageMediator);

	ORBVersionServiceContext ovsc =
	    ServiceContextDefaults.makeORBVersionServiceContext();

	// Don't do it for SOAP.
	if (messageMediator instanceof CorbaMessageMediator) {
	    ((CorbaMessageMediator)messageMediator)
		.getRequestServiceContexts().put( ovsc );
	}

	OutputObject outputObject =
	    contactInfo.createOutputObject(messageMediator);

        connection.registerWaiter(messageMediator);

	messageMediator.initializeMessage();

	return outputObject;
    }
    
    public InputObject marshalingComplete(java.lang.Object self,
					  OutputObject outputObject) 
    {
        // NOTE: exceptions not handled.
	outputObject.getMessageMediator().finishSendingRequest();
	InputObject inputObject =
	    outputObject.getMessageMediator().waitForResponse();
	if (inputObject instanceof CDRInputStream) {
	    ((CDRInputStream)inputObject).performORBVersionSpecificInit();
	}
	return inputObject;
    }
    
    public void endRequest(Broker broker, 
			   Object self, InputObject inputObject) 
    {
        MessageMediator messageMediator = 
	    broker.getInvocationInfo().getMessageMediator();
        messageMediator.getConnection().unregisterWaiter(messageMediator);
    }
}

// End of file.
