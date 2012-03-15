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
//
// Created       : 2000 Nov 11 (Sat) 10:45:48 by Harold Carr.
// Last Modified : 2001 May 10 (Thu) 15:45:53 by Harold Carr.
//

package corba.hcks;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;

public class rmiiIServantPOA
    extends 
        PortableRemoteObject
    implements
        rmiiI
{
    public static final String baseMsg = rmiiIServantPOA.class.getName();

    public ORB orb;
    public String name;

    public rmiiIServantPOA (ORB orb, String name)
        throws
            RemoteException
    { 
        // DO NOT CALL SUPER - that would connect the object.
        this.orb = orb; 
        this.name = name;
    }

    public String sayHello ()
    {
        return C.helloWorld;
    }

    public int sendBytes (byte[] x)
    {
        if (x == null)
            return -1;
        return x.length;
    }

    public Object sendOneObject (Object x)
        throws
            rmiiMyException
    {
        return x;
    }

    public Object sendTwoObjects (Object x, Object y)
    {
        return x;
    }

    public String makeColocatedCallFromServant ()
        throws
            RemoteException
    {
        rmiiI rrmiiI = null;
        String result = "";
        try {
            result = new String(U.getPOACurrentOperation(orb));

            // Colocated via narrow.

            rrmiiI = null;
            rrmiiI = (rmiiI) this.narrow(this, rmiiI.class);
            result = doCall(rrmiiI, result);

            // Colocated via PortableRemoteObject.narrow

            rrmiiI = null;
            rrmiiI = (rmiiI) PortableRemoteObject.narrow(this, rmiiI.class);
            result = doCall(rrmiiI, result);

            // Colocated via common context and PRO.narrow

            rrmiiI = null;
            rrmiiI = (rmiiI)
                U.lookupAndNarrow(name, rmiiI.class, Server.initialContext);
            result = doCall(rrmiiI, result);

        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg + C.makeColocatedCallFromServant,
                                     e);
            INTERNAL exc = new INTERNAL(U.SHOULD_NOT_SEE_THIS);
            exc.initCause( e ) ;
            throw exc ;
        }
        return result;
    }

    private String doCall(rmiiI rrmiiI, String resultSoFar)
        throws
            Exception
    {
        String result = rrmiiI.colocatedCallFromServant(resultSoFar);
        String op = new String(U.getPOACurrentOperation(orb));
        return op + " " + result;
    }

    public String colocatedCallFromServant (String a)
        throws
            RemoteException,
            Exception
    {
        String op = new String(U.getPOACurrentOperation(orb));
        return op + " " + a;
    }

    public String throwThreadDeathInServant (String a)
        throws
            RemoteException,
            ThreadDeath
    {
        U.sop(U.servant(a));
        throw new ThreadDeath();
    }

    public Object returnObjectFromServer (boolean isSerializable)
        throws
            RemoteException
    {
        if (isSerializable) {
            return new SerializableObject();
        } else {
            return new NonSerializableObject();
        }
    }
}

// End of file.
