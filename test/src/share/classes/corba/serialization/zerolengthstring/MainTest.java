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

/**
 * A Simple test to check copy created from Util.copyObject() preserves the
 * object structure correctly. This test was added to test the bug fix for
 * a P2 Bug (4728756), Util.copyObjects () use to fail when there were 2 fields
 * with different Zero length string instances and 2 other fields aliasing 
 * to those two Strings.  
 */
package corba.serialization.zerolengthstring;

import java.util.Properties;
import org.omg.CORBA.ORB;
import javax.rmi.CORBA.Util;

public class MainTest {

    private static boolean runTest( String[] args ) {
        try {
            ORB orb = ORB.init( args, null );
            ClassWithZeroLengthStrings object = 
                new ClassWithZeroLengthStrings();
            ClassWithZeroLengthStrings copiedObject =
                (ClassWithZeroLengthStrings) Util.copyObject( object, orb );
            // After copying the object successfully, check to see if
            // the structure is the same as expected.
            return copiedObject.validateObject( );
        } catch ( Exception e ) {
            System.err.println( "Exception " + e + " caught in runTest() " );
            e.printStackTrace( );
            return false;
        }
    }

    public static void main(String[] args) {
	System.out.println("Server is ready.");
	if ( runTest( args ) )
	    System.out.println("Test PASSED");
        else
	    System.out.println("Test FAILED");
    }
}
