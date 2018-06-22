/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1998-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.glassfish.rmic.iiop;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Vector;

/**
 * ArrayType is a wrapper for any of the other types. The getElementType()
 * method can be used to get the array element type.  The getArrayDimension()
 * method can be used to get the array dimension.
 *
 * @author      Bryan Atsatt
 */
public class ArrayType extends Type {

    private Type type;
    private int arrayDimension;
    private String brackets;
    private String bracketsSig;

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an ArrayType object for the given type.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static ArrayType forArray(   org.glassfish.rmic.tools.java.Type theType,
                                        ContextStack stack) {


        ArrayType result = null;
        org.glassfish.rmic.tools.java.Type arrayType = theType;

        if (arrayType.getTypeCode() == TC_ARRAY) {

            // Find real type...

            while (arrayType.getTypeCode() == TC_ARRAY) {
                arrayType = arrayType.getElementType();
            }

            // Do we already have it?

            Type existing = getType(theType,stack);
            if (existing != null) {

                if (!(existing instanceof ArrayType)) return null; // False hit.

                                // Yep, so return it...

                return (ArrayType) existing;
            }

            // Now try to make a Type from it...

            Type temp = CompoundType.makeType(arrayType,null,stack);

            if (temp != null) {

                                // Got a valid one. Make an array type...

                result = new ArrayType(stack,temp,theType.getArrayDimension());

                                // Add it...

                putType(theType,result,stack);

                // Do the stack thing in case tracing on...

                stack.push(result);
                stack.pop(true);
            }
        }

        return result;
    }

    /**
     * Return signature for this type  (e.g. com.acme.Dynamite
     * would return "com.acme.Dynamite", byte = "B")
     */
    public String getSignature() {
        return bracketsSig + type.getSignature();
    }

    /**
     * Get element type. Returns null if not an array.
     */
    public Type getElementType () {
        return type;
    }

    /**
     * Get array dimension. Returns zero if not an array.
     */
    public int getArrayDimension () {
        return arrayDimension;
    }

    /**
     * Get brackets string. Returns "" if not an array.
     */
    public String getArrayBrackets () {
        return brackets;
    }

    /**
     * Return a string representation of this type.
     */
    public String toString () {
        return getQualifiedName() + brackets;
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Array of " + type.getTypeDescription();
    }


    /**
     * Return the name of this type. For arrays, will include "[]" if useIDLNames == false.
     * @param useQualifiedNames If true, print qualified names; otherwise, print unqualified names.
     * @param useIDLNames If true, print IDL names; otherwise, print java names.
     * @param globalIDLNames If true and useIDLNames true, prepends "::".
     */
    public String getTypeName ( boolean useQualifiedNames,
                                boolean useIDLNames,
                                boolean globalIDLNames) {
        if (useIDLNames) {
            return super.getTypeName(useQualifiedNames,useIDLNames,globalIDLNames);
        } else {
            return super.getTypeName(useQualifiedNames,useIDLNames,globalIDLNames) + brackets;
        }
    }

    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________


    /**
     * Convert all invalid types to valid ones.
     */
    protected void swapInvalidTypes () {
        if (type.getStatus() != STATUS_VALID) {
            type = getValidType(type);
        }
    }

    /*
     * Add matching types to list. Return true if this type has not
     * been previously checked, false otherwise.
     */
    protected boolean addTypes (int typeCodeFilter,
                                HashSet checked,
                                Vector matching) {

        // Check self.

        boolean result = super.addTypes(typeCodeFilter,checked,matching);

        // Have we been checked before?

        if (result) {

            // No, so add element type...

            getElementType().addTypes(typeCodeFilter,checked,matching);
        }

        return result;
    }

    /**
     * Create an ArrayType instance for the given type.  The resulting
     * object is not yet completely initialized.
     */
    private ArrayType(ContextStack stack, Type type, int arrayDimension) {
        super(stack,TYPE_ARRAY);
        this.type = type;
        this.arrayDimension = arrayDimension;

        // Create our brackets string...

        brackets = "";
        bracketsSig = "";
        for (int i = 0; i < arrayDimension; i ++) {
            brackets += "[]";
            bracketsSig += "[";
        }

        // Now set our names...

        String idlName = IDLNames.getArrayName(type,arrayDimension);
        String[] module = IDLNames.getArrayModuleNames(type);
        setNames(type.getIdentifier(),module,idlName);

        // Set our repositoryID...

        setRepositoryID();
    }


    /*
     * Load a Class instance. Return null if fail.
     */
    protected Class loadClass() {
        Class result = null;
        Class elementClass = type.getClassInstance();
        if (elementClass != null) {
            result = Array.newInstance(elementClass, new int[arrayDimension]).getClass();
        }
        return result;
    }

    /**
     * Release all resources
     */
    protected void destroy () {
        super.destroy();
        if (type != null) {
            type.destroy();
            type = null;
        }
        brackets = null;
        bracketsSig = null;
    }
}
