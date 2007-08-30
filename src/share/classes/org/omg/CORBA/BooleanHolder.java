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

import org.omg.CORBA.portable.Streamable;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;


/**
 * The Holder for <tt>Boolean</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * A Holder class for a <code>boolean</code>
 * that is used to store "out" and "inout" parameters in IDL methods.
 * If an IDL method signature has an IDL <code>boolean</code> as an "out"
 * or "inout" parameter, the programmer must pass an instance of
 * <code>BooleanHolder</code> as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the
 * value corresponding to the "out" value returned from the server.
 * <P>
 * If <code>myBooleanHolder</code> is an instance of <code>BooleanHolder</code>,
 * the value stored in its <code>value</code> field can be accessed with
 * <code>myBooleanHolder.value</code>.
 *
 * @version	1.14, 09/09/97
 * @since       JDK1.2
 */
public final class BooleanHolder implements Streamable {

    /**
     * The <code>boolean</code> value held by this <code>BooleanHolder</code>
     * object.
     */
    public boolean value;

    /**
     * Constructs a new <code>BooleanHolder</code> object with its
     * <code>value</code> field initialized to <code>false</code>.
     */
    public BooleanHolder() {
    }

    /**
     * Constructs a new <code>BooleanHolder</code> object with its
     * <code>value</code> field initialized with the given <code>boolean</code>.
     * @param initial the <code>boolean</code> with which to initialize
     *                the <code>value</code> field of the newly-created
     *                <code>BooleanHolder</code> object
     */
    public BooleanHolder(boolean initial) {
	value = initial;
    }

    /**
     * Reads unmarshalled data from <code>input</code> and assigns it to this
     * <code>BooleanHolder</code> object's <code>value</code> field.
     *
     * @param input the <code>InputStream</code> object containing
     *              CDR formatted data from the wire
     */
    public void _read(InputStream input) {
	value = input.read_boolean();
    }

    /**
     * Marshals the value in this <code>BooleanHolder</code> object's
     * <code>value</code> field to the output stream <code>output</code>.
     *
     * @param output the OutputStream which will contain the CDR formatted data
     */
    public void _write(OutputStream output) {
	output.write_boolean(value);
    }

    /**
     * Retrieves the <code>TypeCode</code> object that corresponds to the 
     * value held in this <code>BooleanHolder</code> object.
     *
     * @return    the <code>TypeCode</code> for the value held 
     *            in this <code>BooleanHolder</code> object
     */
    public TypeCode _type() {
	return ORB.init().get_primitive_tc(TCKind.tk_boolean);
    }
}
