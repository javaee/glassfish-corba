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

package corba.orbconfig;

import java.io.PrintStream ;

import java.util.ArrayList ;
import java.util.List ;
import org.glassfish.pfl.basic.func.NullaryFunction;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.glassfish.pfl.test.ObjectUtility;

/** TestSession manages running of tests and checking results within
* a test session.  If the session fails any test, the whole session 
* fails with an Error, which can be used to trigger failure in the
* CORBA test framework.  This allows complete testing on a major subsystem
* to report a series of failures, while better containing failures by
* not testing related subsystems.
*/
public class TestSession 
{
    private JUnitReportHelper helper ;
    private PrintStream out ;
    private boolean errorFlag ;
    private String sessionName ;
    private List<String> failures ;

/////////////////////////////////////////////////////////////////////////////////////
// Public interface
/////////////////////////////////////////////////////////////////////////////////////

    public TestSession( PrintStream out, JUnitReportHelper helper )
    {
        this.out = System.out ;
        this.helper = helper ;
    }

    /** Print a message indicating the start of the session.
    * Also clears the error flag.
    */
    public void start( String sessionName ) 
    {
        this.sessionName = sessionName ;
        this.errorFlag = false ;
        out.println( "Test Session " + sessionName ) ;
        helper.start( sessionName ) ;
        failures = new ArrayList<String>() ;
    }

    /** Check for errors at the end of the session.
    */
    public void end()
    {
        if (errorFlag) {
            StringBuilder sb = new StringBuilder() ;
            sb.append( "Test failed with errors:\n" ) ;
            for (String thr : failures) {
                sb.append( "    " ) ;
                sb.append( thr ) ;
                sb.append( "\n" ) ;
            }
            String msg = sb.toString() ;
            helper.fail( msg ) ;
            throw new Error( msg ) ;
        } else {
            helper.pass() ;
        }
    }

    public void testForPass( String name, NullaryFunction<Object> closure,
        Object expectedResult )
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
    
    public void testForException( String name, NullaryFunction<Object> closure,
        Class expectedExceptionClass )
    {
        try {
            testStart( name ) ;
            closure.evaluate();
            testFail( "NullaryFunction<Object> did not throw expected exception" ) ;
        } catch (Throwable thr) {
            if (expectedExceptionClass.isAssignableFrom( thr.getClass() ))
                testPass( "with exception " + thr ) ;
            else
                testFail( "Unexpected exception" + thr ) ;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////
// Internal implementation
/////////////////////////////////////////////////////////////////////////////////////

    private void testStart( String name )
    {
        out.println( "\tTest " + name + "..." ) ;
    }

    private void testFail( String msg )
    {
        failures.add( msg ) ;
        out.println( "\t\tFAILED: " + msg ) ;
        errorFlag = true ;
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
    }
}
