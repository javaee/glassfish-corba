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
/* @(#)JRMPPerformance.java	1.4 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

/*
 * @test 1.0 98/04/21
 * @summary Verify that RMI IIOP Serialization works correctly.
 *
 *
 * @compile javax\rmi\CORBA\serialization\SerializationTest.java
 * @run main  SerializationTest
 */

package javax.rmi.CORBA.serialization;

import javax.rmi.CORBA.serialization.*;
import javax.rmi.CORBA.*;
import rmic.ObjectByValue;

import java.io.*;

public class JRMPPerformance extends test.Test
{

    public void run()
    {
        
        try {        

	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream sos = new ObjectOutputStream(bos);

	    TestOBV2 tobv2 = new TestOBV2();
	    sos.writeObject(tobv2);

            Exception exception = new Exception("Test Exception");
            sos.writeObject(exception);

            Float f = new Float(1.23);
            sos.writeObject(f);

            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.writeObject(test1);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwo test2 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwo();
            sos.writeObject(test2);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass test2subclass =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass();
            sos.writeObject(test2subclass);

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults test2subclassDefaults =
                new javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults();
            sos.writeObject(test2subclassDefaults);

            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyA=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.writeObject(testEmptyA);

            javax.rmi.CORBA.serialization.EmptyTestObject testEmptyB=
                new javax.rmi.CORBA.serialization.EmptyTestObject();
            sos.writeObject(testEmptyB);

            javax.rmi.CORBA.serialization.ComplexTestObjectOne test1b =
                new javax.rmi.CORBA.serialization.ComplexTestObjectOne();
            sos.writeObject(test1b);

            Double d = new Double(3.5);
            sos.writeObject(d);

            javax.rmi.CORBA.serialization.ComplexTestObjectThree test3 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectThree();
            sos.writeObject(test3);

            javax.rmi.CORBA.serialization.ComplexTestObjectFour test4 =
                new javax.rmi.CORBA.serialization.ComplexTestObjectFour();
            sos.writeObject(test4);

	    java.util.Properties props = new java.util.Properties();
            props.put("Key1","Value1");
            sos.writeObject(props);

	    // Test arrays
            int anIntArray[] = {9,8,7};
            sos.writeObject(anIntArray);

            Object aSharedRefsArray[] = { testEmptyA, test1b, anIntArray };
            sos.writeObject( aSharedRefsArray );

            // Check single dimensional primitive array...
            
            int[] array1 = {0,5,7,9,11,13};
            sos.writeObject(array1);


            // Check 2 dimensional primitive array...

            long[][] array2 =   {
		{9,8,7,6,1},
		{18,4,6},
		{0,5,7,9,11,13}
	    };
            sos.writeObject(array2);

	    // Recursive array references
	    Object recursiveArray[] = {null, "Hello", null, null};
	    recursiveArray[0] = recursiveArray;
	    recursiveArray[2] = recursiveArray;
	    recursiveArray[3] = recursiveArray;
	    sos.writeObject(recursiveArray);



            // Check 3 dimensional primitive array...
            short[][][] dim3 = {
		{
		    {0,8,7,6,1},
		    {1,5,7,13},
		    {2,4,6},
		},
		{
		    {3,4,10},
		    {4,5,7,9,13}
		},
		{
		    {5,4,6},
		    {6,5,8,9,11,13},
		    {7,8,7,6,1},
		    {8,8,7,6,9},
		},
	    };
            sos.writeObject(dim3);

            // Check single dimensional object array...
    	    
            ObjectByValue[] array3 =    {
		new ObjectByValue(5,10,"a","f"),
		new ObjectByValue(6,11,"b","g"),
		new ObjectByValue(7,12,"c","h"),
		new ObjectByValue(8,13,"d","i"),
		new ObjectByValue(9,14,"e","j"),
	    };
            sos.writeObject(array3);      
            
            // Check multi dimensional object array...

            ObjectByValue[][] array4 =   {   {
		new ObjectByValue(0,10,"a","g"),
		new ObjectByValue(0,11,"b","h"),
		new ObjectByValue(0,12,"c","i"),
	    },
					     {
						 new ObjectByValue(1,13,"d","j"),
						 new ObjectByValue(1,14,"e","k"),
					     },
					     {
						 new ObjectByValue(2,15,"f","l"),
					     }
	    };
                                
            sos.writeObject(array4);

            /***************************************************************/
            /*********************** READ DATA BACK IN *********************/
            /***************************************************************/

            ObjectInputStream sis = 
		new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
														  

	    TestOBV2 _tobv2 = (TestOBV2)sis.readObject();
			
            Exception _exception = (Exception)sis.readObject();
            if (!_exception.getMessage().equals("Test Exception"))
                throw new Error("Test Exception failed!");

            Float _f = (Float)sis.readObject();
            if (!f.equals(_f))
                throw new Error("Test Float failed!");

            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.readObject();

            if (test1.equals(_test1))
                ;
            else
                throw new Error("FAILURE!  Test1 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwo _test2 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwo)sis.readObject();

            if (test2.equals(_test2))
                ;
            else
                throw new Error("FAILURE!  Test2 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass _test2subclass =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclass)sis.readObject();

            if (test2subclass.equals(_test2subclass))
                ;
            else
                throw new Error("FAILURE!  Test2subclass Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults _test2subclassDefaults =
                (javax.rmi.CORBA.serialization.ComplexTestObjectTwoSubclassDefaults)sis.readObject();

            if (test2subclassDefaults.equals(_test2subclassDefaults))
                ;
            else
                throw new Error("FAILURE!  Test2subclassDefaults Failed");

            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyA =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.readObject();

            if (testEmptyA.equals(_testEmptyA))
                ;
            else
                throw new Error("FAILURE!  TestEmptyA Failed");

            javax.rmi.CORBA.serialization.EmptyTestObject _testEmptyB =
                (javax.rmi.CORBA.serialization.EmptyTestObject)sis.readObject();

            if (testEmptyB.equals(_testEmptyB))
                ;
            else
                throw new Error("FAILURE!  TestEmptyB Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectOne _test1b =
                (javax.rmi.CORBA.serialization.ComplexTestObjectOne)sis.readObject();

            if (test1b.equals(_test1b))
                ;
            else
                throw new Error("FAILURE!  Test1b Failed");

            Double _d = (Double)sis.readObject();

            if (d.equals(_d))
                ;
            else
                throw new Error("FAILURE!  d Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectThree _test3 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectThree)sis.readObject();

            if (test3.equals(_test3))
                ;
            else
                throw new Error("FAILURE!  Test3 Failed");

            javax.rmi.CORBA.serialization.ComplexTestObjectFour _test4 =
                (javax.rmi.CORBA.serialization.ComplexTestObjectFour)sis.readObject();

            if (test4.equals(_test4))
                ;
            else
                throw new Error("FAILURE!  Test4 Failed");

	    java.util.Properties _props = (java.util.Properties)sis.readObject();
            if (_props.toString().equals(props.toString()))
            	;
            else
            	throw new Error("FAILURE!  props Failed");


            int _anIntArray[] = (int[])sis.readObject();

            if ((_anIntArray[0] == anIntArray[0]) &&
            	(_anIntArray[1] == anIntArray[1]) &&
                (_anIntArray[2] == anIntArray[2]))
                ;
            else
            	throw new Error("FAILURE!  anIntArray Failed");

            Object _aSharedRefsArray[] = (Object[])sis.readObject();

            if ((_aSharedRefsArray[0] == _testEmptyA) &&
            	(_aSharedRefsArray[1] == _test1b) &&
                (_aSharedRefsArray[2] == _anIntArray))
                ;
            else
            	throw new Error("FAILURE!  aSharedRefsArray[] == Failed");

            int[] array1Echo = (int[])sis.readObject();

            for (int i = 0; i < array1.length; i++) {
                if (array1[i] != array1Echo[i]) {
    	            throw new Exception("HelloTest: echoArray (int[]) failed");
                }
            }

            long[][] array2Echo = (long[][])sis.readObject();
            
            for (int i = 0; i < array2.length; i++) {
                for (int j = 0; j < array2[i].length; j++) {
                    if (array2[i][j] != array2Echo[i][j]) {
			throw new Exception("HelloTest: echoArray (int[][]) failed");
                    }
                }
            }

	    // Recursive array references
	    Object recursiveArrayEcho[] = (Object[])sis.readObject();
	    if ((recursiveArrayEcho[0] != recursiveArrayEcho) || 
		(recursiveArrayEcho[2] != recursiveArrayEcho) || 
		(recursiveArrayEcho[3] != recursiveArrayEcho) || 
		(!((String)recursiveArrayEcho[1]).equals("Hello")))
		throw new Exception("RecursiveArray test failed!");

            short[][][] dim3Echo = (short[][][])sis.readObject();
            
            for (int i = 0; i < dim3.length; i++) {
                for (int j = 0; j < dim3[i].length; j++) {
                    for (int k = 0; k < dim3[i][j].length; k++) {
                        if (dim3[i][j][k] != dim3[i][j][k]) {
            	            throw new Exception("HelloTest: echoArray (short[][][]) failed");
                        }
                    }
                }
            }

            ObjectByValue[] array3Echo = (ObjectByValue[])sis.readObject();
            for (int i = 0; i < array3.length; i++) {
                if (!array3[i].equals(array3Echo[i])) {
    	            throw new Exception("HelloTest: echoArray (ObjectByValue[]) failed");
                }
            }

            ObjectByValue[][] array4Echo = (ObjectByValue[][])sis.readObject();
      
            for (int i = 0; i < array4.length; i++) {
                for (int j = 0; j < array4[i].length; j++) {
                    if (!array4[i][j].equals(array4Echo[i][j])) {
			throw new Exception("HelloTest: echoArray (ObjectByValue[][]) failed");
                    }
                }
            }
 

        }
        catch(Throwable e)
	    {
		status = new Error(e.getMessage());
		e.printStackTrace();
	    }
    }

}
