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
package corba.ortremote ;

import java.io.PrintStream ;

import java.util.Properties ;
import java.util.LinkedList ;
import java.util.Iterator ;
import java.util.StringTokenizer ;
import java.util.Arrays ;
import java.util.Map ;
import java.util.Set ;
import java.util.List ;
import java.util.ListIterator ;
import java.util.Map.Entry ;
import java.util.HashMap ;

import com.sun.corba.se.spi.orbutil.closure.Closure ;

import com.sun.corba.se.spi.orbutil.misc.ObjectUtility ;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

/** TestSession manages running of tests and checking results within
* a test session.  If the session fails any test, the whole session 
* fails with an Error, which can be used to trigger failure in the
* CORBA test framework.  This allows complete testing on a major subsystem
* to report a series of failures, while better containing failures by
* not testing related subsystems.
*/
public class TestSession 
{
    private PrintStream out ;
    private boolean errorFlag ;
    private String sessionName ;
    private JUnitReportHelper helper ;

/////////////////////////////////////////////////////////////////////////////////////
// Public interface
/////////////////////////////////////////////////////////////////////////////////////

    public TestSession( PrintStream out, Class cls )
    {
	this.out = System.out ;
        helper = new JUnitReportHelper( cls.getName() ) ;
    }

    /** Print a message indicating the start of the session.
    * Also clears the error flag.
    */
    public void start( String sessionName ) 
    {
	this.sessionName = sessionName ;
	this.errorFlag = false ;
	out.println( "Test Session " + sessionName ) ;
    }

    /** Check for errors at the end of the session.
    */
    public void end()
    {
        helper.done() ;
	if (errorFlag)
	    throw new Error( "Test session " + sessionName + " failed" ) ;
    }

    public void testForPass( String name, Closure closure, Object expectedResult )
    {
	try {
	    testStart( name ) ;
	    Object result = closure.evaluate() ;

	    if (ObjectUtility.equals( result, expectedResult))
		testPass() ;
	    else {
		testFail( "Unexpected result returned" ) ;
		out.println( "\t\t\tExpected Result=" +
		    ObjectUtility.defaultObjectToString( expectedResult ) ) ;
		out.println( "\t\t\tActual   Result=" +
		    ObjectUtility.defaultObjectToString( result ) ) ;
	    }
	} catch (Throwable thr) {
	    testFail( "Unexpected exception " + thr ) ;
	    thr.printStackTrace() ;
	}
    }
    
    public void testForException( String name, Closure closure,
	Class expectedExceptionClass )
    {
	try {
	    testStart( name ) ;
	    closure.evaluate();
	    testFail( "Closure did not throw expected exception" ) ;
	} catch (Throwable thr) {
	    if (expectedExceptionClass.isAssignableFrom( thr.getClass() ))
		testPass( "with exception " + thr ) ;
	    else
		testFail( "Unexpected exception" + thr ) ;
	}
    }

    public void testAbort( String msg, Throwable thr )
    {
	out.println( "\t" + msg + ": Test aborted due to unexpected exception " + thr ) ;
	thr.printStackTrace() ;
        helper.done() ;
	System.exit( 1 ) ;
    }

/////////////////////////////////////////////////////////////////////////////////////
// Internal implementation
/////////////////////////////////////////////////////////////////////////////////////

    private void testStart( String name )
    {
	out.println( "\tTest " + name + "..." ) ;
        helper.start( name ) ;
    }

    private void testFail( String msg )
    {
	out.println( "\t\tFAILED: " + msg ) ;
	errorFlag = true ;
        helper.fail( msg ) ;
    }

    private void testPass() 
    {
	testPass( null ) ;
    }

    private void testPass( String msg )
    {
	out.print( "\t\tPASSED" ) ;
	if ((msg != null) && (msg != ""))
	    out.print( ": " + msg ) ;
	out.println() ;
        helper.pass() ;
    }
}
