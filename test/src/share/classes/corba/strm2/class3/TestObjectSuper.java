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
import java.io.*;

public class TestObjectSuper implements Serializable
{
    private static final long serialVersionUID = 6234419445336614908L;

    private int dataS1;
    private char dataS2;
    private Long dataS3;

    private transient Double optDataS1;
    private transient int optDataS2;

    public TestObjectSuper() {
        dataS1 = 23910;
        dataS2 = '\u6A5F';
        dataS3 = new Long(999211L);
        optDataS1 = new Double((double)24124.23121);
        optDataS2 = 2412;
    }

    public String toString() {
        return 
            (super.getClass().equals(Object.class) ? "" : super.toString())
            + " [TestObjectSuper dataS1=" + dataS1
            + ", dataS2=" + (int)dataS2
            + ", dataS3=" + dataS3
            + ", optDataS1=" + optDataS1
            + ", optDataS2=" + optDataS2
            + "]";
    }

    private boolean defaultedValues() {
        return dataS1 == 0 && (int)dataS2 == 0 && dataS3 == null;
    }

    public boolean equals(Object obj) {
        try {
            TestObjectSuper other = (TestObjectSuper)obj;
            if (other == null)
                return false;

            return (defaultedValues() || other.defaultedValues()) ||
                (dataS1 == other.dataS1 &&
                 dataS2 == other.dataS2 &&
                 dataS3.equals(other.dataS3) &&
                 optDataS1.equals(other.optDataS1) &&
                 optDataS2 == other.optDataS2);

        } catch (ClassCastException cce) {
            return false;
        }
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        try {
            out.defaultWriteObject();
            // Should throw an error for calling this twice

            throw new IOException("Error -- should not allow defWrObj call twice");
        } catch (IOException ex) {
            // Should throw this
        }

        out.writeObject(optDataS1);
        out.writeInt(optDataS2);
    }

    private void readObject(java.io.ObjectInputStream is)
        throws IOException, ClassNotFoundException 
    {
	is.defaultReadObject();

        try {
            is.defaultReadObject();
            
            // Should throw an error for calling this twice

            throw new IOException("Error -- should not allow defRdObj call twice");
        } catch (IOException ex) {
            // Should throw this
        }

    /*    try {
            is.readFields();

            // Should throw an error for reading defaults twice

            throw new IOException("Error -- should not allow default read twice");

        } catch (IOException ex) {
            // Should throw this
        }
    */

        try {
            optDataS1 = (Double)is.readObject();
        } catch (OptionalDataException ode) {
            // Optional object data not present
            optDataS1 = new Double((double)24124.23121);
        }

        try {
            optDataS2 = is.readInt();
        } catch (EOFException eof) {
            // Int wasn't on wire
            optDataS2 = 2412;
        }
    }
}
