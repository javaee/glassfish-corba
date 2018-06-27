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

//
// Created       : 2000 Nov 08 (Wed) 09:18:37 by Harold Carr.
// Last Modified : 2000 Nov 25 (Sat) 13:11:12 by Harold Carr.

package corba.hcks;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.SystemException;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;

public class MyServantActivator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantActivator
{

    public static final String baseMsg = MyServantActivator.class.getName();

    public ORB orb;

    public MyServantActivator(ORB orb) { this.orb = orb; }

    public Servant incarnate(byte[] oid, POA poa)
        throws
            ForwardRequest
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".incarnate " + soid);

        if (soid.startsWith("idl")) {

            // IDL.

            if (soid.equals(C.idlSAI1)) {
                throw new ForwardRequest(
                    poa.create_reference_with_id(C.idlSAI2.getBytes(),
                                                 idlSAIHelper.id()));
            } else if (soid.equals(C.idlSAIRaiseObjectNotExistInIncarnate)) {
                throw new OBJECT_NOT_EXIST();
            } else if (soid.equals(C.idlSAIRaiseSystemExceptionInIncarnate)){
                throw new IMP_LIMIT();
            }
            return new idlSAIServant(orb);

        } else if (soid.startsWith("rmii")) {
            
            // RMII

            return makermiiIServant(orb, soid);

        } else {
            SystemException e = new INTERNAL(U.SHOULD_NOT_SEE_THIS);
            U.sopUnexpectedException(baseMsg + ".incarnate", e);
            throw e;
        }
    }

    public void  etherealize(byte[] oid, POA poa, Servant servant,
                             boolean cleanupInProgress,
                             boolean remainingActivations)
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".etherealize " + soid);
    }

    static Servant makermiiIServant(ORB orb, String name)
    {
        Servant servant = null;
        try {
            servant = 
                (Servant)javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA(orb, name));
        } catch (Exception e) {
            U.sopUnexpectedException(baseMsg + ".makermiiIServant", e);
        }
        return servant;
    }
}

// End of file.
