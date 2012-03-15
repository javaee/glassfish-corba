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

Summary and suggested reading order:

==============================================================================
Connection interceptor (called an ORBSocketFactory):

Summary:

The server side part of the ORBSocketFactory is told the type to
create as well as a port number.

The client side part of the ORBSocketFactory is called on every client
request.  An ORB first asks the factory for type/host/port information
(given an IOR).  If the ORB already has a connection of the
type/host/port it will use the existing connection.  Otherwise it will
then ask the factory to create a client socket, giving it that
type/host/port.  Finally, the createSocket method may throw an
exception to tell the ORB to ask it for type/host/port info again.
The information passed back and forth between the ORB and factory can
act as a cookie for the factory if desired.

Interfaces:

        com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory
        com.sun.corba.ee.spi.legacy.connection.EndPointInfo
        com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException

==============================================================================
Access to a request's socket:

Summary:

The request's socket is available via ClientRequestInfo and
ServerRequestInfo.  We enable this by having them implement the
RequestInfoExt interface.

Interfaces:

        com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt
        com.sun.corba.ee.spi.legacy.connection.Connection

==============================================================================
Extending IORInfo to support the multiple server port API:

Summary:

We support the multiple server port API in PortableInterceptors by
having IORInfo implement the IORInfoExt interface.  The description on
how to use the multiple server port APIs is found in
ORBSocketFactory.java.

Interfaces:

       com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt
       com.sun.corba.ee.spi.legacy.interceptor.UnknownType

;; End.


