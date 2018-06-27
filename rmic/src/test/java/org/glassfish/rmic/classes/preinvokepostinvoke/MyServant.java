/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.classes.preinvokepostinvoke;

import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

public class MyServant extends PortableRemoteObject implements Interface
{
    private org.omg.CORBA.ORB orb;
    public MyServant( org.omg.CORBA.ORB orb) throws RemoteException
    {
        this.orb = orb;
    }

    public String o1(String arg)
    {
        System.out.println( "Interface.o1 called with " + arg );
        System.out.flush( );
        try {
            org.omg.CORBA.Object objRef =
                 orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            objRef = ncRef.resolve_str( "Instance2" );
            Interface i2 =
                (Interface) PortableRemoteObject.narrow( objRef, Interface.class );
            i2.o2( "Invoking from Interface.o1..." );
        } catch( Throwable e ) {
            System.err.println( " Exception ..." + e );
            e.printStackTrace( );
            System.exit( 1 );
        }
        return "return value for interface.o1";
    }

    public String o2( String arg )
    {
        System.out.println( "Interface.o2 called with " + arg );
        System.out.flush( );
        return "return value for interface.o2";
    }
}

