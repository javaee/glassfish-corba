#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#  
#  Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
#  
#  The contents of this file are subject to the terms of either the GNU
#  General Public License Version 2 only ("GPL") or the Common Development
#  and Distribution License("CDDL") (collectively, the "License").  You
#  may not use this file except in compliance with the License.  You can
#  obtain a copy of the License at
#  https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
#  or packager/legal/LICENSE.txt.  See the License for the specific
#  language governing permissions and limitations under the License.
#  
#  When distributing the software, include this License Header Notice in each
#  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
#  
#  GPL Classpath Exception:
#  Oracle designates this particular file as subject to the "Classpath"
#  exception as provided by Oracle in the GPL Version 2 section of the License
#  file that accompanied this code.
#  
#  Modifications:
#  If applicable, add the following below the License Header, with the fields
#  enclosed by brackets [] replaced by your own identifying information:
#  "Portions Copyright [year] [name of copyright owner]"
#  
#  Contributor(s):
#  If you wish your version of this file to be governed by only the CDDL or
#  only the GPL Version 2, indicate your decision by adding "[Contributor]
#  elects to include this software in this distribution under the [CDDL or GPL
#  Version 2] license."  If you don't indicate a single choice of license, a
#  recipient has the option to distribute your version of this file under
#  either the CDDL, the GPL Version 2 or to extend the choice of license to
#  its licensees as provided above.  However, if you add GPL Version 2 code
#  and therefore, elected the GPL Version 2 license, then the option applies
#  only if the new code is made subject to such option by the copyright
#  holder.

//
// Created       : 2005 Jul 12 (Tue) 12:19:01 by Harold Carr.
// Last Modified : 2005 Aug 15 (Mon) 09:00:40 by Harold Carr.
//

------------------------------------------------------------------------------
General Interaction:

client                 server



A general call to see IORUpdates and failover:

                                             (RFM with and without components)
--- echo --+   +-------------------------------------------+    +---> Test
           |   |                                           |    |
           |   |                                           |    |
           v   |                                           |    v
 ClientGroupManager                                        | ServerRequestInt
  IORToSocketInfo                                          |  send_reply
   (get addresses from IOR)                                |   check mem label
  IIOPrimaryToContactInfo                                  |   maybe IORUpdate
   (.hasNext/.next - FailoverCache)                        |    |
  ClientRequestInterceptor                                 |    |
   (send membership label                                  |    |
     and handle IORUpdate)
                                                       ServerGroupManager

                                                         |     ^      |
                                                         |     |      |
                                                    register  notify  getInfo
                                                         |     |      |
                                                         |     |      |
                                                         |     |      |
                                                         |     |      |
Adding and removing "instances" to test IORUpdates:      v     |      v

--- add/remove ------> GISTest/(POA) --- add/remove -->     GISImpl



Adding and removing acceptors/connections to cause failover:

--- add/remove ------> Test/(POA)

------------------------------------------------------------------------------
Tests:

Bootstrap:
        Missing membership label
        Therefore IORUpdate

Normal operation:
        Send label
        No IORUpdate

IORUpdate only:
        Setup: kill "instance"
        Execute:
                Invoke
                IORUpdate

Failover without update:
        Setup: kill listener/connections.
        Execute:
                Invoke
                Cannot connect to address
                Therefore try next address
                No IORUpdate since GIS not aware of "failure"

Failover with update:
        Setup: kill "instance"
        Then setup/execute above: Failover with update.
        IORUpdate

Independent POAs:
        Ensure no address tag or membership labels added to POAs.
        Ensure no restarted by RFM.

"Circular" failover success:
        Setup: kill listener/connections and invoke to get in middle of list.
               kill listener/connections in remainder of list.
               restart listener/connections before middle of list.
        Execute:
                Invoke
                Ensure .hasNext/.next "mod" to beginning of list
                       on a single request

"Circular" failover fail:
        Same as above but do not restart.
        Ensure failure returned to client when get back to 
        middle starting point in list.

------------------------------------------------------------------------------
Server-side configuration:

Server.setProperties:
        Sets persistent server port.
        Sets user-defined listen ports. W, X, Y, Z.
        Registers ORBConfigurator for the ServerGroupManager.
                 addORBInitializer
                        add_ior_interceptor
                        add_server_request_interceptor
        Registers ORBConfigurator for test.
                 register_initial_reference of fake GIS for test.
                 register_initial_reference of real ReferenceFactoryManager

Server.main:
        ORB.init executes configurators.

        Create a ReferenceFactory for the test.
        Create (using above RF) and bind reference for the test.

        Create a "special" ReferenceFactory that does not add components.
        Create (using special RF) and bind a reference for the test.

        Create and bind an object managed by an independent POA.
        (This object also controls fake GIS.)

ServerGroupManager.initialize
        updateMembershipLabel - first time.
        Get RFM and GIS from initial references
        Register with GIS for change notifications.
        

// End of file.
