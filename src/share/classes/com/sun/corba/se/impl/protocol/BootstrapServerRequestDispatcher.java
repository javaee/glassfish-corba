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
package com.sun.corba.se.impl.protocol ;

import java.util.Iterator ;

import org.omg.CORBA.SystemException ;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.protocol.ServerRequestDispatcher ;
import com.sun.corba.se.spi.protocol.MessageMediator;

import com.sun.corba.se.impl.encoding.MarshalInputStream ;
import com.sun.corba.se.impl.encoding.MarshalOutputStream ;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;

/**
 * Class BootstrapServerRequestDispatcher handles the requests coming to the
 * BootstrapServer. It implements Server so that it can be registered
 * as a subcontract. It is passed a BootstrapServiceProperties object
 * which contains
 * the supported ids and their values for the bootstrap service. This
 * Properties object is only read from, never written to, and is shared
 * among all threads.
 * <p>
 * The BootstrapServerRequestDispatcher responds primarily to GIOP requests,
 * but LocateRequests are also handled for graceful interoperability.
 * The BootstrapServerRequestDispatcher handles one request at a time.
 */
public class BootstrapServerRequestDispatcher 
    implements ServerRequestDispatcher
{
    private ORB orb;

    static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final boolean debug = false;

    public BootstrapServerRequestDispatcher(ORB orb )
    {
	this.orb = orb;
    }
    
    /**
     * Dispatch is called by the ORB and will serve get(key) and list()
     * invocations on the initial object key.
     */
    public void dispatch(MessageMediator messageMediator)
    {
	MessageMediator request = (MessageMediator) messageMediator;
	MessageMediator response = null;

	try {
	    MarshalInputStream is = (MarshalInputStream) 
		request.getInputObject();
	    String method = request.getOperationName();
	    response = request.getProtocolHandler().createResponse(request, null);
	    MarshalOutputStream os = (MarshalOutputStream) 
		response.getOutputObject();

            if (method.equals("get")) {
                // Get the name of the requested service
                String serviceKey = is.read_string();

                // Look it up
		org.omg.CORBA.Object serviceObject = 
		    orb.getLocalResolver().resolve( serviceKey ) ;

                // Write reply value
                os.write_Object(serviceObject);
            } else if (method.equals("list")) {
		java.util.Set keys = orb.getLocalResolver().list() ;
		os.write_long( keys.size() ) ;
		Iterator iter = keys.iterator() ;
		while (iter.hasNext()) {
		    String obj = (String)iter.next() ;
		    os.write_string( obj ) ;
		}
	    } else {
		throw wrapper.illegalBootstrapOperation( method ) ;
            }

	} catch (org.omg.CORBA.SystemException ex) {
            // Marshal the exception thrown
	    response = request.getProtocolHandler().createSystemExceptionResponse(
		request, ex, null);
	} catch (java.lang.RuntimeException ex) {
            // Unknown exception
	    SystemException sysex = wrapper.bootstrapRuntimeException( ex ) ;
	    response = request.getProtocolHandler().createSystemExceptionResponse(
                 request, sysex, null ) ;
	} catch (java.lang.Exception ex) {
            // Unknown exception
	    SystemException sysex = wrapper.bootstrapException( ex ) ;
	    response = request.getProtocolHandler().createSystemExceptionResponse(
                 request, sysex, null ) ;
	}

	return;
    }

    /**
     * Locates the object mentioned in the locate requests, and returns
     * object here iff the object is the initial object key. A SystemException
     * thrown if the object key is not the initial object key.
     */
    public IOR locate( ObjectKey objectKey) {
	return null;
    }

    /**
     * Not implemented
     */
    public int getId() {
	throw wrapper.genericNoImpl() ;
    }
}
