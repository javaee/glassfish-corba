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

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

/** Used to set up an appropriate instance of TestNG for running a test.
 * Used inside the CORBA test framework in order to generate useful reports
 * in JUnitReport format for integration with Hudson.
 */
public class TestngRunner {
    private Set<Class<?>> suiteClasses ;
    private TestNG testng ;
    private String outdirName ;
    private boolean hasFailure ;

    private class JUnitReportTestListener implements ITestListener {
        private JUnitReportHelper helper ;

        JUnitReportTestListener( String name ) {
            helper = new JUnitReportHelper( name ) ;
        }

        private void msg( String str ) {
            System.out.println( str ) ;
        }

        public void onStart( ITestContext context ) {
        }

        public void onFinish( ITestContext context ) {
            helper.done() ;
        }

        public void onTestStart( ITestResult result ) {
            helper.start( result.getName() ) ;
        }

        public void onTestSkipped( ITestResult result ) {
            helper.fail( "Test was skipped" ) ;
        }

        public void onTestFailure( ITestResult result ) {
            Throwable err = result.getThrowable() ;

            helper.fail( err ) ;
        }

        public void onTestSuccess( ITestResult result ) {
            helper.pass() ;
        }

        public void onTestFailedButWithinSuccessPercentage( ITestResult result ) {
            helper.pass() ;
        }
    }

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
     * The test report is generated in outdir under the name <classname>.xml.
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
            testng.setTestClasses( new Class<?>[] { cls } ) ;
            testng.setOutputDirectory( outdirName )  ;
            testng.setDefaultSuiteName( cls.getName() ) ;
            testng.setDefaultTestName( cls.getName() ) ;
            testng.addListener( new JUnitReportTestListener( cls.getName() ) ) ;
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
