/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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


package com.sun.corba.ee.spi.transport ;

import com.sun.corba.ee.spi.protocol.ClientDelegate ;
import com.sun.corba.ee.spi.protocol.ClientDelegateFactory ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.ior.IOR ;

// Internal imports, not used in the interface to this package
import com.sun.corba.ee.impl.protocol.ClientDelegateImpl ;
import com.sun.corba.ee.impl.transport.AcceptorAcceptOnlyImpl;
import com.sun.corba.ee.impl.transport.ContactInfoListImpl;
import com.sun.corba.ee.impl.transport.AcceptorImpl ;
import com.sun.corba.ee.impl.transport.AcceptorLazyImpl ;
import java.net.Socket;
import org.glassfish.pfl.basic.func.UnaryVoidFunction;

/** This class provices standard building blocks for the ORB, as do all Default classes
 * in the various packages.  
 */
public abstract class TransportDefault {
    private TransportDefault() {}

    public static ContactInfoListFactory makeCorbaContactInfoListFactory(
        final ORB broker ) 
    {
        return new ContactInfoListFactory() {
            public void setORB(ORB orb) { }
            public ContactInfoList create( IOR ior ) {
                return new ContactInfoListImpl(
                    (com.sun.corba.ee.spi.orb.ORB)broker, ior ) ;
            }
        };
    }

    public static ClientDelegateFactory makeClientDelegateFactory(
        final ORB broker )
    {
        return new ClientDelegateFactory() {
            public ClientDelegate create( ContactInfoList info ) {
                return new ClientDelegateImpl(
                    (com.sun.corba.ee.spi.orb.ORB)broker, info ) ;
            }
        };
    }

    public static IORTransformer makeIORTransformer(
        final ORB broker )
    {
        return null ;
    }

    public static Acceptor makeStandardCorbaAcceptor(
        ORB orb, int port, String name, String type ) {

        return new AcceptorImpl( orb, port, name, type ) ;
    }

    public static Acceptor makeLazyCorbaAcceptor(
        ORB orb, int port, String name, String type ) {

        return new AcceptorLazyImpl( orb, port, name, type ) ;
    }

    public static Acceptor makeAcceptOnlyCorbaAcceptor(
        ORB orb, int port, String name, String type,
        UnaryVoidFunction<Socket> operation ) {

        return new AcceptorAcceptOnlyImpl( orb, port, name, type,
            operation ) ;
    }
}
    
// End of file.
