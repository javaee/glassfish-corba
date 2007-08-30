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
// Created       : 2003 Apr 19 (Sat) 07:25:10 by Harold Carr.
// Last Modified : 2003 Aug 07 (Thu) 14:59:17 by Harold Carr.
//

Steps to plug in a new encoding, protocol and/or transport (EPt):

Client:

1. Define a CorbaContactInfoList implementation with an "iterator" method
   that returns an iterator knowledgable of the new EPt.

2. Define a CorbaContactInfoListIterator implementation with a "next" method
   that returns a ContactInfo knowledgable of the new EPt.

3. Define a CorbaContactInfo implementation with a:

   "createOutputObject" method that returns an OutputObject representing
   the new encoding.

   "createConnection" method that returns a connection representing
   the new transport.

4. Define a CorbaConnection implementation that handles the new transport.
   
5. REVISIT: NEEDS SPI:
   Plug the CorbaContactInfoList defined in step 1 either into the stub's
   delegate, or replace stub's delegate.

AT: XConnectionImpl.sendWithoutLock/.write
    MessageBase.readFully - moved pieces to connection
    CDROutputObject.writeTo - moved pieces to connection
    

// End of file.

