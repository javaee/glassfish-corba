/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.EventHandler;
import com.sun.corba.se.spi.transport.ListenerThread;
import com.sun.corba.se.spi.transport.ReaderThread;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchThreadPoolException;
import com.sun.corba.se.spi.orbutil.threadpool.NoSuchWorkQueueException;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import java.util.Map;
import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

/**
 * @author Harold Carr
 */
@ManagedObject
@Description( "The Selector, which handles incoming requests to the ORB" )
public class SelectorImpl
    extends
	Thread
    implements
	com.sun.corba.se.spi.transport.Selector
{
    private ORB orb;
    private Selector selector;
    private long timeout;
    private List deferredRegistrations;
    private List interestOpsList;
    private Map<EventHandler,ListenerThread> listenerThreads;
    private Map<EventHandler,ReaderThread>  readerThreads;
    private boolean selectorStarted;
    private volatile boolean closed;
    private ORBUtilSystemException wrapper ;

    // XXX This needs more work on statistics:
    // - how many registered events of different types?
    // - how many events received?
    @ManagedAttribute
    @Description( "List of listener threads dedicated to listening "
        + "for new connections on an acceptor")
    private synchronized List<ListenerThread> getListenerThreads() {
        return new ArrayList<ListenerThread>( listenerThreads.values()) ;
    }

    @ManagedAttribute
    @Description( "List of reader threads dedicated to listening "
        + "for new messages on a connection" )
    private synchronized List<ReaderThread> getReaderThreads() {
        return new ArrayList<ReaderThread>( readerThreads.values()) ;
    }

    public SelectorImpl(ORB orb)
    {
	this.orb = orb;
	selector = null;
	selectorStarted = false;
	timeout = 60000;
	deferredRegistrations = new ArrayList();
	interestOpsList = new ArrayList();
	listenerThreads = new HashMap<EventHandler,ListenerThread>();
	readerThreads = new HashMap<EventHandler,ReaderThread>();
	closed = false;
	wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
    }

    public void setTimeout(long timeout) 
    {
	this.timeout = timeout;
    }

    @ManagedAttribute
    @Description( "The selector timeout" ) 
    public long getTimeout()
    {
	return timeout;
    }

    public void registerInterestOps(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".registerInterestOps:-> " + eventHandler);
	}

	SelectionKey selectionKey = eventHandler.getSelectionKey();
	if (selectionKey.isValid()) {
            int ehOps = eventHandler.getInterestOps();
            SelectionKeyAndOp keyAndOp = new SelectionKeyAndOp(selectionKey, ehOps);
	    synchronized(interestOpsList) {
		interestOpsList.add(keyAndOp);
	    }
            // tell Selector Thread there's an update to a SelectorKey's Ops
            selector.wakeup();
	}
	else {
            wrapper.selectionKeyInvalid(eventHandler.toString());
	    if (orb.transportDebugFlag) {
		dprint(".registerInterestOps: EventHandler SelectionKey not valid " + eventHandler);
	    }
	}

	if (orb.transportDebugFlag) {
	    dprint(".registerInterestOps:<- ");
	}
    }

    public void registerForEvent(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".registerForEvent: " + eventHandler);
	}

	if (isClosed()) {
	    if (orb.transportDebugFlag) {
		dprint(".registerForEvent: closed: " + eventHandler);
	    }
	    return;
	}

	if (eventHandler.shouldUseSelectThreadToWait()) {
	    synchronized (deferredRegistrations) {
		deferredRegistrations.add(eventHandler);
	    }
	    if (! selectorStarted) {
		startSelector();
	    }
	    selector.wakeup();
	    return;
	}

	switch (eventHandler.getInterestOps()) {
	case SelectionKey.OP_ACCEPT :
	    createListenerThread(eventHandler);
	    break;
	case SelectionKey.OP_READ :
	    createReaderThread(eventHandler);
	    break;
	default:
	    if (orb.transportDebugFlag) {
		dprint(".registerForEvent: default: " + eventHandler);
	    }
	    throw new RuntimeException(
                "SelectorImpl.registerForEvent: unknown interest ops");
	}
    }

    public void unregisterForEvent(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".unregisterForEvent: " + eventHandler);
	}

	if (isClosed()) {
	    if (orb.transportDebugFlag) {
		dprint(".unregisterForEvent: closed: " + eventHandler);
	    }
	    return;
	}

	if (eventHandler.shouldUseSelectThreadToWait()) {
	    SelectionKey selectionKey = eventHandler.getSelectionKey();
            if (selectionKey != null) {
                selectionKey.cancel();
                selector.wakeup();
            }

	    return;
	}

	switch (eventHandler.getInterestOps()) {
	case SelectionKey.OP_ACCEPT :
	    destroyListenerThread(eventHandler);
	    break;
	case SelectionKey.OP_READ :
	    destroyReaderThread(eventHandler);
	    break;
	default:
	    if (orb.transportDebugFlag) {
		dprint(".unregisterForEvent: default: " + eventHandler);
	    }
	    throw new RuntimeException(
                "SelectorImpl.uregisterForEvent: unknown interest ops");
	}
    }

    public void close()
    {
	if (orb.transportDebugFlag) {
	    dprint(".close");
	}

	if (isClosed()) {
	    if (orb.transportDebugFlag) {
		dprint(".close: already closed");
	    }
	    return;
	}

	setClosed(true);

	Iterator i;

	// Kill listeners.

        synchronized (this) {
            for (ListenerThread lthread : listenerThreads.values()) {
                lthread.close() ;
            }
        }

	// Kill readers.

        synchronized (this) {
            for (ReaderThread rthread : readerThreads.values()) {
                rthread.close() ;
            }
        }

	// Selector

	try {
	    if (selector != null) {
		// wakeup Selector thread to process close request
		selector.wakeup();
	    }
	} catch (Throwable t) {
	    if (orb.transportDebugFlag) {
		dprint(".close: selector.close: " + t);
	    }
	}
    }

    ///////////////////////////////////////////////////
    //
    // Thread methods.
    //

    public void run()
    {
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    setName("SelectorThread");
                    return null;
                }
            });
	while (!closed) {
	    try {
		int n = 0;
		if (timeout == 0 && orb.transportDebugFlag) {
		    dprint(".run: Beginning of selection cycle");
		}
		handleDeferredRegistrations();
		enableInterestOps();
		try {
		    n = selector.select(timeout);
		} catch (IOException  e) {
		    if (orb.transportDebugFlag) {
			dprint(".run: selector.select: " + e);
		    }
		}
		if (closed) {
		    selector.close();
		    if (orb.transportDebugFlag) {
			dprint(".run: closed - .run return");
		    }
		    return;
		}
		/*
		  if (timeout == 0 && orb.transportDebugFlag) {
		  dprint(".run: selector.select() returned: " + n);
		  }
		  if (n == 0) {
		  continue;
		  }
		*/
		Iterator iterator = selector.selectedKeys().iterator();
		if (orb.transportDebugFlag) {
		    if (iterator.hasNext()) {
			dprint(".run: n = " + n);
		    }
		}
		while (iterator.hasNext()) {
		    SelectionKey selectionKey = (SelectionKey) iterator.next();
		    iterator.remove();
                    
                    // It is possible that a different thread (other than the 
                    // thread that is executing this code has cancelled the
                    // SelectionKey as a result of it inititiating a Connection 
                    // close and cleanup of its temporary Selectors. Hence, 
                    // the check for a valid SelectionKey.
                    // IMPORTANT: Further assuming the thread that has cancelled
                    // the SelectionKey is releasing other Connection resources
                    // such as cleaning any temporary Selectors which may be
                    // been active in addition to closing the Connection.

                    if (selectionKey.isValid()) {
                        EventHandler eventHandler = 
                                (EventHandler)selectionKey.attachment();
                        try {
                            eventHandler.handleEvent();
                        } catch (Throwable t) {
                            wrapper.exceptionInSelector( t, eventHandler ) ;
                        }
                    } else {
                        wrapper.canceledSelectionKey( selectionKey ) ;
                        if (orb.transportDebugFlag) {
                            dprint(".run: skipping event since this " +
                                   "EventHandler's SelectionKey has been " +
                                   "found to be cancelled. It will be removed " +
                                   "from this Selector on the next select() " +
                                   "operation.");
                        }
                    }
		}
		if (timeout == 0 && orb.transportDebugFlag) {
		    dprint(".run: End of selection cycle");
		}
	    } catch (Throwable t) {
		// IMPORTANT: ignore all errors so the select thread keeps running.
		// Otherwise a guaranteed hang.
		if (orb.transportDebugFlag) {
		    dprint(".run: ignoring", t);
		}
	    }
	}
    }

    /////////////////////////////////////////////////////
    //
    // Implementation.
    //

    private synchronized boolean isClosed ()
    {
	return closed;
    }

    private synchronized void setClosed(boolean closed)
    {
	this.closed = closed;
    }

    private void startSelector()
    {
	try {
	    selector = Selector.open();
	} catch (IOException e) {
	    if (orb.transportDebugFlag) {
		dprint(".startSelector: Selector.open: IOException: " + e);
	    }
	    // REVISIT - better handling/reporting
	    RuntimeException rte =
		new RuntimeException(".startSelector: Selector.open exception");
	    rte.initCause(e);
	    throw rte;
	}
	setDaemon(true);
	start();
	selectorStarted = true;
	if (orb.transportDebugFlag) {
	    dprint(".startSelector: selector.start completed.");
	}
    }

    private void handleDeferredRegistrations()
    {
	synchronized (deferredRegistrations) {
            int deferredListSize = deferredRegistrations.size();
            for (int i = 0; i < deferredListSize; i++) {
                EventHandler eventHandler = 
		    (EventHandler)deferredRegistrations.get(i);
                if (orb.transportDebugFlag) {
                    dprint(".handleDeferredRegistrations: " + eventHandler);
                }
                SelectableChannel channel = eventHandler.getChannel();
                SelectionKey selectionKey = null;
                try {
                    selectionKey =
                        channel.register(selector,
                                         eventHandler.getInterestOps(),
                                         (Object)eventHandler);
                } catch (ClosedChannelException e) {
                    if (orb.transportDebugFlag) {
                        dprint(".handleDeferredRegistrations: " + e);
                    }
                }
                eventHandler.setSelectionKey(selectionKey);
            }
            deferredRegistrations.clear();
        }
    }

    private void enableInterestOps()
    {
	synchronized (interestOpsList) {
	    int listSize = interestOpsList.size();
	    if (listSize > 0) {
                if (orb.transportDebugFlag) {
                    dprint(".enableInterestOps:->");
                }
                SelectionKey selectionKey = null;
		SelectionKeyAndOp keyAndOp = null;
		int keyOp, selectionKeyOps = 0;
		for (int i = 0; i < listSize; i++) {
		    keyAndOp = (SelectionKeyAndOp)interestOpsList.get(i);
		    selectionKey = keyAndOp.selectionKey;

		    // Need to check if the SelectionKey is valid because a
		    // connection's SelectionKey could be put on the list to
		    // have its OP enabled and before it's enabled have its
                    // associated connection reclaimed and/or closed which will
                    // cancel the SelectionKey.
 
		    // Otherwise, the enabling of the OP will throw an exception
		    // here and exit this method an potentially not enable all
		    // registered interest ops.
		    //
		    // So, we ignore SelectionKeys that are invalid. They will
                    // get cleaned up and removed from this Selector's key set
                    // on the next Selector.select() call.

		    if (selectionKey.isValid()) {
                        if (orb.transportDebugFlag) {
                            dprint(".enableInterestOps: " + keyAndOp);
                        }
		        keyOp = keyAndOp.keyOp;
                        try {
		            selectionKeyOps = selectionKey.interestOps();
		            selectionKey.interestOps(selectionKeyOps | keyOp);
                        } catch (CancelledKeyException cke) {
                            // It is possible that between the time when an
                            // SelectionKey's interestOp was registered to be
                            // enabled by Thread 1 that the Connection 
                            // associated with the SelectionKey was closed by
                            // Thread 2 where Thread 2 will cancel the 
                            // SelectionKey.  As a result, we catch and 
                            // ignore this exception condition.
                            if (orb.transportDebugFlag) {
                                dprint(".enableInterestOps: ignoring " +
                                       "CancelledKeyException, connection " +
                                       "has been closed and its SelectionKey " +
                                       "cancelled.");
                            }
                        }
		    }
		}
		interestOpsList.clear();
                if (orb.transportDebugFlag) {
                    dprint(".enableInterestOps:<-");
                }
	    }
	}
    }

    private void createListenerThread(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".createListenerThread: " + eventHandler);
	}
	CorbaAcceptor acceptor = (CorbaAcceptor)eventHandler.getAcceptor();
	ListenerThread listenerThread =
	    new ListenerThreadImpl(orb, acceptor);
        synchronized (this) {
            listenerThreads.put(eventHandler, listenerThread);
        }
	Throwable throwable = null;
	try {
	    orb.getThreadPoolManager().getThreadPool(0)
		.getWorkQueue(0).addWork((Work)listenerThread);
	} catch (NoSuchThreadPoolException e) {
	    throwable = e;
	} catch (NoSuchWorkQueueException e) {
	    throwable = e;
	}
	if (throwable != null) {
	    RuntimeException rte = new RuntimeException(throwable.toString());
	    rte.initCause(throwable);
	    throw rte;
	}
    }

    private void destroyListenerThread(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".destroyListenerThread: " + eventHandler);
	}

        ListenerThread listenerThread ;
        synchronized (this) {
            listenerThread = listenerThreads.get(eventHandler);
            if (listenerThread == null) {
                if (orb.transportDebugFlag) {
                    dprint(".destroyListenerThread: cannot find ListenerThread - ignoring.");
                }
                return;
            }
            listenerThreads.remove(eventHandler);
        }

	listenerThread.close();
    }

    private void createReaderThread(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".createReaderThread: " + eventHandler);
	}
	CorbaConnection connection = eventHandler.getConnection();
	ReaderThread readerThread = 
	    new ReaderThreadImpl(orb, connection );
        synchronized (this) {
            readerThreads.put(eventHandler, readerThread);
        }
	Throwable throwable = null;
	try {
	    orb.getThreadPoolManager().getThreadPool(0)
		.getWorkQueue(0).addWork((Work)readerThread);
	} catch (NoSuchThreadPoolException e) {
	    throwable = e;
	} catch (NoSuchWorkQueueException e) {
	    throwable = e;
	}
	if (throwable != null) {
	    RuntimeException rte = new RuntimeException(throwable.toString());
	    rte.initCause(throwable);
	    throw rte;
	}
    }

    private void destroyReaderThread(EventHandler eventHandler)
    {
	if (orb.transportDebugFlag) {
	    dprint(".destroyReaderThread: " + eventHandler);
	}
	ReaderThread readerThread ;
        synchronized (this) {
	    readerThread = readerThreads.get(eventHandler);
            if (readerThread == null) {
                if (orb.transportDebugFlag) {
                    dprint(".destroyReaderThread: cannot find ReaderThread - ignoring.");
                }
                return;
            }
            readerThreads.remove(eventHandler);
        }
	readerThread.close();
    }

    private void dprint(String msg)
    {
	ORBUtility.dprint("SelectorImpl", msg);
    }

    protected void dprint(String msg, Throwable t)
    {
	dprint(msg);
	t.printStackTrace(System.out);
    }

    // Private class to contain a SelectionKey and a SelectionKey op.
    // Used only by SelectorImpl to register and enable SelectionKey
    // Op.
    // REVISIT - Could do away with this class and use the EventHanlder
    //           directly.
    private static class SelectionKeyAndOp
    {
        // A SelectionKey.[OP_READ|OP_WRITE|OP_ACCEPT|OP_CONNECT]
        public int keyOp;
        public SelectionKey selectionKey;

        // constructor
        public SelectionKeyAndOp(SelectionKey selectionKey, int keyOp) {
	    this.selectionKey = selectionKey;
	    this.keyOp = keyOp;
	}
    }

// End of file.
}

