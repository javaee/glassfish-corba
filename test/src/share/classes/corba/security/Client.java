/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.security;

import java.security.ProtectionDomain ;
import java.security.Permission ;
import java.security.PermissionCollection ;
import java.security.Principal ;
import java.security.CodeSource ;
import java.security.cert.Certificate ;
import java.security.Policy ;

import java.util.Properties ;
import java.util.Enumeration ;

import java.io.PrintStream ;
import java.net.URL ;

public class Client 
{
    private PrintStream out ;
    private PrintStream err ;
    // private ORB orb ;

    public static void main(String args[])
    {
	System.out.println( "Starting Permission test" ) ;
        try{
	    Properties props = new Properties( System.getProperties() ) ;
	    props.put( "org.omg.CORBA.ORBClass", 
		"com.sun.corba.se.impl.orb.ORBImpl" ) ;
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
	//this.orb = (ORB)ORB.init( args, props ) ;
	this.out = System.out ;
	this.err = System.err ;

	runTests() ;
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
	RuntimeException exc = new RuntimeException( msg ) ;
	throw exc ;
    }
    
    private void info( String msg )
    {
	out.println( msg ) ;
    }


// *************************************************
// ***************   TESTS   ***********************
// *************************************************
    private void dumpPermissions( PermissionCollection pc ) 
    {
	Enumeration perms = pc.elements() ;
	while (perms.hasMoreElements()) {
	    Permission current = (Permission)perms.nextElement() ;
	    info( "\t\t" + current ) ;
	}
    }

    private void dumpProtectionDomain( String msg, ProtectionDomain pd ) 
    {
	CodeSource cs = pd.getCodeSource() ;
	Policy policy = Policy.getPolicy() ;
	PermissionCollection pc = policy.getPermissions( pd ) ;

	info( msg ) ;
	info( "\tCodeSource: " + cs ) ;
	info( "\tPermissions:" ) ;
	dumpPermissions( pc ) ;
    }

    private void dumpProtectionDomainForClass( Class cls )
    {
	dumpProtectionDomain( "ProtectionDomain for " + cls, 
	    cls.getProtectionDomain() ) ;
    }


    private void dumpProtectionDomainForPath( String path )
    {
	URL url = null ;

	try {
	    url = new URL( "file:" + path  + "/-" ) ;
	} catch (Exception exc) {
	    exc.printStackTrace( ) ;
	    System.exit(1) ;
	}

	CodeSource cs = new CodeSource( url, (Certificate[])null ) ;
	Policy policy = Policy.getPolicy() ;
	PermissionCollection pc = policy.getPermissions( cs ) ;
	info( "Permissions for code loaded from path " + path ) ;
	info( "URL: " + url ) ;
	info( "\tPermissionCollection:" ) ;
	dumpPermissions( pc ) ;
	info( "" ) ;
    }

    private Class getClass( String name ) 
    {
	try {
	    return Class.forName( name ) ;
	} catch (Exception exc) {
	    return null ;
	}
    }

    private void dumpProperty( String name ) 
    {
	info( "Property " + name + " has value " + 
	    System.getProperty( name ) ) ;
    }

    private void runTests()
    {
	info( "System.getSecurityManager() = " + System.getSecurityManager() ) ;
	dumpProperty( "com.sun.corba.se.POA.ORBServerId" ) ;
	dumpProperty( "com.sun.corba.se.ORBBase" ) ;
	dumpProperty( "java.security.policy" ) ;
	dumpProperty( "java.security.debug" ) ;
	dumpProperty( "java.security.manager" ) ;
	info( "" ) ;
	
	dumpProtectionDomainForPath(
	    System.getProperty( "com.sun.corba.se.ORBBase" ) + "/build" ) ;
	dumpProtectionDomainForPath(
	    System.getProperty( "com.sun.corba.se.ORBBase" ) + "/optional/build" ) ;
	dumpProtectionDomainForPath(
	    System.getProperty( "com.sun.corba.se.ORBBase" ) + "/test/build" ) ;

	dumpProtectionDomainForClass( 
	    com.sun.corba.se.spi.orb.ORB.class ) ;
	dumpProtectionDomainForClass( 
	    com.sun.corba.se.impl.orb.ORBImpl.class ) ;
	dumpProtectionDomainForClass( 
	    org.omg.CORBA.ORB.class ) ;
	dumpProtectionDomainForClass(
	    corba.security.Client.class ) ;

	Class cls = getClass( 
	    "com.sun.corba.se.spi.copyobject.OptimizedCopyobjectDefaults" ) ; 
	if (cls != null)
	    dumpProtectionDomainForClass( cls ) ;

	cls = getClass(
	    "com.sun.corba.se.impl.copyobject.newreflect.ClassCopier" ) ;
	if (cls != null)
	    dumpProtectionDomainForClass( cls ) ;
    }
}
