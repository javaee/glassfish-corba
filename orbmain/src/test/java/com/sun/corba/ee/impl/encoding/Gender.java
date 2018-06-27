/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * A class which uses writeReplace and readResolve to place a custom version into the object stream.
 */
class Gender implements Serializable {
    private static final long serialVersionUID = 0x34789521D52D7FF2L;
    
    final static String REPID = "RMI:com.sun.corba.ee.impl.encoding.Gender\\U0024SerializedForm:F85634868214EB9C:34789521D52D7FF2";
    final static Gender MALE = new Gender("Male");
    final static Gender FEMALE = new Gender("Female");

    private String name;

    private Gender(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Gender{" +
                "name='" + name + '\'' +
                '}';
    }

    private Object writeReplace() throws ObjectStreamException {
        if (this.equals(MALE)) {
            return SerializedForm.MALE_FORM;
        } else {
            return SerializedForm.FEMALE_FORM;
        }
    }

    private static class SerializedForm implements Serializable {

        final static SerializedForm MALE_FORM = new SerializedForm(0);
        final static SerializedForm FEMALE_FORM = new SerializedForm(1);

        private int value;

        SerializedForm(int value) {
            this.value = value;
        }

        private Object readResolve() throws ObjectStreamException {
            if (value == MALE_FORM.value) {
                return Gender.MALE;
            } else {
                return Gender.FEMALE;
            }
        }
    }
}
