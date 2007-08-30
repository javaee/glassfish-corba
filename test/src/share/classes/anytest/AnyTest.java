/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
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
/*****************************************************************************/
/*                    Copyright (c) IBM Corporation 1998                     */
/*                                                                           */
/* IBM Confidential                                             AnyTest.java */
/*                                                                           */
/* OCO Source Materials                                                      */
/*                                                                           */
/* (C) Copyright IBM Corp. 1998                                              */
/*                                                                           */
/* The source code for this program is not published or otherwise            */
/* divested of its trade secrets, irrespective of what has been              */
/* deposited with the U.S. Copyright Office.                                 */
/*                                                                           */
/*****************************************************************************/


package anytest;

import org.omg.CORBA.*;
import com.sun.corba.se.impl.corba.*;
import javax.rmi.CORBA.serialization.*;
import javax.rmi.CORBA.*;
import rmic.ObjectByValue;

import java.io.*;
import java.util.*;

public class AnyTest extends test.Test
{

    public void run()
    {
        try
	    {
		org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(getArgsAsArgs(),null);
	        Any any1 = orb.create_any();
	        Any any2 = orb.create_any();

		//
		// Equality tests
		//

		// null
	        if (!any1.equal(any2))
		    throw new Error ("Failed null equality test!");

		// short
	        short shortData = Short.MAX_VALUE;
	        any1.insert_short(shortData);
	        any2.insert_short(shortData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed short equality test!");

		// ushort
	        short ushortData = (short) -1;
	        any1.insert_ushort(ushortData);
	        any2.insert_ushort(ushortData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed ushort equality test!");

		// long
	        int longData = Integer.MAX_VALUE;
	        any1.insert_long(longData);
	        any2.insert_long(longData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed long equality test!");

		// ulong
	        int ulongData = -1;
	        any1.insert_ulong(ulongData);
	        any2.insert_ulong(ulongData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed ulong equality test!");

		// longlong
	        long longlongData = Long.MAX_VALUE;
	        any1.insert_longlong(longlongData);
	        any2.insert_longlong(longlongData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed longlong equality test!");

		// ulonglong
	        long ulonglongData = -1L;
	        any1.insert_ulonglong(ulonglongData);
	        any2.insert_ulonglong(ulonglongData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed ulonglong equality test!");

		// float
	        float floatData = Float.MAX_VALUE;
	        any1.insert_float(floatData);
	        any2.insert_float(floatData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed float equality test!");

		// double
	        double doubleData = Double.MAX_VALUE;
	        any1.insert_double(doubleData);
	        any2.insert_double(doubleData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed double equality test!");

		// char
	        char charData = Character.MAX_VALUE;
	        any1.insert_char(charData);
	        any2.insert_char(charData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed char equality test!");

		// octet
	        byte octetData = Byte.MAX_VALUE;
	        any1.insert_octet(octetData);
	        any2.insert_octet(octetData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed octet equality test!");

		// any
	        Any anyData =  orb.create_any();
	        anyData.insert_octet(octetData);
	        any1.insert_any(anyData);
	        any2.insert_any(anyData);
          
	        if (!any1.equal(any2))
		    throw new Error ("Failed any equality test!");

		// TypeCode
	        TypeCode typeCodeData = anyData.type();
	        any1.insert_TypeCode(typeCodeData);
	        any2.insert_TypeCode(typeCodeData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed TypeCode equality test!");

		// string
	        String stringData = "stringData";
	        any1.insert_string(stringData);
	        any2.insert_string(stringData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed string equality test!");

		/*
		  org.omg.CORBA.Object objrefData = anyObj;
		  any1.insert_Object(objrefData);
		  any2.insert_Object(objrefData);

		  if (!any1.equal(any2))
		  throw new Error ("Failed objref equality test!");
		*/

	        enum1 enumData = enum1.zeroth;
	        enum1Helper.insert(any1, enumData);
	        enum1Helper.insert(any2, enumData);

	        if (!any1.equal(any2))
		    throw new Error ("Failed enum equality test!");

	    }
        catch(Throwable e)
	    {
		status = new Error(e.getMessage());
		e.printStackTrace();
	    }
    }

}
