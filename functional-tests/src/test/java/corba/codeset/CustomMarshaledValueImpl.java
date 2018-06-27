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

package CodeSetTester;

import java.util.Arrays;
import org.omg.CORBA.DataOutputStream;
import org.omg.CORBA.DataInputStream;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.CharSeqHelper;
import org.omg.CORBA.WCharSeqHelper;

// Custom marshaled valuetype with all the data types we're interested in.

public class CustomMarshaledValueImpl extends CustomMarshaledValue
{
    public CustomMarshaledValueImpl() {
        str = "";
        wstr = "";
    }

    public CustomMarshaledValueImpl(char ch, 
                                    char wch, 
                                    String str, 
                                    String wstr,
                                    char[] chSeq,
                                    char[] wchSeq) {
        this.ch = ch;
        this.wch = wch;
        if (str == null)
            this.str = "";
        else 
            this.str = new String(str);
        if (wstr == null)
            this.wstr = "";
        else
            this.wstr = new String(wstr);
        this.chSeq = chSeq;
        this.wchSeq = wchSeq;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();

        sbuf.append("ch: ");
        sbuf.append((int)ch);
        sbuf.append(" wch: ");
        sbuf.append((int)wch);
        sbuf.append(" str: ");
        if (str == null)
            sbuf.append("null");
        else
            sbuf.append(str.length());
        sbuf.append(" wstr: ");
        if (wstr == null)
            sbuf.append("null");
        else
            sbuf.append(wstr.length());

        sbuf.append(" chSeq: ");
        if (chSeq == null)
            sbuf.append("null");
        else {
            for (int i = 0; i < chSeq.length; i++) {
                sbuf.append((int)chSeq[i]);
                sbuf.append(' ');
            }
        }

        sbuf.append(" wchSeq: ");
        if (wchSeq == null)
            sbuf.append("null");
        else {
            for (int i = 0; i < wchSeq.length; i++) {
                sbuf.append((int)wchSeq[i]);
                sbuf.append(' ');
            }
        }

        return sbuf.toString();
    }

    public boolean equals(Object obj) {
        try {
            CustomMarshaledValue cmv = (CustomMarshaledValue)obj;

            return (ch == cmv.ch &&
                    wch == cmv.wch &&
                    ((str == null && cmv.str == null) ||
                     (str.equals(cmv.str))) &&
                    ((wstr == null && cmv.wstr == null) ||
                     (wstr.equals(cmv.wstr))) &&
                    Arrays.equals(chSeq, cmv.chSeq) &&
                    Arrays.equals(wchSeq, cmv.wchSeq));

        } catch (ClassCastException cce) {
            return false;
        }
    }

    public void unmarshal(DataInputStream is) {
        System.out.println("Unmarshaling custom data...");
        ch = is.read_char();
        wch = is.read_wchar();
        str = is.read_string();
        wstr = is.read_wstring();

        chSeq = CharSeqHelper.read((InputStream)is);
        wchSeq = WCharSeqHelper.read((InputStream)is);

        System.out.println("Done");
    }

    public void marshal(DataOutputStream os) {
        System.out.println("Marshaling custom data...");
        os.write_char(ch);
        os.write_wchar(wch);
        os.write_string(str);
        os.write_wstring(wstr);

        if (chSeq == null)
            os.write_long(0);
        else
            CharSeqHelper.write((OutputStream)os, chSeq);

        if (wchSeq == null)
            os.write_long(0);
        else
            WCharSeqHelper.write((OutputStream)os, wchSeq);

        System.out.println("Done");
    }
}
