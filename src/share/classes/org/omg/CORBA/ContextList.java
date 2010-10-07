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

package org.omg.CORBA;

/**
 * An object containing a modifiable list of <code>String</code> objects
 * that represent property names.
 * This class is used in <code>Request</code> operations to
 * describe the contexts that need to be resolved and sent with the
 * invocation.  (A context is resolved by giving a property name
 * and getting back the value associated with it.)  This is done
 * by calling the <code>Context</code> method
 * <code>get_values</code> and supplying a string from a
 * <code>ContextList</code> object as the third parameter.
 * The method <code>get_values</code> returns an <code>NVList</code>
 * object containing the <code>NamedValue</code> objects that hold
 * the value(s) identified by the given string.
 * <P>
 * A <code>ContextList</code> object is created by the ORB, as
 * illustrated here:
 * <PRE>
 *   ORB orb = ORB.init(args, null);
 *   org.omg.CORBA.ContextList ctxList = orb.create_context_list();
 * </PRE>
 * The variable <code>ctxList</code> represents an empty
 * <code>ContextList</code> object.  Strings are added to
 * the list with the method <code>add</code>, accessed
 * with the method <code>item</code>, and removed with the
 * method <code>remove</code>.
 *
 * @see Context
 * @version 1.2, 09/09/97
 * @since   JDK1.2
 */

public abstract class ContextList {

    /**
     * Returns the number of <code>String</code> objects in this
     * <code>ContextList</code> object.
     *
     * @return			an <code>int</code> representing the number of
     * <code>String</code>s in this <code>ContextList</code> object
     */

    public abstract int count();

    /**
     * Adds a <code>String</code> object to this <code>ContextList</code>
     * object.
     *
     * @param ctx		the <code>String</code> object to be added
     */

    public abstract void add(String ctx);

    /**
     * Returns the <code>String</code> object at the given index.
     *
     * @param index		the index of the string desired, with 0 being the
     index of the first string
     * @return			the string at the given index
     * @exception org.omg.CORBA.Bounds  if the index is greater than
     *				or equal to the number of strings in this
     *                <code>ContextList</code> object
     */

    public abstract String item(int index) throws org.omg.CORBA.Bounds;

    /**
     * Removes the <code>String</code> object at the given index. Note that
     * the indices of all strings following the one removed are
     * shifted down by one.
     *
     * @param index	the index of the <code>String</code> object to be removed,
     *                with 0 designating the first string
     * @exception org.omg.CORBA.Bounds  if the index is greater than
     *				or equal to the number of <code>String</code> objects in
     *                this <code>ContextList</code> object
     */

    public abstract void remove(int index) throws org.omg.CORBA.Bounds;

}
