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
//
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 11:12:48 by Harold Carr.
//

package corba.jsg;

import javax.naming.InitialContext;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.spi.orb.ORB;

import java.rmi.Remote; 
import java.rmi.RemoteException; 
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.*;

import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;

import com.sun.corba.se.spi.servicecontext.ORBVersionServiceContext;
import com.sun.corba.se.spi.orb.ORBVersion;

interface rmiiI extends Remote {

    // Primitive types.

    public byte ping(byte value) throws java.rmi.RemoteException;
    public boolean ping(boolean value) throws java.rmi.RemoteException;
    public char ping(char value) throws java.rmi.RemoteException;
    public short ping(short value) throws java.rmi.RemoteException;
    public int ping(int value) throws java.rmi.RemoteException;
    public long ping(long value) throws java.rmi.RemoteException;
    public float ping(float value) throws java.rmi.RemoteException;
    public double ping(double value) throws java.rmi.RemoteException;
    public String ping(String value) throws java.rmi.RemoteException;

    // Array types.

    public byte[] ping(byte[] value) throws java.rmi.RemoteException;
    public boolean[] ping(boolean[] value) throws java.rmi.RemoteException;
    public char[] ping(char[] value) throws java.rmi.RemoteException;
    public short[] ping(short[] value) throws java.rmi.RemoteException;
    public int[] ping(int[] value) throws java.rmi.RemoteException;
    public long[] ping(long[] value) throws java.rmi.RemoteException;
    public float[] ping(float[] value) throws java.rmi.RemoteException;
    public double[] ping(double[] value) throws java.rmi.RemoteException;
    
    // Complex types.

    public java.io.Serializable ping(java.io.Serializable value)
	throws java.rmi.RemoteException;
    public org.omg.CORBA.Object ping(org.omg.CORBA.Object value)
	throws java.rmi.RemoteException;

    // System and User exceptions.

    public void ping(String msg, int flag)
	throws java.rmi.RemoteException, java.lang.CloneNotSupportedException;
}

class rmiiIServantPOA extends PortableRemoteObject implements rmiiI {

    rmiiIServantPOA() throws RemoteException {
	// DO NOT CALL SUPER - that would connect the object.
    }

    // Primitive types.

    public byte ping(byte value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public boolean ping(boolean value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public char ping(char value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public short ping(short value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public int ping(int value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public long ping(long value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public float ping(float value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public double ping(double value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public String ping(String value) {
	System.out.println("ping received: " + value);
	return value;
    }

    // Array types.

    public byte[] ping(byte[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public boolean[] ping(boolean[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public char[] ping(char[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public short[] ping(short[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public int[] ping(int[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public long[] ping(long[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public float[] ping(float[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public double[] ping(double[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    // Complex types.

    public java.io.Serializable ping(java.io.Serializable value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public org.omg.CORBA.Object ping(org.omg.CORBA.Object value) {
	System.out.println("ping received: " + value);
	return value;
    }

    // User and System exceptions.

    public void ping(String msg, int flag) // User exception
	    throws java.lang.CloneNotSupportedException {
	System.out.println("ping received: exception msg: " + msg);
	if (flag == 0) {
	    throw new java.lang.CloneNotSupportedException(msg);
	} else {
	    throw new org.omg.CORBA.UNKNOWN(msg);
	}
    }
}

class idlIServantPOA extends idlIPOA {

    // Primitive types.

    public byte m1(byte value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public boolean m2(boolean value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public char m3(char value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public short m4(short value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public int m5(int value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public long m6(long value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public float m7(float value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public double m8(double value) {
	System.out.println("ping received: " + value);
	return value;
    }

    public String m9(String value) {
	System.out.println("ping received: " + value);
	return value;
    }

    // Array types.

    public byte[] m10(byte[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public boolean[] m11(boolean[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public char[] m12(char[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public short[] m13(short[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public int[] m14(int[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public long[] m15(long[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public float[] m16(float[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    public double[] m17(double[] value) {
	System.out.println("ping received: " + value.length);
	return value;
    }

    // User and System exceptions.

    public void m18(String msg, int flag) // User exception
	    throws AppException {
	System.out.println("ping received: exception msg: " + msg);
	if (flag == 0) {
	    throw new AppException(msg);
	} else {
	    throw new org.omg.CORBA.UNKNOWN(msg);
	}
    }

    // Complex types.

    public ValueObject m19(ValueObject value) {
	System.out.println("ping received: " + value.getName());
	return value;
    }

    public idlI m20(idlI value) {
	System.out.println("ping received: " + value);
	return value;
    }
}

interface codecTestI extends Remote {
    public void invoke() throws java.rmi.RemoteException;
}

class codecTestIServantPOA extends PortableRemoteObject implements codecTestI {

    codecTestIServantPOA() throws RemoteException {
	// DO NOT CALL SUPER - that would connect the object.
    }

    public void invoke() {
	System.out.println("invoke() called");
    }
}

public class Server extends org.omg.CORBA.LocalObject
        implements ORBInitializer, IORInterceptor, ServerRequestInterceptor {

    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
	Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
	thisPackage + "._rmiiIServantPOA_Tie";

    public static final String rmiiIPOA = "rmiiIPOA";
    public static final String idlIPOA = "idlIPOA";
    public static final String codecTestIPOA = "codecTestIPOA";

    public static final int POLICY_TYPE = 10;
    public static final int TAG_VALUE = 5000;

    public static ORB orb;
    public static InitialContext initialContext;

    private Codec codec;

    public static void main(String[] av) {

        try {
	    U.sop(main + " starting");

	    if (!(ColocatedClientServer.isColocated)) {
		U.sop(main + " : creating ORB.");
		orb = (ORB) ORB.init(av, null);
		U.sop(main + " : creating InitialContext.");
		initialContext = C.createInitialContext(orb);
	    }

	    POA rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            // RMI-IIOP references.
            U.sop("Creating/binding RMI-IIOP references.");
            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA());
            U.createWithServantAndBind(rmiiIPOA, servant, rootPOA, orb);

            servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new codecTestIServantPOA());
            U.createWithServantAndBind(codecTestIPOA, servant, rootPOA, orb);

            // IDL references.
            U.sop("Creating/binding IDL references.");
            U.createWithServantAndBind(idlIPOA,
                                       new idlIServantPOA(), rootPOA, orb);

	    U.sop(main + " ready");
	    U.sop(Options.defServerHandshake);
	    System.out.flush();

	    synchronized (ColocatedClientServer.signal) {
		ColocatedClientServer.signal.notifyAll();
	    }
	    
	    orb.run();

        } catch (Exception e) {
	    U.sopUnexpectedException(main, e);
	    System.exit(1);
        }
	U.sop(main + " ending successfully");
	System.exit(Controller.SUCCESS);
    }

    ////////////////////////////////////////////////////
    //    
    // ORBInitializer interface implementation.
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) {

        // get hold of the codec instance to pass onto interceptors.

        CodecFactory codecFactory = info.codec_factory();
        Encoding enc = new Encoding(
                            ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2);
        try {
            codec = codecFactory.create_codec(enc);
        } catch (UnknownEncoding e) {
            INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
        }

        // register the interceptors.
        try {
            info.add_ior_interceptor(this);
	    info.add_server_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
        }
        U.sop("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //    
    // IORInterceptor interface implementation.
    //

    public void establish_components (IORInfo info) {

        // use codec to encode policy value into an CDR encapsulation.

        Any any = ORB.init().create_any();
        any.insert_long(TAG_VALUE);

        byte[] value = null;
        try {
            value = this.codec.encode_value(any);
        } catch (InvalidTypeForEncoding e) {
            INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
        }

        // create IOR TaggedComponents for OTSPolicy and InvocationPolicy.

        TaggedComponent tagComp = new TaggedComponent(POLICY_TYPE, value);

        // add ior components.

        info.add_ior_component(tagComp);
    }

    // org.omg.PortableInterceptors.InterceptorOperations implementation

    public String name(){
        return "Server.IORInterceptor";
    }

    public void destroy() {}

    ////////////////////////////////////////////////////
    //    
    // ServerRequestInterceptor interface implementation.
    //

    public void receive_request_service_contexts (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
    }

    public void receive_request (ServerRequestInfo ri) throws ForwardRequest {

	if (!(ri.operation().equals("invoke"))) {
	    return;
	}

	// CASE 1: test for expected servicecontext

	ServiceContext sc =
	    ri.get_request_service_context(Client.SERVICE_CTX_ID);

	TypeCode typeCode = ORB.init().get_primitive_tc(TCKind.tk_long);
	Any any = null;
	try {
	    any = codec.decode_value(sc.context_data, typeCode);
	} catch (TypeMismatch e) {
	    INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
	} catch (FormatMismatch e) {
	    INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
	}
	int value = any.create_input_stream().read_ulong();
	U.sop("service context value: " + value);
	value = any.extract_long(); // just to make sure this form works!
	U.sop("service context value: " + value);

	if (value != Client.SERVICE_CTX_VALUE) {
	    throw new RuntimeException("Service context data mismatch");
	}

	// CASE 2: test for ORBVersionServiceContext. This should fail.

	sc = ri.get_request_service_context(
				  ORBVersionServiceContext.SERVICE_CONTEXT_ID);

	typeCode = ORB.init().get_primitive_tc(TCKind.tk_octet);
	try {
	    any = codec.decode_value(sc.context_data, typeCode);
	} catch (TypeMismatch e) {
	    INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
	} catch (FormatMismatch e) {
	    INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
	}
	byte orbVersion = any.create_input_stream().read_octet();

	// The reason why the orbVersion value read from the codec stream is
	// expected to be different is because, the ORBVersionServiceContext
	// was created using JSG encoding, and is being unmarshaled using
	// CDR encoding using the codec implementation.
	if (ORBVersion.PEORB == orbVersion) {
	    U.sop("Test failed: ORBVersion must not be equal");
	    throw new RuntimeException("ORBVersion should be different");
	} else {
	    U.sop("Test passed: ORBVersion is different as expected");
	}
    }

    public void send_reply (ServerRequestInfo ri) 
    {
    }

    public void send_exception (ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

    public void send_other (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
    }
}

// End of file.
