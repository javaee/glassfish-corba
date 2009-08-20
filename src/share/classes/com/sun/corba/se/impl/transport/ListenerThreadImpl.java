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
package com.sun.corba.se.impl.transport;


import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.ListenerThread;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;


public class ListenerThreadImpl
    implements
	ListenerThread,
	Work
{
    private ORB orb;
    private CorbaAcceptor acceptor;
    private boolean keepRunning;
    private long enqueueTime;
    private ORBUtilSystemException wrapper ;

    public ListenerThreadImpl(ORB orb, CorbaAcceptor acceptor)
    {
	this.orb = orb;
	this.acceptor = acceptor;
	keepRunning = true;
	wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
    }

    ////////////////////////////////////////////////////
    // 
    // ListenerThread methods.
    //

    public CorbaAcceptor getAcceptor()
    {
	return acceptor;
    }

    public synchronized void close()
    {
	if (orb.transportDebugFlag) {
	    dprint(".close: " + acceptor);
	}

	keepRunning = false;
        acceptor.close() ;
    }

    public synchronized boolean isRunning() {
        return keepRunning ;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    // REVISIT - this needs alot more from previous ListenerThread

    public void doWork()
    {
	try {
	    if (orb.transportDebugFlag) {
		dprint(".doWork: Start ListenerThread: " + acceptor);
	    }
	    while (isRunning()) {
		try {
		    if (orb.transportDebugFlag) {
			dprint(".doWork: BEFORE ACCEPT CYCLE: " + acceptor);
		    }

		    acceptor.accept();

		    if (orb.transportDebugFlag) {
			dprint(".doWork: AFTER ACCEPT CYCLE: " + acceptor);
		    }
		} catch (Throwable t) {
                    wrapper.exceptionInListenerThread( t ) ;
		    if (orb.transportDebugFlag) {
			dprint(".doWork: Exception in accept: " + acceptor,t);
		    }
		    orb.getTransportManager().getSelector(0)
			.unregisterForEvent(getAcceptor().getEventHandler());

                    try {
                        if (isRunning()) {
                            getAcceptor().close();
                        }
                    } catch (Exception exc) {
                        wrapper.ioExceptionOnClose( exc ) ;
                    }
		}
	    }
	} finally {
	    if (orb.transportDebugFlag) {
		dprint(".doWork: Terminated ListenerThread: " + acceptor);
	    }
	}
    }

    public void setEnqueueTime(long timeInMillis) 
    {
	enqueueTime = timeInMillis;
    }

    public long getEnqueueTime() 
    {
	return enqueueTime;
    }

    public String getName() { return "ListenerThread"; }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    private void dprint(String msg)
    {
	ORBUtility.dprint("ListenerThreadImpl", msg);
    }

    private void dprint(String msg, Throwable t)
    {
	dprint(msg);
	t.printStackTrace(System.out);
    }
}

// End of file.
