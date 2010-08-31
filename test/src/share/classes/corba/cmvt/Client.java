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
package corba.cmvt;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.* ;
import java.rmi.RemoteException;
import java.io.*;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

public class Client
{


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

    public static CustomMarshalledValueType constructCustomMarshalledValueType(int len, String r){
        CustomMarshalledValueType cmvt = null;
        try{
            cmvt = new CustomMarshalledValueType(len, (byte)r.charAt(0));
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        return cmvt;
    }

    public static java.util.Vector constructVector(int len, String r){
        java.util.Vector ht = new java.util.Vector();
        for(int i=0; i<len; i++) 
            ht.addElement(getStringObject(i,r));
        return ht;
    }

    public static java.util.Hashtable constructHashtable(int len, String r){
        java.util.Hashtable ht = new java.util.Hashtable();
        for(int i=0; i<len; i++) 
            ht.put(getStringObject(i,r), getStringObject(i,r));
        return ht;
    }

    public static java.lang.Object getStringObject(int len, String r){
        String s = new String();
        for(int i=0; i<len; i++) s+=r;
        return s;
    }

    public static void main(String args[])
    {
        int len = 250;
        String rep = "a";
        boolean doVector=false, doHashtable=true, doCMVT=true, doString=false, doLargeString=false, doHello=false;

        for (int i=0; i<args.length; i++){
            if(args[i].equals("-len")){
                len = Integer.parseInt(args[i+1]);
            }            
            if(args[i].equals("-rep")){
                rep = args[i+1];
            }    
            if(args[i].equals("-vector")){
                doVector=true;
            }    
            if(args[i].equals("-hashtable")){
                doHashtable=true;
            }    
            if(args[i].equals("-cmvt")){
                doCMVT=true;
            }          
            if(args[i].equals("-string")){
                doString=true;
            } 
            if(args[i].equals("-largeString")){
                doLargeString=true;
            }  
            if(args[i].equals("-hello")){
                doHello=true;
            }   
        }

        try{

            ORB orb = ORB.init(args, System.getProperties());

            com.sun.corba.se.spi.orb.ORB ourORB
                = (com.sun.corba.se.spi.orb.ORB)orb;

            System.out.println("==== Client GIOP version "
                               + ourORB.getORBData().getGIOPVersion()
                               + " with strategy "
                               + ourORB.getORBData().getGIOPBuffMgrStrategy(
				    ourORB.getORBData().getGIOPVersion())
                               + "====");

            org.omg.CORBA.Object obj = readObjref("IOR", orb);

	    GIOPCombo ref = 
                (GIOPCombo) PortableRemoteObject.narrow(obj, 
                                                            GIOPCombo.class);

            // Check various data types.
	    int invalue = 1234;

            if(doHello){
                System.out.println("helloClient: Got server objref ! Invoking ...") ;
                System.out.println("ref.sayHello("+invalue+") = "+ref.sayHello(invalue));
            }

            if(doString){
                //echo String
                String string = (String)getStringObject(len,rep);
                System.out.println("String constructed for transmission");
                String stringEcho = ref.echo(string);
                System.out.println("ref.echo(string) = " + stringEcho);
                System.out.println("Echoed String equals the sent String == " +string.equals(stringEcho));
            }

            if(doLargeString){
                //echo String of 10 times the len
                String string = (String)getStringObject(10*len,rep);
                System.out.println("String constructed for transmission");
                String stringEcho = ref.echo(string);
                System.out.println("ref.echo(string) = " + stringEcho);
                System.out.println("Echoed String equals the sent String == " +string.equals(stringEcho));
            }

            if(doCMVT){
                //echo CustomMarshalledValueType containing string objects
                CustomMarshalledValueType cmvt = constructCustomMarshalledValueType(len,rep);
                System.out.println("CustomMarshalledValueType constructed for transmission");
                CustomMarshalledValueType cmvtEcho = ref.echo(cmvt);
                System.out.println("ref.echo(cmvt) = " + cmvtEcho);
                System.out.println("Echoed CustomMarshalledValueType equals the sent CustomMarshalledValueType == " +cmvt.equals(cmvtEcho));
            }

            if(doVector){
                //echo Vector containing string objects
                java.util.Vector vector = constructVector(len,rep);
                System.out.println("Vector constructed for transmission");
                java.util.Vector vectorEcho = ref.echo(vector);
                System.out.println("ref.echo(vector) = " + vectorEcho);
                System.out.println("Echoed Vector equals the sent Vector == " +vector.equals(vectorEcho));
            }

            if(doHashtable){
                //echo Hashtable containing string objects
                java.util.Hashtable ht = constructHashtable(len,rep);
                System.out.println("Hashtable constructed for transmission");
                java.util.Hashtable htEcho = ref.echo(ht);
                System.out.println("ref.echo(ht) = " + htEcho);
                System.out.println("Echoed Hashtable equals the sent Hashtable == " +ht.equals(htEcho));
            }

	    System.out.println("\nhelloClient exiting ...") ;

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
