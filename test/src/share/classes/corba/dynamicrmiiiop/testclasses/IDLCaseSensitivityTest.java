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
package corba.dynamicrmiiiop.testclasses;

public class IDLCaseSensitivityTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    public static final String[] IDL_NAMES = {   
        "ABCDEFGHIJKLmNOPQRSTUVWXYzA_0_1_2_3_4_5_6_7_8_9_10_11_13_14_15_16_17_18_19_20_21_22_23_24_26",
        "B_0", 
        "JACK_0_1_2_3",
        "JACKY",
        "Jack_0",
        "a",
        "abcdefghijklMnopqrstuvwxyza_12",
        "abcdefghijklmnopqrstuvwxyzA_26",
        "b_",
        "b__",
        "jAcK_1_3",
        "jack_"
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLCaseSensitivity extends java.rmi.Remote {
        String ABCDEFGHIJKLmNOPQRSTUVWXYzA(int a) 
            throws java.rmi.RemoteException;

        void B() throws java.rmi.RemoteException;

        boolean JACK() throws java.rmi.RemoteException;
        void JACKY() throws java.rmi.RemoteException;
        void Jack() throws java.rmi.RemoteException;

        void a() throws java.rmi.RemoteException;

        void abcdefghijklMnopqrstuvwxyza() throws java.rmi.RemoteException;
        void abcdefghijklmnopqrstuvwxyzA() throws java.rmi.RemoteException;

        void b() throws java.rmi.RemoteException;

        void b__() throws java.rmi.RemoteException;


        int jAcK() throws java.rmi.RemoteException;
        void jack() throws java.rmi.RemoteException;        
        
    }

}
