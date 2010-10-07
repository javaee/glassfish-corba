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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;

import java.lang.String;

public class ObjectByValue implements java.io.Serializable {

    public static int foo = 1;
    public static final int FOO = 3;

    int value1;
    int value2;
    private String str1;
    String str2;
    private transient PrivateValue1 transientPrivate = null;
    private static PrivateValue2 staticPrivate = new PrivateValue2();
    private static final PrivateValue3 constantPrivate = new PrivateValue3();

    //private PrivateValue4 memberPrivate; // Should not be allowed by rmic.


    public ObjectByValue(int val1, int val2, String str1, String str2 )
    {
        this.value1 = val1;
        this.value2 = val2;
        this.str1 = str1;
        this.str2 = str2;
    }

    public String toString () {
        return "{" + value1 + ", " + value2 + ", " + str1 + ", " + str2 + "}";
    }

    public boolean equals (Object right) {
        if (right instanceof ObjectByValue) {
            ObjectByValue other = (ObjectByValue)right;
            return  value1 == other.value1 &&
		value2 == other.value2 &&
		str1.equals(other.str1) &&
		str2.equals(other.str2);
        } else {
            return false;
        }
    }

    public int getValue1() { return value1; }
    public int getValue2() { return value2; }

    public String getString1() { return str1; }
    public String getString2() { return str2; }

    public synchronized void checkPrivate(PrivateValue1 it) {
    }
}

class PrivateValue1 implements java.io.Serializable {
    private int value = 1;
}

class PrivateValue2 implements java.io.Serializable {
    private int value = 2;
}

class PrivateValue3 implements java.io.Serializable {
    private int value = 3;
}

class PrivateValue4 implements java.io.Serializable {
    private int value = 4;
}
