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
package com.sun.corba.se.impl.resolver ;

import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.RemarshalException ;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.IORFactories ;
import com.sun.corba.se.spi.ior.IORTemplate ;
import com.sun.corba.se.spi.ior.ObjectKey ;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.resolver.Resolver ;

import com.sun.corba.se.impl.ior.ObjectIdImpl;
import com.sun.corba.se.impl.ior.ObjectKeyImpl;
import com.sun.corba.se.spi.logging.ORBUtilSystemException ;
import com.sun.corba.se.impl.orbutil.ORBUtility ;

public class BootstrapResolverImpl implements Resolver {
    private org.omg.CORBA.portable.Delegate bootstrapDelegate ;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public BootstrapResolverImpl(ORB orb, String host, int port) {
	// Create a new IOR with the magic of INIT
	byte[] initialKey = "INIT".getBytes();
	ObjectKey okey = new ObjectKeyImpl(orb.getWireObjectKeyTemplate(),
					   new ObjectIdImpl(initialKey));

	IIOPAddress addr = IIOPFactories.makeIIOPAddress( host, port ) ;
	IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate(
	    orb, GIOPVersion.V1_0, addr);
	    
	IORTemplate iortemp = IORFactories.makeIORTemplate( okey.getTemplate() ) ;
	iortemp.add( ptemp ) ;

	IOR initialIOR = iortemp.makeIOR( orb, "", okey.getId() ) ;

	bootstrapDelegate = ORBUtility.makeClientDelegate( initialIOR ) ;	
    }

    /**
     * For the BootStrap operation we do not expect to have more than one 
     * parameter. We do not want to extend BootStrap protocol any further,
     * as INS handles most of what BootStrap can handle in a portable way.
     *
     * @return InputStream which contains the response from the 
     * BootStrapOperation.
     */
    private InputStream invoke( String operationName, String parameter )
    { 
	boolean remarshal = true;

	// Invoke.

	InputStream inStream = null;

	// If there is a location forward then you will need
	// to invoke again on the updated information.
	// Just calling this same routine with the same host/port
	// does not take the location forward info into account.

	while (remarshal) {
	    org.omg.CORBA.Object objref = null ;
	    remarshal = false;

	    OutputStream os = bootstrapDelegate.request(objref, operationName,
                true);

            if ( parameter != null ) {
                os.write_string( parameter );
            }

	    try {
		// The only reason a null objref is passed is to get the version of
		// invoke used by streams.  Otherwise the PortableInterceptor
		// call stack will become unbalanced since the version of
		// invoke which only takes the stream does not call 
		// PortableInterceptor ending points.
		// Note that the first parameter is ignored inside invoke.

		inStream = bootstrapDelegate.invoke( objref, os);
	    } catch (ApplicationException e) {
		throw wrapper.bootstrapApplicationException( e ) ;
	    } catch (RemarshalException e) {
                wrapper.bootstrapRemarshalException( e ) ;
		remarshal = true;
	    }
	}

        return inStream;
    }

    public org.omg.CORBA.Object resolve( String identifier ) 
    {
	InputStream inStream = null ;
	org.omg.CORBA.Object result = null ;

	try { 
	    inStream = invoke( "get", identifier ) ;

	    result = inStream.read_Object();

	    // NOTE: do note trap and ignore errors.
	    // Let them flow out.
	} finally {
	    bootstrapDelegate.releaseReply( null, inStream ) ;
	}

	return result ;
    }

    public java.util.Set list()
    {
	InputStream inStream = null ;
	java.util.Set result = new java.util.HashSet() ;

	try {
	    inStream = invoke( "list", null ) ;

	    int count =	inStream.read_long();
	    for (int i=0; i < count; i++) {
                result.add(inStream.read_string());
            }

	    // NOTE: do note trap and ignore errors.
	    // Let them flow out.
	} finally {
	    bootstrapDelegate.releaseReply( null, inStream ) ;
	}

	return result ;
    }
}
