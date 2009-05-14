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
// Created       : 2003 Apr 19 (Sat) 07:49:04 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 12:01:06 by Harold Carr.

package corba.pept;

import java.nio.ByteBuffer;

import org.omg.CORBA.INTERNAL;


import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.SocketInfo;

import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.se.impl.transport.SocketOrChannelConnectionImpl;

/**
 * @author Harold Carr
 */
public class XConnectionImpl
    extends
	SocketOrChannelConnectionImpl
{
    public XConnectionImpl(ORB orb,
			  CorbaContactInfo contactInfo)
    {
	super(orb, false, false);

	// REVISIT - probably need a contact info for both
	// client and server for removing connections from cache?
	this.contactInfo = contactInfo;
    }

    // Factored out of client constructor so it can be called recursively.
    // But not called recursively anymore.
    private void initialize(ORB orb, boolean isBlocking,
			    IOR ior, SocketInfo socketInfo)
    {
    }

    ////////////////////////////////////////////////////
    //
    // pept.transport.Connection
    //

    public boolean shouldRegisterReadEvent()
    {
	return false;
    }

    public boolean shouldRegisterServerReadEvent()
    {
	return false;
    }

    public void writeLock()
    {
    }

    public void writeUnlock()
    {
    }

    CDROutputObject outputObject;

    /*
    // The body of this method is similar to 
    // SocketOrChannelContactInfoImpl.createMessageMediator body.
    public void sendWithoutLock(OutputObject output)
    {
	// Kludge alert - used in read below.
	outputObject = (CDROutputObject) output;

	MessageBase header = (MessageBase)
	    MessageBase.createFromStream(orb, this);

	ByteBuffer byteBuffer =
	    read(header.getSize(),
		 MessageBase.GIOPMessageHeaderLength,
		 header.getSize() - MessageBase.GIOPMessageHeaderLength);
	byteBuffer.position(0).limit(header.getSize());

	// REVISIT - MessageBase cast.
	CorbaMessageMediator messageMediator =
	    new CorbaMessageMediatorImpl(orb, this, header, byteBuffer);
	messageMediator.getProtocolHandler().handleRequest(messageMediator);
	// *****
	System.out.println("**** In progress...");
    }
    */

    ////////////////////////////////////////////////////
    //
    // spi.transport.Connection
    //

    public ByteBuffer read(int size, int offset, int length)
    {
	if (size != MessageBase.GIOPMessageHeaderLength) {
	    throw new INTERNAL("Only expecting to be used for header");
	}

	/* REVISIT - turned off for now.
	ByteBuffer byteBuffer = ByteBuffer.wrap(outputObject.getByteBuffer());

	byteBuffer.position(size);
	byteBuffer.limit(size);

	return byteBuffer;
	*/
	return null;
    }

    public void write(ByteBuffer byteBuffer)
    {
    }

    public void serverRequestMapPut(int requestId, 
				    CorbaMessageMediator messageMediator)
    {
    }

    public CorbaMessageMediator serverRequestMapGet(int requestId)
    {
	return null;
    }

    public void serverRequestMapRemove(int requestId)
    {
    }

    ////////////////////////////////////////////////////
    //
    // implementation
    //



    public String toString()
    {
	return 
	    "XConnectionImpl[" + " "
	    + "]" ;
    }
}

// End of file.


// End of file.
