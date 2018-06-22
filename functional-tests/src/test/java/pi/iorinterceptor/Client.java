/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package pi.iorinterceptor;

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

public class Client 
    implements InternalProcess 
{

    // Set from run()
    private ORB orb;
    
    // Set from run()
    private PrintStream out;
    
    // Set from run()
    private PrintStream err;
    
    private CodecFactory codecFactory;

    public static void main(String args[]) {
        try {
            (new Client()).run( System.getProperties(),
                                args, System.out, System.err, null );
        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        out.println( "Instantiating ORB" );
        out.println( "=================" );

        // Initializer classes
        String testInitializer = "pi.orbinit.ClientTestInitializer";

        // create and initialize the ORB
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        ORB orb = ORB.init(args, props);

        this.out = out;
        this.err = err;
        this.orb = orb;

        if( ServerTestInitializer.postInitFailed ) {
            throw new RuntimeException( "post_init failed" );
        }

        // Test IORInterceptor
        testIORInterceptor();

        // Test IORInfo
        testIORInfo();
    }

    /**
     * Perform IORInterceptor tests
     */
    private void testIORInterceptor() {
        out.println();
        out.println( "Testing IORInterceptor" );
        out.println( "======================" );
        out.println( "+ Nothing to test on client side, yet." );
    }

    /**
     * Perform IORInfo-related tests
     */
    private void testIORInfo() {
        out.println();
        out.println( "Testing IORInfo" );
        out.println( "===============" );
        out.println( "+ Nothing to test on client side, yet." );
    }
}
