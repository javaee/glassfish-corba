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

package com.sun.corba.se.impl.protocol;

import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.CompletionStatus ;

import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.protocol.ForwardException;

// XXX This should be in the SPI
import com.sun.corba.se.impl.protocol.LocalClientRequestDispatcherBase;

import com.sun.corba.se.spi.oa.OAInvocationInfo;
import com.sun.corba.se.spi.oa.ObjectAdapter;
import com.sun.corba.se.spi.oa.OADestroyed;

import com.sun.corba.se.spi.orb.ORB;

import com.sun.corba.se.spi.ior.IOR ;

public abstract class ServantCacheLocalCRDBase extends LocalClientRequestDispatcherBase
{

    private OAInvocationInfo cachedInfo ;

    protected ServantCacheLocalCRDBase( ORB orb, int scid, IOR ior )
    {
	super( orb, scid, ior ) ;
    }

    protected void cleanupAfterOADestroyed() {
	if (debug)
	    dprint( ".cleanupAfterOADestroyed called" ) ;
	cachedInfo = null ;
    }

    protected synchronized OAInvocationInfo getCachedInfo(
	) throws OADestroyed
    {
	if (debug) 
	    dprint( ".getCachedInfo->:" ) ;
	
	try {
	    if (!servantIsLocal) {
		if (debug)
		    dprint( ".getCachedInfo: servantIsLocal is false" ) ;
		throw poaWrapper.servantMustBeLocal() ;
	    }

	    if (cachedInfo == null) {
		if (debug)
		    dprint( ".getCachedInfo: calling updateCachedInfo" ) ;
		updateCachedInfo() ;
	    }

	    if (debug)
		dprint( ".getCachedInfo: returning cachedInfo" ) ;

	    return cachedInfo ;
	} finally {
	    if (debug)
		dprint( ".getCachedInfo<-:" ) ;
	}
    }

    private void updateCachedInfo() throws OADestroyed {
	if (debug)
	    dprint( ".updateCachedInfo->:" ) ;

	try {
	    // If find throws an exception, just let it propagate out
	    ObjectAdapter oa = oaf.find( oaid ) ;
	    if (debug)
		dprint( ".updateCachedInfo: find returned " + oa ) ;

	    cachedInfo = oa.makeInvocationInfo( objectId ) ;
	    if (debug)
		dprint( ".updateCachedInfo: cachedInfo = " + cachedInfo ) ;

	    oa.enter( );
	    if (debug)
		dprint( ".updateCachedInfo: oa.enter() called" ) ;

	    // InvocationInfo must be pushed before calling getInvocationServant
	    orb.pushInvocationInfo( cachedInfo ) ;
	    if (debug)
		dprint( ".updateCachedInfo: pushed invocation info" ) ;

	    try {
		oa.getInvocationServant( cachedInfo ) ;
		if (debug)
		    dprint( ".updateCachedInfo: set servant" ) ;
	    } catch (ForwardException freq) {
		if (debug)
		    dprint( ".updateCachedInfo: caught ForwardException" ) ;
		throw poaWrapper.illegalForwardRequest( freq ) ;
	    } finally {
		oa.returnServant();
		if (debug)
		    dprint( ".updateCachedInfo: after returnServant" ) ;

		oa.exit();
		if (debug)
		    dprint( ".updateCachedInfo: after oa.exit()" ) ;

		orb.popInvocationInfo() ;
		if (debug)
		    dprint( ".updateCachedInfo: after popping InvocationInfo" ) ;
	    }

	    return ;
	} finally {
	    if (debug)
		dprint( ".updateCachedInfo<-:" ) ;
	}
    }
}

// End of File
