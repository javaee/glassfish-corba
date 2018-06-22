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

package corba.cdrext;

import java.io.*;

public class TestExternalizable implements Externalizable
{
    private long data1;
    private String data2;
    private int data3;
    private char data4;

    public TestExternalizable() {}

    public TestExternalizable(long data1,
                              String data2,
                              int data3,
                              char data4) {
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.data4 = data4;
    }

    public boolean equals(Object obj) {
        try {
            if (obj == null)
                return false;
            
            TestExternalizable other
                = (TestExternalizable)obj;

            return (data1 == other.data1 &&
                    (data2 == null ||
                     data2.equals(other.data2)) &&
                    data3 == other.data3 &&
                    data4 == other.data4);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(data1);

        // Check to make sure these are working
        // the same
        out.writeObject(data2);
        out.writeUTF(data2);

        out.writeInt(data3);

        out.writeChar(data4);
    }

    public void readExternal(ObjectInput in) 
        throws IOException, ClassNotFoundException {

        data1 = in.readLong();

        String data2_obj = (String)in.readObject();
        String data2_utf = in.readUTF();

        if (data2_obj == null && data2_obj != data2_utf)
            throw new IOException("data2_obj null mismatch");
        else
        if (data2_obj != null && !data2_obj.equals(data2_utf))
            throw new IOException("data2_obj data2_utf mismatch");

        data2 = data2_obj;

        data3 = in.readInt();

        data4 = in.readChar();
    }
}
