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

package corba.copyobject ;

import java.io.Serializable;
import java.io.ObjectStreamException;

import java.util.Collections;
import java.util.List;
import java.util.Arrays;

public abstract class Operation implements Serializable {

    private final transient String name;

    protected Operation(String name) {
        this.name = name;
    }

    public static final Operation PLUS = new Operation("+") {
        protected double eval(double x, double y) {
            return x + y;
        }
    };

    public static final Operation MINUS = new Operation("-") {
        protected double eval(double x, double y) {
            return x - y;
        }
    };

    protected abstract double eval(double x, double y);

    public String toString() {
        return name;
    }

    public final boolean equals(Object o) {
        return super.equals(o);
    }

    public final int hashCode() {
        return super.hashCode();
    }

    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;

    private static final Operation[] VALUES = { PLUS, MINUS };

    Object readResolve() throws ObjectStreamException {
        return VALUES[ordinal];
    }
}

abstract class ExtendedOperation extends Operation {

    private ExtendedOperation(String name) {
        super(name);
    }

    public static final Operation TIMES = new ExtendedOperation("*") {
        protected double eval(double x, double y) {
            return x * y;
        }
    };

    public static final Operation DIVIDE = new ExtendedOperation("/") {
        protected double eval(double x, double y) {
            return x / y;
        }
    };

    private static int nextOrdinal = 0;
    private final int ordinal = nextOrdinal++;
    private static final Operation[] VALUES = { TIMES, DIVIDE };

    public static final List LIST =
        Collections.unmodifiableList(Arrays.asList(VALUES));

    Object readResolve() throws ObjectStreamException {
        return VALUES[ordinal];
    }

}
