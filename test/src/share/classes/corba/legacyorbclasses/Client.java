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
//
// Created       : 2003 Dec 11 (Thu) 11:03:27 by Harold Carr.
// Last Modified : 2003 Dec 19 (Fri) 10:36:14 by Harold Carr.
//

package corba.legacyorbclasses;

import java.util.Properties;
import corba.framework.Controller;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String ORBClassKey = 
	"org.omg.CORBA.ORBClass";
    public static final String ORBSingletonClassKey =
	"org.omg.CORBA.ORBSingletonClass";

    public static void main(String av[])
    {
        try {
	    // ORBSingletons
	    // Note: this negative test must come first, since you
	    // can only create one singleton in a JVM.
	    createORB(false, false,  "x", null);
	    createORB(true,  false,
		      "com.sun.corba.se.internal.corba.ORBSingleton",
		      com.sun.corba.se.internal.corba.ORBSingleton.class);

	    // FULL ORBs
	    createORB(false, true,  "x", null);		
	    createORB(true,  true,  
		      "com.sun.corba.se.impl.orb.ORBImpl",
		      com.sun.corba.se.impl.orb.ORBImpl.class);
	    createORB(true,  true,
		      "com.sun.corba.se.internal.Interceptors.PIORB",
		      com.sun.corba.se.internal.Interceptors.PIORB.class);
	    createORB(true,  true,  
		      "com.sun.corba.se.internal.POA.POAORB",
		      com.sun.corba.se.internal.POA.POAORB.class);
	    createORB(true,  true,  
		      "com.sun.corba.se.internal.iiop.ORB",
		      com.sun.corba.se.internal.iiop.ORB.class);

	    System.out.println("Test PASSED.");

        } catch (Throwable t) {
            System.out.println(main + ": unexpected exception: " + t);
	    System.out.println("Test FAILED.");
	    System.exit(1);
        }
	System.exit(Controller.SUCCESS);
    }

    private static void createORB(boolean shouldExist, 
				  boolean isFullORB,
				  String className,
				  Class clazz)
	throws
	    Exception
    {
	ORB orb = null;
	creating(className);
	try {
	    if (isFullORB) {
		System.getProperties()
		    .setProperty(ORBClassKey, className);
		// NOTE: without setting this explicitly it is getting
		// the default and failing.  Not sure why this is needed
		// in this test but not in others.
		System.getProperties()
		    .setProperty(ORBConstants.INITIAL_PORT_PROPERTY, "1049");
		orb = ORB.init((String[])null, System.getProperties());
	    } else {
		System.getProperties()
		    .setProperty(ORBSingletonClassKey,className);
		orb = ORB.init();
	    }

	    created(orb);
	    checkShouldNotExist(shouldExist, className);
	    checkType(clazz, orb);

	    // Do something to make sure the ORB works.

	    if (isFullORB) {
		NamingContext nameService =
		    NamingContextHelper.narrow(
                        orb.resolve_initial_references("NameService"));
		NameComponent nc = new NameComponent("FOO", "");
		NameComponent path[] = { nc };
		nameService.rebind(path, nameService);
	    } else {
		orb.create_any();
	    }
	} catch (Exception e) {
	    if (shouldExist) {
		throw e;
	    }
	}
    }

    public static void creating(String className)
    {
	System.out.println(baseMsg + ".createORB: creating: " + className);
    }

    public static void created(ORB orb)
    {
	System.out.println(baseMsg + ".createORB: created: " + orb);
    }

    public static void checkShouldNotExist(boolean shouldExist,
					   String className)
	throws
	    Exception
    {
	if (! shouldExist) {
	    throw new Exception("should not exist: " + className);
	}
    }

    public static void checkType(Class clazz, ORB orb)
	throws
	    Exception
    {
	// If we get here we created an ORB as expected.
	// Be sure it is the one we wanted to create.
	if (! clazz.isInstance(orb)) {
	    throw new Exception("Expected: " + clazz + " got: " + orb);
	}
    }
}

// End of file.
