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
package com.sun.corba.ee.impl.oa.poa;

import java.util.EmptyStackException;

import org.omg.PortableServer.*;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.logging.POASystemException ;

public class DelegateImpl implements org.omg.PortableServer.portable.Delegate
{
    private static final POASystemException wrapper =
        POASystemException.self ;

    private ORB orb ;
    private POAFactory factory;

    public DelegateImpl(ORB orb, POAFactory factory){
        this.orb = orb ;
        this.factory = factory;
    }

    public org.omg.CORBA.ORB orb(Servant self)
    {
        return orb;
    }

    public org.omg.CORBA.Object this_object(Servant self)
    {
        byte[] oid;
        POA poa;
        try {
            oid = orb.peekInvocationInfo().id();
            poa = (POA)orb.peekInvocationInfo().oa();
            String repId = self._all_interfaces(poa,oid)[0] ;
            return poa.create_reference_with_id(oid, repId); 
        } catch (EmptyStackException notInInvocationE) { 
            //Not within an invocation context
            POAImpl defaultPOA = null;
            try {
                defaultPOA = (POAImpl)self._default_POA();
            } catch (ClassCastException exception){
                throw wrapper.defaultPoaNotPoaimpl( exception ) ;
            }

            try {
                if (defaultPOA.getPolicies().isImplicitlyActivated() ||
                    (defaultPOA.getPolicies().isUniqueIds() && 
                     defaultPOA.getPolicies().retainServants())) {
                    return defaultPOA.servant_to_reference(self);
                } else {
                    throw wrapper.wrongPoliciesForThisObject() ;
                }    
            } catch ( org.omg.PortableServer.POAPackage.ServantNotActive e) {
                throw wrapper.thisObjectServantNotActive( e ) ;
            } catch ( org.omg.PortableServer.POAPackage.WrongPolicy e) {
                throw wrapper.thisObjectWrongPolicy( e ) ;
            }
        } catch (ClassCastException e) {
            throw wrapper.defaultPoaNotPoaimpl( e ) ;
        }
    }

    public POA poa(Servant self)
    {
        try {
            return (POA)orb.peekInvocationInfo().oa();
        } catch (EmptyStackException exception){
            POA returnValue = factory.lookupPOA(self);
            if (returnValue != null) {
                return returnValue;
            }
            
            throw wrapper.noContext( exception ) ;
        }
    }

    public byte[] object_id(Servant self)
    {
        try{
            return orb.peekInvocationInfo().id();
        } catch (EmptyStackException exception){
            throw wrapper.noContext(exception) ;
        }
    }

    public POA default_POA(Servant self)
    {
        return factory.getRootPOA();
    }

    public boolean is_a(Servant self, String repId)
    {
        String[] repositoryIds = self._all_interfaces(poa(self),object_id(self));
        for ( int i=0; i<repositoryIds.length; i++ ) {
            if (repId.equals(repositoryIds[i])) {
                return true;
            }
        }

        return false;
    }

    public boolean non_existent(Servant self)
    {
        //REVISIT
        try{
            byte[] oid = orb.peekInvocationInfo().id();
            return oid == null ;
        } catch (EmptyStackException exception){
            throw wrapper.noContext(exception) ;
        }
    }

    // The get_interface() method has been replaced by get_interface_def()

    public org.omg.CORBA.Object get_interface_def(Servant Self)
    {
        throw wrapper.methodNotImplemented() ;
    }
}
