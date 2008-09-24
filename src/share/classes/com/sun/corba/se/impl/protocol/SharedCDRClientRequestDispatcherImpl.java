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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.protocol;

import java.io.IOException;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.nio.ByteBuffer;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.WrongTransaction;
import org.omg.CORBA.Request;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.NVList;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.UNKNOWN;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ServantObject;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.UnknownException;
import org.omg.IOP.TAG_CODE_SETS;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.ClientRequestDispatcher;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.Connection;
import com.sun.corba.se.pept.transport.ConnectionCache;
import com.sun.corba.se.pept.transport.ContactInfo;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.ior.iiop.CodeSetsComponent;
import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBVersion;
import com.sun.corba.se.spi.orb.ORBVersionFactory;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.transport.CorbaContactInfo ;
import com.sun.corba.se.spi.transport.CorbaContactInfoList ;
import com.sun.corba.se.spi.transport.CorbaContactInfoListIterator ;
import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.spi.servicecontext.ServiceContext;
import com.sun.corba.se.spi.servicecontext.ServiceContexts;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext;
import com.sun.corba.se.spi.servicecontext.CodeSetServiceContext;
import com.sun.corba.se.spi.servicecontext.MaxStreamFormatVersionServiceContext;
import com.sun.corba.se.spi.servicecontext.SendingContextServiceContext;
import com.sun.corba.se.impl.encoding.ByteBufferWithInfo;
import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.encoding.CDROutputStream;
import com.sun.corba.se.impl.encoding.CodeSetComponentInfo;
import com.sun.corba.se.impl.encoding.CodeSetConversion;
import com.sun.corba.se.impl.encoding.MarshalOutputStream;
import com.sun.corba.se.impl.encoding.MarshalInputStream;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.se.impl.protocol.giopmsgheaders.KeyAddr;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ProfileAddr;
import com.sun.corba.se.impl.protocol.giopmsgheaders.ReferenceAddr;
import com.sun.corba.se.impl.transport.CorbaContactInfoListIteratorImpl;

/**
 * ClientDelegate is the RMI client-side subcontract or representation
 * It implements RMI delegate as well as our internal ClientRequestDispatcher
 * interface.
 */
public class SharedCDRClientRequestDispatcherImpl
    extends
	CorbaClientRequestDispatcherImpl
{
    // REVISIT:
    // Rather than have separate CDR subcontract,
    // use same CorbaClientRequestDispatcherImpl but have
    // different MessageMediator finishSendingRequest and waitForResponse
    // handle what is done below. 
    // Benefit: then in ContactInfo no need to do a direct new
    // of subcontract - does not complicate subcontract registry.

    public InputObject marshalingComplete(java.lang.Object self, 
					  OutputObject outputObject)
	throws 
	    ApplicationException, 
	    org.omg.CORBA.portable.RemarshalException
    {
      ORB orb = null;
      CorbaMessageMediator messageMediator = null;
      try {
	messageMediator = (CorbaMessageMediator)
	    outputObject.getMessageMediator();

	orb = (ORB) messageMediator.getBroker();

	if (orb.subcontractDebugFlag) {
	    dprint(".marshalingComplete->: " + opAndId(messageMediator));
	}

	CDROutputObject cdrOutputObject = (CDROutputObject) outputObject;

	//
	// Create server-side input object.
	//

	ByteBufferWithInfo bbwi = cdrOutputObject.getByteBufferWithInfo();
	cdrOutputObject.getMessageHeader().setSize(bbwi.getByteBuffer(), bbwi.getSize());

	CDRInputObject cdrInputObject =
	    new CDRInputObject(orb, null, bbwi.getByteBuffer(),
                            cdrOutputObject.getMessageHeader());
	messageMediator.setInputObject(cdrInputObject);
	cdrInputObject.setMessageMediator(messageMediator);

	//
	// Dispatch
	//

	// REVISIT: Impl cast.
	((CorbaMessageMediatorImpl)messageMediator).handleRequestRequest(
            messageMediator);

        // InputStream must be closed on the InputObject so that its
        // ByteBuffer can be released to the ByteBufferPool. We must do
        // this before we re-assign the cdrInputObject reference below.
        try { cdrInputObject.close(); }
        catch (IOException ex) {
            // No need to do anything since we're done with the input stream
            // and cdrInputObject will be re-assigned a new client-side input
            // object, (i.e. won't result in a corba error).
           
            if (orb.transportDebugFlag) {
               dprint(".marshalingComplete: ignoring IOException - " + ex.toString());
            }
        }

	//
	// Create client-side input object
	//

	cdrOutputObject = (CDROutputObject) messageMediator.getOutputObject();
	bbwi = cdrOutputObject.getByteBufferWithInfo();
	cdrOutputObject.getMessageHeader().setSize(bbwi.getByteBuffer(), bbwi.getSize());
	cdrInputObject =
	    new CDRInputObject(orb, null, bbwi.getByteBuffer(), 
                            cdrOutputObject.getMessageHeader());
	messageMediator.setInputObject(cdrInputObject);
	cdrInputObject.setMessageMediator(messageMediator);

	cdrInputObject.unmarshalHeader();

	InputObject inputObject = cdrInputObject;

	return processResponse(orb, messageMediator, inputObject);

      } finally {
        if (orb.subcontractDebugFlag) {
	    dprint(".marshalingComplete<-: " + opAndId(messageMediator));
	}
      }
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("SharedCDRClientRequestDispatcherImpl", msg);
    }
}

// End of file.
