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
package com.sun.corba.se.spi.oa;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.portable.ServantObject;

import org.omg.PortableServer.Servant;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import com.sun.corba.se.spi.oa.ObjectAdapter ;

/** This class is a holder for the information required to implement POACurrent.
* It is also used for the ServantObject that is returned by _servant_preinvoke calls.
* This allows us to avoid allocating an extra object on each collocated invocation.
*/
public class OAInvocationInfo extends ServantObject {
    // This is the container object for the servant.
    // In the RMI-IIOP case, it is the RMI-IIOP Tie, and the servant is the
    // target of the Tie.
    // In all other cases, it is the same as the Servant.
    private java.lang.Object	servantContainer ;

    // These fields are to support standard OMG APIs.
    private ObjectAdapter	oa; 
    private byte[]		oid;

    // These fields are to support the Object adapter implementation.
    private CookieHolder	cookieHolder;
    private String		operation;

    // This is the copier to be used by javax.rmi.CORBA.Util.copyObject(s)
    // For the current request.
    private ObjectCopierFactory	factory ;

    public OAInvocationInfo(ObjectAdapter oa, byte[] id )
    {
        this.oa = oa;
        this.oid  = id;
    }

    // Copy constructor of sorts; used in local optimization path
    public OAInvocationInfo( OAInvocationInfo info, String operation )
    {
	this.servant		= info.servant ;
	this.servantContainer	= info.servantContainer ;
	this.cookieHolder	= info.cookieHolder ;
        this.oa			= info.oa;
        this.oid		= info.oid;
	this.factory		= info.factory ;

	this.operation		= operation;
    }

    //getters
    public ObjectAdapter    oa()		    { return oa ; }
    public byte[]	    id()		    { return oid ; }
    public Object	    getServantContainer()   { return servantContainer ; }

    // Create CookieHolder on demand.  This is only called by a single
    // thread, so no synchronization is needed.
    public CookieHolder	    getCookieHolder()	
    { 
	if (cookieHolder == null)
	    cookieHolder = new CookieHolder() ;

	return cookieHolder; 
    }

    public String	    getOperation()	{ return operation; }
    public ObjectCopierFactory	getCopierFactory()	{ return factory; }

    //setters
    public void setOperation( String operation )    { this.operation = operation ; }
    public void setCopierFactory( ObjectCopierFactory factory )    { this.factory = factory ; } 

    public void setServant(Object servant) 
    { 
	servantContainer = servant ;
	if (servant instanceof Tie)
	    this.servant = ((Tie)servant).getTarget() ;
	else
	    this.servant = servant; 
    }
}
