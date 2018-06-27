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

package com.sun.corba.ee.impl.corba;

public final class Enum1 implements org.omg.CORBA.portable.IDLEntity {
    public static final int _zeroth = 0,
        _first = 1,
        _second = 2,
        _third = 3;
    public static final Enum1 zeroth = new Enum1(_zeroth);
    public static final Enum1 first = new Enum1(_first);
    public static final Enum1 second = new Enum1(_second);
    public static final Enum1 third = new Enum1(_third);
    public int value() {
        return _value;
    }
    public static final Enum1 from_int(int i)  throws  org.omg.CORBA.BAD_PARAM {
        switch (i) {
        case _zeroth:
            return zeroth;
        case _first:
            return first;
        case _second:
            return second;
        case _third:
            return third;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }
    private Enum1(int _value){
        this._value = _value;
    }
    private int _value;
}
