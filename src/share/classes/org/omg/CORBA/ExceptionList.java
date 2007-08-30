/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.omg.CORBA;

/**
 * An object used in <code>Request</code> operations to
 * describe the exceptions that can be thrown by a method.  It maintains a
 * modifiable list of <code>TypeCode</code>s of the exceptions.
 * <P>
 * The following code fragment demonstrates creating
 * an <code>ExceptionList</code> object:
 * <PRE>
 *    ORB orb = ORB.init(args, null);
 *    org.omg.CORBA.ExceptionList excList = orb.create_exception_list();
 * </PRE>
 * The variable <code>excList</code> represents an <code>ExceptionList</code>
 * object with no <code>TypeCode</code> objects in it.
 * <P>
 * To add items to the list, you first create a <code>TypeCode</code> object
 * for the exception you want to include, using the <code>ORB</code> method
 * <code>create_exception_tc</code>.  Then you use the <code>ExceptionList</code>
 * method <code>add</code> to add it to the list.
 * The class <code>ExceptionList</code> has a method for getting
 * the number of <code>TypeCode</code> objects in the list, and  after
 * items have been added, it is possible to call methods for accessing
 * or deleting an item at a designated index.
 *
 * @version 1.13, 09/09/97
 * @since   JDK1.2
 */

public abstract class ExceptionList {

    /**
     * Retrieves the number of <code>TypeCode</code> objects in this
     * <code>ExceptionList</code> object.
     *
     * @return		the	number of <code>TypeCode</code> objects in this
     * <code>ExceptionList</code> object
     */

    public abstract int count();

    /**
     * Adds a <code>TypeCode</code> object describing an exception
     * to this <code>ExceptionList</code> object.
     *
     * @param exc			the <code>TypeCode</code> object to be added
     */

    public abstract void add(TypeCode exc);

    /**
     * Returns the <code>TypeCode</code> object at the given index.  The first
     * item is at index 0.
     *
     * @param index		the index of the <code>TypeCode</code> object desired.
     *                    This must be an <code>int</code> between 0 and the
     *                    number of <code>TypeCode</code> objects
     *                    minus one, inclusive.
     * @return			the <code>TypeCode</code> object  at the given index
     * @exception org.omg.CORBA.Bounds   if the index given is greater than
     *				or equal to the number of <code>TypeCode</code> objects
     *                in this <code>ExceptionList</code> object
     */

    public abstract TypeCode item(int index)
	throws org.omg.CORBA.Bounds;

    /**
     * Removes the <code>TypeCode</code> object at the given index.
     * Note that the indices of all the <code>TypeCoded</code> objects
     * following the one deleted are shifted down by one.
     *
     * @param index		the index of the <code>TypeCode</code> object to be
     *                    removed.
     *                    This must be an <code>int</code> between 0 and the
     *                    number of <code>TypeCode</code> objects
     *                    minus one, inclusive.
     *
     * @exception org.omg.CORBA.Bounds if the index is greater than
     *				or equal to the number of <code>TypeCode</code> objects
     *                in this <code>ExceptionList</code> object
     */

    public abstract void remove(int index)
	throws org.omg.CORBA.Bounds;
}
