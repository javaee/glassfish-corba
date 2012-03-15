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
// Created       : 2005 Oct 05 (Wed) 20:27:30 by Harold Carr.
// Last Modified : 2005 Oct 10 (Mon) 11:31:40 by Harold Carr.
//

This test shows how to stop an ORB from receiving further requests by
throwing TRANSIENT to the client.

REVISIT:  This test should be refactored to use callbacks from the
server to the client to indicate test phase changes (i.e., rejecting
on/off; always rejecting/retry timeout; shutdown) rather than Thread.sleep.

------------------------------------------------------------------------------
        ***** IMPORTANT NOTICE *****

Should only use RetryClientRequestInterceptor if the client *ONLY* talks
to stateless beans.

If client even talks to one single stateful bean then it should *NOT*
install the RetryClientRequestInterceptor.  Instead the client should
be changed to catch TRANSIENT and retry.

------------------------------------------------------------------------------

Operation:

The client will keep retrying the requests that get TRANSIENT until
success or until the server is shutdown.

If the server is shutdown the client will try another server address,
if the reference has multiple addresses.  If there is only one address
then a MarshalException/COMM_FAILURE is thrown to the client
application.

Configuration:

The server-side installs the RetryServerRequestInterceptor.
Call RetryServerRequestInterceptor.setRejectingRequests(true)
to stop receiving accepting further requests.  After an appropriate
period of time shutdown the server.

The client-side install the RetryClientRequestInterceptor.
No changes are necessary in client code.

Note:

If the timeout is reached then the TRANSIENT exception is thrown to the client.
However, a bug in the ORB may mask that exception and return an
exception indicating the client info stack is null.  That exception
can be ignored and treated as TRANSIENT timing out.

Note:

If testing this in an ORB after Oct 4, 2005, the
RetryClientRequestInterceptor will not even see the TRANSIENT since
that is handled by the ContactInfoListIterator.

// End of file.
