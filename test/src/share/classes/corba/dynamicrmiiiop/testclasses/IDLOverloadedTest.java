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
package corba.dynamicrmiiiop.testclasses;

public class IDLOverloadedTest {

    public class Inner\u0300 {

        public class Extra\u0301Inner {}
    }
    
    
    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    public static final String[] IDL_NAMES = {   

        "A__",
        "A__org_omg_boxedRMI_seq1_octet",
        "A__org_omg_boxedRMI_seq1_wchar",
        "A__org_omg_boxedRMI_seq1_double",
        "A__org_omg_boxedRMI_seq1_float",
        "A__org_omg_boxedRMI_seq1_long",
        "A__org_omg_boxedRMI_seq1_long_long",

        "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_IDLOverloadedTest__InnerU0300__ExtraU0301Inner",        
        "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_IDLOverloadedTest__InnerU0300",

        "A__org_omg_boxedRMI_java_io_seq1_Externalizable",
        "A__org_omg_boxedRMI_java_io_seq1_Serializable",
        "A__org_omg_boxedRMI_java_lang_seq1_Boolean",
        "A__org_omg_boxedRMI_java_lang_seq1_Byte",
        "A__org_omg_boxedRMI_java_lang_seq1_Character",
        "A__org_omg_boxedRMI_javax_rmi_CORBA_seq1_ClassDesc",
        "A__org_omg_boxedRMI_java_lang_seq1_Double",
        "A__org_omg_boxedRMI_java_lang_seq1_Float",
        "A__org_omg_boxedRMI_java_lang_seq1_Integer",
        "A__org_omg_boxedRMI_java_lang_seq1_Long",
        "A__org_omg_boxedRMI_java_lang_seq1_Object",

        "A__org_omg_boxedRMI_java_lang_seq1_Short",
        "A__org_omg_boxedRMI_CORBA_seq1_WStringValue",
        "A__org_omg_boxedRMI_java_rmi_seq1_Remote",
        "A__org_omg_boxedRMI_javax_swing_seq1_UIDefaults__ActiveValue",
        "A__org_omg_boxedRMI_seq1_Object",

        "A__org_omg_boxedRMI_seq1_short",
        "A__org_omg_boxedRMI_seq1_boolean",
        "A__org_omg_boxedRMI_seq2_boolean",
        "A__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq4_IDLOverloadedTest__InnerU0300__ExtraU0301Inner__org_omg_boxedRMI_CORBA_seq2_WStringValue__long",
        "A__org_omg_boxedRMI_seq16_boolean",
        "A__boolean",
        "A__octet",
        "A__wchar",
        "A__corba_dynamicrmiiiop_testclasses_IDLOverloadedTest__InnerU0300",
        "A__corba_dynamicrmiiiop_testclasses_IDLOverloadedTest__InnerU0300__ExtraU0301Inner",
        "A__org_omg_boxedIDL_corba_dynamicrmiiiop_testclasses_TestStruct",
        "A__double",
        "A__float",
        "A__long",
        "A__long__float__double__wchar__octet__boolean__java_io_Serializable__CORBA_WStringValue",

        "A__java_io_Externalizable",
        "A__java_io_Serializable",
        "A__java_lang_Boolean",
        "A__java_lang_Byte",
        "A__java_lang_Character",
        "A__javax_rmi_CORBA_ClassDesc",
        "A__java_lang_Double",
        "A__java_lang_Float",
        "A__java_lang_Integer",
        "A__java_lang_Long",
        "A__java_lang_Object",
        "A__java_lang_Short",
        "A__CORBA_WStringValue",
        "A__java_rmi_Remote",
        "A__javax_swing_UIDefaults__ActiveValue",

        "A__long_long",        
        "A__Object",
        "A__short"
       
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLOverloaded extends java.rmi.Remote {

        void A() throws java.rmi.RemoteException;

        void A(byte[] b) throws java.rmi.RemoteException;
        void A(char[] c) throws java.rmi.RemoteException;
        void A(double[] d) throws java.rmi.RemoteException;
        void A(float[] f) throws java.rmi.RemoteException;
        void A(int[] a) throws java.rmi.RemoteException;
        void A(long[] a) throws java.rmi.RemoteException;

        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner[] b) throws java.rmi.RemoteException;
        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300[] a)
            throws java.rmi.RemoteException;

        void A(java.io.Externalizable[] e) throws java.rmi.RemoteException;
        void A(java.io.Serializable[] s) throws java.rmi.RemoteException;
        void A(java.lang.Boolean[] b) throws java.rmi.RemoteException;
        void A(java.lang.Byte[] b) throws java.rmi.RemoteException;
        void A(java.lang.Character[] b) throws java.rmi.RemoteException;
        void A(java.lang.Class[] c) throws java.rmi.RemoteException;
        void A(java.lang.Double[] d) throws java.rmi.RemoteException;
        void A(java.lang.Float[] f) throws java.rmi.RemoteException;
        void A(java.lang.Integer[] i) throws java.rmi.RemoteException;
        void A(java.lang.Long[] l) throws java.rmi.RemoteException;
        void A(java.lang.Object[] o) throws java.rmi.RemoteException;

        void A(java.lang.Short[] s) throws java.rmi.RemoteException;
        void A(java.lang.String[] s) throws java.rmi.RemoteException;
        void A(java.rmi.Remote[] r) throws java.rmi.RemoteException;

        void A(javax.swing.UIDefaults.ActiveValue[] s) throws java.rmi.RemoteException;
        void A(org.omg.CORBA.Object[] o) throws java.rmi.RemoteException;

        void A(short[] s) throws java.rmi.RemoteException;
        void A(boolean[] b) throws java.rmi.RemoteException;
        void A(boolean[][] b) throws java.rmi.RemoteException;
        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner[][][][] a, java.lang.String[][] b, int c) throws java.rmi.RemoteException;
        void A(boolean[][][][][][][][][][][][][][][][] b) throws java.rmi.RemoteException;

        void A(boolean z) throws java.rmi.RemoteException;
        void A(byte b) throws java.rmi.RemoteException;
        void A(char c) throws java.rmi.RemoteException;
        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300 d) throws java.rmi.RemoteException;
        void A(corba.dynamicrmiiiop.testclasses.IDLOverloadedTest.Inner\u0300.Extra\u0301Inner e) throws java.rmi.RemoteException;
        void A(double d) throws java.rmi.RemoteException;
        void A(float f) throws java.rmi.RemoteException;
        void A(int i) throws java.rmi.RemoteException;
        void A(int i, float f, double d, char c, byte b, boolean z,
               java.io.Serializable s, java.lang.String t) throws java.rmi.RemoteException;

        void A(java.io.Externalizable e) throws java.rmi.RemoteException;
        void A(java.io.Serializable s) throws java.rmi.RemoteException;
        void A(java.lang.Boolean b) throws java.rmi.RemoteException;
        void A(java.lang.Byte b) throws java.rmi.RemoteException;
        void A(java.lang.Character b) throws java.rmi.RemoteException;
        void A(java.lang.Class c) throws java.rmi.RemoteException;
        void A(java.lang.Double d) throws java.rmi.RemoteException;
        void A(java.lang.Float f) throws java.rmi.RemoteException;
        void A(java.lang.Integer i) throws java.rmi.RemoteException;
        void A(java.lang.Long l) throws java.rmi.RemoteException;
        void A(java.lang.Object o) throws java.rmi.RemoteException;
        void A(java.lang.Short s) throws java.rmi.RemoteException;
        void A(java.lang.String s) throws java.rmi.RemoteException;
        void A(java.rmi.Remote r) throws java.rmi.RemoteException;
        void A(javax.swing.UIDefaults.ActiveValue s) throws java.rmi.RemoteException;

        void A(long j) throws java.rmi.RemoteException;        
        
        void A(org.omg.CORBA.Object o) throws java.rmi.RemoteException;
        void A(short s) throws java.rmi.RemoteException;
        
        void A(TestStruct t) throws java.rmi.RemoteException; 
    }

}
