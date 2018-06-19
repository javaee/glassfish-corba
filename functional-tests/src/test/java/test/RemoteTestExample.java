/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 * 
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 * 
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 * 
 * Contributor(s):
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

package test;

import java.rmi.Remote;
import java.rmi.RemoteException;
import test.ServantContext;
import test.RemoteTest;
import javax.rmi.PortableRemoteObject;

// NOTE: This is a template for subclasses of RemoteTest. Copy it, then:
//
//  1. Change the class name/package.
//  2. Change the static Strings as needed for your class.
//  3. Replace 'test.Hello' in the 2nd to last line with the name of your remote interface.
//  4. Replace last line with your test code.

/*
 * @test
 */
public class RemoteTestExample extends RemoteTest {

    private static final String publishName     = "HelloServer";
    private static final String servantClass    = "test.HelloServant";
    private static final String[] compileEm     = {servantClass};
   
    /**
     * Return an array of fully qualified remote servant class
     * names for which ties/skels need to be generated. Return
     * empty array if none.
     */
    protected String[] getRemoteServantClasses () {
        return compileEm;  
    }

    /**
     * Append additional (i.e. after -iiop and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        if (iiop) {
            String[] ourArgs = {"-alwaysGenerate"};
            return super.getAdditionalRMICArgs(ourArgs);
        } else {
            return super.getAdditionalRMICArgs(currentArgs);
        }
    }

    /**
     * Perform the test.
     * @param context The context returned by getServantContext().
     */
    public void doTest (ServantContext context) throws Throwable {

        // Start up our servant. (The 'iiop' flag is set to true by RemoteTest
        // unless the -jrmp flag was used).

        Remote remote = context.startServant(servantClass,publishName,true,iiop);

        if (remote == null) {
            throw new Exception ("Could not start servant: " + servantClass);
        }

        // Narrow to our expected interface...

        test.Hello objref = (test.Hello) PortableRemoteObject.narrow(remote,test.Hello.class);

        // TEST CODE HERE...

        System.out.println(objref.sayHello("RemoteTestExample"));
    }
}
