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

package corba.cdrext;

import org.omg.CORBA.*;
import org.omg.CORBA.portable.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.*;

import java.rmi.*;
import javax.rmi.PortableRemoteObject;
import javax.naming.*;
import java.io.*;
import java.util.*;

public class Server extends PortableRemoteObject implements Tester
{
    public Server() throws java.rmi.RemoteException {}

    public MarshalTester verify(byte[] predata, 
                                MarshalTester input, 
                                byte[] postdata)
        throws DataCorruptedException
    {
        if (predata == null) {
            System.out.println("predata is null");
            throw new DataCorruptedException("predata is null");
        }
        if (postdata == null) {
            System.out.println("postdata is null");
            throw new DataCorruptedException("postdata is null");
        }
        if (!Arrays.equals(predata, postdata)) {
            System.out.println("byte arrays not equal");
            throw new DataCorruptedException("Byte arrays not equal");
        }

        return input;
    }

    public java.lang.Object verify(java.lang.Object obj) {
        return obj;
    }

    public Map verify(Map map) {
        return map;
    }

    public List verify(List list) {
        return list;
    }

    public java.sql.Date verify(java.sql.Date date) {
        return date;
    }

    public Properties verify(Properties props) {
        return props;
    }

    public Hashtable verify(Hashtable table) {
        return table;
    }

    public void throwCheckedException() throws CheckedException {
        throw new CheckedException("CheckedException");
    }

    public void throwRuntimeException() {
        throw new UncheckedException("Runtime Exception");
    }

    public void throwRemoteException() throws RemoteException {
        throw new RemoteException("This is a remote exception");
    }

    public AbsTester getAbsTester() {
        return this;
    }

    public void ping() {}

    public static void main(String[] args) {
        try {

            ORB orb = ORB.init(args, System.getProperties());
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            Server impl = new Server();
            javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie( impl ) ; 

            byte[] id = rootPOA.activate_object( 
                                                 (org.omg.PortableServer.Servant)tie ) ;
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id ) ;

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Tester", "");
            NameComponent path[] = {nc};
            
            ncRef.rebind(path, obj);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();

//             Context rootContext = new InitialContext();
//             Server p = new Server();
//             rootContext.rebind("Tester", p);
//             System.out.println("Server is ready.");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

}

