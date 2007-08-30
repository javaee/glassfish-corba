/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
// Created       : 2001 Sep 18 (Tue) 11:16:00 by Harold Carr.
// Last Modified : 2004 May 17 (Mon) 16:09:13 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.legacy.connection.EndPointInfoImpl;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.transport.SocketOrChannelConnectionImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelContactInfoImpl;

import com.sun.corba.se.impl.orbutil.ORBUtility;

public class IIOPConnection
    extends
	SocketOrChannelConnectionImpl
{
    // This exists so it will get the host/port from ContactInfo.
    public IIOPConnection(ORB broker, IIOPContactInfo contactInfo)
    {
	super(broker);
	wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
	setUseSelectThreadToWait(false);
	setUseWorkerThreadForEvent(false);

	this.contactInfo = contactInfo;
	SocketInfo socketInfo = null;
        try {
	    socketInfo = contactInfo.getEndPointInfo();
	    InetSocketAddress address =
		new InetSocketAddress(socketInfo.getHost(),
				      socketInfo.getPort());
	    socketChannel = ORBUtility.openSocketChannel(address);
	    socket = socketChannel.socket();
	    socketChannel.configureBlocking(true);
        } catch (Exception ex) {
	    throw wrapper.connectFailure(
                ex, socketInfo.getType(), socketInfo.getHost(),
		Integer.toString(socketInfo.getPort()));
        }
        state = OPENING;     
    }
}

// End of file
