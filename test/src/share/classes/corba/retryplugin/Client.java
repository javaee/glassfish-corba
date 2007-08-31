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
// Created       : 2005 Oct 05 (Wed) 14:43:22 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:59:21 by Harold Carr.
//

package corba.retryplugin;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;

import com.sun.corba.se.impl.plugin.hwlb.RetryClientRequestInterceptor ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

/**
 * @author Harold Carr
 */
public class Client
{
    static {
	// This is needed to guarantee that this test will ALWAYS use dynamic
	// RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
	// but static in the default "se" packaging, and this test will
	// fail without dynamic RMI-IIOP.
	System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final long CLIENT_RUN_LENGTH = 1000 * 30; // 30 seconds

    public static void main(String[] av)
    {
	try {
	    Properties props = new Properties();

	    props.setProperty(
                "org.omg.PortableInterceptor.ORBInitializerClass."
		+ RetryClientRequestInterceptor.class.getName(),
		"dummy");

	    ORB orb = ORB.init((String[])null, props);
	    Hashtable env = new Hashtable();
	    env.put("java.naming.corba.orb", orb);
	    InitialContext initialContext = new InitialContext(env);

	    Test ref  = (Test)
		lookupAndNarrow(Common.ReferenceName, Test.class, 
				initialContext);

	    long startTime = System.currentTimeMillis();
	    int i = 0;
	    while (System.currentTimeMillis() - startTime < CLIENT_RUN_LENGTH){
		int result = ref.echo(i);
		if (result != i) {
		    throw new Exception("incorrect echo");
		}
		i++;
		System.out.println("correct echo response: " + result);
	    }
	    System.out.println("Loop completed.");
	    System.out.println();
	    System.out.println("Sleeping for 1 minute before timeout test");
	    Thread.sleep(1000 * 60);

	    try {
		System.out.println("This echo should timeout");
		RetryClientRequestInterceptor
		    .setTransientRetryTimeout(1000 * 5);
		ref.echo(-1);
		throw new Exception("timeout echo should not succeed");
	    } catch (Exception e) {
		// See note in README.txt for the type of this exception.
		System.out.println("******: " + e);
	    }

	    System.out.println("Sleeping for 30 seconds before shutdown test");
	    Thread.sleep(1000 * 30);

	    try {
		System.out.println("This echo should fail since server done.");
		ref.echo(-1);
		throw new Exception("shutdown echo should not succeed");
	    } catch (java.rmi.MarshalException e) {
		if (! e.detail.getClass().isInstance(new COMM_FAILURE())) {
		    throw new Exception("!!! Did not receive correct failure");
		}
	    }

	    System.out.println("--------------------------------------------");
	    System.out.println("Client SUCCESS");
	    System.out.println("--------------------------------------------");
	    System.exit(0);

	} catch (Exception e) {
	    e.printStackTrace(System.out);
	    System.out.println("--------------------------------------------");
	    System.out.println("Client FAILURE");
	    System.out.println("--------------------------------------------");
	    System.exit(1);
	}
    }

    public static Object lookupAndNarrow(String name, 
					 Class clazz,
					 InitialContext initialContext)
	throws
	    NamingException
    {
	return PortableRemoteObject.narrow(initialContext.lookup(name), clazz);
    }
}

// End of file.

