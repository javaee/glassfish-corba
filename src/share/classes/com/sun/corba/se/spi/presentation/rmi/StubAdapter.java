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

package com.sun.corba.ee.spi.presentation.rmi ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA.portable.ObjectImpl ;
import org.omg.CORBA.portable.OutputStream ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.POAManager ;
import org.omg.PortableServer.POAManagerPackage.State ;
import org.omg.PortableServer.Servant ;

import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.ServantNotActive ;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive ;

import org.omg.CORBA.ORB ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import com.sun.corba.ee.impl.oa.poa.POAManagerImpl ;

/** Provide access to stub delegate and type id information
 * independent of the stub type.  This class exists because
 * ObjectImpl does not have an interface for the 3 delegate and
 * type id methods, so a DynamicStub has a different type.
 * We cannot simply change ObjectImpl as it is a standard API.
 * We also cannot change the code generation of Stubs, as that
 * is also standard.  Hence I am left with this ugly class.
 */ 
public abstract class StubAdapter 
{
    private StubAdapter() {}

    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public static boolean isStubClass( Class cls )
    {
        return (ObjectImpl.class.isAssignableFrom( cls )) ||
            (DynamicStub.class.isAssignableFrom( cls )) ;
    }

    public static boolean isStub( Object stub )
    {
        return (stub instanceof DynamicStub) ||
            (stub instanceof ObjectImpl) ;
    }

    public static void setDelegate( Object stub, Delegate delegate ) 
    {
        if (stub instanceof DynamicStub) {
            ((DynamicStub) stub).setDelegate(delegate);
        } else if (stub instanceof ObjectImpl) {
            ((ObjectImpl) stub)._set_delegate(delegate);
        } else {
            throw wrapper.setDelegateRequiresStub();
        }
    }

    /** Use implicit activation to get an object reference for the servant.
     */
    public static org.omg.CORBA.Object activateServant( Servant servant ) 
    {
        POA poa = servant._default_POA() ;
        org.omg.CORBA.Object ref = null ;

        try {
            ref = poa.servant_to_reference( servant ) ;
        } catch (ServantNotActive sna) {
            throw wrapper.getDelegateServantNotActive( sna ) ;
        } catch (WrongPolicy wp) {
            throw wrapper.getDelegateWrongPolicy( wp ) ;
        }

        // Make sure that the POAManager is activated if no other
        // POAManager state management has taken place.
        POAManager mgr = poa.the_POAManager() ;
        if (mgr instanceof POAManagerImpl) {
            // This servant is managed by one of our POAs,
            // so only activate it if there has not been
            // an explicit state change, that is, if the POA
            // has never changed state from the initial 
            // HOLDING state.
            POAManagerImpl mgrImpl = (POAManagerImpl)mgr ;
            mgrImpl.implicitActivation() ;
        } else {
            // This servant is not managed by one of our POAs,
            // so activate it if the state is HOLDING, which is the
            // initial state.  Note that this may NOT be exactly
            // what the user intended!
            if (mgr.get_state().value() == State._HOLDING) {
                try {
                    mgr.activate() ;
                } catch (AdapterInactive ai) {
                    throw wrapper.adapterInactiveInActivateServant( ai ) ;
                }
            }
        }

        return ref ;
    }

    /** Given any Tie, return the corresponding object refernce, activating
     * the Servant if necessary.
     */
    public static org.omg.CORBA.Object activateTie( Tie tie )
    {
        /** Any implementation of Tie should be either a Servant or an ObjectImpl,
         * depending on which style of code generation is used.  rmic -iiop by
         * default results in an ObjectImpl-based Tie, while rmic -iiop -poa
         * results in a Servant-based Tie.  Dynamic RMI-IIOP also uses Servant-based
         * Ties (see impl.presentation.rmi.ReflectiveTie).
         */
        if (tie instanceof ObjectImpl) {
            return tie.thisObject() ;
        } else if (tie instanceof Servant) {
            Servant servant = (Servant)tie ;
            return activateServant( servant ) ;
        } else {
            throw wrapper.badActivateTieCall() ;
        }
    }


    /** This also gets the delegate from a Servant by
     * using Servant._this_object()
     */
    public static Delegate getDelegate( Object stub ) 
    {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub) stub).getDelegate();
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._get_delegate();
        } else if (stub instanceof Tie) {
            Tie tie = (Tie)stub ;
            org.omg.CORBA.Object ref = activateTie( tie ) ;
            return getDelegate( ref ) ;
        } else {
            throw wrapper.getDelegateRequiresStub();
        }
    }
    
    public static ORB getORB( Object stub ) 
    {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub)stub).getORB() ;
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl) stub)._orb() ;
        } else {
            throw wrapper.getOrbRequiresStub() ;
        }
    }

    public static String[] getTypeIds( Object stub )
    {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub)stub).getTypeIds() ;
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl)stub)._ids() ;
        } else {
            throw wrapper.getTypeIdsRequiresStub() ;
        }
    }

    public static void connect( Object stub, 
        ORB orb ) throws java.rmi.RemoteException 
    {
        if (stub instanceof DynamicStub) {
            ((DynamicStub)stub).connect( 
                (com.sun.corba.ee.spi.orb.ORB)orb ) ;
        } else if (stub instanceof javax.rmi.CORBA.Stub) {
            ((javax.rmi.CORBA.Stub)stub).connect( orb ) ;
        } else if (stub instanceof ObjectImpl) {
            orb.connect( (org.omg.CORBA.Object)stub ) ;
        } else {
            throw wrapper.connectRequiresStub() ;
        }
    }

    public static boolean isLocal( Object stub )
    {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub)stub).isLocal() ;
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl)stub)._is_local() ;
        } else {
            throw wrapper.isLocalRequiresStub() ;
        }
    }

    public static OutputStream request( Object stub, 
        String operation, boolean responseExpected ) 
    {
        if (stub instanceof DynamicStub) {
            return ((DynamicStub)stub).request( operation,
                responseExpected ) ;
        } else if (stub instanceof ObjectImpl) {
            return ((ObjectImpl)stub)._request( operation,
                responseExpected ) ;
        } else {
            throw wrapper.requestRequiresStub() ;
        }
    }
}
