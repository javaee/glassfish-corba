/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.spi.folb;

import java.io.Serializable ;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

/** Class representing endpoint info for the ORB.
 *
 * @author ken
 */
public class SocketInfo implements Serializable {
    private final String type ;
    private final String host ;
    private final int port ;

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

    public void write( OutputStream os ) {
        os.write_string( type ) ;
        os.write_string( host ) ;
        os.write_long(port);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( "SocketInfo[" ) ;
        sb.append( "type=" ) ;
        sb.append( type ) ;
        sb.append( " host=" ) ;
        sb.append( host ) ;
        sb.append( " port=" ) ;
        sb.append( port ) ;
        sb.append( ']' ) ;
        return sb.toString() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final SocketInfo other = (SocketInfo) obj;

        if ((this.type == null) ? (other.type() != null)
            : !this.type.equals(other.type())) {

            return false;
        }

        if ((this.host == null) ? (other.host() != null)
            : !this.host.equals(other.host())) {

            return false;
        }

        if (this.port != other.port()) {
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
