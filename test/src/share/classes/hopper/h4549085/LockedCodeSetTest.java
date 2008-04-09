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
package hopper.h4549085;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.se.impl.orbutil.ORBConstants;
import org.omg.CORBA.*;

/**
 * Simple tests in GIOP 1.1 and 1.2 of chars and wstrings.
 */
public class LockedCodeSetTest extends CORBATest
{
    public static final String[] idlFiles = { "Tester.idl" };

    public static final String[] javaFiles = { "Server.java",
                                               "Client.java" };


    protected void doTest() throws Throwable
    {
        Options.addIDLCompilerArgs("-fall");
        Options.setIDLFiles(idlFiles);
        Options.setJavaFiles(javaFiles);
        compileIDLFiles();
        compileJavaFiles();

        Controller orbd = createORBD();

        // Make the server only advertise UTF-8 for char, forcing the
        // client to select it.  The server will still use ISO8859-1 to
        // unmarshal the operation name, but should be able to handle
        // multibyte chars after the service context is unmarshaled.
        Properties serverProps = Options.getServerProperties();

        serverProps.setProperty(ORBConstants.CHAR_CODESETS,
                                "83951617,83951617");

        Controller server = createServer("hopper.h4549085.Server");
        Controller client = createClient("hopper.h4549085.Client");

        orbd.start();
        server.start();
        client.start( getHelper() );

        // Wait for the client to finish for up to 2 minutes, then
        // throw an exception.
        client.waitFor(120000);
        client.stop();
        server.stop();
        orbd.stop();
    }
}
    
