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

public class IDLPropertiesTest {

    //
    // Set of idl names corresponding to alphabetically sorted set of
    // interface methods.  See TestIDLNameTranslator for sorting details.    
    //
    static final String[] IDL_NAMES = {   
        "a",
        "get", 
        "_get_a__",
        "_get_ABc",
        "_get_b",
        "_get_CDE",
        "getDAB",
        "getDCD",
        "getDzzz",
        "getEfg",
        "_get_zde",
        "is",
        "isA",
        "isBCD",
        "_get_c",
        "_get_CCCCCe",
        "isCZ",
        "_get_cf",
        "set",
        "_set_a__",
        "_set_b",
        "setCDE",
        "setEfg",
        "_set_zde"
    };
    
    public static String[] getIDLNames() {
        return IDL_NAMES;
    }

    public interface IDLProperties extends java.rmi.Remote {

        // should force a __ to be added to getter attribute
        void a() throws java.rmi.RemoteException;

        // not a property since there is no <name> portion
        int get() throws java.rmi.RemoteException;
                
        // valid getter
        int getA() throws java.rmi.RemoteException;
        
        // valid getter
        int getABc() throws java.rmi.RemoteException;

        // valid getter
        int getB() throws java.rmi.RemoteException;
        
        // getter
        int getCDE() throws java.rmi.RemoteException;

        // not a getter. can't have void return type.
        void getDAB() throws java.rmi.RemoteException;

        // not a getter. can't have void return type.
        void getDCD(int a) throws java.rmi.RemoteException;

        // not a getter. can't have any parameters.
        int getDzzz(int a) throws java.rmi.RemoteException;

        // valid getter
        boolean getZde() throws java.rmi.RemoteException;

        // not a getter. throws at least one checked exception in addition to
        // java.rmi.RemoteException(or one of its subclasses)
        int getEfg() throws java.rmi.RemoteException, java.lang.Exception;

        // not a property since there is no <name> portion
        boolean is() throws java.rmi.RemoteException;

        // not a property since "is" only applies to boolean
        int isA() throws java.rmi.RemoteException;

        // not valid.  must be boolean primitive
        Boolean isBCD() throws java.rmi.RemoteException;
        
        // valid boolean property
        boolean isC() throws java.rmi.RemoteException;

        // valid boolean property
        boolean isCCCCCe() throws java.rmi.RemoteException;

        // not boolean property.  must have 0 args
        boolean isCZ(int a) throws java.rmi.RemoteException;

        // valid boolean property
        boolean isCf() throws java.rmi.RemoteException;        
        
        // not a property since there is no <name> portion
        int set() throws java.rmi.RemoteException;

        void setA(int c) throws java.rmi.RemoteException;
        
        // valid setter
        void setB(int b) throws java.rmi.RemoteException;

        // not a setter. no corresponding getter with correct type.
        void setCDE(Integer i) throws java.rmi.RemoteException;

        // not a setter.  no corresponding getter.
        void setEfg(int a) throws java.rmi.RemoteException;

        // valid setter
        void setZde(boolean a) throws java.rmi.RemoteException;
                
    }

}
