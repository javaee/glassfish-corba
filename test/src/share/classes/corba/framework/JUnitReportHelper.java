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

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import corba.framework.junitreport.JUnitReportWriter ;
import corba.framework.junitreport.XMLJUnitReportWriter ;

import java.io.File ;
import java.io.FileOutputStream ;
import java.io.OutputStream ;

/** Helper class for generating reports for tests that do not adapt well to 
 * Testng/JUnit.  For example, several tests re-run the same test method and
 * class many times with different parameters.  JUnit does not support this at
 * all.  Testng does, but it is too much work to adapt these tests.  Instead,
 * we can just bracket test case execution with start/(pass|fail) calls.
 */
public class JUnitReportHelper {
    public class Counts extends Pair<Integer,Integer> {
        Counts( int numPass, int numFail ) {
            super( numPass, numFail ) ;
        }

        public int numPass() {
            return first() ;
        }

        public int numFail() {
            return second() ;
        }
    }

    private JUnitReportWriter writer ;
    private String className ;
    private String fileName ;
    private JUnitReportWriter.TestDescription current ;
    private boolean testComplete ;

    /** Prepare to generate a JUnitReport in the file named
     * ${junit.report.dir}/${name}.xml.  junit.report.dir is obtained from
     * the environment variable which is passed to all CTF controllers.
     * @param name The class name of the class for this test
     */
    public JUnitReportHelper( String className ) {
        current = null ;
        testComplete = false ;
        String processName = System.getProperty( "corba.test.process.name" ) ;
        if (processName != null) 
            this.fileName = className + "." + processName ;
        else
            this.fileName = className ;

        String outdirName = System.getProperty( "junit.report.dir" ) ; 
        if (outdirName == null)
            throw new RuntimeException( "property junit.report.dir is not set" ) ;

        File outdir = new File( outdirName ) ;
        if (!outdir.exists())
            throw new RuntimeException( outdir + " does not exist" ) ;

        if (!outdir.isDirectory())
            throw new RuntimeException( outdir + " is not a directory" ) ;

        OutputStream os = null ;

        try {
            File file = new File( outdir, this.fileName + ".xml" ) ;
            os = new FileOutputStream( file ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }

        writer = new XMLJUnitReportWriter() ;
        writer.setOutput( os ) ;
        writer.startTestSuite( fileName, System.getProperties() ) ;
    }

    // current must be non-null, and the test must not have been completed.
    // Reporting the completion of a test multiple times results in multiple
    // entries in the report, and double-counting of test case results.
    private void checkCurrent() {
        if (current == null)
            throw new RuntimeException( "No current test set!" ) ;

        if (testComplete) 
            throw new RuntimeException( "Test " + current + " has already been completed!" ) ;

        testComplete = true ;
    }

    /** Start executing a test case with the given name.
     * All names MUST be unique for an instance of JUnitReportHelper.
     * @param The name of the test case
     */
    public void start( String name ) {
        if ((current != null) && !testComplete)
            throw new RuntimeException( "Trying to start test named " + name 
                + " before current test " + current + " has completed!" ) ;

        testComplete = false ;

        current = new JUnitReportWriter.TestDescription( name, className ) ;
        writer.startTest( current ) ;
    }

    /** Report that the current test passed.
     */
    public void pass() {
        checkCurrent() ;

        writer.endTest( current ) ;
    }

    public void fail( String msg ) {
        fail( new AssertionError( msg ) ) ;
    }

    /** Report that the current test failed with the given exception
     * as cause.
     */
    public void fail( Throwable thr ) {
        checkCurrent() ;

        if (thr instanceof AssertionError)
            writer.addFailure( current, thr ) ; 
        else
            writer.addError( current, thr ) ; 

        writer.endTest( current ) ;
    }

    private Counts counts = null ;

    /** Testing is complete.  Calls to start, pass, or fail after
     * this call will result in an IllegalStateException.
     * This method may be called multiple times, but only the first
     * call will write a report.
     */
    public Counts done() {
        if ((current != null) && !testComplete)
            throw new RuntimeException( "Trying to terminate test suite before current test " 
                + current + " has completed!" ) ;

        if (counts == null) {
            JUnitReportWriter.TestCounts tc = writer.endTestSuite() ;
            counts = new Counts( tc.pass(), tc.fail() + tc.error() ) ;
        }

        return counts ;
    }
}
