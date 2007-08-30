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

import org.omg.CORBA.TRANSIENT ;
import org.omg.CORBA.SystemException ;

import org.omg.CORBA.portable.ServantObject;


import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;
import com.sun.corba.se.spi.protocol.ForwardException ;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.IOR ;

import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.oa.OADestroyed ;

import com.sun.corba.se.spi.ior.ObjectAdapterId;
import com.sun.corba.se.spi.ior.TaggedProfile;
import com.sun.corba.se.spi.ior.ObjectKeyTemplate;
import com.sun.corba.se.spi.ior.ObjectId; 

import com.sun.corba.se.impl.logging.POASystemException;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;

import com.sun.corba.se.impl.orbutil.ORBUtility;

public abstract class LocalClientRequestDispatcherBase implements LocalClientRequestDispatcher
{
    // XXX May want to make some of this configuratble as in the remote case
    // XXX Should this be unified?  How can we better handle this retry/backoff
    // implementation with a single implementation?
    private static final int INITIAL_BACKOFF = 1 ;  // initially start off very small
						    // because 1 millisecond is a long time for a local call.
						   
    private static final int MAX_BACKOFF   = 1000 ;   // Never sleep longer than this
    private static final int MAX_WAIT_TIME = 10 * 1000 ; // Total time to wait for a local request.

    protected ORB orb;
    private int scid;

    // Cached information needed for local dispatch
    protected boolean servantIsLocal ;
    protected ObjectAdapterFactory oaf ;
    protected ObjectAdapterId oaid ;
    protected byte[] objectId ;
    protected boolean debug ;
    protected POASystemException poaWrapper ;
    protected ORBUtilSystemException wrapper ;

    // If isNextIsLocalValid.get() == Boolean.TRUE, 
    // the next call to isLocal should be valid
    private static final ThreadLocal isNextCallValid = new ThreadLocal() {
	    protected synchronized Object initialValue() {
		return Boolean.TRUE;
	    }
	};

    protected LocalClientRequestDispatcherBase(ORB orb, int scid, IOR ior)
    {
	this.orb = orb ;
	debug = orb.subcontractDebugFlag ;
	wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;
	poaWrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_POA() ;

	TaggedProfile prof = ior.getProfile() ;
	servantIsLocal = orb.getORBData().isLocalOptimizationAllowed() && 
	    prof.isLocal();

	ObjectKeyTemplate oktemp = prof.getObjectKeyTemplate() ;
	this.scid = oktemp.getSubcontractId() ;
	RequestDispatcherRegistry sreg = orb.getRequestDispatcherRegistry() ;
	oaf = sreg.getObjectAdapterFactory( scid ) ;
	oaid = oktemp.getObjectAdapterId() ;
	ObjectId oid = prof.getObjectId() ;
	objectId = oid.getId() ;
    }

    public byte[] getObjectId() 
    {
	return objectId ;
    }

    public boolean is_local(org.omg.CORBA.Object self)
    {
	return false;
    }

    /*
    * Possible paths through
    * useLocalInvocation/servant_preinvoke/servant_postinvoke:
    *
    * A: call useLocalInvocation
    * If useLocalInvocation returns false, servant_preinvoke is not called.
    * If useLocalInvocation returns true,
    * call servant_preinvoke
    *	If servant_preinvoke returns null,
    *	    goto A
    *   else
    *	    (local invocation proceeds normally)
    *	    servant_postinvoke is called
    *
    */
    public boolean useLocalInvocation( org.omg.CORBA.Object self ) 
    {
	if (isNextCallValid.get() == Boolean.TRUE)
	    return servantIsLocal ;
	else
	    isNextCallValid.set( Boolean.TRUE ) ;

	return false ;    
    }

    /** Check that the servant in info (which must not be null) is
    * an instance of the expectedType.  If not, set the thread local flag
    * and return false.
    */
    protected boolean checkForCompatibleServant( ServantObject so, 
	Class expectedType )
    {
	if (so == null)
	    return false ;

	// Normally, this test will never fail.  However, if the servant
	// and the stub were loaded in different class loaders, this test
	// will fail.
	if (!expectedType.isInstance( so.servant )) {
	    isNextCallValid.set( Boolean.FALSE ) ;

	    // When servant_preinvoke returns null, the stub will
	    // recursively re-invoke itself.  Thus, the next call made from 
	    // the stub is another useLocalInvocation call.
	    return false ;
	}

	return true ;
    }

    // The actual servant_preinvoke implementation, which must be 
    // overridden.  This method may throw exceptions 
    // which are handled by servant_preinvoke.
    protected ServantObject internalPreinvoke( 
	org.omg.CORBA.Object self, String operation, 
	Class expectedType ) throws OADestroyed 
    {
	return null ;
    }

    // This method is called when OADestroyed is caught.  This allows
    // subclasses to provide cleanup code if necessary.
    protected void cleanupAfterOADestroyed() 
    {
	// Default is NO-OP
    }

    // servant_preinvoke is here to contain the exception handling
    // logic that is common to all POA based servant_preinvoke implementations.
    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
	String operation, Class expectedType )
    {
	if (debug)
	    dprint( ".servant_preinvoke->:" ) ;

	try {
	    // XXX Should we consider an implementation here that is
	    // event driven instead of polled?  We could take advantage
	    // of the ORT OAManager state change notification to drive
	    // this.  This would require registering an IORInterceptor_3_0
	    // implementation that signals a condition on which thread 
	    // wait in colocated invocations.  But this is probably not
	    // justified.
	    long startTime = -1 ;
	    long backoff = INITIAL_BACKOFF ;
	    long maxWait = MAX_WAIT_TIME ;

	    while (true) {
		try {
		    if (debug)
			dprint( ".servant_preinvoke: calling internalPreinvoke" ) ;
		    return internalPreinvoke( self, operation, expectedType ) ;
		} catch (OADestroyed pdes) {
		    cleanupAfterOADestroyed() ;

		    if (debug)
			dprint( ".internalPreinvoke: retrying after OADestroyed" ) ;
		} catch (TRANSIENT exc) {
		    // Local calls are very fast, so don't waste time setting
		    // up to track the time UNLESS the first attempt fails
		    // with a TRANSIENT exception.
		    long currentTime = System.currentTimeMillis() ;

		    // Delay if not too much time passed, otherwise re-throw
		    // the TRANSIENT exception.
		    if (startTime == -1) {
			if (debug)
			    dprint( ".servant_preinvoke: first retry on TRANSIENT, backoff = " + backoff ) ;

			startTime = currentTime ;
		    } else if ((currentTime-startTime) > MAX_WAIT_TIME) {
			if (debug)
			    dprint( ".servant_preinvoke: total time exceeded " + MAX_WAIT_TIME ) ;

			throw exc ;
		    } else {
			backoff *= 2 ;
			if (backoff > MAX_BACKOFF) {
			    backoff = MAX_BACKOFF ;
			}

			if (debug)
			    dprint( ".servant_preinvoke: increasing backoff to " + backoff ) ;
		    }

		    try {
			Thread.sleep( backoff ) ;
		    } catch (InterruptedException iexc) {
			// As usual, ignore the possible InterruptedException
		    }

		    if (debug)
			dprint( ".servant_preinvoke: retry after backoff for TRANSIENT exception" ) ;
		} catch ( ForwardException ex ) {
		    /* REVISIT
		    ClientRequestDispatcher csub = (ClientRequestDispatcher)
			StubAdapter.getDelegate( ex.forward_reference ) ;
		    IOR ior = csub.getIOR() ;
		    setLocatedIOR( ior ) ;
		    */
		    if (debug)
			dprint( ".internalPreinvoke: caught unsupported ForwardException" ) ;

		    RuntimeException runexc = new RuntimeException("deal with this.");
		    runexc.initCause( ex ) ;
		    throw runexc ;
		} catch ( ThreadDeath ex ) {
		    // ThreadDeath on the server side should not cause a client
		    // side thread death in the local case.  We want to preserve
		    // this behavior for location transparency, so that a ThreadDeath
		    // has the same affect in either the local or remote case.
		    // The non-colocated case is handled in iiop.ORB.process, which
		    // throws the same exception.
		    if (debug)
			dprint( ".internalPreinvoke: caught ThreadDeath" ) ;

		    throw wrapper.runtimeexception( ex, ex.getClass().getName(), ex.getMessage() ) ;
		} catch ( Throwable t ) {
		    if (debug)
			dprint( ".internalPreinvoke: caught Throwable " + t ) ;

		    if (t instanceof SystemException)
			throw (SystemException)t ;

		    throw poaWrapper.localServantLookup( t ) ;
		} 
	    }
	} finally { 
	    if (debug)
		dprint( ".servant_preinvoke<-:" ) ;
	}
    }

    protected void dprint( String msg ) {
	ORBUtility.dprint( this, msg ) ;
    }
}

// End of file.
