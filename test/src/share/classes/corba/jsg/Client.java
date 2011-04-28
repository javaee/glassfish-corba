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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 10:06:37 by Harold Carr.
//

package corba.jsg;

import javax.naming.InitialContext;
import javax.rmi.CORBA.Util;

import javax.activity.ActivityRequiredException;
import javax.activity.ActivityCompletedException;
import javax.activity.InvalidActivityException;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.se.impl.misc.ORBUtility;

import org.omg.CORBA.*;
import org.omg.PortableInterceptor.*;

import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;

public class Client extends org.omg.CORBA.LocalObject
        implements ORBInitializer, ClientRequestInterceptor {
      
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static final int SERVICE_CTX_ID = 101;
    public static final int SERVICE_CTX_VALUE = 1001;

    public static ORB orb;
    public static InitialContext initialContext;
    
    private Codec codec;

    public static void main(String[] av) {

        try {

	    U.sop(main + " starting");

	    if (!(ColocatedClientServer.isColocated)) {
		U.sop(main + " : creating ORB.");
		orb = ORB.init(av, null);
		U.sop(main + " : creating InitialContext.");
		initialContext = C.createInitialContext(orb);
	    }

	    // RMI invocations

	    rmiiI r = (rmiiI) U.lookupAndNarrow(Server.rmiiIPOA,
						rmiiI.class, initialContext);
	    U.sop("\nRMI invocations:\n");

	    // Primitive types.
	    System.out.println("byte: " + r.ping((byte)99));
	    System.out.println("boolean: " + r.ping(true));
	    System.out.println("char: " + r.ping('V'));
	    System.out.println("short: " + r.ping((short)999));
	    System.out.println("int: " + r.ping(999999999));
	    System.out.println("long: " + r.ping(999999999999L));
	    System.out.println("float: " + r.ping(999999999.999999999f));
	    System.out.println("double: " + r.ping(99e+10d));
	    System.out.println("String: " + r.ping("hello"));

	    // Array types.
	    System.out.println("byte[]: " + (r.ping(new byte[2000])).length);
	    System.out.println("boolean[]: " + 
			       (r.ping(new boolean[2000])).length);
	    System.out.println("char[]: " + (r.ping(new char[2000])).length);
	    System.out.println("short[]: " + (r.ping(new short[2000])).length);
	    System.out.println("int[]: " + (r.ping(new int[2000])).length);
	    System.out.println("long[]: " + (r.ping(new long[2000])).length);
	    System.out.println("float[]: " + (r.ping(new float[2000])).length);
	    System.out.println("double[]: " +
			       (r.ping(new double[2000])).length);

	    // User and System exceptions.
	
	    try {
		r.ping("user exception", 0);
	    } catch (java.lang.CloneNotSupportedException e) {
		System.out.println("java.lang.CloneNotSupportedException");
		//e.printStackTrace(System.out);
	    }

	    try {
		r.ping("system exception", 1);
	    } catch (java.rmi.RemoteException e) {
		if (!(e.getCause() instanceof org.omg.CORBA.UNKNOWN)) {
		    throw e; // test failed
		}
		System.out.println("java.rmi.RemoteException");
		//e.printStackTrace(System.out);
	    }

	    // Complex types.

	    // ValueType
	    class TestObject implements java.io.Serializable {
		private final String value = "TestObject";
		public String toString() {
		    return value;
		}
	    }
	    System.out.println("reply received: " + r.ping(new TestObject()));

	    // org.omg.CORBA.Object (IOR)

	    System.out.println("reply received: " +
			       r.ping((org.omg.CORBA.Object)r));

	    // IDL invocations

	    idlI i = idlIHelper.narrow(U.resolve(Server.idlIPOA, orb));
	    U.sop("\nIDL invocations:\n");
	    // Primitive types.
	    System.out.println("byte: " + i.m1((byte)99));
	    System.out.println("boolean: " + i.m2(true));
	    System.out.println("char: " + i.m3('V'));
	    System.out.println("short: " + i.m4((short)999));
	    System.out.println("int: " + i.m5(999999999));
	    System.out.println("long: " + i.m6(999999999999L));
	    System.out.println("float: " + i.m7(999999999.999999999f));
	    System.out.println("double: " + i.m8(99e+10d));
	    System.out.println("String: " + i.m9("hello"));

	    // Array types.
	    System.out.println("byte[]: " + (i.m10(new byte[2000])).length);
	    System.out.println("boolean[]: " + 
			       (i.m11(new boolean[2000])).length);
	    System.out.println("char[]: " + (i.m12(new char[2000])).length);
	    System.out.println("short[]: " + (i.m13(new short[2000])).length);
	    System.out.println("int[]: " + (i.m14(new int[2000])).length);
	    System.out.println("long[]: " + (i.m15(new long[2000])).length);
	    System.out.println("float[]: " + (i.m16(new float[2000])).length);
	    System.out.println("double[]: " +
			       (i.m17(new double[2000])).length);

	    // User and System exceptions.
	
	    try {
		i.m18("user exception", 0);
	    } catch (AppException e) {
		System.out.println("corba.jsg.AppException");
		//e.printStackTrace(System.out);
	    }

	    try {
		i.m18("system exception", 1);
	    } catch (org.omg.CORBA.UNKNOWN e) {
		System.out.println("org.omg.CORBA.UNKNOWN");
		//e.printStackTrace(System.out);
	    }

	    // Complex types.

	    // Serializable
	    ValueObject vo = new ValueObjectImpl();
	    vo.setName("corba.jsg.ValueObject");
	    System.out.println("reply received: " + i.m19(vo).getName());

	    // org.omg.CORBA.Object (IOR)

	    System.out.println("reply received: " + i.m20(i));

	    /*
	     * Need to add test cases:
	     *
	     * 1. implicit servant activation (client and server).
	     *
	     * 2. remote code downloading (client and server).
	     *
	     * 3. class evolution (client and server).
	     */

	    // CodecTestPOA target invocation test

	    codecTestI c = (codecTestI) U.lookupAndNarrow(Server.codecTestIPOA,
							 codecTestI.class,
							 initialContext);
	    c.invoke();

	    orb.shutdown(true);

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
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

    public void post_init(ORBInitInfo info) 
    {
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
            info.add_client_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
        }
        U.sop("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //
    // implementation of the Interceptor interface.
    //

    public String name() 
    {
        return "ClientInterceptor";
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //    
    // implementation of the ClientInterceptor interface.
    //

    public void send_request(ClientRequestInfo ri) throws ForwardRequest {

	if (!(ri.operation().equals("invoke"))) {
	    return;
	}

        // do IOR policy checking.

        TaggedComponent tagComp = null;
        try {
            tagComp = ri.get_effective_component(Server.POLICY_TYPE);
        } catch (BAD_PARAM e) {
            U.sop("send_request failed at get_effective_component");
        }

	TypeCode typeCode = ORB.init().get_primitive_tc(TCKind.tk_long);
	Any any = null;
	try {
	    any = codec.decode_value(tagComp.component_data, typeCode);
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
	U.sop("tag component value: " + value);
	if (value != Server.TAG_VALUE) {
	    throw new RuntimeException("codecTest failed");
	}

	// Add a service context
        any = ORB.init().create_any();
        any.insert_long(SERVICE_CTX_VALUE);

        byte[] svcData = null;
        try {
            svcData = this.codec.encode_value(any);
        } catch (InvalidTypeForEncoding e) {
            INTERNAL exc = new INTERNAL();
	    exc.initCause(e);
	    throw exc;
        }
	ServiceContext sc = new ServiceContext(SERVICE_CTX_ID, svcData);
	ri.add_request_service_context(sc, true);
    }

    public void send_poll(ClientRequestInfo ri) 
    {
    }

    public void receive_reply(ClientRequestInfo ri) 
    {    
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
    }    
}

// End of file.

