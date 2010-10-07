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

package com.sun.corba.se.impl.protocol;

import org.omg.CORBA.portable.ServantObject;

import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.spi.oa.OADestroyed;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.trace.Subcontract;

@Subcontract
public class POALocalCRDImpl extends LocalClientRequestDispatcherBase {

    public POALocalCRDImpl( ORB orb, int scid, IOR ior) {
	super( orb, scid, ior );
    }

    @Subcontract
    private OAInvocationInfo servantEnter( ObjectAdapter oa ) throws OADestroyed {
	oa.enter() ;

	OAInvocationInfo info = oa.makeInvocationInfo( objectId ) ;
	orb.pushInvocationInfo( info ) ;

	return info ;
    }

    @Subcontract
    private void servantExit( ObjectAdapter oa ) {
	try {
	    oa.returnServant();
	} finally {
	    oa.exit() ;
	    orb.popInvocationInfo() ; 
	}
    }

    // Look up the servant for this request and return it in a 
    // ServantObject.  Note that servant_postinvoke is always called
    // by the stub UNLESS this method returns null.  However, in all
    // cases we must be sure that ObjectAdapter.getServant and
    // ObjectAdapter.returnServant calls are paired, as required for
    // Portable Interceptors and Servant Locators in the POA.
    // Thus, this method must call returnServant if it returns null.
    @Subcontract
    @Override
    public ServantObject internalPreinvoke( org.omg.CORBA.Object self,
        String operation, Class expectedType) throws OADestroyed {

	ObjectAdapter oa = null ;

        oa = oaf.find( oaid ) ;

        OAInvocationInfo info = servantEnter( oa ) ;
        info.setOperation( operation ) ;

        try {
            oa.getInvocationServant( info );
            if (!checkForCompatibleServant( info, expectedType )) {
                servantExit( oa ) ;
                return null ;
            }

            return info ;
        } catch (Error err) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit( oa ) ;
            throw err ;
        } catch (RuntimeException re) {
            // Cleanup after this call, then throw to allow
            // outer try to handle the exception appropriately.
            servantExit( oa ) ;
            throw re ;
        }
    }

    public void servant_postinvoke(org.omg.CORBA.Object self,
                                   ServantObject servantobj) 
    {
        ObjectAdapter oa = orb.peekInvocationInfo().oa() ; 
	servantExit( oa ) ;	
    }
}

// End of file.
