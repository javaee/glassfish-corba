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
package hopper.h4670827;

public class TestConstants {
     public static final String ORBInitialPort = "2089";

     public static final String INSServiceName = "HelloService";

     // Flavor 1: iiop version and port specified
     public static final String corbalocURL1 = 
         "corbaloc:iiop:1.2@[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 2: iiop version not specified and port specified
     public static final String corbalocURL2 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 3: iiop version specified and port not specified
     public static final String corbalocURL3 = 
         "corbaloc:iiop:1.2@[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 4: iiop version not specified and port not specified
     public static final String corbalocURL4 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]:" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 5: negative test no colon
     public static final String corbalocURL5 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14]" 
         + ORBInitialPort + "/" + INSServiceName;

     // Flavor 6: negative test no bracket and no colon
     public static final String corbalocURL6 = 
         "corbaloc:iiop:[::FFFF:10.5.32.14" 
         + ORBInitialPort + "/" + INSServiceName;
     public static final String returnString = "HELLO";

     Object[][] data = new Object[][] {
         { "testIIOPVersionAndPort", corbalocURL1, true },
         { "testIIOPNoVersionAndPort", corbalocURL2, true },
         { "testIIOPVersionAndNoPort", corbalocURL3, true },
         { "testIIOPNoVersionAndNoPort", corbalocURL4, true },
         { "testNoColon", corbalocURL5, false },
         { "testNoColonNoBracket", corbalocURL6, false },
     } ;
}
