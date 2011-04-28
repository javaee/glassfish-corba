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

package corba.connectioncache ;

import com.sun.corba.se.spi.transport.connection.ContactInfo;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class ContactInfoImpl implements ContactInfo<ConnectionImpl> {
    private String address ;

    private static AtomicLong nextId =
        new AtomicLong() ;
    private static AtomicBoolean simulateAddressUnreachable = 
        new AtomicBoolean() ;

    private static ConcurrentMap<String,ContactInfoImpl> cinfoMap =
        new ConcurrentHashMap<String,ContactInfoImpl>() ;

    private RandomDelay rdel ;

    private ContactInfoImpl( String address, int minDelay, int maxDelay ) {
        this.address = address ;
        rdel = new RandomDelay( minDelay, maxDelay ) ;
    }

    public static ContactInfoImpl get( String address ) {
        return get( address, 0, 0 ) ;
    }

    public static ContactInfoImpl get( String address, int minDelay, int maxDelay ) {
        ContactInfoImpl result = new ContactInfoImpl( address, minDelay, maxDelay ) ;
        ContactInfoImpl entry = cinfoMap.putIfAbsent( address, result ) ;
        if (entry == null)
            return result ;
        else
            return entry ;
    }

    public void remove( String address ) {
        cinfoMap.remove( address ) ;
    }

    public void setUnreachable( boolean arg ) {
        simulateAddressUnreachable.set( arg ) ;
    }

    public ConnectionImpl createConnection() throws IOException {
        if (simulateAddressUnreachable.get()) {
            throw new IOException( "Address " + address 
                + " is currently unreachable" ) ;
        } else {
            long id = nextId.getAndIncrement() ;
            ConnectionImpl result = new ConnectionImpl( address, id, this ) ;
            return result ;
        }
    }

    @Override
    public String toString() {
        return "ContactInfoImpl[" + address + "]" ;
    }
}

