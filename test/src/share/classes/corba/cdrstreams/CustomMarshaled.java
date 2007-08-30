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
package corba.cdrstreams;

import java.io.*;

public class CustomMarshaled implements Serializable
{
    int value1;
    long value2;

    boolean good;

    public CustomMarshaled(int value1, long value2, boolean good)
    {
        this.value1 = value1;
        this.value2 = value2;
        this.good = good;
    }

    public boolean equals(Object obj)
    {
        CustomMarshaled gcm = (CustomMarshaled)obj;

        return (value1 == gcm.value1 && value2 == gcm.value2);
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        byte[] buffer = new byte[1024];
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = (byte)(i % 255);

        out.write(buffer);

        out.writeObject("CustomMarshaled 1.0");

    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        if (good) {
            byte[] buffer = new byte[1024];
            in.readFully(buffer);

            for (int i = 0; i < buffer.length; i++)
                if (buffer[i] != (byte)(i % 255))
                    throw new IOException("Data buffer corrupted");

            if (!((String)(in.readObject())).equals("CustomMarshaled 1.0"))
                throw new IOException("Strings didn't match properly");
        } 

        // If it's a bad (has a bug) custom marshaler, it leaves the
        // string on the wire
    }
}
