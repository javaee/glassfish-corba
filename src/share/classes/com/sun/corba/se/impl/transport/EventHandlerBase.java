/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2010 Sun Microsystems, Inc. All rights reserved.
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

import java.nio.channels.SelectionKey;

import com.sun.corba.se.spi.transport.EventHandler;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchWorkQueueException;
import com.sun.corba.se.spi.orbutil.threadpool.Work;

import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.Transport;

@Transport
public abstract class EventHandlerBase
    implements
	EventHandler
{
    protected ORB orb;
    protected Work work;
    protected boolean useWorkerThreadForEvent;
    protected boolean useSelectThreadToWait;
    protected SelectionKey selectionKey;

    ////////////////////////////////////////////////////
    //
    // EventHandler methods
    //

    public void setUseSelectThreadToWait(boolean x)
    {
	useSelectThreadToWait = x;
    }

    public boolean shouldUseSelectThreadToWait()
    {
	return useSelectThreadToWait;
    }

    public void setSelectionKey(SelectionKey selectionKey)
    {
	this.selectionKey = selectionKey;
    }

    public SelectionKey getSelectionKey()
    {
	return selectionKey;
    }

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    /*
     * NOTE:
     * This is not thread-safe by design.
     * Only one thread should call it - a reader/listener/select thread.
     * Not stateless: interest ops, registration.
     */
    @Transport
    public void handleEvent()
    {
	getSelectionKey().interestOps(getSelectionKey().interestOps() &
				      (~ getInterestOps()));
	if (shouldUseWorkerThreadForEvent()) {
	    Throwable throwable = null;
	    try {
                display( "add work to pool 0") ;
		orb.getThreadPoolManager().getThreadPool(0)
		    .getWorkQueue(0).addWork(getWork());
	    } catch (NoSuchThreadPoolException e) {
		throwable = e;
	    } catch (NoSuchWorkQueueException e) {
		throwable = e;
	    }
	    // REVISIT: need to close connection.
	    if (throwable != null) {
                display( "unexpected exception", throwable ) ;
		throw orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil()
                         .noSuchThreadpoolOrQueue(throwable);
	    }
	} else {
            display( "doWork" ) ;
	    getWork().doWork();
	}
    }

    public boolean shouldUseWorkerThreadForEvent()
    {
	return useWorkerThreadForEvent;
    }

    public void setUseWorkerThreadForEvent(boolean x)
    {
	useWorkerThreadForEvent = x;
    }

    public void setWork(Work work)
    {
	this.work = work;
    }

    public Work getWork()
    {
	return work;
    }
}

// End of file.
