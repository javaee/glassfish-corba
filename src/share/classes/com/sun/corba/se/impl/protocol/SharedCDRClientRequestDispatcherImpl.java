/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

import org.omg.CORBA.portable.ApplicationException;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;

import com.sun.corba.se.impl.encoding.ByteBufferWithInfo;
import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Subcontract;

/**
 * ClientDelegate is the RMI client-side subcontract or representation
 * It implements RMI delegate as well as our internal ClientRequestDispatcher
 * interface.
 */
@Subcontract
public class SharedCDRClientRequestDispatcherImpl
    extends
	CorbaClientRequestDispatcherImpl
{

    @InfoMethod
    private void operationAndId( String msg, int rid ) { }

    // REVISIT:
    // Rather than have separate CDR subcontract,
    // use same CorbaClientRequestDispatcherImpl but have
    // different MessageMediator finishSendingRequest and waitForResponse
    // handle what is done below. 
    // Benefit: then in ContactInfo no need to do a direct new
    // of subcontract - does not complicate subcontract registry.

    @Override
    @Subcontract
    public CDRInputObject marshalingComplete(java.lang.Object self,
					  CDROutputObject outputObject)
	throws 
	    ApplicationException, 
	    org.omg.CORBA.portable.RemarshalException
    {
        ORB orb = null;
        CorbaMessageMediator messageMediator = null;
	messageMediator = (CorbaMessageMediator)
	    outputObject.getMessageMediator();
        operationAndId( messageMediator.getOperationName(),
            messageMediator.getRequestId() ) ;
	orb = (ORB) messageMediator.getBroker();
        operationAndId(messageMediator.getOperationName(), 
            messageMediator.getRequestId());

	CDROutputObject cdrOutputObject = (CDROutputObject) outputObject;

	//
	// Create server-side input object.
	//

	ByteBufferWithInfo bbwi = cdrOutputObject.getByteBufferWithInfo();
	cdrOutputObject.getMessageHeader().setSize(bbwi.getByteBuffer(),
            bbwi.getSize());

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
        try {
            cdrInputObject.close();
        } catch (IOException ex) {
            // No need to do anything since we're done with the input stream
            // and cdrInputObject will be re-assigned a new client-side input
            // object, (i.e. won't result in a corba error).
            // XXX log this
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

	CDRInputObject inputObject = cdrInputObject;

	return processResponse(orb, messageMediator, inputObject);
    }
}

// End of file.
