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
package hopper.h4486041;

import org.omg.CORBA.ORB;
import java.util.Properties;

public class Client
{
    public static final String ORBClassKey =
	"org.omg.CORBA.ORBClass";

    public static final String ORBSingletonClassKey =
	"org.omg.CORBA.ORBSingletonClass";

    public static int numberOfErrors = 0;

    public static void main(String[] av)
    {
        try {

	    Properties properties = new Properties();

	    // --------------------------------------

	    properties.put(ORBClassKey, "NotFound");
	    expectException("NotFound", av, properties,
			    ClassNotFoundException.class, false, false);

	    // --------------------------------------

	    properties.put(ORBClassKey, "hopper.h4486041.TestORB");
	    expectNormal("TestORB Good", av, properties, false);

	    // --------------------------------------

	    properties.put(TestORB.ThrowError, "dummy");
	    expectException("TestORB ORBInitException", av, properties,
			    ORBInitException.class, true, false);

	    // --------------------------------------

	    System.getProperties().put(ORBSingletonClassKey, 
				       "hopper.h4486041.TestORB");
	    expectNormal("TestORB Singleton Good", null, null, true);

	    // --------------------------------------

	    /* NOTE:
	     * set_parameters is not called for singletons so
	     * this test will not work.
	    System.getProperties().put(TestORB.ThrowError, "dummy");
	    expectException("TestORB Singleton ORBInitException", null, null,
			    ORBInitException.class, true, true);
	    */

	    // --------------------------------------

	    if (numberOfErrors > 0) {
		throw new Throwable("Test found errors.");
	    }

        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    //
    // isSetParameters: if the exception happens during set_parameters
    // then the exception is not in INITIALIZE from create_impl.
    // It is directly from set_parameters.
    //
    public static void expectException(String message,
				       String[] av,
				       Properties properties,
				       Class expectedException, 
				       boolean isSetParameters,
				       boolean isSingleton)
    {
	System.out.println();
	System.out.println("------------------------------------------------");
	System.out.println("Begin expectException: " + message);
	try {
	    if (isSingleton) {
		ORB orb = ORB.init();
	    } else {
		ORB orb = ORB.init(av, properties);
	    }
	    System.out.println("\tERROR: Should not see this.");
	    System.out.println("\t\tExpected exception: " + expectedException);
	    numberOfErrors++;
	} catch (Throwable t) {
	    Throwable cause;
	    if (isSetParameters) {
		cause = t;
	    } else {
		cause = t.getCause();
	    }
	    System.out.println("\tExpected cause: " + expectedException);
	    System.out.println("\tCause: " + cause);
	    if (cause == null ||
		(! cause.getClass().equals(expectedException)))
            {
		numberOfErrors++;
		System.out.println("\tERROR: Wrong cause.");
	    } else {
		System.out.println("\tOK");
	    }
            
	}
	System.out.println("End expectException: " + message);
	System.out.println("------------------------------------------------");
    }

    public static void expectNormal(String message, 
				    String[] av,
				    Properties properties,
				    boolean isSingleton)
    {
	System.out.println();
	System.out.println("------------------------------------------------");
	System.out.println("Begin expectNormal: " + message);
	try {
	    if (isSingleton) {
		ORB orb = ORB.init();
	    } else {
		ORB orb = ORB.init(av, properties);
	    }
	    System.out.println("\tOK");
	} catch (Throwable t) {
	    numberOfErrors++;
	    System.out.println("\tERROR: Should not see this");
	    System.out.println("\t\tUnexpected exception: "+ t);
	}
	System.out.println("End expectNormal: " + message);
	System.out.println("------------------------------------------------");
    }
}
		
// End of file.
