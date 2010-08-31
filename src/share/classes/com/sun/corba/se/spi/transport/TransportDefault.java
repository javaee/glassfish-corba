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


package com.sun.corba.se.spi.transport ;

import com.sun.corba.se.spi.protocol.CorbaClientDelegate ;
import com.sun.corba.se.spi.protocol.ClientDelegateFactory ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.ior.IOR ;

// Internal imports, not used in the interface to this package
import com.sun.corba.se.impl.protocol.CorbaClientDelegateImpl ;
import com.sun.corba.se.impl.transport.CorbaAcceptorAcceptOnlyImpl;
import com.sun.corba.se.impl.transport.CorbaContactInfoListImpl;
import com.sun.corba.se.impl.transport.SocketOrChannelAcceptorImpl ;
import com.sun.corba.se.impl.transport.CorbaAcceptorLazyImpl ;
import com.sun.corba.se.spi.orbutil.generic.UnaryVoidFunction;
import java.net.Socket;

/** This class provices standard building blocks for the ORB, as do all Default classes
 * in the various packages.  
 */
public abstract class TransportDefault {
    private TransportDefault() {}

    public static CorbaContactInfoListFactory makeCorbaContactInfoListFactory( 
	final ORB broker ) 
    {
	return new CorbaContactInfoListFactory() {
	    public void setORB(ORB orb) { }
	    public CorbaContactInfoList create( IOR ior ) {
		return new CorbaContactInfoListImpl( 
		    (com.sun.corba.se.spi.orb.ORB)broker, ior ) ;
	    }
	};
    }

    public static ClientDelegateFactory makeClientDelegateFactory(
	final ORB broker )
    {
	return new ClientDelegateFactory() {
	    public CorbaClientDelegate create( CorbaContactInfoList info ) {
		return new CorbaClientDelegateImpl( 
		    (com.sun.corba.se.spi.orb.ORB)broker, info ) ;
	    }
	};
    }

    public static IORTransformer makeIORTransformer(
	final ORB broker )
    {
	return null ;
    }

    public static CorbaAcceptor makeStandardCorbaAcceptor( 
        ORB orb, int port, String name, String type ) {

        return new SocketOrChannelAcceptorImpl( orb, port, name, type ) ;
    }

    public static CorbaAcceptor makeLazyCorbaAcceptor(
        ORB orb, int port, String name, String type ) {

        return new CorbaAcceptorLazyImpl( orb, port, name, type ) ;
    }

    public static CorbaAcceptor makeAcceptOnlyCorbaAcceptor(
        ORB orb, int port, String name, String type,
        UnaryVoidFunction<Socket> operation ) {

        return new CorbaAcceptorAcceptOnlyImpl( orb, port, name, type,
            operation ) ;
    }
}
    
// End of file.
