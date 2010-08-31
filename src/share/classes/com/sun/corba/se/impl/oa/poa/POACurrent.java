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

package com.sun.corba.se.impl.oa.poa;

import java.util.*;
import org.omg.CORBA.CompletionStatus;
import org.omg.PortableServer.CurrentPackage.NoContext;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.spi.oa.ObjectAdapter ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.logging.POASystemException ;

// XXX Needs to be turned into LocalObjectImpl.

public class POACurrent extends org.omg.CORBA.portable.ObjectImpl
    implements org.omg.PortableServer.Current 
{
    private ORB orb;
    private POASystemException wrapper ;

    public POACurrent(ORB orb)
    {
	this.orb = orb;
	wrapper = orb.getLogWrapperTable().get_OA_INVOCATION_POA() ;
    }

    public String[] _ids()
    {
        String[] ids = new String[1];
        ids[0] = "IDL:omg.org/PortableServer/Current:1.0";
        return ids;
    }

    //
    // Standard OMG operations.
    //

    public POA get_POA()
        throws 
	    NoContext
    {
        POA poa = (POA)(peekThrowNoContext().oa());
	throwNoContextIfNull(poa);
	return poa;
    }

    public byte[] get_object_id()
        throws 
	    NoContext
    {
	byte[] objectid = peekThrowNoContext().id();
	throwNoContextIfNull(objectid);
	return objectid;
    }

    //
    // Implementation operations used by POA package.
    //

    public ObjectAdapter getOA()
    {
        ObjectAdapter oa = peekThrowInternal().oa();
	throwInternalIfNull(oa);
	return oa;
    }

    public byte[] getObjectId()
    {
	byte[] objectid = peekThrowInternal().id();
	throwInternalIfNull(objectid);
	return objectid;
    }

    Servant getServant()
    {
	Servant servant = (Servant)(peekThrowInternal().getServantContainer());
	// If is OK for the servant to be null.
	// This could happen if POAImpl.getServant is called but
	// POAImpl.internalGetServant throws an exception.
	return servant;
    }

    CookieHolder getCookieHolder()
    {
	CookieHolder cookieHolder = peekThrowInternal().getCookieHolder();
	throwInternalIfNull(cookieHolder);
	return cookieHolder;
    }

    // This is public so we can test the stack balance.
    // It is not a security hole since this same info can be obtained from 
    // PortableInterceptors.
    public String getOperation()
    {
	String operation = peekThrowInternal().getOperation();
	throwInternalIfNull(operation);
	return operation;
    }

    void setServant(Servant servant)
    {
	peekThrowInternal().setServant( servant );
    }

    //
    // Class utilities.
    //

    private OAInvocationInfo peekThrowNoContext()
	throws
	    NoContext
    {
	OAInvocationInfo invocationInfo = null;
	try {
	    invocationInfo = orb.peekInvocationInfo() ;
	} catch (EmptyStackException e) {
	    throw new NoContext();
	}
	return invocationInfo;
    }

    private OAInvocationInfo peekThrowInternal()
    {
	OAInvocationInfo invocationInfo = null;
	try {
	    invocationInfo = orb.peekInvocationInfo() ;
	} catch (EmptyStackException e) {
	    // The completion status is maybe because this could happen
	    // after the servant has been invoked.
	    throw wrapper.poacurrentUnbalancedStack( e ) ;
	}
	return invocationInfo;
    }

    private void throwNoContextIfNull(Object o)
	throws
	    NoContext
    {
	if ( o == null ) {
	    throw new NoContext();
	}
    }

    private void throwInternalIfNull(Object o)
    {
	if ( o == null ) {
	    throw wrapper.poacurrentNullField( CompletionStatus.COMPLETED_MAYBE ) ;
	}
    }
}
