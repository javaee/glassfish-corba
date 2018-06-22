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
// Created       : by Everett Anderson.
// Last Modified : 2004 Apr 14 (Wed) 19:25:53 by Harold Carr.
//

package corba.connectintercept_1_4;

import java.util.Properties;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client
{
    public static final String baseMsg = Client.class.getName();
    
    public static final String defaultFactoryClassName =
        //REVISIT Common.DEFAULT_FACTORY_CLASS
        "com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory";

    public static void main(String args[])
    {
        try {
            Properties props = new Properties();

            props.setProperty(Common.ORBClassKey, MyPIORB.class.getName());

            props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                              ClientORBInitializer.class.getName(),
                              "dummy");

            //
            // Case 1.
            //

            System.out.println();
            System.out.println("Case 1:  Default factory");
            System.out.println();

            testFactory(args, 
                        props,
                        defaultFactoryClassName
                        );


            //
            // Case 2.
            //

            System.out.println();
            System.out.println("Case 2:  Custom factory");
            System.out.println();

            props.put(ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY,
                      Common.CUSTOM_FACTORY_CLASS);

            testFactory(args, 
                        props,
                        Common.CUSTOM_FACTORY_CLASS);

            // 
            // Success.
            //

            System.out.println();
            System.out.println(baseMsg + ".main: Test PASSED.");

        } catch (Exception e) {
            System.out.println(baseMsg + ".main: Test FAILED: " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public static void testFactory(String args[], 
                                   Properties props,
                                   String factoryName)
        throws Exception
    {
        ORB orb = ORB.init(args, props);

        Common.upDownReset();

        resolveAndInvoke(orb, Common.serverName1);

        // Invoke on another object in same server to observe
        // already connected behavior.
        resolveAndInvoke(orb, Common.serverName2);


        // Make sure that the factory that was used matches the name given.
        ORBSocketFactory socketFactory = 
           ((com.sun.corba.ee.spi.orb.ORB)orb).getORBData().getLegacySocketFactory();
        if (socketFactory == null) {
            if (factoryName.equals(defaultFactoryClassName)) {
                // OK - default does not use socket factory any longer.
                ;
            } else {
                // Not the default - so expect a socket factory.
                throw new Exception(baseMsg + "unexpected null socketFactory");
            }
        } else {
            String orbSocketFactoryName = socketFactory.getClass().getName();
            if (! factoryName.equals(orbSocketFactoryName)) {
                throw new Exception(baseMsg + ".testFactory: "
                                    + "Wrong socket factory class: "
                                    + orbSocketFactoryName
                                    + " should be "
                                    + factoryName);
            }
        }
        orb.shutdown(false);
        orb.destroy();
    }

    public static void resolveAndInvoke (ORB orb, String name)
        throws
            Exception
    {
        ExI exIRef;

        System.out.println();
        System.out.println("BEGIN: invoke on " + name);

        exIRef = ExIHelper.narrow(resolve("First", name, orb));

        // The second resolve is to observe caching behavior.

        exIRef = ExIHelper.narrow(resolve("Second", name, orb));

        // The multiple invokes are to observe using various
        // endpoints in the component data (and to observe caching behavior).

        invoke("First", exIRef);
        invoke("Second", exIRef);
        invoke("Third", exIRef);
        invoke("Fourth", exIRef);
        invoke("Fifth", exIRef);

        System.out.println("END: invoke on " + name);
    }

    public static org.omg.CORBA.Object resolve(String msg,
                                               String name, 
                                               ORB orb)
        throws Exception
    {
        // List initial references.

        System.out.println();
        System.out.println("BEGIN: " + msg + " list_initial_references.");

        String services[] = orb.list_initial_services();
        for (int i = 0; i < services.length; i++) {
            System.out.print(" " + services[i]);
        }
        System.out.println();

        System.out.println("END: " + msg + " list_initial_references.");


        // Resolve.

        System.out.println();
        System.out.println("BEGIN: " + msg + " resolve.");

        org.omg.CORBA.Object ref
            = ExIHelper.narrow(Common.getNameService(orb)
                               .resolve(Common.makeNameComponent(name)));

        System.out.println("END: " + msg + " resolve.");
        return ref;
    }

    public static void invoke(String msg, ExI exIRef)
    {
        System.out.println();
        System.out.println("BEGIN: " + msg + " invocation.");

        exIRef.sayHello();

        System.out.println("END: " + msg + " invocation.");
    }
}

// End of file.
