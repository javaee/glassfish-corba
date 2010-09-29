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

import com.sun.corba.se.spi.logging.POASystemException;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Subcontract;

@Subcontract
public abstract class LocalClientRequestDispatcherBase implements LocalClientRequestDispatcher
{
    protected static final POASystemException poaWrapper =
        POASystemException.self ;
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

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

    // If isNextIsLocalValid.get() == Boolean.TRUE, 
    // the next call to isLocal should be valid
    private static final ThreadLocal isNextCallValid = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return Boolean.TRUE;
        }
    };

    protected LocalClientRequestDispatcherBase(ORB orb, int scid, IOR ior)
    {
	this.orb = orb ;

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
	if (isNextCallValid.get() == Boolean.TRUE) {
            return servantIsLocal;
        } else {
            isNextCallValid.set(Boolean.TRUE);
        }

	return false ;    
    }

    /** Check that the servant in info (which must not be null) is
    * an instance of the expectedType.  If not, set the thread local flag
    * and return false.
    */
    protected boolean checkForCompatibleServant( ServantObject so, 
	Class expectedType )
    {
	if (so == null) {
            return false;
        }

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

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, int value ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }


    // servant_preinvoke is here to contain the exception handling
    // logic that is common to all POA based servant_preinvoke implementations.
    @Subcontract
    public ServantObject servant_preinvoke( org.omg.CORBA.Object self,
	String operation, Class expectedType ) {

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
                display( "Calling internalPreinvoke") ;
                return internalPreinvoke( self, operation, expectedType ) ;
            } catch (OADestroyed pdes) {
                display( "Caught OADestroyed: will retry") ;
                cleanupAfterOADestroyed() ;
            } catch (TRANSIENT exc) {
                display( "Caught transient") ;
                // Local calls are very fast, so don't waste time setting
                // up to track the time UNLESS the first attempt fails
                // with a TRANSIENT exception.
                long currentTime = System.currentTimeMillis() ;

                // Delay if not too much time passed, otherwise re-throw
                // the TRANSIENT exception.
                if (startTime == -1) {
                    display( "backoff (first retry)", backoff ) ;
                    startTime = currentTime ;
                } else if ((currentTime-startTime) > MAX_WAIT_TIME) {
                    display( "Total time exceeded", MAX_WAIT_TIME ) ;
                    throw exc ;
                } else {
                    backoff *= 2 ;
                    if (backoff > MAX_BACKOFF) {
                        backoff = MAX_BACKOFF ;
                    }

                    display( "increasing backoff (will retry)", backoff ) ;
                }

                try {
                    Thread.sleep( backoff ) ;
                } catch (InterruptedException iexc) {
                    // As usual, ignore the possible InterruptedException
                }

                display( "retry" ) ;
            } catch ( ForwardException ex ) {
                /* REVISIT
                ClientRequestDispatcher csub = (ClientRequestDispatcher)
                    StubAdapter.getDelegate( ex.forward_reference ) ;
                IOR ior = csub.getIOR() ;
                setLocatedIOR( ior ) ;
                */
                display( "Unsupported ForwardException" ) ;
                throw new RuntimeException("deal with this.", ex) ;
            } catch ( ThreadDeath ex ) {
                // ThreadDeath on the server side should not cause a client
                // side thread death in the local case.  We want to preserve
                // this behavior for location transparency, so that a ThreadDeath
                // has the same affect in either the local or remote case.
                // The non-colocated case is handled in iiop.ORB.process, which
                // throws the same exception.
                display( "Caught ThreadDeath") ;
                throw wrapper.runtimeexception( ex, ex.getClass().getName(), ex.getMessage() ) ;
            } catch ( Throwable t ) {
                display( "Caught Throwable") ;

                if (t instanceof SystemException)
                    throw (SystemException)t ;

                throw poaWrapper.localServantLookup( t ) ;
            }
        }
    }
}

// End of file.
