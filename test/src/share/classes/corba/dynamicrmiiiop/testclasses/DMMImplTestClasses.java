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

package corba.dynamicrmiiiop.testclasses ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

public interface DMMImplTestClasses 
{
    public interface AllRemote
    {
        AllRemote make( AllRemote arg ) throws RemoteException ;

        AllRemote make( String arg ) throws RemoteException ;
    }
    
    public interface SomeRemote
    {
        SomeRemote make( SomeRemote arg ) throws RemoteException ;

        SomeRemote make( String arg ) ;
    }
    
    public interface NoRemote
    {
        NoRemote make( NoRemote arg ) ;
    }

    public interface NoMethods
    {
    }

    public interface IDLSimpleInterface extends org.omg.CORBA.portable.IDLEntity
    {
        void noop ();
    }

    public interface IDLValue extends org.omg.CORBA.portable.ValueBase
    {
        public abstract void foo ();

        public abstract int bar ();

    } 

    public final class IDLStruct implements org.omg.CORBA.portable.IDLEntity
    {
        public int arg1 = (int)0;
        public int arg2 = (int)0;

        public IDLStruct ()
        {
        }

        public IDLStruct (int _arg1, int _arg2)
        {
            arg1 = _arg1;
            arg2 = _arg2;
        } 
    } 

    public interface IDLInterface extends org.omg.CORBA.Object, 
        org.omg.CORBA.portable.IDLEntity 
    {
        String name ();
        void name (String newName);
    } 
}
