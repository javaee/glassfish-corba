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

package corba.codeset;

import CodeSetTester.Verifier ;
import CodeSetTester.VerifierHelper ;
import CodeSetTester.VerifierPackage.TestCharSeqHolder;
import CodeSetTester.VerifierPackage.TestWCharSeqHolder;
import java.util.Properties;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.encoding.OSFCodeSetRegistry;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class Client
{
    private static final char TEST_CHAR = 'f';
    private static final char TEST_WCHAR = '\u3044';

    public static void testTransmission(Verifier ver,
                                        boolean testValueTypes) throws Exception
    {
        char[] latin1 = CharGenerator.getLatin1Chars();
        char[] someUnicode = CharGenerator.getSomeUnicodeChars();

        System.out.println("Testing char...");
        char res = ver.verifyChar(Client.TEST_CHAR);
        if (res != Client.TEST_CHAR)
            throw new Exception("Invalid char result: " + res);

        System.out.println("Testing wchar...");
        res = ver.verifyWChar(Client.TEST_WCHAR);
        if (res != Client.TEST_WCHAR)
            throw new Exception("Invalid wchar result: (int)" + (int)res);
        
        System.out.println("Testing string...");
        String sending = new String(latin1);
        String resStr = ver.verifyString(sending);
        if (!sending.equals(resStr)) {
            System.out.println("Got: " + resStr.length());
            System.out.println("Expected: " + sending.length());
            throw new Exception("Invalid result string: " + resStr);
        }

        System.out.println("Testing wstring...");
        sending = new String(someUnicode);
        resStr = ver.verifyWString(sending);
        if (!sending.equals(resStr)) {
            System.out.println("Got: " + resStr.length());
            System.out.println("Expected: " + sending.length());
            if (resStr.length() != sending.length()) {
                throw new Exception("different lengths");
            }
            for (int i = 0; i < sending.length(); i++) {
                if (sending.charAt(i) != resStr.charAt(i)) {
                    System.out.println("chars not eq:");
                }
                if (Character.UnicodeBlock.of(sending.charAt(i)) != 
                    Character.UnicodeBlock.of(resStr.charAt(i))) {
                    System.out.println("chars UnicodeBlock not eq:");
                }
                System.out.println(
                    "send: " 
                    + sending.charAt(i)
                    + " " + Character.UnicodeBlock.of(sending.charAt(i))
                    + "/n"
                    + "recv: " 
                    + resStr.charAt(i)
                    + " " + Character.UnicodeBlock.of(resStr.charAt(i))
                    );
            }
            throw new Exception("Invalid result wstring: \n" + 
                                " Got: " + resStr + "\n" +
                                " Expected: " + sending);
        }

        System.out.println("Testing char sequence...");
        char[] inputCopy = new char[latin1.length];
        System.arraycopy(latin1, 0, inputCopy, 0, latin1.length);
        TestCharSeqHolder chHolder = new TestCharSeqHolder(inputCopy);
        ver.verifyCharSeq(chHolder);
        if (chHolder.value == null)
            throw new Exception("Got null char sequence");
        if (chHolder.value.length != latin1.length)
            throw new Exception("Result char sequence of different length: "
                                + chHolder.value.length);
        for (int i = 0; i < latin1.length; i++)
            if (chHolder.value[i] != latin1[i])
                throw new Exception("Unequal char at idx " + i);

        System.out.println("Testing wchar sequence...");
        inputCopy = new char[someUnicode.length];
        System.arraycopy(someUnicode, 0, inputCopy, 0, someUnicode.length);
        TestWCharSeqHolder wchHolder = new TestWCharSeqHolder(inputCopy);
        ver.verifyWCharSeq(wchHolder);
        if (wchHolder.value == null)
            throw new Exception("Got null wchar sequence");
        if (wchHolder.value.length != someUnicode.length)
            throw new Exception("Result wchar sequence of different length: "
                                + wchHolder.value.length);
        for (int i = 0; i < someUnicode.length; i++)
            if (wchHolder.value[i] != someUnicode[i])
                throw new Exception("Unequal wchar at idx " + i);     
        
        if (testValueTypes) {

            System.out.println("Testing custom marshaler...");
            CodeSetTester.CustomMarshaledValueImpl cv 
                = new CodeSetTester.CustomMarshaledValueImpl(Client.TEST_CHAR,
                                                             Client.TEST_WCHAR,
                                                             new String(latin1),
                                                             new String(someUnicode),
                                                             latin1,
                                                             someUnicode);
            
            CodeSetTester.CustomMarshaledValue rescv = ver.verifyTransmission(cv);
            
            if (!cv.equals(rescv)) {
                System.out.println("Unequal custom values:");
                System.out.println("old: " + cv);
                System.out.println("new: " + rescv);
                
                throw new Exception("Unequal custom values");
            }
        }
            
        System.out.println("PASSED");
    }

    /**
     * Tests the ORB's ability to parse our code set
     * property.
     */
    public static void testORBCodeSetListParsing() throws Exception {

        System.out.println("Testing code set list parsing...");

        StringBuffer list1 = new StringBuffer();
        list1.append(OSFCodeSetRegistry.ISO_8859_1.getNumber());
        list1.append(",0x");
        list1.append(Integer.toHexString(OSFCodeSetRegistry.UTF_8.getNumber()));

        StringBuffer list2 = new StringBuffer();
        list2.append("0x");
        list2.append(Integer.toHexString(OSFCodeSetRegistry.UTF_16.getNumber()));
        list2.append(", ");
        list2.append(OSFCodeSetRegistry.UTF_8.getNumber());
        list2.append(",");
        list2.append(OSFCodeSetRegistry.UCS_2.getNumber());

        Properties props = new Properties();
        props.setProperty(ORBConstants.CHAR_CODESETS,
                          list1.toString());
        props.setProperty(ORBConstants.WCHAR_CODESETS,
                          list2.toString());

        // Should throw INITIALIZE if there are any problems
        ORB testORB1 = ORB.init((String[])null, props);

        testORB1.shutdown(false);

        System.out.println("PASSED");
    }

    public static void main(String args[])
    {
        try {
            // First test parsing of the code set
            // properties
            Client.testORBCodeSetListParsing();

            // create and initialize the ORB
            ORB orb = ORB.init(args, System.getProperties());

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            // resolve the Object Reference in Naming
            NameComponent nc = new NameComponent("Verifier", "");
            NameComponent path[] = {nc};
            Verifier verifierRef = VerifierHelper.narrow(ncRef.resolve(path));

            // We don't support valuetypes in GIOP 1.1, so don't bother
            // testing them.
            String giopVersion = System.getProperty(ORBConstants.GIOP_VERSION);
            boolean testValueTypes = (giopVersion == null ||
                                      !giopVersion.equals("1.1"));

            System.out.println("GIOP version: " + giopVersion);
            System.out.println("Test value types? " + testValueTypes);

            Client.testTransmission(verifierRef, testValueTypes);

            orb.shutdown(true);

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
