/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

//
// Created       : 2000 Nov 11 (Sat) 10:45:48 by Harold Carr.
// Last Modified : 2001 May 10 (Thu) 15:45:53 by Harold Carr.
//

package org.glassfish.rmic.classes.hcks;

import org.omg.CORBA.ORB;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

@SuppressWarnings({"WeakerAccess", "unused"})
public class RmiIIServantPOA extends PortableRemoteObject implements RmiII {
    public static final String baseMsg = RmiIIServantPOA.class.getName();

    public String name;

    RmiIIServantPOA(ORB orb, String name) throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
        this.name = name;
    }

    public String sayHello() {
        return "Hello, World!";
    }

    public int sendBytes(byte[] x) {
        if (x == null) return -1;
        return x.length;
    }

    public Object sendOneObject(Object x) throws RmiIMyException {
        return x;
    }

    public Object sendTwoObjects(Object x, Object y) {
        return x;
    }

    public String makeColocatedCallFromServant() throws RemoteException {
        return "";
    }

    private String doCall(RmiII rrmiiI, String resultSoFar) throws Exception {
        String result = rrmiiI.colocatedCallFromServant(resultSoFar);
        String op = "op";
        return op + " " + result;
    }

    public String colocatedCallFromServant(String a) throws RemoteException {
        String op = "op";
        return op + " " + a;
    }

    public String throwThreadDeathInServant(String a) throws RemoteException, ThreadDeath {
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer(boolean isSerializable) throws RemoteException {
        return isSerializable ? "" : new RmiIIServantPOA(null, "");
    }
}

// End of file.
