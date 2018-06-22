/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package com.sun.corba.ee.impl.protocol;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.omg.CORBA.portable.ApplicationException;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.trace.Subcontract;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * ClientDelegate is the RMI client-side subcontract or representation
 * It implements RMI delegate as well as our internal ClientRequestDispatcher
 * interface.
 */
@Subcontract
public class SharedCDRClientRequestDispatcherImpl
    extends
        ClientRequestDispatcherImpl
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
        MessageMediator messageMediator = null;
        messageMediator = (MessageMediator)
            outputObject.getMessageMediator();
        operationAndId( messageMediator.getOperationName(),
            messageMediator.getRequestId() ) ;
        final ORB orb = (ORB) messageMediator.getBroker();
        operationAndId(messageMediator.getOperationName(), 
            messageMediator.getRequestId());

        CDROutputObject cdrOutputObject = outputObject;
        final CDROutputObject fCDROutputObject = cdrOutputObject;

        //
        // Create server-side input object.
        //

        CDRInputObject cdrInputObject = AccessController.doPrivileged(
        		new PrivilegedAction<CDRInputObject>() {
					@Override
					public CDRInputObject run() {
						return fCDROutputObject.createInputObject(orb);
					}
        		});
        		
        messageMediator.setInputObject(cdrInputObject);
        cdrInputObject.setMessageMediator(messageMediator);

        //
        // Dispatch
        //

        // REVISIT: Impl cast.
        ((MessageMediatorImpl)messageMediator).handleRequestRequest(
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

        cdrOutputObject = messageMediator.getOutputObject();
        final CDROutputObject fCDROutputObject2 = cdrOutputObject;
        cdrInputObject = AccessController.doPrivileged(
        		new PrivilegedAction<CDRInputObject>() {

					@Override
					public CDRInputObject run() {
						// TODO Auto-generated method stub
						return fCDROutputObject2.createInputObject(orb);
					}
        			
        		});
        messageMediator.setInputObject(cdrInputObject);
        cdrInputObject.setMessageMediator(messageMediator);

        cdrInputObject.unmarshalHeader();

        CDRInputObject inputObject = cdrInputObject;

        return processResponse(orb, messageMediator, inputObject);
    }

}

// End of file.
