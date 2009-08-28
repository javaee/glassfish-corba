/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.impl.transport;

import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.spi.ior.IORTemplate;
import java.net.Socket;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.generic.UnaryVoidFunction ;

/** This version of an acceptor is the other half of CorbaAcceptorLazyImpl.
 * The idea is that AcceptOnly will NOT contribute to an IORTemplate, and will
 * actually accept from a ServerSocket (and so it must initialize the
 * server socket and close it).  The LazyImpl will contribute to an IORTemplate,
 * and will not actually accept, but does the actual processing of sockets
 * from the server socket.
 *
 * @author ken
 */
public class CorbaAcceptorAcceptOnlyImpl extends SocketOrChannelAcceptorImpl {
    private UnaryVoidFunction<Socket> operation ;

    public CorbaAcceptorAcceptOnlyImpl( ORB orb, int port,
        String name, String type, UnaryVoidFunction<Socket> operation ) {
        super( orb, port, name, type ) ;
        this.operation = operation  ;
    }

    @Override
    public void accept() {
        operation.evaluate( getAcceptedSocket() ) ;
    }

    @Override
    public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        // does nothing in this case.
    }
}
