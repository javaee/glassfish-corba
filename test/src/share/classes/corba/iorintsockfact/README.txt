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

Created       : 2002 Jul 22 (Mon) 12:11:07 by Harold Carr.
Last Modified : 2003 Jun 03 (Tue) 18:00:02 by Harold Carr.

------------------------------------------------------------------------------
Operation:

The server side code looks up all the host's address and puts them
into TAG_ALTERNATE_IIOP_ADDRESS components in IORs.  It does that
using proprietary APIs (it is possible to use standard APIs to create
and insert the TAG_ALTERNATE_IIOP_ADDRESS component) within standard OMG
PortableInterceptor IORInteceptors.

When the client side makes the first request on an IOR it looks for
TAG_ALTERNATE_IIOP_ADDRESS components.  If any are found it uses the
address in the first one found.  It does this by overriding the
getEndPointInfo method of the DefaultSocketFactory provided with the
ORB.  This is a proprietary API.

------------------------------------------------------------------------------
Prepare:

Note: IorIntSockFactTest.java is part of the CORBA unit test framework.
Delete that file before compile with ExampleMakefile standalone.

rm IorIntSockFactTest.java

------------------------------------------------------------------------------
Compile:

export ALT_BOOTDIR=<jdk home>
gnumake -f ExampleMakefile b

------------------------------------------------------------------------------
Run:

# Start ORBD
gnumake -f ExampleMakefile o &

# Start server (note it prints "Server is read.")
gnumake -f ExampleMakefile s &

# Run client (note it prints "Server echoes: Hello" and exits).
gnumake -f ExampleMakefile c

kill <s and o process ids>

;;; End of file.
