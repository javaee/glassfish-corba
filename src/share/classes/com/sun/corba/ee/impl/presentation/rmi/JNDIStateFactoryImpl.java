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

package com.sun.corba.ee.impl.presentation.rmi ;

import java.lang.reflect.Field ;

import java.util.Hashtable;

import javax.naming.*;
import javax.naming.spi.StateFactory;

import java.security.AccessController ;
import java.security.PrivilegedAction ;

import javax.rmi.PortableRemoteObject ;

import com.sun.corba.ee.spi.orb.ORB;

import java.rmi.Remote;

// This creates a dependendcy on the implementation
// of the CosNaming service provider.
import com.sun.jndi.cosnaming.CNCtx ;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

/**
  * StateFactory that turns java.rmi.Remote objects to org.omg.CORBA.Object.
  * This version works either with standard RMI-IIOP or Dynamic RMI-IIOP.
  * Based on the original com.sun.jndi.cosnaming.RemoteToCorba and
  * com.sun.jndi.toolkit.corba.CorbaUtils.
  *
  * @author Ken Cavanaugh 
  */

public class JNDIStateFactoryImpl implements StateFactory 
{
    private static final Field orbField ;

    static {
        orbField = AccessController.doPrivileged(
            new PrivilegedAction<Field>() {

            public Field run() {
                Field fld = null;
                try {
                    Class cls = CNCtx.class;
                    fld = cls.getDeclaredField("_orb");
                    fld.setAccessible(true);
                } catch (Exception exc) {
                }
                return fld;
            }
        }) ;
    }

    public JNDIStateFactoryImpl() 
    {
    }

    /**
     * Returns the CORBA object for a Remote object.
     * If input is not a Remote object, or if Remote object uses JRMP, return null.
     * If the RMI-IIOP library is not available, throw ConfigurationException.
     *
     * @param orig The object to turn into a CORBA object. If not Remote, 
     *             or if is a JRMP stub or impl, return null.
     * @param name Ignored
     * @param ctx The non-null CNCtx whose ORB to use.
     * @param env Ignored
     * @return The CORBA object for <tt>orig</tt> or null.
     * @exception ConfigurationException If the CORBA object cannot be obtained
     *    due to configuration problems
     * @exception NamingException If some other problem prevented a CORBA
     *    object from being obtained from the Remote object.
     */
    public Object getStateToBind(Object orig, Name name, Context ctx,
        Hashtable<?,?> env) throws NamingException 
    {
        if (orig instanceof org.omg.CORBA.Object) {
            return orig;
        }

        if (!(orig instanceof Remote)) {
            return null;
        }

        ORB orb = getORB( ctx ) ; 
        if (orb == null) {
            // Wrong kind of context, so just give up and let another StateFactory
            // try to satisfy getStateToBind.
            return null ;
        }

        Remote stub = null;

        try {
            stub = PortableRemoteObject.toStub( (Remote)orig ) ;
        } catch (Exception exc) {
            Exceptions.self.noStub( exc ) ;
            // Wrong sort of object: just return null to allow another StateFactory
            // to handle this.  This can happen easily because this StateFactory
            // is specified for the application, not the service context provider.
            return null ;
        }

        if (StubAdapter.isStub( stub )) {
            try {
                StubAdapter.connect( stub, orb ) ; 
            } catch (Exception exc) {
                Exceptions.self.couldNotConnect( exc ) ;

                if (!(exc instanceof java.rmi.RemoteException)) {
                    // Wrong sort of object: just return null to allow another StateFactory
                    // to handle this call.
                    return null ;
                }

                // ignore RemoteException because stub might have already
                // been connected
            }
        }

        return stub ;
    }

    // This is necessary because the _orb field is package private in 
    // com.sun.jndi.cosnaming.CNCtx.  This is not an ideal solution.
    // The best solution for our ORB is to change the CosNaming provider
    // to use the StubAdapter.  But this has problems as well, because
    // other vendors may use the CosNaming provider with a different ORB
    // entirely.
    private ORB getORB( Context ctx ) 
    {
        ORB orb = null ;

        try {
            orb = (ORB)orbField.get( ctx ) ;
        } catch (Exception exc) {
            Exceptions.self.couldNotGetORB( exc, ctx ) ;
            // ignore the exception and return null.
            // Note that the exception may be because ctx
            // is not a CosNaming context.
        }

        return orb ;
    }
}
