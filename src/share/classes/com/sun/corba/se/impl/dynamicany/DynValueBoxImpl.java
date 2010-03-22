/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;
import org.omg.DynamicAny.DynAnyPackage.TypeMismatch;
import org.omg.DynamicAny.DynAnyPackage.InvalidValue;
import org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode;

import com.sun.corba.se.spi.orb.ORB ;
import org.omg.DynamicAny.DynAny;
import org.omg.DynamicAny.DynValueBox;

public class DynValueBoxImpl extends DynValueCommonImpl implements DynValueBox
{
    private static final long serialVersionUID = 670401668768259219L;
    //
    // Constructors
    //

    private DynValueBoxImpl() {
        this(null, (Any)null, false);
    }

    protected DynValueBoxImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    protected DynValueBoxImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }

    //
    // DynValueBox methods
    //

    public Any get_boxed_value()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitAny();
        return any;
    }

    public void set_boxed_value(org.omg.CORBA.Any boxed)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ! isNull && ! boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        any = boxed;
        representations = REPRESENTATION_ANY;
        index = 0;
        isNull = false;
    }

    public DynAny get_boxed_value_as_dyn_any()
        throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
    {
        if (isNull) {
            throw new InvalidValue();
        }
        checkInitComponents();
        return components[0];
    }

    public void set_boxed_value_as_dyn_any(DynAny boxed)
        throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
    {
        if ( ! isNull && ! boxed.type().equal(this.type())) {
            throw new TypeMismatch();
        }
        clearData();
        components = new DynAny[] {boxed};
        representations = REPRESENTATION_COMPONENTS;
        index = 0;
        isNull = false;
    }

    @Override
    protected boolean initializeComponentsFromAny() {
        try {
            components = new DynAny[] {DynAnyUtil.createMostDerivedDynAny(any, orb, false)};
        } catch (InconsistentTypeCode ictc) {
            return false; // impossible
        }
        return true;
    }

    @Override
    protected boolean initializeComponentsFromTypeCode() {
        try {
            any = DynAnyUtil.createDefaultAnyOfType(any.type(), orb);
            components = new DynAny[] {DynAnyUtil.createMostDerivedDynAny(any, orb, false)};
        } catch (InconsistentTypeCode ictc) {
            return false; // impossible
        }
        return true;
    }

    @Override
    protected boolean initializeAnyFromComponents() {
        any = getAny(components[0]);
        return true;
    }
}
