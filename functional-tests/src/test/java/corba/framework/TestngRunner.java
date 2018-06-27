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

package corba.framework ;

import java.io.File ;

import java.util.Set ;
import java.util.HashSet ;
import org.glassfish.pfl.test.JUnitReportHelper;

import org.testng.TestNG ;
import org.testng.ITestResult ;
import org.testng.ITestListener ;
import org.testng.ITestContext ;

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
        final String propName = "junit.report.dir" ;

        String reportDir = System.getProperty( propName ) ; 
        if (reportDir == null) {
            System.setProperty( propName, "." ) ;
            reportDir = "." ;
        }

        File outdir = new File( reportDir ) ;
        if (!outdir.exists())
            throw new RuntimeException( outdir + " does not exist" ) ;

        if (!outdir.isDirectory())
            throw new RuntimeException( outdir + " is not a directory" ) ;

        outdirName = reportDir + File.separatorChar + 
            System.getProperty( "corba.test.controller.name", "default" ) ;

        File destDir = new File( outdirName ) ;
        destDir.mkdir() ;

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
