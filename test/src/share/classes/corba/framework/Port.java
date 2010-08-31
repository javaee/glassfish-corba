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
package corba.framework;

import java.io.*;
import java.net.*;

/**
 * Simple port abstraction with the capability of
 * late binding to an unused port.
 */
public class Port
{
    public static class PortException extends RuntimeException {
        public PortException(String reason) {
            super(reason);
        }
    }

    private static final int UNBOUND = 0;
    private int port = Port.UNBOUND;

    /**
     * Port will late bind to an unused port when
     * getValue or toString is called.
     */
    public Port() {}


    /** 
     * Will use the provided port value when 
     * getValue or toString is called.
     */
    public Port(int value) {
        port = value;
    }

    public int getValue() {
        bind();

        return port;
    }

    public String toString() {
        return String.valueOf(getValue());
    }

    /**
     * Is this port available for use?
     */
    public boolean isFree() {
        bind();

        return Port.isFree(port);
    }

    /**
     * If the port is unbound, find an unused and
     * set our instance variable to it.
     */
    private int bind() {
        if (port == Port.UNBOUND) {

            try {
                ServerSocket socket = new ServerSocket(0);

                port = socket.getLocalPort();

                socket.close();
            } catch (IOException ex) {
                throw new PortException(ex.getMessage());
            }
        }
        
        return port;
    }
    
    /**
     * Determine if the provided port is unused.  Tries to
     * create a ServerSocket at the given port.
     *
     *@param port Port to test
     *@return true if port is unused
     */
    public static boolean isFree(int port) {
        boolean result = false;
        try {
            ServerSocket ssocket = new ServerSocket(port);

            ssocket.close();

            return true;

        } catch (IOException ex) {
            return false;
        }
    }
}
