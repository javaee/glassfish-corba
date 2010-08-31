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

/**
 * Invalid RMI/IDL Remote Interface Types
 */ 
public class InvalidRemotes {

    // should extend java.rmi.Remote, either directly or indirectly
    public interface InvalidRemote1 {}
    
    // should extend java.rmi.Remote, either directly or indirectly 
    public interface InvalidRemote2 extends InvalidRemote1 {}

    // RMI/IDL Exceptions should not extend java.rmi.Remote, either directly
    // or indirectly
    public class InvalidException1 extends java.lang.Exception
        implements java.rmi.Remote {}

    // RMI/IDL Exceptions should not extend java.rmi.Remote, either directly
    // or indirectly
    public class InvalidException2 extends InvalidException1 {}

    // contains method with invalid exception type
    public interface InvalidRemote3 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, InvalidException1;
    }

    // contains method with invalid exception type
    public interface InvalidRemote4 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, InvalidException2;
    }

    // Each remote method should throw java.rmi.RemoteException or one of its
    // super-class exception types.
    public interface InvalidRemote5 extends java.rmi.Remote {
        public void foo1();
    }    

    // contains method with invalid exception type
    public interface InvalidRemote6 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, java.lang.Error;
    } 

    // contains method with invalid exception type
    public interface InvalidRemote7 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            java.lang.RuntimeException;
    } 

    private class InvalidException3 extends java.lang.RuntimeException {}
    // contains method with invalid exception type
    public interface InvalidRemote8 extends java.rmi.Remote {
        public void foo1() throws java.rmi.RemoteException, 
            InvalidException3;
    } 
    
    // has a field other than primitive or String
    public interface InvalidRemote9 extends java.rmi.Remote {
        Object o = null;
    }

    private interface A {
        void foo() throws java.rmi.RemoteException;
    }

    private interface B {
        void foo() throws java.rmi.RemoteException;
    }

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote10 extends java.rmi.Remote, A, B {}

    private interface C extends A {}
    private interface D extends B {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name.  
    public interface InvalidRemote11 extends java.rmi.Remote, C, D {}
    
    private interface E {
        void foo() throws java.rmi.RemoteException;
    }

    private interface F {
        void foo(int a) throws java.rmi.RemoteException;
    }

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote12 extends java.rmi.Remote, E, F {}

    private interface G extends E {}
    private interface H extends F {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote13 extends java.rmi.Remote, G, H {}

    // can't directly inherit from multiple base interfaces which define a
    // method with the same name
    public interface InvalidRemote14 extends G, java.rmi.Remote, H {}

    
    // can't directly inherit from multiple base interfaces which define a
    // method with the same name.  doesn't matter if a method with the same
    // name is defined in the most derived interface
    public interface InvalidRemote15 extends G, java.rmi.Remote, H {
        void foo() throws java.rmi.RemoteException;
    }

    // must be an interface
    public class InvalidRemote16 {}

    // illegal constant type. must be primitive or String
    public interface InvalidRemote17 extends java.rmi.Remote {
        int[] FOO = { 1, 2, 3 };
    }
    
    // applying mangling rules results in clash
    public interface InvalidRemote18 extends java.rmi.Remote {
        void J_foo() throws java.rmi.RemoteException;
        void _foo() throws java.rmi.RemoteException;
    }

    // applying mangling rules results in clash
    public interface InvalidRemote19 extends java.rmi.Remote {
        void foo() throws java.rmi.RemoteException;
        void foo(int a) throws java.rmi.RemoteException;
        void foo__long() throws java.rmi.RemoteException;
    }
    

}
