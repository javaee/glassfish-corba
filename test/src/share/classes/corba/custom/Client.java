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

package corba.custom;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.*;
import java.rmi.RemoteException;
import java.io.*;

public class Client
{
    // Create a new string with size number of filler chars
    private static String createLargeString(int size, char filler)
    {
        char valueBuf[] = new char[size];

        for (int i = 0; i < size; i++)
            valueBuf[i] = filler;
        
        return new String(valueBuf);
    }

    // Create a simple ArrayListNode (custom marshaled) with a large
    // data value.  Make it's next array support two links, but don't
    // give them any values.
    //
    // Fails at fragment size 160.  Seems to have something to do with
    // reading the Any (Anys are used when marshaling arrays).  TypeCodes
    // may assume something about the CDR buffer that is no longer valid
    // now that we fragment.
    public static void testArrayListNodeFailure1(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing ArrayListNode Failure 1 ----");

        ArrayListNode a = new ArrayListNode();

        a.data = Client.createLargeString(1024, 'A');
        a.next = new java.lang.Object[2];

        ArrayListNode result = (ArrayListNode)verifier.verifyTransmission(a);

        if (!a.data.equals(result.data))
            throw new Exception("result.data isn't equal to a.data");

        System.out.println("---- Successful ----");
    }

    // This is just a little harder than test 1.  Create two ArrayListNodes
    // with large data values.  Give the first a link to the second.
    //
    // Fails at fragment sizes 32, 64, and 160.
    //
    // These are really scary failures because the ORB level doesn't throw
    // exceptions.  It delivers data, but it's not the same as what was
    // sent!  
    public static void testArrayListNodeFailure2(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing ArrayListNode Failure 2 ----");

        ArrayListNode a = new ArrayListNode();
        ArrayListNode b = new ArrayListNode();

        a.data = Client.createLargeString(1024, 'A');
        b.data = Client.createLargeString(577, 'B');

        a.next = new java.lang.Object[1];
        b.next = null;
        a.next[0] = b;

        ArrayListNode result = (ArrayListNode)verifier.verifyTransmission(a);

        if (!a.data.equals(result.data))
            throw new Exception("result.data isn't equal to a.data");

        if (!b.data.equals(((ArrayListNode)result.next[0]).data))
            throw new Exception("result.next.data isn't equal to b.data");

        System.out.println("---- Successful ----");
    }

    public static void testComplexHashtable(Verifier verifier)
        throws RemoteException, Exception
    {
        System.out.println("---- Testing Complex Hashtable ----");

        Node a = Node.createNode(1024, 'A');
        Node b = Node.createNode(577, 'B');
        Node c = Node.createNode(222, 'C');
        Node d = Node.createNode(799, 'D');
        Node e = Node.createNode(1024, 'E');

        a.links.add(b);
        b.links.add(c);
        c.links.add(d);
        d.links.add(e);
        e.links.add(a);
        c.links.add(c);
        b.links.add(d);
        a.links.add(e);

        String aStr = "A";
        //        String bStr = "B";

        String bStr = new String(CharGenerator.getSomeUnicodeChars());

        String cStr = "C";
        String dStr = "D";
        String eStr = "E";

        Hashtable complex = new Hashtable();

        complex.put(aStr, a);
        complex.put(bStr, b);
        complex.put(cStr, c);
        complex.put(dStr, d);
        complex.put(eStr, e);

        Hashtable result = (Hashtable)verifier.verifyTransmission(complex);

        if (result.size() != complex.size())
            throw new Exception("Result has fewer items: " + result.size());

        Node resA = (Node)result.get(aStr);
        Node resB = (Node)result.get(bStr);
        Node resC = (Node)result.get(cStr);
        Node resD = (Node)result.get(dStr);
        Node resE = (Node)result.get(eStr);

        if (!a.equals(resA))
            throw new Exception("result a != a");
        if (!b.equals(resB))
            throw new Exception("result b != b");
        if (!c.equals(resC))
            throw new Exception("result c != c");
        if (!d.equals(resD))
            throw new Exception("result d != d");
        if (!e.equals(resE))
            throw new Exception("result e != e");
    }

    public static void main(String args[])
    {
        try {

            String fragmentSize = System.getProperty(com.sun.corba.ee.spi.misc.ORBConstants.GIOP_FRAGMENT_SIZE);

            if (fragmentSize != null)
                System.out.println("---- Fragment size: " + fragmentSize);

            ORB orb = ORB.init(args, System.getProperties());

            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("Verifier", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);

            Verifier verifier = 
                (Verifier) PortableRemoteObject.narrow(obj, 
                                                       Verifier.class);

            Client.testArrayListNodeFailure1(verifier);
            Client.testArrayListNodeFailure2(verifier);
            Client.testComplexHashtable(verifier);

        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
