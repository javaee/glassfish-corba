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

package corba.dynamicrmiiiop.testclasses;

public class IDLDefaultTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    static final String[] IDL_NAMES = {

        "AAA__corba_dynamicrmiiiop_testclasses_Default",
        "AAA__corba_dynamicrmiiiop_testclasses_J_Default",
        "BBB__corba_dynamicrmiiiop_testclasses_Default__corba_dynamicrmiiiop_testclasses_Default__Inner__corba_dynamicrmiiiop_testclasses_Default___Inner__corba_dynamicrmiiiop_testclasses_Default___Default",
        "BBB__corba_dynamicrmiiiop_testclasses_J_Default__corba_dynamicrmiiiop_testclasses_J_Default__Inner__corba_dynamicrmiiiop_testclasses_J_Default___Inner__corba_dynamicrmiiiop_testclasses_J_Default__Default",
        "CCC__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default",
        "CCC__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default",

        "DDD__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default__Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default___Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_Default___Default",
        "DDD__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default___Inner__org_omg_boxedRMI_corba_dynamicrmiiiop_testclasses_seq1_J_Default__Default"

    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLDefault extends java.rmi.Remote {
        
        void AAA(Default d) throws java.rmi.RemoteException;
        void AAA(_Default d) throws java.rmi.RemoteException;

        void BBB(Default d, Default.Inner e, Default._Inner f,
                 Default._Default g) throws java.rmi.RemoteException;
        void BBB(_Default d, _Default.Inner e, _Default._Inner f,
                 _Default.Default g) throws java.rmi.RemoteException;
       
 
        void CCC(Default[] d) throws java.rmi.RemoteException;
        void CCC(_Default[] d) throws java.rmi.RemoteException;

        void DDD(Default[] d, Default.Inner[] e, Default._Inner[] f,
                 Default._Default[] g) throws java.rmi.RemoteException;
        void DDD(_Default[] d, _Default.Inner[] e, _Default._Inner[] f,
                 _Default.Default[] g) throws java.rmi.RemoteException;

    }

}
