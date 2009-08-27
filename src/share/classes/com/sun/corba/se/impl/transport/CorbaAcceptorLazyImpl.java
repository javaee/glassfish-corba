/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.impl.transport;

import com.sun.corba.se.spi.orb.ORB;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;

/**
 *
 * @author ken
 */
public class CorbaAcceptorLazyImpl extends SocketOrChannelAcceptorBase {

    public CorbaAcceptorLazyImpl( ORB orb, int port, String name, String type ) {
        super( orb, port, name, type ) ;
    }

    public Socket getAcceptedSocket() {
        // XXX Throw exception
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public SelectableChannel getChannel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean initialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void accept() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public ServerSocket getServerSocket() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void doWork() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean shouldRegisterAcceptEvent() {
        return false;
    }
}
