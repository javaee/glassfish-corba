/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package javax.rmi.CORBA.serialization;

import java.util.*;
import java.io.*;

public class TestOBV2 extends TestOBV implements java.io.Serializable {
    public int bar;
    public static int foo = 1;
    public TestOBV2 self = null;
    public static final int FOO = 3;
    public TestOBV2 arrayOfThis[];

    public TestOBV2(){
        super();
        self = this;
        bar = new Random().nextInt();
        arrayOfThis = new TestOBV2[3];
        arrayOfThis[0] = null;
        arrayOfThis[1] = this;
        arrayOfThis[2] = null;
    }

    public boolean equals (Object o){
        try
            {
                TestOBV2 target = (TestOBV2)o;
                return ((target != null) &&
                        (target.self == target) &&      
                        (target.arrayOfThis != null) &&
                        (target.arrayOfThis[0] == null) &&
                        (target.arrayOfThis[1] == target) &&
                        (target.arrayOfThis[0] == null) &&
                        (target.bar == bar) &&
                        (target.foo == foo) &&
                        (target.FOO == FOO));
            }
        catch(Throwable t)
            {
                return false;
            }
    }
}
