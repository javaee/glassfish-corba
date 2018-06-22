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

package tools.ior;

import java.lang.reflect.*;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.IOP.Codec;

/**
 * If you have a Helper class which will read the type of
 * thing you want to map from a tag, you don't have to
 * write an EncapsHandler, just specify the helper
 * in the appropriate setup file.
 */
public class TagHelperHandler implements EncapsHandler
{
    private TypeCode typeCode;
    private Method extractMethod;
    private Codec codec;

    private static final Class[] EXTRACT_ARG_TYPES
        = new Class[] { org.omg.CORBA.Any.class };

    // Surely these are already defined somewhere
    private static final Class[] NO_ARG_TYPES = new Class[] {};
    private static final Object[] NO_ARGS = new Object[] {};

    public TagHelperHandler(String helperClassName, Codec codec)
        throws ClassNotFoundException, 
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException,
               NoSuchMethodException,
               SecurityException {

        // This codec was indicated in the setup file, or
        // defaulted to the GIOP 1.0 CDR Encapsulation Codec.
        this.codec = codec;

        // Find the indicated helper class so we can get the
        // desired type's TypeCode as well as the helper's
        // extract method.
        Class helper = Class.forName(helperClassName);

        typeCode
            = (TypeCode)helper.getDeclaredMethod("type", 
                                                 NO_ARG_TYPES).invoke(null, 
                                                                      NO_ARGS);

        extractMethod
            = helper.getDeclaredMethod("extract", EXTRACT_ARG_TYPES);
    }

    public void display(byte[] data,
                        TextOutputHandler out,
                        Utility util)
        throws DecodingException {

        try {

            out.output("type: " + typeCode.id());

            // Decode using the TypeCode from the helper.
            Any any = codec.decode_value(data, typeCode);

            // Have the helper extract the desired type from
            // the any.
            java.lang.Object value = extractMethod.invoke(null, 
                                                          new Object[] { any });

            // Recursively display the type via reflection.
            util.recursiveDisplay("data", value, out);

        } catch (Exception ex) {
            throw new DecodingException(ex.getMessage());
        }
    }
}
