/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.corba.se.spi.folb;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/** Class representing endpoint info for the ORB.
 *
 * @author ken
 */
public class SocketInfo {
    private final String type ;
    private final String host ;
    private final int port ;

    public void write( OutputStream os ) {
        os.write_string( type ) ;
        os.write_string( host ) ;
        os.write_long(port);
    }

    public SocketInfo( InputStream is ) {
        this.type = is.read_string() ;
        this.host = is.read_string() ;
        this.port = is.read_long() ;
    }

    public SocketInfo( String type, String host, int port ) {
        this.type = type ;
        this.host = host ;
        this.port = port ;
    }

    public String type() { return type ; }
    public String host() { return host ; }
    public int port() { return port ; }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SocketInfo other = (SocketInfo) obj;
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 71 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 71 * hash + this.port;
        return hash;
    }
}
