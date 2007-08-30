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

package corba.poapolicies;

import org.omg.CORBA.*;

import Util.*;
import HelloStuff.*;

public class HelloClient {
    public static Hello createHello(CreationMethods c, Factory f) {
        System.out.println("createHello");
        String id = HelloHelper.id();
        System.out.println("id: " + id);

        System.out.println("Factory class: " + f.getClass().getName());

        org.omg.CORBA.Object obj = f.create(id, "corba.poapolicies.HelloImpl", c);

        System.out.println("Created object");

        Hello result = HelloHelper.narrow(obj);
        
        System.out.println("narrowed it");

        return result;

        /*
	return HelloHelper.narrow(f.create(HelloHelper.id(),
					   "HelloImpl",
					   c));
        */
    }

    static final void invoke(Hello h) {
	System.out.println(h.hi());
    }
    
    public static void main(String[] args) {
	
	try {

            System.out.println("Client starting");

	    Utility u = new Utility(args);
	    Factory f = u.readFactory();

	    System.out.println("readFactory");
	    

            System.out.println("invoke 1");

	    Hello h1 =
		createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_POA_ASSIGNED_OIDS,
			    f);

            System.out.println("created 1, now invoking");

	    invoke(h1);

            System.out.println("invoke 2");

	    Hello h2 =
		createHello(CreationMethods.EXPLICIT_ACTIVATION_WITH_USER_ASSIGNED_OIDS,
			    f);
	    invoke(h2);

            System.out.println("invoke 3");

	    Hello h3 =
		createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_POA_ASSIGNED_OIDS,
			    f);
	    invoke(h3);

            System.out.println("invoke 4");

	    Hello h4 =
		createHello(CreationMethods.CREATE_REFERENCE_BEFORE_ACTIVATION_WITH_USER_ASSIGNED_OIDS,
			    f);
	    invoke(h4);

            System.out.println("Calling overAndOut");

	    f.overAndOut();

            System.out.println("Client finished");

	} catch (Exception e) {
            System.err.println("Client level");
	    e.printStackTrace();
            try {
                System.err.flush();
            } catch (Exception ex) {}
            System.exit(1);
	} 
    }
}

