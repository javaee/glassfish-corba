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

package corba.invocation;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.SystemException;
import java.util.*;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client implements Runnable {

    private String[] args;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public Client(String[] args) {
        this.args = args;
    }

    public static void main(String args[]) {
        new Client(args).run();
    }

    public void run() {

        try {

            Properties props = new Properties() ;
            //props.put("com.sun.corba.ee.ORBDebug", "transport,subcontract");
            props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, "250:1000:100");
            ORB orb = ORB.init(args, props);

            String corbalocURL =
                System.getProperty(InvocationTest.URL_PROPERTY);

            Object obj = orb.string_to_object(corbalocURL);

            if (obj == null) {
                throw new RuntimeException("string_to_object(" +
                                           corbalocURL + ")");
            }

            try {
                Hello helloRef = HelloHelper.narrow( obj );

                String msg = "FAILURE: call incorrectly succeeded";
                System.out.println("------------------------------------");
                System.out.println(msg);
                System.out.println("------------------------------------");
                throw new Exception(msg);

            } catch (org.omg.CORBA.COMM_FAILURE e) {
                SystemException connectException =
                    wrapper.connectFailure( new RuntimeException(),
                        "foo", "bar", "baz");
                if (e.getClass().isInstance(connectException)
                    && e.minor == connectException.minor
                    && e.completed == connectException.completed)
                {
                    System.out.println("------------------------------------");
                    System.out.println("SUCCESS");
                    System.out.println("------------------------------------");
                } else {
                    System.out.println("------------------------------------");
                    System.out.println("FAILURE");
                    System.out.println("------------------------------------");
                    e.printStackTrace(System.out);
                    RuntimeException rte = 
                        new RuntimeException("Incorrect exception");
                    rte.initCause(e);
                    throw rte;
                }
            }

        } catch (Exception e) {
             e.printStackTrace(System.err);
             System.exit(1);
        }
    }
}



