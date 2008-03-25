/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.copyobject  ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.se.spi.copyobject.CopyobjectDefaults ;
import com.sun.corba.se.spi.orb.ORB ;

import junit.framework.Test ;

public class ORBStreamTest extends Client
{
    // Mostly these fail because they are not Serializable.
    // TypeCode test failures are currently unknown, but have something
    // to do with not finding the appropriate helper class.
    // I'm not sure what the UserException problem is, but org.omg.CORBA.UserException
    // is an abstract class (see Client.throwUserException).
    // testIdentityHashMap seems to be failing because the elements come out in a
    // different order?
    // testExternalizable fails because readExternal and writeExternal are not written
    // to write the data.
    // I have not explored the other failures, but this list should be reduced to
    // those tests that are correct for reflective copy and cannot work for stream
    // copy.
    private static final String[] EXCLUDE_LIST = new String[] {
	// The first three of these should be looked into
	"testImmutableClassArray",
	"testImmutableClassArrayAlias",
	"testHashMapComplex",
	"testObject", "testTimedObject", "testObjects", "testComplexClassArray",
	"testComplexClassAliasedArray", "testComplexClassGraph",
	"testSimpleTypeCode", "testUnionTypeCode", "testValuetypeTypeCode",
	"testRecursiveTypeCode", "testUserException", 
	"testRemoteStub", "testCORBAObject", "testInnerClass", 
	"testExtendedInnerClass", "testNestedClass", "testLocalInner",
	"testAnonymousLocalInner", "testDynamicProxy", "testIdentityHashMap",
	"testExternalizable", "testTransientNonSerializableField1", 
	"testTransientNonSerializableField2", "testTransientNonSerializableField3", 
	"testNonSerializableSuperClass",
	"testEnum" // excluded until we figure out a fix for CDR marshalling of Enums.
    } ;

    public ORBStreamTest() { }

    public ORBStreamTest( String name ) { super( name ) ; }

    protected boolean usesCDR() {
	return true ;
    }

    public static void main( String[] args ) 
    { 
  	// Create an instance of the test suite that is used only
	// to invoke the suite() method.  No name is needed here.
	Client root = new ORBStreamTest() ;
	Client.doMain( args, root ) ; 
    }

    public static Test suite() {
	Client root = new ORBStreamTest() ;
	return root.makeSuite() ;
    }
    
    public ObjectCopierFactory getCopierFactory( ORB orb )
    {
	return CopyobjectDefaults.makeORBStreamObjectCopierFactory( orb ) ;
    }

    public boolean isTestExcluded()
    {
	String testName = getName() ;
	for (int ctr=0; ctr<EXCLUDE_LIST.length; ctr++) 
	    if (testName.equals( EXCLUDE_LIST[ctr]))
		return true ;

	return false ;
    }

    public Client makeTest( String name ) 
    {
	return new ORBStreamTest( name ) ;
    }
}
