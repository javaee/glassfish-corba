/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
/* @(#)PROTest.java	1.14 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.server.ExportException;
import test.ServantContext;
import test.RemoteTest;
import com.sun.corba.se.impl.util.Utility;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Properties ;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;
import javax.naming.Context;
import javax.naming.InitialContext;
import com.sun.org.omg.SendingContext.CodeBase;
import alpha.bravo.A;
import alpha.bravo.B;
import alpha.bravo.DuckBill;
import alpha.bravo.Multi;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;
import com.sun.corba.se.spi.presentation.rmi.PresentationManager ;

/*
 * @test
 */
public class PROTest extends RemoteTest {

    private static final String servantName     = "PROServer";
    private static final String servantClass    = "javax.rmi.PROImpl";
    private static final String[] compileEm     =   {
	"javax.rmi.PROImpl",
	"javax.rmi.PROImpl2",
	"javax.rmi.DogServer",
	"javax.rmi.ServantInner",
	"javax.rmi.ServantOuter.Inner",
	"rmic.OnlyRemoteServant",
	"javax.rmi.HashCodeImpl",
	"javax.rmi.HashCodeAImpl",
	"alpha.bravo.Multi",
    };

    private static final int TIMING_ITERATIONS = 100;

    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-always", "-keep"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable 
    {
	dprint( "test starts" ) ;
	boolean usesDynamicStubs = 
	    com.sun.corba.se.spi.orb.ORB.getPresentationManager().
		useDynamicStubs() ;
	
	// Certain tests that depend on the absence of iiop stubs and ties
	// cannot function correctly with dynamic RMI-IIOP, since 
	// dynamic RMI-IIOP can always create any needed stub or tie.
	// We assume that JRMP is only usable when we are not using
	// dynamic RMI-IIOP.  Since we test the JRMP case in static mode
	// anyway, we'll just return here in the dynamic case for JRMP.
	if (usesDynamicStubs && !iiop)
	    return ;

        // First ensure that the caches are cleared out so
        // that we can switch between IIOP and JRMP...
        
        Utility.clearCaches();
        
        // Check toStub(). First try an unconnected servant...

        PROImpl localImpl = new PROImpl();
        Remote stub = PortableRemoteObject.toStub(localImpl);
        boolean fail = false;
        
	dprint( "test.1" ) ;

        // Make sure it does not have a delegate set...
        /* This will suceed with dynamic stubs, due to the use
	   of the POA for implicit activation.
        if (iiop) {
            try {
		StubAdapter.getDelegate( stub ) ;
            } catch (Exception e) {
                fail = true;
            }
            if (!fail) {
                throw new Exception ("toStub() on unconnected servant succeeded.");
            }
        }
	*/

        // Now connect it up and try again...
        
        ORB defaultORB = context.getORB();
        
        if (iiop) {
            Tie tie = Util.getTie(localImpl);
            tie.orb(defaultORB);

	    dprint( "test.3: ORB.init returned " + defaultORB ) ;
        }
        
        stub = PortableRemoteObject.toStub(localImpl);
        if (stub == null) {
            throw new Exception ("toStub() on connected servant failed.");
        }

	dprint( "test.4" ) ;

        // Make sure second export fails...

        boolean exportFail = false;
        try {
            PortableRemoteObject.exportObject(localImpl);
        } catch (ExportException e) {
            exportFail = true;
        }

	dprint( "test.5" ) ;

        if (!exportFail) {
            throw new Exception ("exportObject twice did not fail");
        }

        // Try narrow on local stub...

        PROHello stubref = (PROHello) PortableRemoteObject.narrow(stub,PROHello.class);

	dprint( "test.6" ) ;
        if (stubref == null) {
            throw new Exception ("narrow() failed for stub");
        }

        // Start up our servant. This exercises exportObject()...

	Remote remote = context.startServant(servantClass,servantName,true,iiop);

	dprint( "test.7" ) ;
        if (remote == null) {
            throw new Exception ("startServant() failed");
        }

        // Try narrow...

        PROHello objref = (PROHello) PortableRemoteObject.narrow(remote,PROHello.class);

	dprint( "test.8" ) ;
        if (objref == null) {
            throw new Exception ("narrow() failed for remote");
        }

	// Make sure we can invoke our remote object...

	if (!objref.sayHello().equals(PROHello.HELLO)) {
	    throw new Exception("sayHello() failed");
	}

        // Test abstract types

        String bark;

        Dog dogValue = objref.getDogValue ();
        if ( dogValue == null ) {
	    throw new Exception ("sayHello() dogValue is null");
        }
        bark = dogValue.bark ();

        Dog dogServer = objref.getDogServer ();
        if ( dogValue == null ) {
	    throw new Exception ("sayHello() dogServer is null");
        }
        bark = dogServer.bark ();

        // Make sure we cannot unexport the stub...
        
	dprint( "test.9" ) ;
        boolean unexportFail = false;
        try {
            PortableRemoteObject.unexportObject(objref);
        } catch (NoSuchObjectException e) {
            unexportFail = true;
        }
        
	dprint( "test.10" ) ;
        if (!unexportFail) {
            throw new Exception("unexport of stub succeeded!");
        }
    

        // Now get a round-trip timing...
	dprint( "test.11" ) ;

        if (verbose) {
            int count = TIMING_ITERATIONS;
            long startTime = System.currentTimeMillis();
            while (count-- > 0) {
                String hello = objref.sayHello();
            }
            long roundTrip = (System.currentTimeMillis() - startTime) / TIMING_ITERATIONS;
            System.out.print("Round-trip time for sayHello() = "
			     + roundTrip + " ms average over " + TIMING_ITERATIONS + " iterations.");
        }

        // Now check stub streaming and connect(stub,stub)...
	dprint( "test.12" ) ;
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(bos);
        os.writeObject(objref);
        
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream is = new ObjectInputStream(bis);
        PROHello newRef = (PROHello) is.readObject();

	dprint( "test.13" ) ;
        if (newRef == null) {
            throw new Exception("Stub streaming failed.");   
        }

        // Make sure we cannot call methods on it...
        
        fail = false;
        try {
            newRef.sayHello();
        } catch (RemoteException e) {
            if (e.getMessage().startsWith("CORBA BAD_OPERATION 0")) {
                fail = true;
            }
        }
        if (iiop && !fail) {
            throw new Exception("sayHello on unconnected stub succeeded.");   
        }
        
        // Now connect it up (stub,stub) and make sure we can call
        // methods on it...
	dprint( "test.14" ) ;
        
        PortableRemoteObject.connect(newRef,objref);
	if (!newRef.sayHello().equals(PROHello.HELLO)) {
	    throw new Exception("connect(stub,stub) failed");
	}
        
        // Now make sure that they are equal...
        
        if (newRef.hashCode() != objref.hashCode()) {
	    throw new Exception("newRef.hashCode() != objref.hashCode()");
        }
        
        if (!newRef.equals(objref)) {
	    throw new Exception("newRef != objref");
        }

	dprint( "test.15" ) ;
        // Try connect(impl,impl)...

	PROImpl localImpl2 = new PROImpl();
	PortableRemoteObject.connect(localImpl2,localImpl);
	try {
	    PortableRemoteObject.toStub(localImpl2);
	} catch (NoSuchObjectException e) {
	    throw new Exception ("connect(impl,impl) failed");
	}

	// Try connect(stub,impl)...

	dprint( "test.16" ) ;
	ObjectInputStream is2 = new ObjectInputStream(new ByteArrayInputStream(
	    bos.toByteArray()));
	PROHello newRef2 = (PROHello) is2.readObject();
	PortableRemoteObject.connect(newRef2,localImpl2);
	if (!newRef.sayHello().equals(PROHello.HELLO)) {
	    throw new Exception("connect(stub,impl) failed");
	}

	dprint( "test.17" ) ;
	dprint( "test.18" ) ;
        // Try connect(impl,stub)...
	dprint( "test.19" ) ;
        
	PROImpl localImpl3 = new PROImpl();
	PortableRemoteObject.connect(localImpl3,objref);
	try {
	    PortableRemoteObject.toStub(localImpl3);
	} catch (NoSuchObjectException e) {
	    throw new Exception ("connect(impl,stub) failed");
	}

	// Make sure that trying to connect an already connected object
	// succeeds when the ORBs are the same...
	
	PortableRemoteObject.connect(objref,localImpl3);
	PortableRemoteObject.connect(localImpl3,objref);

        // Make sure that trying to connect an already connected object fails
        // when the ORBs are different...
        
        PROImpl newLocalImpl = new PROImpl();
        if (iiop) {
            ORB newORB = ORB.init(new String[]{},null);
            Tie newTie = Util.getTie(newLocalImpl);
	    newTie.orb(newORB);
	}
        PROHello newObjRef = (PROHello) PortableRemoteObject.toStub(newLocalImpl);
        
        boolean callFailed = false;
        try {
            PortableRemoteObject.connect(objref,newLocalImpl);
        } catch (RemoteException e) {
            callFailed = true;   
        }
        if (!callFailed) {
            if (iiop) {
                throw new Exception ("Second connect(stub,impl) succeeded");
            } else {                
                // System.out.println("REMINDER: document connect and JRMP!");                
            }
        }

	callFailed = false;
	try {
	    PortableRemoteObject.connect(localImpl3,newObjRef);
	} catch (RemoteException e) {
	    callFailed = true;   
	}
	if (!callFailed) {
	    if (iiop) {
		throw new Exception ("Second connect(impl,stub) succeeded");
	    } else {                
		// System.out.println("REMINDER: document connect and JRMP!");                
	    }
	}

        // Now export an implementation, get a stub for it, make sure they
        // are both unconnected, then connect the stub and make sure they
        // are *both* connected...

        if (iiop) {
            
            PROImpl theImpl = new PROImpl();
            Tie theTie = Util.getTie(theImpl);
            org.omg.CORBA.Object theStub = 
		(org.omg.CORBA.Object)PortableRemoteObject.toStub(theImpl);
            callFailed = false;
            try {
                theTie.orb();
            } catch (SystemException e) {
                callFailed = true;
            }
            if (!callFailed) {
                throw new Exception ("theTie already connected");
            }
            callFailed = false;
            try {
                StubAdapter.getDelegate( theStub );
            } catch (SystemException e) {
                callFailed = true;
            }
            if (!callFailed) {
                throw new Exception ("theStub already connected");
            }

            StubAdapter.connect( theStub, defaultORB ); // Connect both!
            if (theTie.orb() != defaultORB) {
                throw new Exception("theTie.orb() != defaultORB");
            }
            if (StubAdapter.getDelegate( theStub ).orb(theStub) != defaultORB) {
                throw new Exception("theStub.orb() != defaultORB");
            }

            if (Utility.getAndForgetTie(theStub) != null) {
                throw new Exception("Utility.getAndForgetTie(theStub) != null");
            }
        }
 
        // Now repeat the same test, only this time use JNDI to do the
        // connect on the stub, to insure that our updated CNCtx code
        // with "auto-connect" works correctly...

        Context nameContext = context.getNameContext();
        
        if (iiop) {
            
            PROImpl theImpl = new PROImpl();
            Tie theTie = Util.getTie(theImpl);
            org.omg.CORBA.Object theStub = 
		(org.omg.CORBA.Object)PortableRemoteObject.toStub(theImpl);
            callFailed = false;
            try {
                theTie.orb();
            } catch (SystemException e) {
                callFailed = true;
            }
            if (!callFailed) {
                throw new Exception ("(nameContext) theTie already connected");
            }
            callFailed = false;
            try {
                StubAdapter.getDelegate( theStub );
            } catch (SystemException e) {
                callFailed = true;
            }
            if (!callFailed) {
                throw new Exception ("(nameContext) theStub already connected");
            }

            nameContext.rebind("PROTest auto-connect",theStub); // Connect both!
            
            if (theTie.orb() != defaultORB) {
                throw new Exception("(nameContext) theTie.orb() != defaultORB");
            }
            if (StubAdapter.getDelegate( theStub ).orb(theStub) != defaultORB) {
                throw new Exception("(nameContext) theStub.orb() != defaultORB");
            }

            if (Utility.getAndForgetTie(theStub) != null) {
                throw new Exception("(nameContext) Utility.getAndForgetTie(theStub) != null");
            }
        }
        
	// Now unexport remote object and make sure we can no longer invoke it...
	dprint( "test.20" ) ;

        objref.unexport();
        callFailed = false;
        try {
            objref.sayHello();
        } catch (RemoteException e) {
            callFailed = true;
        }

	dprint( "test.21" ) ;
        if (!callFailed) {

            // _REVISIT_    PortableRemoteObject.unexportObject on pre 1.2 vms,
            //              under JRMP, does nothing,  so we gotta special case.
            //              I really don't like this, and PRO should be fixed
            //              somehow. Until then, we don't fail in this case...

            if (iiop) {
                throw new Exception("unexportObject() failed");
            } else {
                // System.out.print("Warning: PortableRemoteObject.unexportObject() NOP on pre 1.2 JRMP!");
            }
        }

        // Now unexport our local instance...
	dprint( "test.22" ) ;

        PortableRemoteObject.unexportObject(localImpl);

        // Make sure we cannot unexport an object which was never exported...
	dprint( "test.23" ) ;
        
        fail = false;
        try {
            PortableRemoteObject.unexportObject(new PROImpl2()  );
        } catch (NoSuchObjectException e) {
            fail = true;
        }
        
	dprint( "test.24" ) ;
        if (!fail) {
            if (iiop) {
                throw new Exception ("unexportObject() on unconnected servant succeeded.");
            } else {
                // System.out.print("Warning: unexportObject() on unconnected servant succeeded on JRMP!");
            }
        }
        
        // Now make sure that trying to publish an unexported impl
        // fails...

	dprint( "test.25" ) ;
        callFailed = false;
	dprint( "test.26" ) ;
        try {
            nameContext.rebind("foo",new PROImpl2());
        } catch (javax.naming.NamingException e) {
            Throwable cause = e.getRootCause();
            if (cause != null) {
                if (cause instanceof java.rmi.NoSuchObjectException) {
                    callFailed = true;
                } else {
                    callFailed = true;
                    if (iiop) {
                        System.out.print("Warning: Publish unexported impl root cause: " + cause.getClass().getName());
                    }
                }
            }
        } catch (Exception e) {
            callFailed = true;
            System.out.print("Warning: Publish unexported impl caught: " + e);
        }

	dprint( "test.27" ) ;
        if (!callFailed) {
            if (iiop) {
                throw new Exception("Publish unexported impl succeeded!");
            } else {
                // System.out.print("Warning: Publish unexported impl succeeded on 1.1.6/JRMP!");
            }
        }

        // Now make sure that we cannot call toStub with our unexported object...
	dprint( "test.28" ) ;

        callFailed = false;
        try {
            PortableRemoteObject.toStub(localImpl);
        } catch (NoSuchObjectException e) {
            callFailed = true;
        }
	dprint( "test.29" ) ;

        if (!callFailed) {
            if (iiop) {
                throw new Exception("toStub on unexported impl succeeded!");
            } else {
                // System.out.print("Warning: toStub on unexported impl succeeded on JRMP!");
            }
        }
        
        // Now fire up our servant which implements an inner interface and
        // make sure we can talk to it...
 
        Remote inner = context.startServant("javax.rmi.ServantInner","inner",false,iiop);
        SInner innerRef = (SInner) 
            PortableRemoteObject.narrow(inner,SInner.class);
        if (!innerRef.echo(innerRef).equals(innerRef)) {
            throw new Exception("innerRef.echo(innerRef) != innerRef");
        }
       
        // Now fire up our servant which implements an outer interface and
        // make sure we can talk to it...
 
        if (iiop) { // _REVISIT_ This does not work on JRMP - why not?
        
            Remote outer = context.startServant("javax.rmi.ServantOuter$Inner","outer",false,iiop);
            ServantOuter outerRef = (ServantOuter) 
                PortableRemoteObject.narrow(outer,ServantOuter.class);
            if (!outerRef.echo(outerRef).equals(outerRef)) {
                throw new Exception("innerRef.echo(outerRef) != outerRef");
            }
        }
        
        // Make sure we can pass an IDL reference across our RMI stub...
        
        CodeBase cb = innerRef.getCodeBase();
        if (cb == null) {
            throw new Exception("innerRef.getCodeBase() == null");
        }
        
        // Make sure we can pass a servant which only implements Remote...
   
        Remote r = innerRef.getOnlyRemote();
        if (r == null) {
            throw new Exception("innerRef.getOnlyRemote() == null");
        }
        
        // Make sure we can get a stub for a servant which only implements
        // Remote...
        
        Remote onlyRemoteStub = PortableRemoteObject.toStub(
	    new rmic.OnlyRemoteServant());
        if (onlyRemoteStub == null) {
            throw new Exception("onlyRemoteStub == null");
        }
        
        // Hashcode regression test. Ensure that stubs for two distincts types
        // have different hashCodes. This code is (effectively) a copy of the
        // Sun East HashCodeTests.HashCode0002() method.
        
        // This is really a bogus test, as it can always fail the hashcode
        // comparison, but not be equal...
        
        nameContext.rebind("HashCode",new HashCodeImpl());
        nameContext.rebind("HashCodeA",new HashCodeAImpl());
        Object hashCodeObject = nameContext.lookup("HashCode");
        Object hashCodeAObject = nameContext.lookup("HashCodeA");
        HashCode hashCodeStub = 
	    (HashCode)PortableRemoteObject.narrow(hashCodeObject,HashCode.class);
        HashCodeA hashCodeAStub = 
	    (HashCodeA)PortableRemoteObject.narrow(hashCodeAObject,HashCodeA.class);        
        int hashCode = hashCodeStub.hashCode();
        int hashCodeA = hashCodeAStub.hashCode();
        
        if (hashCode == hashCodeA) {
            System.out.println("hashCode == hashCodeA ("+hashCode+")");
            if (hashCodeStub.equals(hashCodeAStub)) {
                throw new Exception("hashCodeStub.equals(hashCodeAStub)");
        }
    }
        
        // RegisterTarget regression test.  The Stub was being cached for
        // the tie, and it's delegate was not cleared. The stub is now
        // removed from the cache by the Util.unexportObject() method.
        
        if (iiop) {
            
            // Create/get tie for impl...
            
            PROImpl obj = new PROImpl();
            Tie tie = Util.getTie(obj);
            
            // Publish it ...
            
            nameContext.rebind("RegisterTarget", obj);
            
            // Unexport it...
    	    
	    PortableRemoteObject.unexportObject(obj);
            
            // Re-register it...
            
            Util.registerTarget(tie, obj);

	    // Publish it again...
	        
            nameContext.rebind("RegisterTarget", obj);
            
            // Lookup it up and make sure it is alive...
            
	    Object registerObject = nameContext.lookup("RegisterTarget");
            PROHello registerRef = (PROHello)PortableRemoteObject.narrow(registerObject, PROHello.class);
	    if (!registerRef.sayHello().equals(PROHello.HELLO)) {
		throw new Exception("RegisterTarget failed");
	    }
        }
  
        // Ensure that Utility.loadStub() manages cache correctly...
        if (iiop) {
            // Get a connected tie...
            Multi servant = new Multi();
            Tie tie = Util.getTie(servant);
            tie.orb(defaultORB);
            String interfaceName;
            String stubRepoId;
            
            // Load A...
            Utility.clearCaches();
            interfaceName = "alpha.bravo.A";
	    stubRepoId = "RMI:alpha.bravo.A:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

            // Load B...
            Utility.clearCaches();
            interfaceName = "alpha.bravo.B";
	    stubRepoId = "RMI:alpha.bravo.B:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

            // Load DuckBill...
            Utility.clearCaches();
            interfaceName = "alpha.bravo.DuckBill";
	    stubRepoId = "RMI:alpha.bravo.DuckBill:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

            // Reload A with onlyMostDerived, and ensure that we get Multi
            interfaceName = "alpha.bravo.A";
	    stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, true ) ;

            // Reload A , and ensure that we still get Multi
            interfaceName = "alpha.bravo.A";
	    stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;
            
            // Load A...
            Utility.clearCaches();
            interfaceName = "alpha.bravo.A";
	    stubRepoId = "RMI:alpha.bravo.A:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, false ) ;

            // Reload A with onlyMostDerived, and ensure that we get Multi
            interfaceName = "alpha.bravo.A";
	    stubRepoId = "RMI:alpha.bravo.Multi:0000000000000000" ;
	    testLoadStub( servant, tie, interfaceName, stubRepoId, true ) ;
        }
    }
    
    private void testLoadStub( Multi servant, Tie tie, 
	String interfaceName, String repoId, boolean flag ) throws Exception
    {
	PresentationManager.StubFactoryFactory sff = 
	    com.sun.corba.se.spi.orb.ORB.getStubFactoryFactory() ;
	PresentationManager.StubFactory stubFactory = 
	    sff.createStubFactory( interfaceName, false, null, null, null ) ;
	Remote stub = Utility.loadStub( tie, stubFactory, 
	    null, flag ) ;
	String actualRepoId = StubAdapter.getTypeIds( stub )[0] ;

	if (!actualRepoId.equals( repoId )) {
	    throw new Exception( "Utility.loadStub: expected " + repoId +
		" got " + actualRepoId ) ;
	}
    }

}
