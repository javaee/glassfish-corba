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
/* @(#)MethodOverload1.java	1.3 99/06/07 */
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;
public class MethodOverload1 {
    public void foo(){}
    public void foo(int i){}
    public void foo(java.rmi.RemoteException e){}

    public static final String[] IDL_NAMES = {
        "create",
        "foo__",
        "foo__long",
        "foo__java_rmi_RemoteException",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
        false,
        false,
    };
}

class MethodOverload2 {
    public void create(){}

    public static final String[] IDL_NAMES = {
        "create__",
        "create",
    };
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
    };
}

class MethodOverload3 {
    public void create(){}
    public void create(boolean e){}

    public static final String[] IDL_NAMES = {
        "create",
        "create__",
        "create__boolean",
    };
    public static final boolean[] CONSTRUCTOR = {
        true,
        false,
        false,
    };
}

class MethodOverload4 {

    public MethodOverload4() {}
    public MethodOverload4(char i) {}
    
    public static final String[] IDL_NAMES = {
        "create__",
        "create__wchar",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
    };
}

class MethodOverload5 {

    public MethodOverload5() {}
    public MethodOverload5(char i) {}
    public void create(){}
    
    public static final String[] IDL_NAMES = {
        "create__",
        "create__wchar",
        "create",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
        false,
    };
}

class MethodOverload6 {

    public MethodOverload6() {}
    public MethodOverload6(char i) {}
    public void create(){}
    public void create(char i){}
    
    public static final String[] IDL_NAMES = {
        "create____",
        "create__wchar__",
        "create__",
        "create__wchar",
    };
    
    public static final boolean[] CONSTRUCTOR = {
        true,
        true,
        false,
        false,
    };
}
