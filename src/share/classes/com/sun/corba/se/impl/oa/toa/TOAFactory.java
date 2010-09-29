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
package com.sun.corba.se.impl.oa.toa ;

import java.util.Map ;
import java.util.HashMap ;


import com.sun.corba.se.spi.oa.ObjectAdapterFactory ;
import com.sun.corba.se.spi.oa.ObjectAdapter ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.ior.ObjectAdapterId ;


import com.sun.corba.se.impl.javax.rmi.CORBA.Util ;

import com.sun.corba.se.impl.ior.ObjectKeyTemplateBase ;

import com.sun.corba.se.spi.logging.ORBUtilSystemException ;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedObject;

@ManagedObject
@Description( "The Factory for the TOA (transient object adapter)")
@AMXMetadata( isSingleton=true )
public class TOAFactory implements ObjectAdapterFactory 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private ORB orb ;

    private TOAImpl toa ;
    private Map<String,TOAImpl> codebaseToTOA ;
    private TransientObjectManager tom ; 

    @ManagedAttribute
    @Description( "The default TOA used only for dispatch, not objref creation")
    private TOAImpl getDefaultTOA() {
        return toa ;
    }

    @ManagedAttribute
    @Description( "The map from Codebase to TOA")
    private synchronized Map<String,TOAImpl> getCodebaseMap() {
        return new HashMap<String,TOAImpl>( codebaseToTOA ) ;
    }

    public ObjectAdapter find ( ObjectAdapterId oaid ) 
    {
	if (oaid.equals( ObjectKeyTemplateBase.JIDL_OAID )  ) {
            return getTOA();
        } else {
            throw wrapper.badToaOaid();
        }
    }

    public void init( ORB orb )
    {
	this.orb = orb ;
	tom = new TransientObjectManager( orb ) ;
	codebaseToTOA = new HashMap<String,TOAImpl>() ;
        orb.mom().registerAtRoot( this ) ;
    }

    public void shutdown( boolean waitForCompletion )
    {
	if (Util.getInstance() != null) {
	    Util.getInstance().unregisterTargetsForORB(orb);
	}
    }

    public synchronized TOA getTOA( String codebase )
    {
	TOAImpl myToa = codebaseToTOA.get( codebase ) ;
	if (myToa == null) {
	    myToa = new TOAImpl( orb, tom, codebase ) ;

	    codebaseToTOA.put( codebase, myToa ) ;
	}

	return myToa ;
    }

    public synchronized TOA getTOA() 
    {
	if (toa == null) {
	    // The dispatch-only TOA is not used for creating
	    // objrefs, so its codebase can be null (and must
	    // be, since we do not have a servant at this point)
	    toa = new TOAImpl( orb, tom, null ) ;
        }

	return toa ;
    }

    public ORB getORB() 
    {
	return orb ;
    }
} ;

