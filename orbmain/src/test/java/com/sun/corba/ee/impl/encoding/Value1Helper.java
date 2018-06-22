/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.encoding;

import com.sun.org.omg.CORBA.portable.ValueHelper;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.OutputStream;

import java.io.Serializable;

import static com.meterware.simplestub.Stub.createStrictStub;

abstract class Value1Helper implements ValueHelper {
    Value1Type typeCode = createStrictStub(Value1Type.class);

    void setModifier(short modifier) {
        typeCode.modifier = modifier;
    }

    @Override
    public TypeCode get_type() {
        return typeCode;
    }

    @Override
    public void write_value(OutputStream os, Serializable value) {
        Value1 value1 = (Value1) value;
        os.write_wchar(value1.aChar);
        os.write_long(value1.anInt);
    }

    @Override
    public String get_id() {
        return Value1.REPID;
    }
}


abstract class Value1Type extends TypeCode {
    short modifier;

    @Override
    public short type_modifier() throws BadKind {
        return modifier;
    }
}
