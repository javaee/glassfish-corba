/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.oa.poa ;

import java.util.Set ;

import org.omg.CORBA.SystemException ;

import org.omg.PortableServer.ServantActivator ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantManager ;
import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ObjectNotActive ;
import org.omg.PortableServer.POAPackage.NoServant ;

import com.sun.corba.se.impl.oa.NullServantImpl ;

import com.sun.corba.se.spi.oa.OAInvocationInfo ;
import com.sun.corba.se.spi.oa.NullServant ;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Poa;

/** Implementation of POARequesHandler that provides policy specific
 * operations on the POA.
 */
@Poa
public class POAPolicyMediatorImpl_R_USM extends POAPolicyMediatorBase_R {
    protected ServantActivator activator ;

    POAPolicyMediatorImpl_R_USM( Policies policies, POAImpl poa ) 
    {
	// assert policies.retainServants() 
	super( policies, poa ) ;
	activator = null ;

	if (!policies.useServantManager()) {
            throw poa.invocationWrapper().policyMediatorBadPolicyInFactory();
        }
    }
   
    /* This handles a rather subtle bug (4939892).  The problem is that
     * enter will wait on the entry if it is being etherealized.  When the
     * deferred state transition completes, the entry is no longer in the
     * AOM, and so we need to get a new entry, otherwise activator.incarnate
     * will be called twice, once for the old entry, and again when a new
     * entry is created.  This fix also required extending the FSM StateEngine
     * to allow actions to throw exceptions, and adding a new state in the
     * AOMEntry FSM to detect this condition.
     */
    private AOMEntry enterEntry( ActiveObjectMap.Key key )
    {
	AOMEntry result = null ;
	boolean failed ;
	do {
	    failed = false ;
	    result = activeObjectMap.get(key) ;

	    try {
		result.enter() ;
	    } catch (Exception exc) {
		failed = true ;
	    }
	} while (failed) ;

	return result ;
    }

    @InfoMethod
    private void servantAlreadyActivated() { }

    @InfoMethod
    private void upcallToIncarnate() { }

    @InfoMethod
    private void incarnateFailed() { }

    @InfoMethod
    private void incarnateComplete() { }

    @InfoMethod
    private void servantAlreadyAssignedToID() { }

    @Poa
    protected java.lang.Object internalGetServant( byte[] id, 
	String operation ) throws ForwardRequest {

        poa.lock() ;
        try {
            ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
            AOMEntry entry = enterEntry(key) ;
            java.lang.Object servant = activeObjectMap.getServant( entry ) ;
            if (servant != null) {
                servantAlreadyActivated() ;
                return servant ;
            }

            if (activator == null) {
                entry.incarnateFailure() ;
                throw poa.invocationWrapper().poaNoServantManager() ;
            }

            // Drop the POA lock during the incarnate call and
            // re-acquire it afterwards.  The entry state machine
            // prevents more than one thread from executing the
            // incarnate method at a time within the same POA.
            try {
                upcallToIncarnate() ;

                poa.unlock() ;

                servant = activator.incarnate(id, poa);

                if (servant == null) {
                    servant = new NullServantImpl(
                        poa.omgInvocationWrapper().nullServantReturned());
                }
            } catch (ForwardRequest freq) {
                throw freq ;
            } catch (SystemException exc) {
                throw exc ;
            } catch (Throwable exc) {
                throw poa.invocationWrapper().poaServantActivatorLookupFailed(
                    exc ) ;
            } finally {
                poa.lock() ;

                // servant == null means incarnate threw an exception,
                // while servant instanceof NullServant means incarnate returned a
                // null servant.  Either case is an incarnate failure to the
                // entry state machine.
                if ((servant == null) || (servant instanceof NullServant)) {
                    incarnateFailed() ;

                    // XXX Does the AOM leak in this case? Yes,
                    // but the problem is hard to fix.  There may be
                    // a number of threads waiting for the state to change
                    // from INCARN to something else, which is VALID or
                    // INVALID, depending on the incarnate result.
                    // The activeObjectMap.get() call above creates an
                    // ActiveObjectMap.Entry if one does not already exist,
                    // and stores it in the keyToEntry map in the AOM.
                    entry.incarnateFailure() ;
                } else {
                    // here check for unique_id policy, and if the servant
                    // is already registered for a different ID, then throw
                    // OBJ_ADAPTER exception, else activate it. Section 11.3.5.1
                    // 99-10-07.pdf
                    if (isUnique) {
                        // check if the servant already is associated with some id
                        if (activeObjectMap.contains((Servant)servant)) {
                            servantAlreadyAssignedToID() ;
                            entry.incarnateFailure() ;
                            throw poa.invocationWrapper().poaServantNotUnique() ;
                        }
                    }

                    incarnateComplete() ;

                    entry.incarnateComplete() ;
                    activateServant(key, entry, (Servant)servant);
                }
            }

            return servant ;
        } finally {
            poa.unlock() ;
        }
    }

    @Poa
    @Override
    public void returnServant() {
        poa.lock() ;
        try {
            OAInvocationInfo info = orb.peekInvocationInfo();
            // 6878245: added null check.
            if (info == null) {
                return ;
            }
            byte[] id = info.id() ;
            ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
            AOMEntry entry = activeObjectMap.get( key ) ;
            entry.exit() ;
        } finally {
            poa.unlock();
        }
    }

    @Poa
    public void etherealizeAll() {	
	if (activator != null)  {
	    Set<ActiveObjectMap.Key> keySet = activeObjectMap.keySet() ;

	    // Copy the elements in the set to an array to avoid
	    // changes in the set due to concurrent modification
	    ActiveObjectMap.Key[] keys = 
		keySet.toArray(new ActiveObjectMap.Key[keySet.size()]) ;

	    for (int ctr=0; ctr<keySet.size(); ctr++) {
		ActiveObjectMap.Key key = keys[ctr] ;
		AOMEntry entry = activeObjectMap.get( key ) ;
		Servant servant = activeObjectMap.getServant( entry ) ;
		if (servant != null) {
		    boolean remainingActivations = 
			activeObjectMap.hasMultipleIDs(entry) ;

		    // Here we etherealize in the thread that called this 
		    // method, rather than etherealizing in a new thread 
		    // as in the deactivate case.  We still inform the 
		    // entry state machine so that only one thread at a 
		    // time can call the etherealize method.
		    entry.startEtherealize( null ) ;
		    try {
			poa.unlock() ;
			try {
			    activator.etherealize(key.id(), poa, servant, true,
				remainingActivations);
			} catch (Exception exc) {
			    // ignore all exceptions
			}
		    } finally {
			poa.lock() ;
			entry.etherealizeComplete() ;
		    }
		}
	    }
	}
    }

    public ServantManager getServantManager() throws WrongPolicy {
	return activator;
    }

    @Poa
    public void setServantManager( 
	ServantManager servantManager ) throws WrongPolicy {

	if (activator != null) {
            throw poa.invocationWrapper().servantManagerAlreadySet();
        }

	if (servantManager instanceof ServantActivator) {
            activator = (ServantActivator) servantManager;
        } else {
            throw poa.invocationWrapper().servantManagerBadType();
        }
    }

    public Servant getDefaultServant() throws NoServant, WrongPolicy 
    {
	throw new WrongPolicy();
    }

    public void setDefaultServant( Servant servant ) throws WrongPolicy
    {
	throw new WrongPolicy();
    }

    @Poa
    private class Etherealizer extends Thread {
	private POAPolicyMediatorImpl_R_USM mediator ;
	private ActiveObjectMap.Key key ;
	private AOMEntry entry ;
	private Servant servant ;

	Etherealizer( POAPolicyMediatorImpl_R_USM mediator, 
	    ActiveObjectMap.Key key, AOMEntry entry, Servant servant )
	{
	    this.mediator = mediator ;
	    this.key = key ;
	    this.entry = entry;
	    this.servant = servant;
	}

	@InfoMethod
	private void key( ActiveObjectMap.Key key ) { }

	@Poa
        @Override
	public void run() {
	    key( key ) ;

	    try {
		mediator.activator.etherealize( key.id(), mediator.poa, servant,
		    false, mediator.activeObjectMap.hasMultipleIDs( entry ) );
	    } catch (Exception exc) {
		// ignore all exceptions
	    }

	    try {
		mediator.poa.lock() ;

		entry.etherealizeComplete() ;
		mediator.activeObjectMap.remove( key ) ;

		POAManagerImpl pm = (POAManagerImpl)mediator.poa.the_POAManager() ;
		POAFactory factory = pm.getFactory() ;
		factory.unregisterPOAForServant( mediator.poa, servant);
	    } finally {
		mediator.poa.unlock() ;
	    }
	}
    } 

    @Poa
    @Override
    public void deactivateHelper( ActiveObjectMap.Key key, AOMEntry entry, 
	Servant servant ) throws ObjectNotActive, WrongPolicy 
    {
	if (activator == null) {
            throw poa.invocationWrapper().poaNoServantManager();
        }
	    
	Etherealizer eth = new Etherealizer( this, key, entry, servant ) ;
	entry.startEtherealize( eth ) ;
    }

    @Poa
    public Servant idToServant( byte[] id ) 
	throws WrongPolicy, ObjectNotActive
    {
	ActiveObjectMap.Key key = new ActiveObjectMap.Key( id ) ;
	AOMEntry entry = activeObjectMap.get(key);

	Servant servant = activeObjectMap.getServant( entry ) ;
	if (servant != null) {
            return servant;
        } else {
            throw new ObjectNotActive();
        }
    }
}
