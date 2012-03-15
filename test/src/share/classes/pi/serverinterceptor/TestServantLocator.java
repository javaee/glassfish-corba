/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

package pi.serverinterceptor;

import java.io.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.CORBA.*;

/**
 * Test Servant locator that throws a ForwardRequest.
 */
public class TestServantLocator
    extends org.omg.CORBA.LocalObject
    implements ServantLocator
{
    // The PrintStream to pass to the ServerRequestInterceptor for output
    // This is set from Server.java, statically.
    PrintStream out;

    /** The ORB to pass to the ServerRequestInterceptor */
    ORB orb;

    // Where to forward the caller on a ForwardRequest
    org.omg.CORBA.Object helloRefForward;

    // We will only throw a ForwardRequest the first time.
    boolean firstTime = true;

    /**
     * Creates the servant locator.
     */
    public TestServantLocator( PrintStream out, ORB orb, 
                               org.omg.CORBA.Object helloRefForward ) 
    {
        this.out = out;
        this.orb = orb;
        this.helloRefForward = helloRefForward;
        this.firstTime = true;
    } 

    public Servant preinvoke(byte[] oid, POA adapter, String operation,
                             CookieHolder the_cookie)
        throws org.omg.PortableServer.ForwardRequest
    {
        out.println( "    - TestServantLocator.preinvoke called." );
        if( firstTime ) {
            firstTime = false;
            out.println( "    - First time - raising ForwardRequest." );
            throw new org.omg.PortableServer.ForwardRequest( helloRefForward );
        }

        return new helloServant( out, "[Hello2]" );
    }

    public void postinvoke(byte[] oid, POA adapter, String operation,
                           java.lang.Object cookie, Servant servant)
    {
        out.println( "    - TestServantLocator.postinvoke called." );
    }

    void resetFirstTime() {
        this.firstTime = true;
    }

}
