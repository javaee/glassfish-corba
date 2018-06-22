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
// Created       : 2000 Nov 07 (Tue) 16:29:22 by Harold Carr.
// Last Modified : 2001 Feb 07 (Wed) 16:36:40 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

public class MyServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public static final String baseMsg = MyServantLocator.class.getName();

    public ORB orb;

    public MyServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
        throws
            ForwardRequest
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".preinvoke " + soid);

        if (soid.equals(C.idlAlwaysForward)) {

            throw new ForwardRequest(
               poa.create_reference_with_id(C.idlAlwaysForwardedToo.getBytes(),
                                            idlSLIHelper.id()));

        } else if (soid.startsWith("idl")) {

            // IDL.

            if (operation.equals(C.raiseForwardRequestInPreinvoke) &&
                soid.equals(C.idlSLI1)) 
            {

                throw new ForwardRequest(
                    poa.create_reference_with_id(C.idlSLI2.getBytes(),
                                                 idlSLIHelper.id()));

            } else if (operation.equals(C.raiseObjectNotExistInPreinvoke)) {

                throw new OBJECT_NOT_EXIST();

            } else if (operation.equals(C.raiseSystemExceptionInPreinvoke)) {

                throw new IMP_LIMIT();

            } else if (operation.equals(C.throwThreadDeathInPreinvoke)) {

                throw new ThreadDeath();

            }

            // Test server-side PICurrent.
            boolean ensure = false;
            if (operation.equals(C.sPic1)) {
                ensure = true;
            }
            C.testAndIncrementPICSlot(ensure, "preinvoke",
                                      SsPicInterceptor.sPic1ASlotId, 1, orb);
            C.testAndIncrementPICSlot(ensure, "preinvoke",
                                      SsPicInterceptor.sPic1BSlotId, 1, orb);

            return new idlSLIServant(orb);

        } else if (soid.startsWith("rmii")) {

            // RMII.

            return MyServantActivator.makermiiIServant(orb, soid);

        } else {

            throw new INTERNAL(U.SHOULD_NOT_SEE_THIS);

        }
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        String soid = new String(oid);
        U.sop(baseMsg + ".postinvoke " + soid);
        if (operation.equals(C.raiseSystemExceptionInPostinvoke) || 
            operation.equals(C.raiseUserInServantThenSystemInPOThenSE) ||
            operation.equals(C.raiseSystemInServantThenPOThenSE))
        {

            throw new IMP_LIMIT();

        } else if (operation.equals(C.throwThreadDeathInPostinvoke)) {

            throw new ThreadDeath();

        } else if (operation.equals(C.throwThreadDeathInServantThenSysInPostThenSysInSendException))
        {

            throw new IMP_LIMIT();
        }


        // Test server-side PICurrent.
        boolean ensure = false;
        if (operation.equals(C.sPic1)) {
            ensure = true;
        }
        C.testAndIncrementPICSlot(ensure, "postinvoke",
                                  SsPicInterceptor.sPic1ASlotId, 4, orb);
        C.testAndIncrementPICSlot(ensure, "postinvoke",
                                  SsPicInterceptor.sPic1BSlotId, 4, orb);
    }
}

// End of file.
