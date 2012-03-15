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

import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

/**
 * The Holder for <tt>String</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * A Holder class for a <code>String</code>
 * that is used to store "out" and "inout" parameters in IDL operations.
 * If an IDL operation signature has an IDL <code>string</code> as an "out"
 * or "inout" parameter, the programmer must pass an instance of
 * <code>StringHolder</code> as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the
 * value corresponding to the "out" value returned from the server.
 * <P>
 * If <code>myStringHolder</code> is an instance of <code>StringHolder</code>,
 * the value stored in its <code>value</code> field can be accessed with
 * <code>myStringHolder.value</code>.
 *
 * @version     1.14, 09/09/97
 * @since       JDK1.2
 */
public final class StringHolder implements Streamable {

    /**
     * The <code>String</code> value held by this <code>StringHolder</code>
     * object.
     */
    public String value;

    /**
     * Constructs a new <code>StringHolder</code> object with its
     * <code>value</code> field initialized to <code>null</code>.
     */
    public StringHolder() {
    }

    /**
     * Constructs a new <code>StringHolder</code> object with its
     * <code>value</code> field initialized to the given
     * <code>String</code>.
     * @param initial the <code>String</code> with which to initialize
     *                the <code>value</code> field of the newly-created
     *                <code>StringHolder</code> object
     */
    public StringHolder(String initial) {
        value = initial;
    }

    /**
     * Reads the unmarshalled data from <code>input</code> and assigns it to
     * the <code>value</code> field of this <code>StringHolder</code> object.
     *
     * @param input the InputStream containing CDR formatted data from the wire.
     */
    public void _read(InputStream input) {
        value = input.read_string();
    }

    /**
     * Marshals the value held by this <code>StringHolder</code> object
     * to the output stream  <code>output</code>.
     *
     * @param output the OutputStream which will contain the CDR formatted data.
     */
    public void _write(OutputStream output) {
        output.write_string(value);
    }

    /**
     * Retrieves the <code>TypeCode</code> object that corresponds to
     * the value held in this <code>StringHolder</code> object.
     *
     * @return    the type code of the value held in this <code>StringHolder</code>
     *            object
     */
    public org.omg.CORBA.TypeCode _type() {
        return ORB.init().get_primitive_tc(TCKind.tk_string);
    }
}
