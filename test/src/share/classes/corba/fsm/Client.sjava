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

package corba.fsm ;
import java.util.Iterator ;
import java.util.Properties ;
import java.util.Map ;
import java.util.List ;
import java.util.ArrayList ;

import java.io.PrintWriter ;

import org.testng.TestNG ;
import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.se.spi.orb.ORB ;


import com.sun.corba.se.impl.orbutil.newtimer.VersionedHashSet ;
import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

public class Client {
    private Foo extends NameBase {
	public Foo( String name ) {
	    super( name ) ;
	}
    }

    @Test()
    public void testNameBase() {
	String name = "Blue" ;
	Foo foo = new Foo( name ) ;
	assertEquals( name, foo.getName() ) ;
	assertEquals( "Client$Foo[" + name + "]", foo.toString() ) ;
    }

    private int value ;

    private ArithAction extends Action {
	private int mult ;
	private int add ;

	public ArithAction( int mult, int add ) {
	    super( "DiffAction(" + mult + "X + " + add + ")" ) ;
	    this.mult = mult ;
	    this.add = add ;
	}

	public void doIt( Runner runner, Input in ) {
	    value = mult * value + add ;
	}
    }

    @Test() 
    public void testAction() {
	Action act1 = new ArithAction( 2, 1 ) ;
	String act1Str = "Action ArithAction(2X + 1)" ;

	Action act2 = new ArithAction( 4, 2 ) ;
	String act2Str = "Action ArithAction(4X + 2)" ;

	Action comp = Action.Base.compose( act1, act2 ) ;

	assertEquals( act1.toString(), act1Str ) ;
	assertEquals( act2.toString(), act2Str ) ;
	assertEquals( comp.toString(), 
	    "Action compose(" + arg1Str + "," + arg2Str + ")" ) ;

	value = 1 ;
	act1.doIt( null, null ) ;
	assertEquals( 3, value ) ;
	ac2.doIt( null, null ) ;
	assertEquals( 14, value ) ;

	value = 1 ;
	comp.doIt( null, null ) ;
	assertEquals( 14, value ) ;
    }

    public static void main( String[] args ) {
	TestNG tng = new TestNG() ;
	tng.setOutputDirectory( "gen/corba/fsm/test-output" ) ;
	Class[] tngClasses = new Class[] {
	    Client.class
	} ;

	tng.setTestClasses( tngClasses ) ;
	tng.run() ;
	System.exit( tng.hasFailure() ? 1 : 0 ) ;
    }
}

