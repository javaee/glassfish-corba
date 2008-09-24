/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.enuminterop  ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import java.rmi.UnexpectedException ;

import java.io.Serializable ;
import java.io.Externalizable ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.SystemException ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.ResponseHandler ;
import org.omg.CORBA.portable.UnknownException ;
import org.omg.CORBA.portable.Delegate ;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.CosNaming.*;
import org.omg.CORBA.ORB;

import java.util.Map ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.Properties ;

import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException ;

import javax.naming.NamingException;
import javax.naming.InitialContext;
import javax.naming.Context;

import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Util;

import corba.framework.*;
import java.util.*;
import java.io.*;

import com.sun.corba.se.spi.presentation.rmi.StubAdapter ;

import org.testng.Assert ;
import org.testng.annotations.Test ;

/**
 * This tests that enums can be correctly deserialized when sent from the JDK ORB (no EnumDesc support)
 * to GlassFish, which supports EnumDesc.  We may also add a config flag to allow testing between two
 * GlassFish ORB instances.
 *
 * Basic test: have server run on JDK ORB (or GF with noEnumDesc configuration), and
 * then see if the client can correctly receive an echoed enum from the server.
 */
public class Client
{
    private PrintStream out ;
    private PrintStream err ;
    private NamingContextExt nctx = null;
    private Echo echo = null;
    private ORB orb;

    private static String[] args;

    public static void main( String[] args ) 
    {
        Client.args = args ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
    }

    public Client() throws Exception {
	this.out = System.out;
	this.err = System.err;

        orb = ORB.init( args, null );

        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");

        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent(Server.REF_NAME, "");
        NameComponent[] path = {nc};
                                                                            
        echo = (Echo)PortableRemoteObject.narrow(ncRef.resolve(path),
                                                   Echo.class);
    }

    @Test
    public void testEcho() throws RemoteException {
        Echo.Day result = (Echo.Day)echo.echoObject( "Sunday" ) ;
        Assert.assertSame( result, Echo.Day.Sunday ) ;
    }

    @Test
    public void testEchoDay() throws RemoteException {
        Echo.Day result = echo.echoDay( "Tuesday" ) ;
        Assert.assertSame( result, Echo.Day.Tuesday ) ;
    }
}
