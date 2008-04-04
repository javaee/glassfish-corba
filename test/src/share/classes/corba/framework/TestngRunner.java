/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.framework ;

import java.io.File ;
import java.io.OutputStream ;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties ;
import java.util.Set ;
import java.util.HashSet ;
import java.util.HashMap ;
import java.util.Map ;
import java.util.List ;

import org.testng.TestNG ;
import org.testng.IReporter ;
import org.testng.ITestResult ;
import org.testng.ISuiteListener ;
import org.testng.ISuite ;
import org.testng.ISuiteResult ;
import org.testng.ITestListener ;
import org.testng.ITestContext ;
import org.testng.IResultMap ;

import corba.framework.junitreport.JUnitReportWriter ;
import corba.framework.junitreport.XMLJUnitReportWriter ;

/** Used to set up an appropriate instance of TestNG for running a test.
 * Used inside the CORBA test framework in order to generate useful reports
 * in JUnitReport format for integration with Hudson.
 */
public class TestngRunner {
    private Set<Class<?>> suiteClasses ;
    private TestNG testng ;
    private String outdirName ;
    private boolean hasFailure ;

/*
    static String statusString( int status ) {
        switch (status) {
            case ITestResult.SUCCESS : 
                return "SUCCESS" ;
            case ITestResult.FAILURE : 
                return "FAILURE" ;
            case ITestResult.SKIP : 
                return "SKIP" ;
            case ITestResult.SUCCESS_PERCENTAGE_FAILURE : 
                return "SUCCESS_PERCENTAGE_FAILURE" ;
            case ITestResult.STARTED : 
                return "STARTED" ;
            default :
                return "*UNKNOWN*" ;
        }
    }

    private class JUnitReportSuiteListener implements ISuiteListener {
        private final JUnitReportWriter writer ;

        JUnitReportSuiteListener() {
            writer = new XMLJUnitReportWriter() ;
        }

        private void msg( String str ) {
            System.out.println( str ) ;
        }

        public void onStart( ISuite suite ) {
            msg( "JUnitReportSuiteListener: onStart" ) ;
            String name = suite.getName() ;

            try {
                File dir = new File( outdirName ) ;
                File file = new File( dir, name + ".xml" ) ;
                OutputStream os = new FileOutputStream( file ) ;
                writer.setOutput( os ) ;
            } catch (IOException exc) {
                throw new RuntimeException( exc ) ;
            }

            Properties props = System.getProperties() ;

            writer.startTestSuite( name, props ) ;
        }

        public void onFinish( ISuite suite ) {
            msg( "JUnitReportSuiteListener: onFinish" ) ;

            for (Map.Entry<String,ISuiteResult> entry : suite.getResults().entrySet()) {
                ITestContext tc = entry.getValue().getTestContext() ;
                for (ITestResult result : tc.getFailedTests().getAllResults()) {
                    Throwable err = result.getThrowable() ;

                    JUnitReportWriter.TestDescription td = new JUnitReportWriter.TestDescription( 
                        result.getName(), result.getTestClass().getName() ) ;
                
                    writer.startTest( td ) ;

                    if (err instanceof AssertionError)
                        writer.addFailure( td, err ) ; 
                    else
                        writer.addError( td, err ) ; 

                    writer.endTest( td ) ;
                }
                    
                for (ITestResult result : tc.getPassedTests().getAllResults()) {
                    JUnitReportWriter.TestDescription td = new JUnitReportWriter.TestDescription( 
                        result.getName(), result.getTestClass().getName() ) ;
                
                    writer.startTest( td ) ;
                    writer.endTest( td ) ;
                }
                
                // XXX What about skipped tests?
            }
                    
            writer.endTestSuite() ;
        }
    }
*/

    private class JUnitReportTestListener implements ITestListener {
        private final JUnitReportWriter writer ;
        private JUnitReportWriter.TestDescription current ;
        private String name ;

        JUnitReportTestListener( String name ) {
            writer = new XMLJUnitReportWriter() ;
            this.name = name ;
        }

        private void msg( String str ) {
            System.out.println( str ) ;
        }

        public void onStart( ITestContext context ) {
            // msg( "TestListener: onStart" ) ;
            // msg( "  context: name=" + context.getName() ) ;
            try {
                File dir = new File( outdirName ) ;
                File file = new File( dir, "TEST-" + name + ".xml" ) ;
                OutputStream os = new FileOutputStream( file ) ;
                writer.setOutput( os ) ;
            } catch (IOException exc) {
                throw new RuntimeException( exc ) ;
            }

            Properties props = System.getProperties() ;

            writer.startTestSuite( name, props ) ;
        }

        public void onFinish( ITestContext context ) {
            // msg( "TestListener: onStart" ) ;
            // msg( "  context: name=" + context.getName() ) ;
            writer.endTestSuite() ;
        }

        public void onTestStart( ITestResult result ) {
            // msg( "TestListener: onTestStart" ) ;
            current= new JUnitReportWriter.TestDescription( 
                result.getName(), result.getTestClass().getName() ) ;
            writer.startTest( current ) ;
        }

        public void onTestSkipped( ITestResult result ) {
            // msg( "TestListener: onTestSkiiped" ) ;

            writer.addError( current, new RuntimeException( "Test was skipped" ) ) ;

            writer.endTest( current ) ;
        }

        public void onTestFailure( ITestResult result ) {
            // msg( "TestListener: onTestFailure" ) ;
            Throwable err = result.getThrowable() ;

            if (err instanceof AssertionError)
                writer.addFailure( current, err ) ; 
            else
                writer.addError( current, err ) ; 

            writer.endTest( current ) ;
        }

        public void onTestSuccess( ITestResult result ) {
            // msg( "TestListener: onTestSuccess" ) ;
            writer.endTest( current ) ;
        }

        public void onTestFailedButWithinSuccessPercentage( ITestResult result ) {
            // msg( "TestListener: onTestFailedButWithinSuccessPercentage" ) ;
            writer.endTest( current ) ;
        }
    }

/*
    // Temporary listeners to understand how TestNG works
    private class SuiteListener implements ISuiteListener {
        private void msg( String str ) {
            System.out.println( str ) ;
        }

        private void displayIResultMap( IResultMap map ) {
            for (ITestResult result : map.getAllResults() ) {
                long duration = result.getEndMillis() - result.getStartMillis() ;
                msg( "        name=" + result.getName()  
                    + " status=" + statusString( result.getStatus() )
                    + " " + duration + " (msec) " ) ;
            }
        }

        private void displaySuite( ISuite suite ) {
            msg( "  name = " + suite.getName() ) ;
            msg( "  output directory = " + suite.getOutputDirectory() ) ;
            msg( "  results:" ) ;
            for (Map.Entry<String,ISuiteResult> entry : suite.getResults().entrySet()) {
                msg( "    " + entry.getKey() + ":" ) ;
                ITestContext tc = entry.getValue().getTestContext() ;

                msg( "      Failed tests:" ) ;
                displayIResultMap( tc.getFailedTests() ) ;

                msg( "      Passed tests:" ) ;
                displayIResultMap( tc.getPassedTests() ) ;

                msg( "      Skipped tests:" ) ;
                displayIResultMap( tc.getSkippedTests() ) ;
            }
        }

        public void onStart( ISuite suite ) {
            msg( "SuiteListener: onStart" ) ;
            displaySuite( suite ) ;
        }

        public void onFinish( ISuite suite ) {
            msg( "SuiteListener: onFinish" ) ;
            displaySuite( suite ) ;
        }
    }

    private class TestListener implements ITestListener {
        private void msg( String str ) {
            System.out.println( str ) ;
        }

        public void onStart( ITestContext context ) {
            msg( "TestListener: onStart" ) ;
            msg( "  context: name=" + context.getName() ) ;
        }

        public void onFinish( ITestContext context ) {
            msg( "TestListener: onStart" ) ;
            msg( "  context: name=" + context.getName() ) ;
        }

        private void displayTestResult( ITestResult result ) {
            long duration = result.getEndMillis() - result.getStartMillis() ;
            msg( "  name=" + result.getName()  
                + " status=" + statusString( result.getStatus() )
                + " " + duration + " (msec) " ) ;
        }

        public void onTestStart( ITestResult result ) {
            msg( "TestListener: onTestStart" ) ;
            displayTestResult( result ) ;
        }

        public void onTestSkipped( ITestResult result ) {
            msg( "TestListener: onTestSkiiped" ) ;
            displayTestResult( result ) ;
        }

        public void onTestFailure( ITestResult result ) {
            msg( "TestListener: onTestFailure" ) ;
            displayTestResult( result ) ;
        }

        public void onTestSuccess( ITestResult result ) {
            msg( "TestListener: onTestSuccess" ) ;
            displayTestResult( result ) ;
        }

        public void onTestFailedButWithinSuccessPercentage( ITestResult result ) {
            msg( "TestListener: onTestFailedButWithinSuccessPercentage" ) ;
            displayTestResult( result ) ;
        }
    }
*/

    /** Create a new TestngRunner.
     * @param outdir The directory in which the test reports should be placed.
     */
    public TestngRunner() {
        outdirName = System.getProperty( "junit.report.dir" ) ; 
        if (outdirName == null)
            throw new RuntimeException( "property junit.report.dir is not set" ) ;

        File outdir = new File( outdirName ) ;
        if (!outdir.exists())
            throw new RuntimeException( outdir + " does not exist" ) ;

        if (!outdir.isDirectory())
            throw new RuntimeException( outdir + " is not a directory" ) ;

        suiteClasses = new HashSet<Class<?>>() ;
        hasFailure = false ;
    }

    /** Register a class container TestNG annotations on test methods.
     * The test report is generated in outdir under the name TEST-<classname>.xml.
     * Note that we assume that each suite is represented by a unique class.
     */
    public void registerClass( Class<?> cls ) {
        suiteClasses.add( cls ) ;
    }

    /** Run the tests in the registered classes and generate reports.
     */
    public void run() {
        for (Class<?> cls : suiteClasses ) {
            testng = new TestNG() ;
            // testng.setDefaultSuiteName( "TEST-" + cls.getName() + ".xml" ) ;
            testng.setTestClasses( new Class<?>[] { cls } ) ;
            testng.setOutputDirectory( outdirName )  ;

            //testng.addListener( new JUnitXMLReporter() ) ;
            //testng.addListener( new SuiteListener() ) ;
            testng.addListener( new JUnitReportTestListener( "TEST-" + cls.getName() ) ) ;
            //testng.addListener( new TestListener() ) ;

            testng.run() ;
            if (testng.hasFailure())
                hasFailure = true ;
        }
    }

    public boolean hasFailure() {
        return hasFailure ;
    }

    public void systemExit() {
        if (hasFailure) 
            System.exit( 1 ) ;
        else 
            System.exit( 0 ) ;
    }
}
