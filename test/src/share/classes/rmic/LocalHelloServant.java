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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;
import com.sun.corba.se.impl.util.JDKBridge;
import javax.rmi.CORBA.Stub;
import org.omg.CORBA.BAD_OPERATION;
import java.rmi.RemoteException;
import java.rmi.MarshalException;

public class LocalHelloServant extends javax.rmi.PortableRemoteObject implements LocalHello {

    public LocalHelloServant() throws java.rmi.RemoteException {
        JDKBridge.setLocalCodebase(null);
    }
	
    public String sayHello (String to) throws java.rmi.RemoteException {
	return "Hello " + to;
    }
    
    public String echoString(String it) throws java.rmi.RemoteException {
        return it;
    }
    
    public Object echoObject(Object it) throws java.rmi.RemoteException {
        return it;
    }

    public int identityHash(Object it) throws java.rmi.RemoteException {
        return System.identityHashCode(it);
    }
    
    public int[] identityHash(Object a, Object b, Object c) throws java.rmi.RemoteException {
        int[] result = new int[3];
        result[0] = System.identityHashCode(a);
        result[1] = System.identityHashCode(b);
        result[2] = System.identityHashCode(c);
        return result;
    }

    public test.Hello echoHello (test.Hello in) throws java.rmi.RemoteException {
        return in;   
    }
    
    public rmic.Hello echoHello (rmic.Hello in) throws java.rmi.RemoteException {
        return in;
    }

    public void argNamesClash(int in,
                              int _in, 
                              int out,
                              int _out,
                              int so,
                              int exCopy,
                              int copies,
                              int method,
                              int reply,
                              int ex) throws java.rmi.RemoteException {
                                
    }

    public Base newServant() throws java.rmi.RemoteException {
        String codebase = JDKBridge.getLocalCodebase();
        if (codebase != null) {
            throw new java.rmi.RemoteException("localCodebase = "+codebase);
        }
        
        return new BaseImpl();
    }

    public String testPrimTypes(String arg0,
                                double arg1,
                                float arg2,
                                String arg3,
                                boolean arg4,
                                Object arg5,
                                String arg6) throws java.rmi.RemoteException {
        return "help";
    }

    public Object echoString(Object value1, String str, Object value2) throws java.rmi.RemoteException {
        if (!(value1 instanceof RemoteException)) {
            throw new RemoteException("value1 not RemoteException. Is "+value1.getClass());   
        }
        if (!(value2 instanceof MarshalException)) {
            throw new RemoteException("value2 not MarshalException. Is "+value2.getClass());   
        }
        
        return str;   
    }
    
    public Object echoArg1(int arg0, Object arg1) throws java.rmi.RemoteException {
        return arg1;
    }

}
