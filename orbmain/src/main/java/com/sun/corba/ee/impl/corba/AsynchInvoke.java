/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.corba;

import com.sun.corba.ee.spi.orb.ORB ;

///////////////////////////////////////////////////////////////////////////
// helper class for deferred invocations

/*
 * The AsynchInvoke class allows for the support of asynchronous
 * invocations. Instances of these are created with a request object,
 * and when run, perform an invocation. The instance is also
 * responsible for waking up a client that might be waiting on the
 * 'get_response' method.
 */

public class AsynchInvoke implements Runnable {

    private final RequestImpl _req;
    private final ORB         _orb;
    private final boolean     _notifyORB;

    public AsynchInvoke (ORB o, RequestImpl reqToInvokeOn, boolean n)
    {
        _orb = o;
        _req = reqToInvokeOn;
        _notifyORB = n;
    };


    /*
     * The run operation calls the invocation on the request object,
     * updates the RequestImpl state to indicate that the asynchronous
     * invocation is complete, and wakes up any client that might be
     * waiting on a 'get_response' call.
     *
     */

    public void run() 
    {
        synchronized (_req) {
            // do the actual invocation
            _req.doInvocation();
        }
    
        // for the asynchronous case, note that the response has been
        // received.
        synchronized (_req) {
            // update local boolean indicator
            _req.gotResponse = true;

            // notify any client waiting on a 'get_response'
            _req.notify();
        }
      
        if (_notifyORB == true) {
            _orb.notifyORB() ;
        }
    }

};

///////////////////////////////////////////////////////////////////////////
