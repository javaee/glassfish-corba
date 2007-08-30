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
import java.io.*;
import java.math.*;

public class TestObject extends TestObjectSuperSub implements Testable
{
    private static final long serialVersionUID = 378730127323820502L;
    private transient String desc;

    private static final ObjectStreamField[] serialPersistentFields = { 
        new ObjectStreamField("data0", Integer.class),
        new ObjectStreamField("data1", Long.TYPE),
        new ObjectStreamField("data2", String.class),
        new ObjectStreamField("desc", String.class),
    };

    private transient Integer data0;
    private transient long data1;
    private transient String data2;
    private transient Long optData0;
    private transient BigInteger optData1;

    public TestObject() {
        data0 = new Integer(342141);
        data1 = 1209409213L;
        data2 = "This is a test\u98DB";

        desc = "class5";

        optData0 = new Long(23124124L);
        optData1 = new BigInteger("892748282821123", 10);
    }

    public String toString() {
        return super.toString()
            + " [TestObject desc=" + desc
            + ", data0=" + data0
            + ", data1=" + data1
            + ", data2= " + data2
            + ", optData0=" + optData0
            + ", optData1=" + optData1
            + "]";
    }

    public boolean equals(Object obj) {
        try {
            TestObject other = (TestObject)obj;
            if (other == null)
                return false;

            return data0.equals(other.data0) &&
                data1 == other.data1 &&
                data2.equals(other.data2) &&
                optData0.equals(optData0) &&
                optData1.equals(optData1) &&
                super.equals(other);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException 
    {
        ObjectInputStream.GetField fields = is.readFields();

        data0 = (Integer)fields.get("data0", null);
        if (data0 == null)
            throw new IOException("Missing data0 field");

        data1 = fields.get("data1", 0L);
        if (data1 == 0L)
            throw new IOException("Missing data1 field");

        data2 = (String)fields.get("data2", null);
        if (data2 == null)
            throw new IOException("Missing data2 field");

        desc = (String)fields.get("desc", null);
        if (desc == null)
            throw new IOException("Missing desc field");

        try {
            optData0 = (Long)is.readObject();
            optData1 = (BigInteger)is.readObject();
        } catch (OptionalDataException ode) {
            optData0 = new Long(23124124L);
            optData1 = new BigInteger("892748282821123", 10);
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        ObjectOutputStream.PutField fields = out.putFields();

        fields.put("data0", data0);
        fields.put("data1", new Long(data1));
        fields.put("data2", data2);
        fields.put("desc", desc);
            
        out.writeFields();        

        out.writeObject(optData0);
        out.writeObject(optData1);
    }

    public String getDescription() {
        return desc;
    }
}
