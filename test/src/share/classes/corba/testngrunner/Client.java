/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.testngrunner ;

import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;

import java.io.PrintWriter ;

import org.testng.Assert ;
import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.AfterSuite ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import corba.framework.TestngRunner ;

public class Client {
    private void msg( String str ) {
        System.out.println( "TestngRunner.Client: " + str ) ;
    }

    @BeforeSuite
    public void setup() {
        msg( "setup called" ) ;
    }

    @Test
    public void test1() {
        msg( "test1 called" ) ;
    }

    @Test
    public void test2() {
        msg( "test2 called" ) ;
    }

    @Test
    public void test3() {
        msg( "test3 called" ) ;
        throw new RuntimeException( "Exception in test3" ) ;
    }

    @Test
    public void test4() {
        msg( "test4 called" ) ;
    }

    @Test
    public void test5() {
        msg( "test5 called" ) ;
        Assert.fail( "test5 failed" ) ;
    }

    @Test
    public void test6() {
        msg( "test6 called" ) ;
    }
    
    @AfterSuite
    public void shutdown() {
        msg( "shutdown called" ) ;
    }

    public static void main( String[] args ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.registerClass( Second.class ) ;
        runner.run() ;
	System.exit( runner.hasFailure() ? 1 : 0 ) ;
    }
}
