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
 * The Holder for <tt>Fixed</tt>.  For more information on 
 * Holder files, see <a href="doc-files/generatedfiles.html#holder">
 * "Generated Files: Holder Files"</a>.<P>
 * FixedHolder is a container class for values of IDL type "fixed",
 * which is mapped to the Java class java.math.BigDecimal.
 * It is usually used to store "out" and "inout" IDL method parameters.
 * If an IDL method signature has a fixed as an "out" or "inout" parameter,
 * the programmer must pass an instance of FixedHolder as the corresponding
 * parameter in the method invocation; for "inout" parameters, the programmer
 * must also fill the "in" value to be sent to the server.
 * Before the method invocation returns, the ORB will fill in the contained
 * value corresponding to the "out" value returned from the server.
 *
 * @version     1.14 09/09/97
 */
public final class FixedHolder implements Streamable {
    /**
     * The value held by the FixedHolder
     */
    public java.math.BigDecimal value;

    /**
     * Construct the FixedHolder without initializing the contained value.
     */
    public FixedHolder() {
    }

    /**
     * Construct the FixedHolder and initialize it with the given value.
     * @param initial the value used to initialize the FixedHolder
     */
    public FixedHolder(java.math.BigDecimal initial) {
        value = initial;
    }

    /**
     * Read a fixed point value from the input stream and store it in
     * the value member.
     *
     * @param input the <code>InputStream</code> to read from.
     */
    public void _read(InputStream input) {
	value = input.read_fixed();
    }

    /**
     * Write the fixed point value stored in this holder to an
     * <code>OutputStream</code>.
     *
     * @param output the <code>OutputStream</code> to write into.
     */
    public void _write(OutputStream output) {
	output.write_fixed(value);
    }

    
    /**
     * Return the <code>TypeCode</code> of this holder object.
     *
     * @return the <code>TypeCode</code> object. 
     */
    public org.omg.CORBA.TypeCode _type() {
	return ORB.init().get_primitive_tc(TCKind.tk_fixed);
    }

}
