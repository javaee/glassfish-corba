/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
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
package pi.policyfactory;

import org.omg.PortableInterceptor.*;
import org.omg.PortableInterceptor.PolicyFactory;
import org.omg.PortableInterceptor.ORBInitInfoPackage.*;

import java.util.*;
import java.io.*;
import org.omg.CORBA.*;

public class TestORBInitializer extends LocalObject
    implements ORBInitializer
{
    public void pre_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        System.out.println( "TestORBInitializer.pre_init() called..." );
        System.out.flush( );
    }

    /** pre_init registers 2 PolicyFactories with types 100, 1000 and 10000
     *  These types will be used in Positive tests to see the validity of
     *  ORB.create_policy() API.
     */
    public void post_init (org.omg.PortableInterceptor.ORBInitInfo info) {
        PolicyFactory policyFactory1000Plus = new PolicyFactoryThousandPlus( );
        PolicyFactory policyFactory100 = new PolicyFactoryHundred( );
        // Same PolicyFactory for types 1000 and 10000. create_policy() method
        // takes care of instantiating the right policy based on policy type. 
        info.register_policy_factory( 1000, policyFactory1000Plus );
        info.register_policy_factory( 10000, policyFactory1000Plus );
        info.register_policy_factory( 100, policyFactory100 );
        System.out.println( "TestORBInitializer.post_init() called..." );
        System.out.flush( );
    }
}
  
