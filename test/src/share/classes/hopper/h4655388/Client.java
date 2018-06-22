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

package hopper.h4655388;

import java.io.PrintStream ;

import java.util.Properties ;
import java.util.LinkedList ;
import java.util.Iterator ;
import java.util.StringTokenizer ;
import java.util.Arrays ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.RequestProcessingPolicyValue ;

import org.omg.PortableServer.POAPackage.WrongPolicy ;
import org.omg.PortableServer.POAPackage.InvalidPolicy ;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists ;

import org.omg.PortableServer.portable.Delegate ;

import org.omg.CORBA.portable.ObjectImpl ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_PARAM ;
import org.omg.CORBA.INTERNAL ;
import org.omg.CORBA.OctetSeqHolder ;

import org.omg.CORBA.ORBPackage.InvalidName ;

import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

public class Client 
{
    private PrintStream out ;
    private PrintStream err ;
    private ORB orb ;

    public static void main(String args[])
    {
        System.out.println( "Starting POA hierarchy test" ) ;
        try{
            Properties props = System.getProperties() ;
            new Client( props, args, System.out, System.err ) ;
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public Client( Properties props, String args[], PrintStream out,
        PrintStream err )
    {
        this.orb = ORB.init( args, props ) ;
        this.out = System.out ;
        this.err = System.err ;

        runTests() ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
        throw new RuntimeException( msg ) ;
    }
    
    private void info( String msg )
    {
        out.println( msg ) ;
    }

 // *************************************************
// ***************   TESTS   ***********************
// *************************************************

    private void runTests()
    {
        String[] args = null ;
        ORB orb = ORB.init( args, null ) ;      
        POA rpoa = null ;
        try {
            rpoa = (POA)(orb.resolve_initial_references( "RootPOA" )) ;
        } catch (InvalidName err) {
            error( err.toString() ) ;
        }

        Policy[] policies = { rpoa.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_DEFAULT_SERVANT ) } ;

        POA cpoa = null ;

        try {
            cpoa = rpoa.create_POA( "Child1", rpoa.the_POAManager(), 
                policies ) ;    
        } catch (InvalidPolicy err) {
            error( err.toString() ) ;
        } catch (AdapterAlreadyExists err) {
            error( err.toString() ) ;
        }

        try {
            cpoa = rpoa.create_POA( "Child2", rpoa.the_POAManager(), 
                policies ) ;    
        } catch (InvalidPolicy err) {
            error( err.toString() ) ;
        } catch (AdapterAlreadyExists err) {
            error( err.toString() ) ;
        }

        // Without the fix for bug 4655388, this call fails with a 
        // ClassCastException.
        POA[] children = rpoa.the_children() ;

        if (children.length != 2)
            error( "Should have exactly 2 children" ) ;

        if (!children[0].the_name().equals( "Child1" ))
            if (!children[0].the_name().equals( "Child2" ))
                error( "children[0] is incorrect" ) ;

        if (!children[1].the_name().equals( "Child1" ))
            if (!children[1].the_name().equals( "Child2" ))
                error( "children[1] is incorrect" ) ;

        if (children[0].the_name().equals( children[1].the_name() ))
            error( "Both children have the same name" ) ;
    }
}
