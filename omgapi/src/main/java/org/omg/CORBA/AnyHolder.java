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

import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * The Holder for <tt>Any</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * A Holder class for <code>Any</code> objects
 * that is used to store "out" and "inout" parameters in IDL methods.
 * If an IDL method signature has an IDL <code>any</code> as an "out"
 * or "inout" parameter, the programmer must pass an instance of
 * <code>AnyHolder</code> as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the
 * value corresponding to the "out" value returned from the server.
 * <P>
 * If <code>myAnyHolder</code> is an instance of <code>AnyHolder</code>,
 * the value stored in its <code>value</code> field can be accessed with
 * <code>myAnyHolder.value</code>.
 *
 * @version     1.14, 09/09/97
 * @since       JDK1.2
 */
public final class AnyHolder implements  Streamable {
    /**
     * The <code>Any</code> value held by this <code>AnyHolder</code> object.
     */

    public Any value;

    /**
     * Constructs a new <code>AnyHolder</code> object with its
     * <code>value</code> field initialized to <code>null</code>.
     */
    public AnyHolder() {
    }

    /**
     * Constructs a new <code>AnyHolder</code> object for the given
     * <code>Any</code> object.
     * @param initial the <code>Any</code> object with which to initialize
     *                the <code>value</code> field of the new
     *                <code>AnyHolder</code> object
     */
    public AnyHolder(Any initial) {
        value = initial;
    }

    /**
     * Reads from <code>input</code> and initalizes the value in the Holder
     * with the unmarshalled data.
     *
     * @param input the InputStream containing CDR formatted data from the wire.
     */
    public void _read(InputStream input) {
        value = input.read_any();
    }

    /**
     * Marshals to <code>output</code> the value in
     * this <code>AnyHolder</code> object.
     *
     * @param output the OutputStream which will contain the CDR formatted data.
     */
    public void _write(OutputStream output) {
        output.write_any(value);
    }

    /**
     * Returns the <code>TypeCode</code> object corresponding to the value 
     * held in this <code>AnyHolder</code> object.
     *
     * @return    the TypeCode of the value held in 
     *              this <code>AnyHolder</code> object
     */
    public TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_any);
    }
}
