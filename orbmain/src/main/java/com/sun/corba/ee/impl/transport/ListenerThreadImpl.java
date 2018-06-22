/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.transport;


import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.ListenerThread;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.threadpool.Work;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public class ListenerThreadImpl
    implements
        ListenerThread,
        Work
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private ORB orb;
    private Acceptor acceptor;
    private boolean keepRunning;
    private long enqueueTime;

    public ListenerThreadImpl(ORB orb, Acceptor acceptor)
    {
        this.orb = orb;
        this.acceptor = acceptor;
        keepRunning = true;
    }

    ////////////////////////////////////////////////////
    // 
    // ListenerThread methods.
    //

    public Acceptor getAcceptor()
    {
        return acceptor;
    }

    @Transport
    public synchronized void close()
    {
        keepRunning = false;
        acceptor.close() ;
    }

    private synchronized boolean isRunning() {
        return keepRunning ;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    // REVISIT - this needs alot more from previous ListenerThread
    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }

    @Transport
    public void doWork()
    {
        while (isRunning()) {
            display( "acceptor", acceptor ) ;
            try {
                display( "Before Accept cycle" ) ;
                acceptor.processSocket( acceptor.getAcceptedSocket() ) ;
                display( "After Accept cycle" ) ;
            } catch (Throwable t) {
                wrapper.exceptionInListenerThread( t ) ;
                display( "Exception in accept", t ) ;

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
}

// End of file.
