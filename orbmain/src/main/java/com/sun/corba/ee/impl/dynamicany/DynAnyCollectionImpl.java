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

package com.sun.corba.ee.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.DynamicAny.*;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

import com.sun.corba.ee.spi.orb.ORB ;

abstract class DynAnyCollectionImpl extends DynAnyConstructedImpl
{
    private static final long serialVersionUID = -4420130353899323070L;

    //
    // Instance variables
    //

    // Keep in sync with DynAny[] components at all times.
    Any[] anys = null;

    //
    // Constructors
    //

    protected DynAnyCollectionImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    protected DynAnyCollectionImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }

    //
    // Utility methods
    //

    protected void createDefaultComponentAt(int i, TypeCode contentType) {
        try {
            components[i] = DynAnyUtil.createMostDerivedDynAny(contentType, orb);
        } catch (InconsistentTypeCode itc) { // impossible
        }
        // get a hold of the default initialized Any without copying
        anys[i] = getAny(components[i]);
    }

    protected TypeCode getContentType() {
        try {
            return any.type().content_type();
        } catch (BadKind badKind) { // impossible
            return null;
        }
    }

    // This method has a different meaning for sequence and array:
    // For sequence value of 0 indicates an unbounded sequence,
    // values > 0 indicate a bounded sequence.
    // For array any value indicates the boundary.
    protected int getBound() {
        try {
            return any.type().length();
        } catch (BadKind badKind) { // impossible
            return 0;
        }
    }

    //
    // DynAny interface methods
    //

    // _REVISIT_ More efficient copy operation

    //
    // Collection methods
    //

    public org.omg.CORBA.Any[] get_elements () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? anys : null);
    }

    protected abstract void checkValue(Object[] value)
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue;

    // Initializes the elements of the ordered collection.
    // If value does not contain the same number of elements as the array dimension,
    // the operation raises InvalidValue.
    // If one or more elements have a type that is inconsistent with the collections TypeCode,
    // the operation raises TypeMismatch.
    // This operation does not change the current position.
    public void set_elements (org.omg.CORBA.Any[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkValue(value);

        components = new DynAny[value.length];
        anys = value;

        // We know that this is of kind tk_sequence or tk_array
        TypeCode expectedTypeCode = getContentType();
        for (int i=0; i<value.length; i++) {
            if (value[i] != null) {
                if (! value[i].type().equal(expectedTypeCode)) {
                    clearData();
                    // _REVISIT_ More info
                    throw new TypeMismatch();
                }
                try {
                    // Creates the appropriate subtype without copying the Any
                    components[i] = DynAnyUtil.createMostDerivedDynAny(value[i], orb, false);
                    //System.out.println(this + " created component " + components[i]);
                } catch (InconsistentTypeCode itc) {
                    throw new InvalidValue();
                }
            } else {
                clearData();
                // _REVISIT_ More info
                throw new InvalidValue();
            }
        }
        index = (value.length == 0 ? NO_INDEX : 0);
        // Other representations are invalidated by this operation
        representations = REPRESENTATION_COMPONENTS;
    }

    public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any () {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        return (checkInitComponents() ? components : null);
    }

    // Same semantics as set_elements(Any[])
    public void set_elements_as_dyn_any (org.omg.DynamicAny.DynAny[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
            throw wrapper.dynAnyDestroyed() ;
        }
        checkValue(value);

        components = (value == null ? emptyComponents : value);
        if (value != null) {
            anys = new Any[value.length];

            // We know that this is of kind tk_sequence or tk_array
            TypeCode expectedTypeCode = getContentType();
            for (int i=0; i<value.length; i++) {
                if (value[i] != null) {
                    if (! value[i].type().equal(expectedTypeCode)) {
                        clearData();
                        // _REVISIT_ More info
                        throw new TypeMismatch();
                    }
                    anys[i] = getAny(value[i]);
                } else {
                    clearData();
                    // _REVISIT_ More info
                    throw new InvalidValue();
                }
            }
            index = (value.length == 0 ? NO_INDEX : 0);
        }
        // Other representations are invalidated by this operation
        representations = REPRESENTATION_COMPONENTS;
    }
}
