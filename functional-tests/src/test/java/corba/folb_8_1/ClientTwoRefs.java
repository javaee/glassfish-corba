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
// Created       : 2002 Jul 19 (Fri) 14:50:37 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:05:14 by Harold Carr.
//

package corba.folb_8_1;

import java.util.Properties;

import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;

/**
 * @author Harold Carr
 */
public class ClientTwoRefs
{
    public static final String baseMsg = ClientTwoRefs.class.getName();

    public static boolean foundErrors = false;

    public static I iRef;
    public static I2 i2Ref;

    public static ORB orb;

    public static void main(String[] av)
    {
        try {

            if (! ColocatedCS.isColocated) {
                Properties props = new Properties();
                Client.withSticky = true;
                Client.setProperties(props);
                orb = ORB.init(av, props);
            }

            runTest();

            if (foundErrors) {
                throw new Exception("foundErrors");
            }

            System.out.println();
            System.out.println(baseMsg + ".main: PASSED");
            System.out.println(baseMsg + ".main: Test complete.");

        } catch (Throwable t) {
            System.out.println(baseMsg + ".main: FAILED");
            System.out.println(baseMsg + ".main: Test complete.");
            t.printStackTrace(System.out);
            System.exit (1);
        }
    }

    private static void runTest()
        throws Exception
    {
        System.out.println("================================================");
        System.out.println();

        iRef =
            IHelper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName1)));

        i2Ref =
            I2Helper.narrow(
                Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName2)));

        //
        // Test to ensure we are only running sticky via host/port/type
        // not the entire ContactInfo.  If returning an entire contactInfo
        // associated with a different object, then the Tie will get an
        // error - either wrong type or unknown method.
        //

        System.out.println();
        System.out.println("First talk to iRef to get stuck on it.");
        System.out.println();

        String stringResult = iRef.m("Hello");
        System.out.println(stringResult);

        // The following calls will go to the wrong reference if
        // using the entire ContactInfo as a key.  Need to return
        // a specific SocketInfo instead.


        // This returns a String in the bad case but gets no
        // error.  But the return value is the length of a string
        // rather than the string converted to an int.
        System.out.println();
        System.out.println("Test wrong return type:");
        System.out.println();

        int intResult = i2Ref.m("45");
        if (intResult != 45) {
            foundErrors = true;
            System.out.println();
            System.out.println("!!!!!!!!! WRONG RETURN TYPE");
            System.out.println();
        } else {
            System.out.println();
            System.out.println("Correct return type: " + intResult);
            System.out.println();
        }

        // This returns a String in the bad case.  When the client-side
        // tries to unmarshal an Object it breaks.
        System.out.println();
        System.out.println("Test marshaling error");
        System.out.println();
        try {
            org.omg.CORBA.Object o = i2Ref.n("dummy");
            System.out.println();
            System.out.println("Correct return type: " + o);
            System.out.println();
        } catch (MARSHAL e) {
            foundErrors = true;
            System.out.println();
            System.out.println("!!!!!!!!! MARSHALING ERROR");
            System.out.println();
        }

        // In the bad case, this ends up in a Tie that does not
        // have the "foo" method.
        System.out.println();
        System.out.println("Test unknown method:");
        System.out.println();
        try {
            intResult = i2Ref.foo(46);
            System.out.println();
            System.out.println("Correct return type: " + intResult);
            System.out.println();
        } catch (BAD_OPERATION e) {
            foundErrors = true;
            System.out.println();
            System.out.println("!!!!!!!!! UNKNOWN METHOD ERROR");
            System.out.println();
        }

        // This next call has nothing to do with failover.
        // It is just to see the information logged on the server side.

        try {
            iRef.throwRuntimeException(0);
        } catch (UNKNOWN e) {
            ;;
        }

        orb.shutdown(false);
        orb.destroy();

        System.out.println("================================================");
    }
}

// End of file.
