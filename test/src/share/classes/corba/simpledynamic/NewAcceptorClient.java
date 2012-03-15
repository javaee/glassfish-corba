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

package corba.simpledynamic;

import java.net.Socket;
import java.util.Properties;
import java.rmi.RemoteException ;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportDefault;
import org.glassfish.pfl.basic.func.UnaryVoidFunction;

import org.testng.Assert ;
import org.testng.annotations.Test ;

public class NewAcceptorClient extends Framework {
    private static final int SERVER_PORT = Integer.parseInt( PORT_NUM ) ;

    // Make sure that the ORB does not create any default acceptors.
    @Override
    protected void setServerPort( Properties props ) {
        super.setServerPort( props ) ;
        props.setProperty( ORBConstants.NO_DEFAULT_ACCEPTORS, "true" ) ;
        props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "1" ) ;
    }

    // Can be overridden if necessary to allow the ORB to be further
    // configured before it is used.
    @Override
    protected void updateORB( ORB orb, boolean isServer ) {
        final Acceptor listener = TransportDefault.makeLazyCorbaAcceptor(orb,
            SERVER_PORT, "localhost", "IIOP_CLEAR_TEXT" ) ;

        UnaryVoidFunction<Socket> func = new UnaryVoidFunction<Socket>() {
            public void evaluate( Socket sock ) {
                msg( "Processing message on socket " + sock ) ;
                listener.processSocket( sock ) ;
            }
        } ;

        final Acceptor processor = TransportDefault.makeAcceptOnlyCorbaAcceptor(
            orb, SERVER_PORT, "localhost", "IIOP_CLEAR_TEXT", func ) ;

        orb.getTransportManager().registerAcceptor( listener ) ;
        orb.getTransportManager().registerAcceptor( processor ) ;
        // Called for side-effect of initializing IORTemplate and transport
        orb.getFVDCodeBaseIOR() ;
    }

    private Echo makeServant( String name ) {
        try {
            return new EchoImpl( name ) ;
        } catch (RemoteException rex) {
            Assert.fail( "Unexpected remote exception " + rex ) ;
            return null ; // never reached
        }
    }

    private void msg( String msg ) {
        System.out.println( "NewAcceptorClient: " + msg ) ;
    }

    private static final int ITERATIONS = 10 ;

    @Test
    public void testNewAcceptor() throws RemoteException {
        final Echo servant = makeServant( "acceptorTest" ) ;
        bindServant( servant, Echo.class, "AcceptorTest" ) ;
        Echo clientRef = findStub( Echo.class, "AcceptorTest" ) ;

        String data = "This is my test string" ;

        for (int ctr=0; ctr<ITERATIONS; ctr++) {
            Object result = clientRef.echo( data ) ;
            Assert.assertTrue( result instanceof String );
            String strres = (String)result ;
            Assert.assertEquals(strres, data);
        }
    }

    public static void main( String[] args ) {
        Class[] classes = { NewAcceptorClient.class } ;
        Framework.run( "gen/corba/simpledynamic/test-output", classes ) ;
    }
}
