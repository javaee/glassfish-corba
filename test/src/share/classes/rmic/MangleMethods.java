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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.MarshalException;
import java.util.Hashtable;
import java.io.IOException;

public interface MangleMethods extends Remote {

    // Methods that should be attributes...
    
    int getFoo () throws RemoteException;
    int getAB () throws RemoteException;
    boolean isX () throws RemoteException;
    char getChar() throws RemoteException;
    void setChar(char c) throws RemoteException, RuntimeException;
    long getLong() throws RemoteException, ClassCastException;      // RuntimeException subclass.
    int getSomething() throws RemoteException, MarshalException;    // RemoteException subclass.
    
    // Methods that look like they should be attributes
    // but aren't...
    
    byte getByte () throws Exception;           // Invalid exception.
  
    boolean is () throws RemoteException;          // No property name
    
    void setZ(int z) throws RemoteException;       // No getter
    
    boolean isLong() throws RemoteException;       // Same name as getLong, different type.
    
    boolean isShort() throws RemoteException;      // getter...
    void setShort(short s) throws RemoteException; // ... setter different types...
    
    int getOther(char i) throws RemoteException;   // Argument.
 
    void getIt() throws RemoteException;            // void return.
 
    int getY() throws RemoteException;             // getter...
    void setY() throws RemoteException;            // ... setter with void arg
    
    // Miscellaneous...
 
    boolean isFloat() throws RemoteException;      // getter...
    void setFloat(float f) throws RemoteException; // ... setter different types ...
    float getFloat() throws RemoteException;       // ... set/get are attrs, 'is' isn't.
    
    boolean isEmpty() throws RemoteException;      // Looks like a case-collision...
    boolean IsEmpty() throws RemoteException;      // ... but isn't.
    
    int doAJob() throws RemoteException;           // Case collision...
    int doAjob() throws RemoteException;           // ... but not attributes.

    boolean isAJob() throws RemoteException;       // Case collision...
    boolean getAjob() throws RemoteException;      // ... and are (different) attributes.
    
    byte getfred() throws RemoteException;          // Not case collision (5.4.3.4 mangling)...
    void setFred(byte b) throws RemoteException;    // ... and are attribute pair.
    
    int _do\u01c3It$() throws RemoteException;         // Methods with illegal chars...
    int _do\u01c3It$(int i) throws RemoteException;    // ... and overloaded versions...
    int _do\u01c3It$(char c) throws RemoteException;   // ...
    
    int getFooBar() throws RemoteException;         // Attribute...
    int fooBar() throws RemoteException;            // ... and colliding method.
    
    // IDL Keyword collisions...
    
    int typeDef () throws RemoteException;          // Method name is keyword
    int getDefault () throws RemoteException;       // Attribute name is keyword.
    int getObject () throws RemoteException;        // Attribute name is keyword.
    int getException () throws RemoteException;     // Attribute name is keyword.     
    
    
    // Assertion mechanism...
    
    class Asserts {
        
        private static Hashtable map = null;
        
        public static String[] getAsserts (String methodSig) {
            
            if (map == null) {
                map = new Hashtable();
                for (int i = 0; i < ASSERTS.length; i++) {
                    map.put(ASSERTS[i][0],new Integer(i));
                }
            }
            
            Integer theIndex = (Integer)map.get(methodSig);
            
            if (theIndex == null) {
                throw new Error("Assert not found for " + methodSig);
            }

            return ASSERTS[theIndex.intValue()];
        }
        
        private static String[][] ASSERTS = {
                                    
	    //   Method Signature           Kind        Attribute Name      Wire Name
	    //   ----------------------     ----------- ------------------- -------------------------
            {"int getFoo()",            "GET",      "foo",              "_get_foo"},
            {"int getAB()",             "GET",      "AB",               "_get_AB"},
            {"boolean isX()",           "IS",       "x",                "_get_x"},
            {"char getChar()",          "GET_RW",   "_char",            "_get_char"},
            {"void setChar(char)",      "SET",      "_char",            "_set_char"},
            {"byte getByte()",          "NONE",     null,               "getByte"},
            {"boolean is()",            "NONE",     null,               "is"},
            {"void setZ(int)",          "NONE",     null,               "setZ"},
            {"boolean isLong()",        "NONE",     null,               "isLong"},
            {"long getLong()",          "GET",      "_long",            "_get_long"},
            {"boolean isShort()",       "NONE",     null,               "isShort"},
            {"void setShort(short)",    "NONE",     null,               "setShort"},
            {"int getSomething()",      "GET",      "something",        "_get_something"},
            {"int getOther(char)",      "NONE",     null,               "getOther"},
            {"void getIt()",            "NONE",     null,               "getIt"},
            {"int getY()",              "GET",      "y",                "_get_y"},
            {"void setY()",             "NONE",     null,               "setY"},
            {"boolean isFloat()",       "NONE",     null,               "isFloat"},
            {"float getFloat()",        "GET_RW",   "_float",           "_get_float"},
            {"void setFloat(float)",    "SET",      "_float",           "_set_float"},
            {"boolean isEmpty()",       "IS",       "empty",            "_get_empty"},
            {"boolean IsEmpty()",       "NONE",     null,               "IsEmpty"},
            {"int doAJob()",            "NONE",     null,               "doAJob_2_3"},
            {"int doAjob()",            "NONE",     null,               "doAjob_2"},
            {"boolean isAJob()",        "IS",       "AJob_0_1",         "_get_AJob_0_1"},
            {"boolean getAjob()",       "GET",      "ajob_",            "_get_ajob_"},
            {"byte getfred()",          "GET",      "fred",             "_get_fred"},
            {"void setFred(byte)",      "NONE",     null,               "setFred"},
            {"int _do\u01c3It$()",      "NONE",     null,               "J_doU01C3ItU0024__"},
            {"int _do\u01c3It$(int)",   "NONE",     null,               "J_doU01C3ItU0024__long"},
            {"int _do\u01c3It$(char)",  "NONE",     null,               "J_doU01C3ItU0024__wchar"},
            {"int getFooBar()",         "GET",      "fooBar__",         "_get_fooBar__"},
            {"int fooBar()",            "NONE",     null,               "fooBar"},
            {"int typeDef()",           "NONE",     null,               "_typeDef"},
            {"int getDefault()",        "GET",      "_default",         "_get_default"},
            {"int getObject()",         "GET",      "_object",          "_get_object"},
            {"int getException()",      "GET",      "_exception",       "_get_exception"},
        };
    }
}
