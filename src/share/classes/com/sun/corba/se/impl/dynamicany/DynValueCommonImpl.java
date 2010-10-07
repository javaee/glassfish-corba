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

package com.sun.corba.se.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;

import com.sun.corba.se.spi.orb.ORB ;
import org.omg.DynamicAny.DynValueCommon;

abstract class DynValueCommonImpl extends DynAnyComplexImpl implements DynValueCommon
{
    private static final long serialVersionUID = -6538058649606934141L;
    //
    // Constructors
    //

    protected boolean isNull;

    private DynValueCommonImpl() {
        this(null, (Any)null, false);
        isNull = true;
    }

    protected DynValueCommonImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
        isNull = checkInitComponents();
    }

    protected DynValueCommonImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
        isNull = true;
    }

    //
    // DynValueCommon methods
    //

    // Returns TRUE if this object represents a null valuetype
    public boolean is_null() {
        return isNull;
    }

    // Changes the representation to a null valuetype.
    public void set_to_null() {
        isNull = true;
        clearData();
    }

    // If this object represents a null valuetype then this operation
    // replaces it with a newly constructed value with its components 
    // initialized to default values as in DynAnyFactory::create_dyn_any_from_type_code.
    // If this object represents a non-null valuetype, then this operation has no effect. 
    public void set_to_value() {
        if (isNull) {
            isNull = false;
            // the rest is done lazily
        }
        // else: there is nothing to do
    }

    //
    // Methods differing from DynStruct
    //

    // Required to raise InvalidValue if this is a null value type.
    public org.omg.DynamicAny.NameValuePair[] get_members ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.dynAnyDestroyed() ;
        }
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return nameValuePairs;
    }

    // Required to raise InvalidValue if this is a null value type.
    public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any ()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (status == STATUS_DESTROYED) {
	    throw wrapper.dynAnyDestroyed() ;
        }
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return nameDynAnyPairs;
    }

    //
    // Overridden methods
    //

    // Overridden to change to non-null status.
    @Override
    public void set_members (org.omg.DynamicAny.NameValuePair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        super.set_members(value);
        // If we didn't get an exception then this must be a valid non-null value
        isNull = false;
    }

    // Overridden to change to non-null status.
    @Override
    public void set_members_as_dyn_any (org.omg.DynamicAny.NameDynAnyPair[] value)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch,
               org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        super.set_members_as_dyn_any(value);
        // If we didn't get an exception then this must be a valid non-null value
        isNull = false;
    }
}
