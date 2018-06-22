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

package corba.poapolicies;

import Util.CreationMethods;
import Util.FactoryPOA;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

public class BasicObjectFactoryImpl extends FactoryPOA 
{
    final boolean useServantToReference = true;

    final String idString = new String("Blue Skies, Black Death");
    
    POA poa;

    public java.lang.Object doneCV = new java.lang.Object();
    
    void setPOA(POA p) {
        poa = p;
    }

    public void overAndOut() {
        synchronized (doneCV) {
            doneCV.notifyAll();
        }
    }
    
    public org.omg.CORBA.Object create(String intfName,
                                       String implName, CreationMethods how) {
        try {

            System.err.println("Creating: " + implName);

            // create Servant first.

            Servant s;
            try {
            s = (Servant)
                Class.forName(implName).newInstance();
            } catch (Exception ex) {
                System.err.println("Problems finding: " + implName);
                ex.printStackTrace();
                System.err.println("---");
                throw ex;
            }

            org.omg.CORBA.Object ref = null;

            switch (how.value()) {
            case Util.CreationMethods._EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS:
                {
                    byte[] id = poa.activate_object(s);
                    if (useServantToReference)
                        ref = poa.servant_to_reference(s);
                    else
                        ref = poa.id_to_reference(id);
                }
                break;
            case Util.CreationMethods._EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS:
                {
                    byte[] id = idString.getBytes();
                    poa.activate_object_with_id(id, s);
                    if (useServantToReference)
                        ref = poa.servant_to_reference(s);
                    else
                        ref = poa.id_to_reference(id);
                }
                break;
            case Util.CreationMethods._CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS:
                {
                    ref = poa.create_reference(intfName);
                    byte[] id = poa.reference_to_id(ref);
                    poa.activate_object_with_id(id, s);
                }
                break;
            case Util.CreationMethods._CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS:
                {
                    String newIdString = "ABCD";
                    byte[] id = newIdString.getBytes();
                    ref =
                        poa.create_reference_with_id(id, intfName);
                    poa.activate_object_with_id(id, s);
                }
                break;
            }
            return ref;
        } catch (Exception e) {
            System.err.println("BasicObjectFactoryImpl");
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
        
            
