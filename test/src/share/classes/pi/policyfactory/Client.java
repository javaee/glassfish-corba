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
package pi.policyfactory;

import com.sun.corba.se.impl.interceptors.*;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.corba.*;
import org.omg.PortableInterceptor.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.BAD_POLICY;
import org.omg.CORBA.Request;
import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.StructMember;
import org.omg.CORBA.NamedValue;
import org.omg.CORBA.NVList;
import org.omg.CORBA.ExceptionList;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.Context;
import org.omg.CORBA.Object;
import java.util.Properties;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

public class Client implements Runnable
{

    static final java.lang.Object lock = new java.lang.Object ();
    static boolean errorOccured = false;

    static ORB orb;

    private static boolean SUCCESS = true;

    private static boolean FAILURE = false;

    private String msg = null ;

    public void signalError () {
        synchronized (Client.lock) {
            errorOccured = true;
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        new Client().run();
    }

    public void run()
    {
        JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;
        try {
            // create and initialize the ORB
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass",
                       "com.sun.corba.se.impl.orb.ORBImpl" );
            props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                       "pi.policyfactory.TestORBInitializer", "" );
            orb = ORB.init( (String[]) null, props );

            boolean testStatus = SUCCESS;
            // Test ClientRequestInfo.arguments() method.
            helper.start( "positiveTest" ) ;
            testStatus = positiveTest();
            if( testStatus == SUCCESS ) {
                System.out.println( "PolicyFactory positive tests Success" );
                System.out.flush();
                helper.pass() ;
            } else {
                System.err.println( "PolicyFactory positive tests Failure" );
                System.err.flush();
                signalError (); 
                helper.fail( msg ) ;
            }

            helper.start( "negativeTest" ) ;
            testStatus = negativeTest();
            if( testStatus == SUCCESS ) {
                System.out.println( "PolicyFactory negative tests Success" );
                System.out.flush();
                helper.pass() ;
            } else {
                System.err.println( "PolicyFactory negative tests Failure" );
                System.err.flush();
                signalError (); 
                helper.fail( msg ) ;
            }
        } catch( Exception e ) {
            System.err.println( "PolicyFactory test Failed with exception" + e);
            System.err.flush();
            signalError (); 
        } finally {
            helper.done() ;
        }
    }

    /** This method tests 
     *  1. To see whether the Policy created with type 100 is created from 
     *     PolicyFactoryHundred. This check is made by testing 
     *     whether policy.policy_type method returns 100.
     *  2. To see whether the Policy created with type 10000 is created from 
     *     PolicyFactoryThousandPlus. This check is made by testing 
     *     whether policy.policy_type method returns 10000.
     */
    private boolean positiveTest( ) {
        org.omg.CORBA.Policy policy = null;
        Any any = orb.create_any() ;
        try {
            policy = orb.create_policy( 100, any );
        }
        catch( Exception e) {
            msg = "PolicyFactoryTest.positiveTest failed with " + " an Exception " + e ;
            System.err.println( msg ) ;
            System.err.flush( );
            e.printStackTrace();
            return FAILURE;
        }
        if( policy == null ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy is not created as expected " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }
        if( policy.policy_type() != 100 ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy.policy_type() != 100 " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }   
        try {
            policy = orb.create_policy( 10000, any );
        } catch( Exception e ) {  
            msg = "PolicyFactoryTest.positiveTest failed with " +
                " an Exception " + e ;
            System.err.println( msg ) ;
            System.err.flush( );
            e.printStackTrace();
            return FAILURE;
        }
        if( policy == null ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy is not created as expected " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }
        if( policy.policy_type() != 10000 ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy.policy_type() != 10000 " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }   
        return SUCCESS;
    }

    /** This method tests to see whether the Policy could be created with 
     *  type 100000 for which there is no PolicyFactory registered.
     *  Before invoking this methos the ORBInitializer (TestORBInitializer)
     *  registers 3 policy factories with types 100, 1000 and 1000000. If the 
     *  call to create policy with type 100000 does not raise policy error 
     *  then it's an error.
     */
    private boolean negativeTest( ) {
        try {
            Any any = orb.create_any() ;
            org.omg.CORBA.Policy policy = orb.create_policy( 100000, any );
        } 
        catch( org.omg.CORBA.PolicyError e ) {
            msg = "Caught org.omg.CORBA.PolicyError in " +
                "PolicyFactory.negativeTest() as expected..." ;
            System.out.println( msg ) ;
            System.out.flush( );
            if( e.reason != BAD_POLICY.value ) {
                return FAILURE;
            }
            return SUCCESS;
        }
        return FAILURE;
    }
       
}
