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

package org.omg.CORBA;

/**
 * An object used in the DII and DSI to describe
 * arguments and return values. <code>NamedValue</code> objects
 * are also used in the <code>Context</code>
 * object routines to pass lists of property names and values.
 * <P>
 * A <code>NamedValue</code> object contains:
 * <UL>
 * <LI>a name -- If the <code>NamedValue</code> object is used to
 * describe arguments to a request, the name will be an argument
 * identifier specified in the OMG IDL interface definition
 * for the operation being described.
 * <LI>a value -- an <code>Any</code> object
 * <LI>an argument mode flag -- one of the following:
 *   <UL>
 *    <LI><code>ARG_IN.value</code>
 *    <LI><code>ARG_OUT.value</code>
 *    <LI><code>ARG_INOUT.value</code>
 *    <LI>zero -- if this <code>NamedValue</code> object represents a property
 *                in a <code>Context</code> object rather than a parameter or
 *                return value
 *   </UL>
 * </UL>
 * <P>
 * The class <code>NamedValue</code> has three methods, which
 * access its fields.  The following code fragment demonstrates
 * creating a <code>NamedValue</code> object and then accessing
 * its fields:
 * <PRE>
 *    ORB orb = ORB.init(args, null);
 *    String s = "argument_1";
 *    org.omg.CORBA.Any myAny = orb.create_any();
 *    myAny.insert_long(12345);
 *    int in = org.omg.CORBA.ARG_IN.value;

 *    org.omg.CORBA.NamedValue nv = orb.create_named_value(
 *        s, myAny, in);
 *    System.out.println("This nv name is " + nv.name());
 *    try {
 *        System.out.println("This nv value is " + nv.value().extract_long());
 *        System.out.println("This nv flag is " + nv.flags());
 *    } catch (org.omg.CORBA.BAD_OPERATION b) {
 *      System.out.println("extract failed");
 *    }
 * </PRE>
 *
 * <P>
 * If this code fragment were put into a <code>main</code> method,
 * the output would be something like the following:
 * <PRE>
 *    This nv name is argument_1
 *    This nv value is 12345
 *    This nv flag is 1
 * </PRE>
 * <P>
 * Note that the method <code>value</code> returns an <code>Any</code>
 * object. In order to access the <code>long</code> contained in the
 * <code>Any</code> object,
 * we used the method <code>extract_long</code>.
 *
 * @see Any
 * @see ARG_IN
 * @see ARG_INOUT
 * @see ARG_OUT
 *
 * @version 1.12 ,09/09/97
 * @since       JDK1.2
 */

public abstract class NamedValue {

    /**
     * Retrieves the name for this <code>NamedValue</code> object.
     *
     * @return                  a <code>String</code> object representing
     *                    the name of this <code>NamedValue</code> object
     */

    public abstract String name();

    /**
     * Retrieves the value for this <code>NamedValue</code> object.
     *
     * @return                  an <code>Any</code> object containing
     *                    the value of this <code>NamedValue</code> object
     */

    public abstract Any value();

    /**
     * Retrieves the argument mode flag for this <code>NamedValue</code> object.
     *
     * @return                  an <code>int</code> representing the argument
     *                    mode for this <code>NamedValue</code> object
     */

    public abstract int flags();

}
