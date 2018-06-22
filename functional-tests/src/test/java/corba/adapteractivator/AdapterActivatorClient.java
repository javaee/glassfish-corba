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

package corba.adapteractivator ;

import org.omg.CORBA.ORB;

import corba.framework.statusU;
import corba.framework.RTMConstants;
import corba.framework.GetID;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

/**
* Purpose                       : To verify that AdapterActivator implementation registered, recreates
*                                         the POA when ORB receives a request for an object reference that
*                                         identifies the target POA does not exist
*
* Expected Result       : Client gets a valid response
*
* Purpose                       : To verify that client gets a OBJECT_NOT_EXIST exception when
*                                         ORB receives a request for an object reference that identifies
*                                         the target POA does not exist and the AdapterActivator
*                                         implementation registered with rootPoa does not create the POA
*
* Expected Result       : Client gets OBJECT_NOT_EXIST exception
*
*/

public class AdapterActivatorClient {

    private Hello helloRef1,helloRef2;
    private Close closeRef;
    private NamingContext rootContext;
    private ORB orb;
    private statusU status = new statusU();
    private JUnitReportHelper helper = new JUnitReportHelper( AdapterActivatorClient.class.getName() ) ;

    final String testDesc = "To verify that the AdapterActivator registers and"+
             "\n             recreates the POA when the ORB received a request"+
             "\n             for an object reference which identifies the" +
             "\n             target POA that doesn't exist";

    public AdapterActivatorClient(String[] args) {

        try {
            orb = ORB.init(args,System.getProperties());

            // Get root Naming Context
            org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
            rootContext = NamingContextHelper.narrow(obj);
            adapterActivatorTest1();
            adapterActivatorTest2();
            System.out.println("\nAdapterActivatorClient : Calling shutdown()");

            //Resolve Object Reference for CloseServer
            NameComponent nc = new NameComponent("CloseServer","");
            NameComponent name[] =      {nc};
            closeRef = CloseHelper.narrow(rootContext.resolve(name));
            closeRef.shutdown();
        } catch(Exception Ex) {
            status.addStatus(GetID.generateID(this, ""), RTMConstants.FAIL,
                             "Got exception: "+ Ex.toString());
        } finally {
            helper.done() ;
            status.printSummary(GetID.generateID(this, ""), testDesc);
            if (status.totalFail() > 0)
                System.exit(1) ;
        }
    }

    void adapterActivatorTest1() {

        String subTestName = "Test01";
        helper.start( subTestName ) ;
        try {

            // Resolve Object Reference
            NameComponent nc = new NameComponent("HelloServer1","");
            NameComponent name[] =      {nc};
            helloRef1 = HelloHelper.narrow(rootContext.resolve(name));

            System.out.println("\nAdapterActivator-1 Started\n");
            System.out.println("Calling Operation on HelloServant of Poa1. Poa1 is "+
                               "child of RootPoa, RootPoa is registered with AdapterActivator"+
                               " implementation which creates Poa1 otherwise returns false");
            System.out.println(helloRef1.sayHello());
            status.addStatus(subTestName, RTMConstants.PASS, "Calling "+
                             "operation on HelloServant of Poa1 ok");
            helper.pass() ;
        } catch(Exception ex) {
            helper.fail( ex ) ;
            ex.printStackTrace();
            status.addStatus(subTestName, RTMConstants.FAIL,
                             "Got exception: "+ ex.toString());
        }
    }

    void adapterActivatorTest2() {

        String subTestName = "Test02";
        helper.start( subTestName ) ;
        try {

            //Resolve Object Reference
            NameComponent nc = new NameComponent("HelloServer2","");
            NameComponent name[] =      {nc};
            helloRef2 = HelloHelper.narrow(rootContext.resolve(name));

            System.out.println("\nAdapterActivator-2 Started\n");
            System.out.println("Calling Operation on HelloServant of Poa2. Poa2 is "+
                               "child of RootPoa, RootPoa is registered with AdapterActivator "+
                               "implementation which creates Poa1 otherwise returns false");
            System.out.println(helloRef2.sayHello());
            helper.fail( "Unexpected success: should see OBJECT_NOT_EXIST exception" ) ;
        } catch (org.omg.CORBA.OBJECT_NOT_EXIST ex) {
            status.addStatus(subTestName, RTMConstants.PASS, "Operation on "+
                             "HelloServant threw the expected OBJECT_NOT_EXIST"+
                             " exception");
            helper.pass() ;
        } catch(Exception ex) {
            ex.printStackTrace();
            status.addStatus(subTestName, RTMConstants.FAIL,
                             "Got exception: "+ ex.toString());
            helper.fail( ex ) ;
        }
    }

    public static void main( String [] args ) {
        AdapterActivatorClient client = new AdapterActivatorClient(args);
    }
}
