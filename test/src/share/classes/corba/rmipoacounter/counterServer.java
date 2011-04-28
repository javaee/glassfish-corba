/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package corba.rmipoacounter;

import javax.rmi.PortableRemoteObject ;
import java.io.*;
import java.io.DataOutputStream ;
import java.util.*;
import java.rmi.RemoteException ;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import com.sun.corba.se.spi.misc.ORBConstants ;

public class counterServer {
    public static boolean debug = true;
    public static String Counter1Id = "abcdef";
    public static String Counter2Id = "qwerty";

    // Temporary hack to get this test to work and keep the output
    // directory clean
    private static final String outputDirOffset 
        = "/corba/rmipoacounter/".replace('/', File.separatorChar);

    public static void main(String args[])
    {
        try{
	    // set debug flag
	    if ( args.length > 0 && args[0].equals("-debug") )
		debug = true;

	    if (debug) {
		System.out.println("ENTER: counterServer");
		System.out.flush();
	    }

            // create and initialize the ORB
            Properties p = new Properties();
            p.put("org.omg.CORBA.ORBClass", 
                  System.getProperty("org.omg.CORBA.ORBClass"));
            p.put( ORBConstants.ORB_SERVER_ID_PROPERTY, "9999");
            ORB orb = ORB.init(args, p);

	    if (debug) {
		System.out.println("counterServer: ORB initialized");
		System.out.flush();
	    }

            // get rootPOA, set the AdapterActivator, and activate RootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_activator(new MyAdapterActivator(orb));
            rootPOA.the_POAManager().activate();

	    if (debug) {
		System.out.println("counterServer: RootPOA activator set");
		System.out.flush();
	    }

            if ( isFirstTime() ) {
		if (debug) {
		    System.out.println("counterServer: Is first time");
		    System.out.flush();
		}
	        POA poa = createPersistentPOA(orb, rootPOA);
                createCounter1(orb, poa);
		poa = createNonRetainPOA(orb, rootPOA);
                createCounter2(orb, poa);
		if (debug) {
		    System.out.println("counterServer: refs created");
		    System.out.flush();
		}
            }

            // wait for invocations from clients
            System.out.println("Server is ready.");
	    System.out.flush();

            orb.run();

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        } finally {
	    if (debug) {
		System.out.println("EXIT: counterServer");
		System.out.flush();
	    }
	}
    }

        
    private static boolean isFirstTime()
	throws Exception
    {
        // Check if the counterValue file is there 
        String name = System.getProperty("output.dir")
            + outputDirOffset
            + "counterValue";
        File file = new File(name);
        return ( !file.exists() );
    }

    static POA createPersistentPOA(ORB orb, POA rootPOA)
	throws Exception
    {
        // create a persistent POA
        Policy[] tpolicy = new Policy[2];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);
 
        // register the ServantActivator with the POA, then activate POA
        CounterServantActivator csa = new CounterServantActivator(orb);
        tpoa.set_servant_manager(csa);
        tpoa.the_POAManager().activate();
	return tpoa;
    }

    static Servant makeCounterServant( ORB orb ) 
    {
	counterImpl impl = null ;

	try {
	    impl = new counterImpl(orb, counterServer.debug);
	} catch (RemoteException exc) {
	    // ignore
	}

	Servant servant = (Servant)(javax.rmi.CORBA.Util.getTie( impl ) ) ;

	return servant ;
    }

    static void createCounter1(ORB orb, POA tpoa)
	throws Exception
    {
        // create an objref using persistent POA
        byte[] id = Counter1Id.getBytes();
        String intf = new _counterImpl_Tie()._all_interfaces(tpoa,id)[0];

        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, 
                                                                 intf);

        Class intfr = Class.forName("corba.rmipoacounter.counterIF");

        counterIF counterRef 
            = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

        // put objref in NameService
        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent("Counter1", "");
        NameComponent path[] = {nc};

        ncRef.rebind(path, obj);

        // invoke on the local objref to test local invocations
        if ( counterServer.debug ) 
	    System.out.println("\nTesting local invocation: Client thread is "+Thread.currentThread());
        long value = counterRef.increment(1);
        if ( counterServer.debug ) 
	    System.out.println(value);
    }

    static POA createNonRetainPOA(ORB orb, POA rootPOA)
	throws Exception
    {
        // create another persistent, non-retaining POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
        tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
        tpolicy[2] = rootPOA.create_servant_retention_policy(ServantRetentionPolicyValue.NON_RETAIN);
        POA tpoa = rootPOA.create_POA("NonRetainPOA", null, tpolicy);
        
        // register the ServantLocator with the POA, then activate POA
        CounterServantLocator csl = new CounterServantLocator(orb);
        tpoa.set_servant_manager(csl);
        tpoa.the_POAManager().activate();
	return tpoa;
    }

    static void createCounter2(ORB orb, POA tpoa)
	throws Exception
    {
        // create a servant and get an objref using persistent POA
        byte[] id = Counter2Id.getBytes();
        org.omg.CORBA.Object obj = tpoa.create_reference_with_id(id, 
								 new _counterImpl_Tie()._all_interfaces(tpoa,id)[0]);
        counterIF counterRef = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

	/********8
		 // put objref in NameService
		 org.omg.CORBA.Object objRef =
		 orb.resolve_initial_references("NameService");
		 NamingContext ncRef = NamingContextHelper.narrow(objRef);
		 NameComponent nc = new NameComponent("Counter2", "");
		 NameComponent path[] = {nc};
		 ncRef.rebind(path, counterRef);
	**********/
        OutputStream f = new FileOutputStream(
					      System.getProperty("output.dir") 
                                              + outputDirOffset
					      + "counterior2") ;
        DataOutputStream out = new DataOutputStream(f) ;
        String ior = orb.object_to_string(obj) ;
        out.writeBytes(ior) ;
        out.close();

    }
}


class MyAdapterActivator extends org.omg.CORBA.LocalObject implements AdapterActivator
{
    private ORB orb;

    MyAdapterActivator(ORB orb)
    {
	this.orb = orb;
    }

    public boolean unknown_adapter(POA parent, String name)
    {
	if ( counterServer.debug ) 
	    System.out.println("\nIn MyAdapterActivator.unknown_adapter, parent = "+parent.the_name()+" child = "+name);
	try {
	    if ( name.equals("PersistentPOA") )
	        counterServer.createPersistentPOA(orb, parent);
	    else if ( name.equals("NonRetainPOA") )
	        counterServer.createNonRetainPOA(orb, parent);
	    else 
	        return false;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    return false;
	}

        return true;
    }
}



class CounterServantActivator extends org.omg.CORBA.LocalObject implements ServantActivator
{
    ORB orb;

    CounterServantActivator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant incarnate(byte[] oid, POA adapter)
        throws org.omg.PortableServer.ForwardRequest
    {
	Servant servant = counterServer.makeCounterServant( orb ) ;

        if ( counterServer.debug ) 
	    System.out.println("\nIn CounterServantActivator.incarnate,   oid = "
			       +oid
			       +" poa = "+adapter.the_name()
			       +" servant = "+servant);
        return servant;
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant, 
			    boolean cleanup_in_progress, boolean remaining_activations)
    {
        if ( counterServer.debug ) 
            System.out.println("\nIn CounterServantActivator.etherealize, oid = "
                               +oid
                               +" poa = "+adapter.the_name()
                               +" servant = "+servant
                               +" cleanup_in_progress = "+cleanup_in_progress
                               +" remaining_activations = "+remaining_activations);
        return;
    }
}

class CounterServantLocator extends org.omg.CORBA.LocalObject implements ServantLocator
{
    ORB orb;

    CounterServantLocator(ORB orb)
    {
        this.orb = orb;
    }

    public Servant preinvoke(byte[] oid, POA adapter, String operation, 
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
	String sid = new String(oid);
        String newidStr = "somethingdifferent";

        // Tests location forwards
	if ( sid.equals(counterServer.Counter2Id) ) { 
	    // construct a new objref to forward to.
            byte[] id = newidStr.getBytes();
            org.omg.CORBA.Object obj = null;
	    try {
                obj = adapter.create_reference_with_id(id, 
						       new _counterImpl_Tie()._all_interfaces(adapter,oid)[0]);
	    } catch ( Exception ex ) {}
            counterIF counterRef = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class );

	    System.out.println("\nCounterServantLocator.preinvoke forwarding ! "
			       +"old oid ="+new String(oid)
			       +"new id ="+new String(id));

	    ForwardRequest fr = new ForwardRequest(obj);
	    throw fr;
	}

	String oidStr = new String(oid);
	if ( !newidStr.equals(oidStr) )
	    System.err.println("\tERROR !!!: preinvoke got wrong id:"+oidStr);

        MyCookie cookie = new MyCookie();
	Servant servant = counterServer.makeCounterServant( orb ) ;

        if ( counterServer.debug ) 
	    System.out.println("\nIn CounterServantLocator.preinvoke,  oid = "
			       +oidStr
			       +" poa = "+adapter.the_name()
			       +" operation = " +operation
			       +" cookie = "+cookie+" servant = "+servant);

        the_cookie.value = cookie;
        return servant;
    }

    public void postinvoke(byte[] oid, POA adapter, String operation, 
                           java.lang.Object cookie, Servant servant)
    {
        if ( counterServer.debug ) 
            System.out.println("\nIn CounterServantLocator.postinvoke, oid = "
                               +new String(oid)
                               +" poa = "+adapter.the_name()
                               +" operation = " +operation
                               +" cookie = "+cookie+" servant = "+servant);
        return;
    }
}

class MyCookie 
{}
