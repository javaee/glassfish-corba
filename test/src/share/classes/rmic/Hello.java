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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;
import java.io.Serializable;

public interface Hello extends java.rmi.Remote
{
    String CONSTANT1 = "constant1";
    int CONSTANT2 = 3+3;

    public String getCodeBase() throws java.rmi.RemoteException;
    public void publishRemoteObject(String name) throws java.rmi.RemoteException;
    public String sayHello() throws java.rmi.RemoteException;
    public int sum(int value1, int value2) throws java.rmi.RemoteException;
    public String concatenate(String str1, String str2) throws java.rmi.RemoteException;
    public String checkOBV(ObjectByValue obv) throws java.rmi.RemoteException;
    public ObjectByValue getOBV() throws java.rmi.RemoteException;
    public Hello getHello () throws java.rmi.RemoteException;

    public int[] echoArray (int[] array) throws java.rmi.RemoteException;
    public long[][] echoArray (long[][] array) throws java.rmi.RemoteException;
    public short[][][] echoArray (short[][][] array) throws java.rmi.RemoteException;
    public ObjectByValue[] echoArray (ObjectByValue[] array) throws java.rmi.RemoteException;
    public ObjectByValue[][] echoArray (ObjectByValue[][] array) throws java.rmi.RemoteException;

    public AbstractObject echoAbstract (AbstractObject absObj) throws java.rmi.RemoteException;
    public AbstractObject[] getRemoteAbstract() throws java.rmi.RemoteException;
    public void shutDown () throws java.rmi.RemoteException;
    public void throwHello (int count, String message) throws java.rmi.RemoteException, HelloException;
    public void throw_NO_PERMISSION (String s, int minor) throws java.rmi.RemoteException;
    public CharValue echoCharValue (CharValue value) throws java.rmi.RemoteException;
    public Object echoObject (Object it) throws java.rmi.RemoteException;
    public Serializable echoSerializable (Serializable it) throws java.rmi.RemoteException;

    public void throwError(Error it) throws java.rmi.RemoteException;
    public void throwRemoteException(java.rmi.RemoteException it) throws java.rmi.RemoteException;
    public void throwRuntimeException(RuntimeException it) throws java.rmi.RemoteException;
   
    public Hello echoRemote (Hello stub) throws java.rmi.RemoteException;
}
