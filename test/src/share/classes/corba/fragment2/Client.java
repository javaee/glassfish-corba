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
package corba.fragment2;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;
import com.sun.corba.se.spi.misc.ORBConstants;

class Tester extends Thread{
    FragmentTester tester;
    int size;
    static int totalThread = 0;
    int threadID;

    public Tester(FragmentTester f, int s){
        tester = f;
        size = s;
        threadID = totalThread;
        totalThread++;
    }

    public void run() 
    {
        System.out.println("Sending array of length " + size);

        byte array[] = new byte[size];

        int i = 0;

        do {

            for (byte x = 0; x < 4; x++) {
                System.out.print("" + x + " ");
                array[i++] = x;
            }
            // System.out.println();

        } while (i < size);

        try{
            tester.verifyTransmission(array);
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }

        System.out.println("testByteArray "+"ID:"+threadID+" completed normally");
    }
}


class TestCatagory{
    public String giopVersion;
    public int fragmentSize;
    public int arrayLength;
    public int threadNumber;
}


public class Client
{


    static TestCatagory testCatagory[];
    static int catagoryNumber;

    public static void setTest(){
        int data[] = {  2,1024,1024,5,
                        2,2048,2048,5,
                        2,2048,4096,5,
                        1,1024,1024,5
        };

        catagoryNumber = data.length / 4;
        testCatagory = new TestCatagory[catagoryNumber];
        for(int i=0;i<catagoryNumber;i++){
            testCatagory[i] = new TestCatagory();
            testCatagory[i].giopVersion = "1."+data[i*4];
            testCatagory[i].fragmentSize = data[i*4+1];
            testCatagory[i].arrayLength = data[i*4+2];
            testCatagory[i].threadNumber = data[i*4+3];
        }
    }   

        

    // size must be divisible by four

    public static org.omg.CORBA.Object readObjref(String file, org.omg.CORBA.ORB orb) {
	String fil = System.getProperty("output.dir")+System.getProperty("file.separator")+file;
	try {
	    java.io.DataInputStream in = 
		new java.io.DataInputStream(new FileInputStream(fil));
	    String ior = in.readLine();
	    System.out.println("IOR: "+ior);
	    return orb.string_to_object(ior);
	} catch (java.io.IOException e) {
	    System.err.println("Unable to open file "+fil);
	    System.exit(1);
	}
	return null;
    }

    public static void main(String args[])
    {
        for(int i=0;i<args.length;i++)
            System.out.println(args[i]);
        setTest();
        try{
            for(int i=0;i<catagoryNumber;i++){
                System.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, "" + testCatagory[i].fragmentSize);
                System.setProperty("array.length", "" + testCatagory[i].arrayLength);
                System.setProperty(ORBConstants.GIOP_VERSION, testCatagory[i].giopVersion);


                ORB orb = ORB.init(args, System.getProperties());

            /*
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            NameComponent nc = new NameComponent("FragmentTester", "");
            NameComponent path[] = {nc};

            org.omg.CORBA.Object obj = ncRef.resolve(path);
            */

                org.omg.CORBA.Object obj = readObjref("IOR", orb);

                FragmentTester tester = 
                    (FragmentTester) PortableRemoteObject.narrow(obj, 
                                                                 FragmentTester.class);

            // Do the crazy work here

                int arrayLen = Integer.parseInt(System.getProperty("array.length"));

                for(int j=0;j < testCatagory[i].threadNumber;j++)
                    new Tester(tester, arrayLen).start();

            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
