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

package corba.timer ;

import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.annotations.ExpectedExceptions ;

import com.sun.corba.se.spi.orbutil.newtimer.NamedBase ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerFactoryBuilder ;

// Test NamedBase
public class NamedBaseSuite {
    private String name = "MyName" ;
    private TimerFactory factory ;
    private NamedTest nb1 ;
    private NamedTest nb2 ;

    private static class NamedTest extends NamedBase {
	public NamedTest( TimerFactory factory, String name ) {
	    super( factory, name ) ;
	}

	public void finish( TimerFactory factory ) {
	    setFactory( factory ) ;
	}
    }

    @Configuration( beforeTest = true )
    public void setUp() {
	factory = TimerFactoryBuilder.make( "NTF", "No description" ) ;
	nb1 = new NamedTest( factory, name ) ;
	nb2 = new NamedTest( null, name ) ;
    }

    @Configuration( afterTest = true ) 
    public void tearDown() {
	TimerFactoryBuilder.destroy( factory ) ;
    }

    @Test() 
    public void name1() {
	Assert.assertEquals( name, nb1.name() ) ;
    }

    @Test() 
    public void name2() {
	Assert.assertEquals( name, nb2.name() ) ;
    }

    @Test()
    public void factory1() {
	Assert.assertEquals( factory, nb1.factory() ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void factory2() {
	TimerFactory tf = nb2.factory() ;
    }

    @Test()
    public void equals() {
	Assert.assertEquals( nb1, nb2 ) ;
    }

    @Test()
    public void hashCode1() {
	Assert.assertEquals( nb1.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void hashCode2() {
	Assert.assertEquals( nb2.hashCode(), name.hashCode() ) ;
    }

    @Test()
    public void toString1() {
	Assert.assertTrue( nb1.toString().contains( factory.name() + ":" + 
	    name ) ) ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void toString2() {
	String ts = nb2.toString() ;
    }

    @Test()
    @ExpectedExceptions( { IllegalStateException.class } ) 
    public void setFactory1() {
	nb1.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "toString2", "factory2" } ) 
    public void setFactory2() {
	nb2.finish( factory ) ;
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void factory2Post() {	
	Assert.assertEquals( factory, nb2.factory() ) ;	
    }

    @Test( dependsOnMethods = { "setFactory2" } )
    public void toString2Post() {	
	Assert.assertTrue( nb2.toString().contains( factory.name() + ":" + 
	    name ) ) ;
    }
}
