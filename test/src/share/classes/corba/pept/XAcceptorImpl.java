/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

//
// Created       : 2003 Apr 09 (Wed) 17:15:35 by Harold Carr.
// Last Modified : 2003 Nov 23 (Sun) 19:04:27 by Harold Carr.
//

package corba.pept;

import java.net.ServerSocket;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.EventHandler;
import com.sun.corba.se.pept.transport.InboundConnectionCache;

import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.transport.SocketOrChannelAcceptor;
import com.sun.corba.se.impl.oa.poa.Policies; // REVISIT impl/poa specific
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl;

/**
 * @author Harold Carr
 */
public class XAcceptorImpl 
    implements
	com.sun.corba.se.spi.transport.CorbaAcceptor,
	SocketOrChannelAcceptor
{
    protected SocketOrChannelAcceptorImpl acceptor;

    public XAcceptorImpl(ORB orb, int port)
    {
	acceptor = new SocketOrChannelAcceptorImpl(orb, port);
    }

    public boolean initialize()
    {
	return acceptor.initialize();
    }

    public boolean initialized()
    {
	return acceptor.initialized();
    }

    public String getConnectionCacheType()
    {
	throw new RuntimeException();
    }

    public void setConnectionCache(InboundConnectionCache connectionCache)
    {
	throw new RuntimeException();
    }

    public InboundConnectionCache getConnectionCache()
    {
	throw new RuntimeException();
    }

    public boolean shouldRegisterAcceptEvent()
    {
	return acceptor.shouldRegisterAcceptEvent();
    }

    public void accept()
    {
	acceptor.accept();
    }

    public void close()
    {
	acceptor.close();
    }

    public EventHandler getEventHandler()
    {
	return acceptor.getEventHandler();
    }

    public MessageMediator createMessageMediator(Broker xbroker,
						 Connection xconnection)
    {
	return acceptor.createMessageMediator(xbroker, xconnection);
    }

    public InputObject createInputObject(Broker broker,
					 MessageMediator messageMediator)
    {
	return acceptor.createInputObject(broker, messageMediator);
    }

    public OutputObject createOutputObject(Broker broker,
					   MessageMediator messageMediator)
    {
	return acceptor.createOutputObject(broker, messageMediator);
    }

    public String getObjectAdapterId()
    {
	return acceptor.getObjectAdapterId();
    }

    public String getObjectAdapterManagerId()
    {
	return acceptor.getObjectAdapterManagerId();
    }

    public void addToIORTemplate(IORTemplate iorTemplate,
				 Policies policies,
				 String codebase)
    {
	acceptor.addToIORTemplate(iorTemplate, policies, codebase);
    }

    public String getMonitoringName()
    {
	return "FOO";
    }

    public ServerSocket getServerSocket()
    {
	return acceptor.getServerSocket();
    }
}

// End of file.
