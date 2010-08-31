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
package pi.orbinit;

import corba.framework.*;
import java.util.*;

/**
 * Tests ORBInitializer and ORBInitInfo as per Portable Interceptors spec
 * orbos/99-12-02, Chapter 9.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ORBInitTest extends CORBATest {
    // Set to true if at least one test fails.
    private boolean failed = false;

    protected void doTest() throws Throwable {
	System.out.println();

	printBeginTest( "[Properties Object] " );
        Controller orbd = createORBD();
        orbd.start();
        Controller client = createClient( "pi.orbinit.PropsClient" );
        client.start();
        client.waitFor();
	printEndTest( client, null );
        client.stop();
        orbd.stop();
	pause();

	/* Second time around is invalid unless flags in ClientTestInitializer
	 * are cleared; so how did this ever work correctly?
	 * Also note that system vs. props test is not needed here,
	 * as the ORB initialization test already covers that.

	printBeginTest( "[System Properties] " );
        orbd = createORBD();
        orbd.start();
        client = createClient( "pi.orbinit.SystemClient" );
        client.start();
        client.waitFor();
	printEndTest( client, null );
        client.stop();
        orbd.stop();
	*/

        System.out.print( "      Final Result: " );
        if( failed ) {
            throw new RuntimeException( "Errors detected" );
        }
    }

    private void printBeginTest( String name ) {
        System.out.print( "      " + name );
    }

    private void printEndTest( Controller client, Controller server )
        throws Throwable
    {
        if( (server != null) && server.finished() ) {
            System.out.println( "FAILED, Server crashed" );
            failed = true;
        }
        else if( client.exitValue() != Controller.SUCCESS ) {
            System.out.println( "FAILED, Client exit value = " +
                client.exitValue() );
            failed = true;
        }
        else {
            System.out.println( "PASSED" );
        }
    }

    // Pause a little to allow all processes to fully terminate.
    private void pause() {
        try {
            Thread.sleep( 2000 );
        }
        catch( InterruptedException e ) {
            // ignore.
        }
    }

}

