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

import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ReaderThread;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.threadpool.Work;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public class ReaderThreadImpl implements ReaderThread, Work {
    private ORB orb;
    private Connection connection;
    private boolean keepRunning;
    private long enqueueTime;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public ReaderThreadImpl(ORB orb, Connection connection)
    {
        this.orb = orb;
        this.connection = connection;
        keepRunning = true;
    }

    ////////////////////////////////////////////////////
    // 
    // ReaderThread methods.
    //

    public Connection getConnection() {
        return connection;
    }

    @Transport
    public synchronized void close() {
        keepRunning = false;

        // Note: do not close the connection here, as it may be 
        // re-used if we are simply closing the ReaderThread
        // because it has completed its operation.
        // If we are calling close because of transport shutdown,
        // the connection will be closed when the connection caches are closed.
    }

    private synchronized boolean isRunning() {
        return keepRunning ;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }


    // REVISIT - this needs alot more from previous ReaderThread.
    @Transport
    public void doWork()
    {
        while (isRunning()) {
            try {
                display( "Start readerThread cycle", connection ) ;

                if (connection.read()) {
                    // REVISIT - put in pool;
                    return;
                }

                display( "End readerThread cycle" ) ;
            } catch (Throwable t) {
                wrapper.exceptionInReaderThread( t ) ;
                display( "Exception in read", t ) ;

                orb.getTransportManager().getSelector(0)
                    .unregisterForEvent(getConnection().getEventHandler());

                try {
                    if (isRunning()) {
                        getConnection().close();
                    }
                } catch (Exception exc) {
                    wrapper.ioExceptionOnClose( exc ) ;
                }
            }
        }
    }

    public void setEnqueueTime(long timeInMillis) {
        enqueueTime = timeInMillis;
    }

    public long getEnqueueTime() {
        return enqueueTime;
    }

    public String getName() { return "ReaderThread"; }
}

// End of file.
