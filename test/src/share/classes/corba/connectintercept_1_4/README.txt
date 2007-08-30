#  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
# 
#  Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
# 
#  The contents of this file are subject to the terms of either the GNU
#  General Public License Version 2 only ("GPL") or the Common Development
#  and Distribution License("CDDL") (collectively, the "License").  You
#  may not use this file except in compliance with the License. You can obtain
#  a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
#  or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
#  language governing permissions and limitations under the License.
# 
#  When distributing the software, include this License Header Notice in each
#  file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
#  Sun designates this particular file as subject to the "Classpath" exception
#  as provided by Sun in the GPL Version 2 section of the License file that
#  accompanied this code.  If applicable, add the following below the License
#  Header, with the fields enclosed by brackets [] replaced by your own
#  identifying information: "Portions Copyrighted [year]
#  [name of copyright owner]"
# 
#  Contributor(s):
# 
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
// Created       : 2002 Mar 21 (Thu) 20:37:23 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:10:06 by Harold Carr.

This tests connection interception.

Be sure to read the JavaDoc in:
   com/sun/corba/se/spi/legacy/connection/*
   com/sun/corba/se/spi/legacy/interceptor/*

Also tests:

This tests proprietary interceptor ordering.
If ordering fails it will raise INTERNAL/-45.

This ensures that interceptor calls are balanced when the ORB is shutdown.
If it fails it raises a RuntimeException.

==============================================================================
Transient

ORBD:

orbd started with no VM properties nor command line args.

SERVER:

ServerTransient started with no VM properties nor command line args.

ServerTransient starts ServerCommon with Transient arg.

ServerCommon sets the socket factory and port properties.

During ORB.init three ports are opened for listening (besides the
default clear text port).

During ORB.init establish_components adds one "Listen Ports"
tagged component containing the data on the 3 "extra" ports.
It gets that data from IORInfoExt (proprietary).

CLIENT:

Client started with no VM properties nor command line args.

When using the default socket factory the client always makes
requests to the server by connecting to the default listen port.

When using custom socket factory the client makes requests to the
server by connecting to ports default, 1, 2, 3 (in a cycle).
Note that once it makes a connection it uses the ephemeral port
allocated by accept (normal TCP/IP operation).

Besides cycling through the listen ports, the client, on the first
invocation on each reference, get bad info back from the socket
factory to test the try again loop.


==============================================================================
Persistent

ORBD:

Start up with VM args:

   -Dcom.sun.corba.se.POA.ORBBadServerIdHandlerClass=corba.connectintercept_1_4.ORBDBadServerIdHandler
   -Dcom.sun.corba.se.connection.ORBSocketFactoryClass=corba.connectintercept_1_4.MySocketFactory
   -Dcom.sun.corba.se.connection.ORBListenSocket=MyType1:2000,MyType2:2001,MyType3:0

SERVER:

Use ServerTool to register ServerPersistent no VM properties nor
command line args). 

ripServerTool -cmd register -applicationName "corba.connectintercept_1_4.ServerPersistent" \
	-server corba.connectintercept_1_4.ServerPersistent -classpath ...

CLIENT:

Client started with no VM properties nor command line args.

//
