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
// Created       : 2005 Oct 05 (Wed) 14:43:22 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:59:21 by Harold Carr.
//

package corba.lb;

import java.util.Hashtable;
import java.util.Properties;
import java.rmi.RemoteException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @Author Ken Cavanaugh
 * @author Harold Carr
 */
public class Client
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static int NUM_ITERATIONS = 1000 ;

    private static int errorCount = 0 ;

    private static InitialContext ic ;

    public static void main(String[] av)
    {
        try {
            Properties props = new Properties() ;
            // props.setProperty("com.sun.corba.ee.ORBDebug","subcontract,transport");
            props.setProperty(ORBConstants.ORB_SERVER_ID_PROPERTY, "100" ) ;
            ORB orb = ORB.init((String[])null, props);

            // See if this reproduces the AmEx problem
            ((com.sun.corba.ee.impl.orb.ORBImpl)orb).getFVDCodeBaseIOR() ;

            Hashtable env = new Hashtable() ;
            env.put( "java.naming.corba.orb", orb ) ;
            ic = new InitialContext(env);

            System.out.println( "Getting test reference" ) ;
            Test ref  = (Test)lookupAndNarrow(Common.ReferenceName, 
                Test.class, ic);

            for (int ctr=0; ctr<NUM_ITERATIONS; ctr++) {
                System.out.print( "Calling echo with argument, " + ctr ) ;

                int result = 0 ;
                try {
                    try {
                        Thread.sleep( 4 ) ;
                    } catch (InterruptedException exc) {
                        System.out.println( "" + exc ) ;
                    }

                    result = ref.echo( ctr ) ;
                    if (result != ctr) {
                        throw new Exception( "Result does not match argument" ) ;
                    } else {
                        System.out.println(", succesfully returned, " + result);
                    }
                } catch (SystemException exc) {
                    System.out.println( "ERROR: " + exc ) ;
                    errorCount++ ;
                    exc.printStackTrace(System.out);
                } catch (RemoteException exc) {
                    System.out.println( "ERROR: " + exc ) ;
                    errorCount++ ;
                    exc.printStackTrace(System.out);
                }
            }

            System.out.println("Loop completed.");
            System.out.println();

            System.out.println("--------------------------------------------");

            if (errorCount == 1) {
                System.out.println("Client failed (" + errorCount + 
                                   ") time(s) due to server restart");
                errorCount = 0 ;
            }

            System.out.println("Client " + ((errorCount==0) ? "SUCCESS" : "FAILURE") );
            System.out.println("--------------------------------------------");
            System.exit(errorCount);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("--------------------------------------------");
            System.out.println("Client FAILURE");
            System.out.println("--------------------------------------------");
            System.exit(1);
        }
    }

    public static Object lookupAndNarrow(String name, 
                                         Class clazz,
                                         InitialContext ic )
        throws Exception
    {
        System.out.println( "Looking up " + name ) ;
        Object obj = ic.lookup( name) ;
        System.out.println( "Narrowing object" ) ;
        return PortableRemoteObject.narrow(obj, clazz);
    }
}

// End of file.

