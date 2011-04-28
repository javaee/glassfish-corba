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
package corba.evolve;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;

import org.omg.CORBA.ORB ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NameComponent ;

import org.testng.annotations.Test ;
import org.testng.annotations.BeforeSuite ;
import org.testng.Assert ;
import org.testng.TestNG ;

import mymath.BigDecimal ;

import com.sun.corba.se.spi.misc.ORBConstants ;


import corba.framework.TestngRunner ;
import mymath.BigDecimal ;

public class Client
{
    private UserNameVerifier verifier ;
    private ORB orb ;

    @BeforeSuite 
    public void setup() throws Exception {
        String[] args = new String[0] ;
        orb = ORB.init(args, System.getProperties());

        System.getProperties().setProperty( ORBConstants.INITIAL_PORT_PROPERTY, 
            "1049" ) ;

        orb = ORB.init(args, System.getProperties());

        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);

        NameComponent nc = new NameComponent("UserNameVerifier", "");
        NameComponent path[] = {nc};

        org.omg.CORBA.Object obj = ncRef.resolve(path);

        verifier = (UserNameVerifier) PortableRemoteObject.narrow(
            obj, UserNameVerifier.class);
    }

    @Test
    public void testUserName() throws Exception {
        Class userNameClass = Class.forName("UserName");
        UserNameInt localName = (UserNameInt)userNameClass.newInstance();
        
        System.out.println("Trying to send a UserName...");
        verifier.verifyName(localName);

        System.out.println("Requesting a name...");
        UserNameInt testName = verifier.requestName();
        Assert.assertTrue( testName != null && testName.validate());
    }

    @Test
    public void testUserNameRO() throws Exception {
        Class userNameClass = Class.forName("UserNameRO");
        UserNameInt localName = (UserNameInt)userNameClass.newInstance();
        
        System.out.println("Trying to send a UserName...");
        verifier.verifyName(localName);

        System.out.println("Requesting a name...");
        UserNameInt testName = verifier.requestName();
        Assert.assertTrue( testName != null && testName.validate());
    }

    @Test
    public void testUserNameROD() throws Exception {
        Class userNameClass = Class.forName("UserNameROD");
        UserNameInt localName = (UserNameInt)userNameClass.newInstance();
        
        System.out.println("Trying to send a UserName...");
        verifier.verifyName(localName);

        System.out.println("Requesting a name...");
        UserNameInt testName = verifier.requestName();
        Assert.assertTrue( testName != null && testName.validate());
    }

    @Test
    public void testFeatureInfo() throws Exception {
        System.out.println( "Requesting a FeatureInfo" ) ;
        FeatureInfo finfo = verifier.getFeatureInfo() ;

        System.out.println("Validating the FeatureInfo" ) ;
        Assert.assertTrue( verifier.validateFeatureInfo( finfo ) ) ;
    }

    @Test
    public void testBigDecimal() throws Exception {
        System.out.println( "Testing BigDecimal interop" )  ;
        BigDecimal orig = new BigDecimal( "123456789012345678901234567890.12312312312" ) ;

        // setSerializationDebug( true ) ;
        BigDecimal result = (BigDecimal)verifier.echo( orig ) ;
        // setSerializationDebug( false ) ;
        
        Assert.assertEquals( orig, result ) ;
    }

    @Test
    public void testWithoutPrimitives() throws Exception {
        System.out.println( "Testing WithoutPrimitives interop" )  ;
        WithoutPrimitives orig = new WithoutPrimitives() ;

        // setSerializationDebug( true ) ;
        WithoutPrimitives result = (WithoutPrimitives)verifier.echo( orig ) ;
        // setSerializationDebug( false ) ;
        
        Assert.assertEquals( orig, result ) ;
    }

    private void setSerializationDebug( boolean flag ) {
        com.sun.corba.se.spi.orb.ORB morb =
            (com.sun.corba.se.spi.orb.ORB)orb ;
        if (flag) {
            morb.setDebugFlags( "cdr", "streamFormatVersion", "valueHandler" ) ;
        } else {
            morb.clearDebugFlags( "cdr", "streamFormatVersion", "valueHandler" ) ;
        }
    }

    public static void main(String args[]) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
