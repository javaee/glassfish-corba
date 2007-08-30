/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
import corba.strm2.Testable;

/**
 * Initial class for start of evolution.
 */
public class TestObject implements Testable
{
    private static final long serialVersionUID = 378730127323820502L;

    private String desc;

    private Integer data0;
    private long data1;
    private String data2;

    public TestObject() {
        data0 = new Integer(342141);
        data1 = 1209409213L;
        data2 = "This is a test\u98DB";

        desc = "class0";
    }

    public boolean equals(Object obj) {
        try {
            TestObject other = (TestObject)obj;
            if (other == null)
                return false;

            return data0.equals(other.data0) &&
                data1 == other.data1 &&
                data2.equals(other.data2);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public String getDescription() {
        return desc;
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObject desc=" + desc
            + ", data0=" + data0
            + ", data1=" + data1
            + ", data2= " + data2
            + "]";
    }
}


        
        
