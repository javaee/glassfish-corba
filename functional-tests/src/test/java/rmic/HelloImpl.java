/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package rmic;

import org.omg.CORBA.ORB;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.CompletionStatus;
import java.io.Serializable;
import java.rmi.server.RMIClassLoader;

public class HelloImpl //extends javax.rmi.PortableRemoteObject
    implements Hello {

    ObjectByValue obv = null;
    AbstractObject[] remotes = null;

    public HelloImpl() throws java.rmi.RemoteException {
    }

    public void initRemotes() throws java.rmi.RemoteException {
        if (remotes == null) {
            remotes = new AbstractObject[3];
            remotes[0] = new RemoteObjectServer(0);
            remotes[1] = new RemoteObjectServer(1);
            remotes[2] = new RemoteObjectServer(2);
        }
    }

    public String getCodeBase() throws java.rmi.RemoteException {
        return RMIClassLoader.getClassAnnotation(getClass());  
    }
    
    public void publishRemoteObject(String name) throws java.rmi.RemoteException {
        try {
            test.Util.singleServantContext.rebind(name,new RemoteObjectServer(192));
        } catch (javax.naming.NamingException e) {
            throw new java.rmi.RemoteException("publishRemoteException caught: "+e); 
        }
    }
        
    public String sayHello() throws java.rmi.RemoteException {
        return "hello";
    }

    public int sum(int value1, int value2) throws java.rmi.RemoteException {
        return value1 + value2;
    }

    public String concatenate(String str1, String str2) throws java.rmi.RemoteException {
        return str1+str2;
    }

    public String checkOBV(ObjectByValue obv) throws java.rmi.RemoteException {
        this.obv = obv;

        return "The Results are: "  +
            (obv.getValue1() + obv.getValue2()) +
            obv.getString1()    +
            obv.getString2();
    }

    public ObjectByValue getOBV() throws java.rmi.RemoteException {
        return obv;
    }

    public Hello getHello () throws java.rmi.RemoteException {
        return (Hello) PortableRemoteObject.toStub(this);
    }

    public int[] echoArray (int[] array) throws java.rmi.RemoteException {
        return array;
    }

    public long[][] echoArray (long[][] array) throws java.rmi.RemoteException {
        return array;
    }

    public short[][][] echoArray (short[][][] array) throws java.rmi.RemoteException {
        return array;
    }

    public ObjectByValue[] echoArray (ObjectByValue[] array) throws java.rmi.RemoteException {
        return array;
    }

    public ObjectByValue[][] echoArray (ObjectByValue[][] array) throws java.rmi.RemoteException {
        return array;
    }

    public AbstractObject echoAbstract (AbstractObject absObj) throws java.rmi.RemoteException {
        return absObj;
    }

    public AbstractObject[] getRemoteAbstract() throws java.rmi.RemoteException {
        initRemotes();
        return remotes;
    }


    public void shutDown () throws java.rmi.RemoteException {
        System.exit(0);
    }

    public void throwHello (int count, String message) throws java.rmi.RemoteException, HelloException {
        throw new HelloException(count,message);
    }

    public void throw_NO_PERMISSION (String s, int minor) throws java.rmi.RemoteException {
        throw new NO_PERMISSION(s,minor,CompletionStatus.COMPLETED_YES);
    }

    public CharValue echoCharValue (CharValue value) throws java.rmi.RemoteException {
        return value;
    }

    public Object echoObject (Object it) throws java.rmi.RemoteException {
        return it;
    }

    public Serializable echoSerializable (Serializable it) throws java.rmi.RemoteException {
        return it;
    }

    public void throwError(Error it) throws java.rmi.RemoteException {
        throw it;
    }
    
    public void throwRemoteException(java.rmi.RemoteException it) throws java.rmi.RemoteException {
        throw it;
    }
    
    public void throwRuntimeException(RuntimeException it) throws java.rmi.RemoteException {
        throw it;
    }

    public Hello echoRemote (Hello stub) throws java.rmi.RemoteException {
        return stub;
    }
}
