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
// Created       : 2001 May 23 (Wed) 15:24:44 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 19:50:01 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.util.Properties;

public class Client 
{
    public static void main(String av[])
    {
        try {
	    Properties props = new Properties();
	    props.put("org.omg.PortableInterceptor.ORBInitializerClass."
		      + "pi.serviceexample.AServiceORBInitializer",
		      "");
	    props.put("org.omg.PortableInterceptor.ORBInitializerClass."
		      + "pi.serviceexample.LoggingServiceClientORBInitializer",
		      "");
	    ORB orb = ORB.init(av, props);

	    //
	    // The client obtains a reference to a service.
	    // The client does not know the service is implemented
	    // using interceptors.
	    //

	    AService aService =	
		AServiceHelper.narrow(
	            orb.resolve_initial_references("AService"));

	    //
	    // The client obtains a reference to some object that
	    // it will invoke.
	    //

	    NamingContext nameService = 
		NamingContextHelper.narrow(
                    orb.resolve_initial_references("NameService"));
	    NameComponent arbitraryObjectPath[] =
	        { new NameComponent("ArbitraryObject", "") };
	    ArbitraryObject arbitraryObject =
		ArbitraryObjectHelper.narrow(nameService.resolve(arbitraryObjectPath));

	    //
	    // The client begins the service so that invocations of
	    // any object will be done with that service in effect.
	    //

	    aService.begin();
	    
	    arbitraryObject.arbitraryOperation1("one");
	    arbitraryObject.arbitraryOperation2(2);

	    //
	    // The client ends the service so that further invocations
	    // of any object will not be done with that service in effect.
	    //

	    aService.end();

	    // This invocation is not serviced by aService since
	    // it is outside the begin/end.
	    arbitraryObject.arbitraryOperation3("just return");


	    aService.begin();
	    try {
		arbitraryObject.arbitraryOperation3("throw exception");
		throw new RuntimeException("should not see this");
	    } catch (ArbitraryObjectException e) {
		// Expected in this example, so do nothing.
	    }
	    aService.end();

        } catch (Exception e) {
            e.printStackTrace();
	    System.exit(-1);
        }
	System.out.println("Client done.");
	System.exit(0);
    }
}

// End of file.

