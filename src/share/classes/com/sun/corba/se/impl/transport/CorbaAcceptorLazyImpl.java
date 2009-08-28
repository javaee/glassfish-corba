/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.impl.transport;

import com.sun.corba.se.spi.orb.ORB;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;

/** A version of an Acceptor that does not own the ServerSocket.
 * Instead, SelectableChannels obtained from the ServerSocket are
 * given to the processSocket method 
 *
 * @author ken
 */
public class CorbaAcceptorLazyImpl extends CorbaAcceptorBase {

    public CorbaAcceptorLazyImpl( ORB orb, int port, String name, String type ) {
        super( orb, port, name, type ) ;
    }

    public Socket getAcceptedSocket() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public SelectableChannel getChannel() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public boolean initialize() {
        return false ;
    }

    public void close() {
        // NO-OP in this case
    }

    public ServerSocket getServerSocket() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public void doWork() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    @Override
    public boolean shouldRegisterAcceptEvent() {
        return false;
    }
}
