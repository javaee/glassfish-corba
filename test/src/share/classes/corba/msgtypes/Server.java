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

package corba.msgtypes;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.protocol.ForwardException;
import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;
import com.sun.corba.ee.impl.ior.IORImpl;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.ObjectKey;
import java.io.FileOutputStream;

import java.util.Properties;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableServer.POA;

public class Server extends LocalObject implements ORBInitializer {

    private static InterceptorImpl interceptor = new InterceptorImpl();

    // ORBInitializer interface implementation.

    public void pre_init(ORBInitInfo info) {}

    public void post_init(ORBInitInfo info) {
        // register the interceptors.
        try {
            info.add_server_request_interceptor(Server.interceptor);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new INTERNAL();
        }
        System.out.println("ORBInitializer.post_init completed");
    }

    // nested class definitions

    private static class ServerRequestHandler implements BadServerIdHandler {

        private org.omg.CORBA.ORB orb = null;
        private String iorString = null;

        ServerRequestHandler(org.omg.CORBA.ORB orb, String iorString) {
            this.orb = orb;
            this.iorString = iorString;
        }

        /**
         * This implements the BadServerIdHandler interface. This will be called
         * by the ORB if the process is setup to be a ORBD or when the ORB gets
         * a locate request.
         */
        public void handle(ObjectKey objectKey) {

            System.out.println("ServerRequestHandler received a request");

            // create an IOR to return back
            IOR ior = new IORImpl((com.sun.corba.ee.spi.orb.ORB) orb);
            throw new ForwardException(
                (com.sun.corba.ee.spi.orb.ORB)orb, ior);
        }
    }

    static class InterceptorImpl extends org.omg.CORBA.LocalObject
            implements ServerRequestInterceptor {
        private static String name = "ServerInterceptor";
        private boolean balanced = true;

        public InterceptorImpl() {}

            // implementation of the Interceptor interface.

        public String name() {
            return InterceptorImpl.name;
        }

        public void destroy() {}

            // implementation of the ServerInterceptor interface.

        public void receive_request_service_contexts(ServerRequestInfo ri)
               throws ForwardRequest {
            if (ri.operation().equals("verifyTransmission")) {
                this.balanced = false;
            }
            System.out.println("receive_request_service_contexts called : " + ri.operation());
        }

        public void receive_request(ServerRequestInfo ri)
                throws ForwardRequest {
            if (ri.operation().equals("verifyTransmission")) {
                this.balanced = false;
            }
            System.out.println("receive_request called : " + ri.operation());
        }

        public void send_reply(ServerRequestInfo ri) {
            if (ri.operation().equals("verifyTransmission")) {
                this.balanced = true;
            }
            System.out.println("send_reply called : " + ri.operation());
        }

        public void send_exception(ServerRequestInfo ri) throws ForwardRequest {
            if (ri.operation().equals("verifyTransmission")) {
                this.balanced = true;
            }
            System.out.println("send_exception called : " + ri.operation());
        }

        public void send_other(ServerRequestInfo ri) throws ForwardRequest {
            if (ri.operation().equals("verifyTransmission")) {
                this.balanced = true;
            }
            System.out.println("send_other called : " + ri.operation());
        }

        public boolean isBalanced() {
            return this.balanced;
        }
    }

    // static methods

    public static void writeObjref(org.omg.CORBA.Object ref, String file, org.omg.CORBA.ORB orb) {
        String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
        try {
            java.io.DataOutputStream out = new
                java.io.DataOutputStream(new FileOutputStream(fil));
            out.writeBytes(orb.object_to_string(ref));
        } catch (java.io.IOException e) {
            System.err.println("Unable to open file "+fil);
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        try {
            Properties props = new Properties( System.getProperties() ) ;
            String className = ServerRequestHandler.class.getName() ;
            props.setProperty( 
                ORBConstants.BAD_SERVER_ID_HANDLER_CLASS_PROPERTY,
                className ) ;
            
            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.ee.spi.orb.ORB ourORB
                = (com.sun.corba.ee.spi.orb.ORB)orb;

            System.out.println("==== Server GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
                                   ourORB.getORBData().getGIOPVersion())
                               + "====");

            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            FragmentTesterImpl impl = new FragmentTesterImpl(Server.interceptor);
            javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie( impl ) ;

            byte[] id = rootPOA.activate_object(
                (org.omg.PortableServer.Servant)tie ) ;
                                                 
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id ) ;

            writeObjref(obj, "IOR", orb);

            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);

            // Make sure to exit with a value greater than 0 on
            // error.
            System.exit(1);
        }
    }
}
