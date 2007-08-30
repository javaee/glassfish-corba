/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.pept.transport;

import java.io.IOException;

import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.protocol.MessageMediator;
import com.sun.corba.se.pept.transport.EventHandler;


/**
 * <p><code>Connection</code> represents a <em>transport</em> in the
 * PEPt architecture.</p>
 *
 * @author Harold Carr 
*/
public interface Connection 
{
    /**
     * Used to determine if the <code>Connection</code> should register
     * with the 
     * {@link com.sun.corba.se.pept.transport.TransportManager
     * TransportManager}
     * {@link com.sun.corba.se.pept.transport.Selector Selector}
     * to handle read events.
     *
     * For example, an HTTP transport would not register since the requesting
     * thread would just block on read when waiting for the reply.
     *
     * @return <code>true</code> if it should be registered.
     */
    public boolean shouldRegisterReadEvent();

    /**
     * Used to determine if the <code>Connection</code> should register
     * with the 
     * {@link com.sun.corba.se.pept.transport.TransportManager
     * TransportManager}
     * {@link com.sun.corba.se.pept.transport.Selector Selector}
     * to handle read events.
     *
     * For example, an HTTP transport would not register since the requesting
     * thread would just block on read when waiting for the reply.
     *
     * @return <code>true</code> if it should be registered.
     */
    public boolean shouldRegisterServerReadEvent(); // REVISIT - why special?

    /**
     * Called to read incoming messages.
     *
     * @return <code>true</code> if the thread calling read can be released.
     */
    public boolean read();

    /**
     * Close the <code>Connection</code>.
     *
     */
    public void close();

    // REVISIT: replace next two with PlugInFactory (implemented by ContactInfo
    // and Acceptor).

    /**
     * Get the
     * {@link com.sun.corba.se.pept.transport.Acceptor Acceptor}
     * that created this <code>Connection</code>.
     *
     * @return 
     * {@link com.sun.corba.se.pept.transport.Acceptor Acceptor}
     */
    public Acceptor getAcceptor();

    /**
     * Get the
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     * that created this <code>Connection</code>.
     *
     * @return 
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     */
    public ContactInfo getContactInfo();

    /**
     * Get the 
     * {@link com.sun.corba.se.pept.transport.EventHandler EventHandler}
     * associated with this <code>Acceptor</code>.
     *
     * @return 
     * {@link com.sun.corba.se.pept.transport.EventHandler EventHandler}
     */
    public EventHandler getEventHandler();

    /**
     * Indicates whether a 
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     * or a
     * {@link com.sun.corba.se.pept.transport.Acceptor Acceptor}
     * created the
     * <code>Connection</code>.
     *
     * @return <code>true</code> if <code>Connection</code> an
     * {@link com.sun.corba.se.pept.transport.Acceptor Acceptor}
     * created the <code>Connection</code>.
     */
    public boolean isServer();

    /**
     * Indicates if the <code>Connection</code> is closed.
     *
     * @return <code>true</code> if the <code>Connection</code> is closed.
     */
    public boolean isClosed();

    /**
     * Indicates if the <code>Connection</code> is in the process of
     * sending or receiving a message.
     *
     * @return <code>true</code> if the <code>Connection</code> is busy.
     */
    public boolean isBusy();

    /**
     * Timestamps are used for connection management, in particular, for
     * reclaiming idle <code>Connection</code>s.
     *
     * @return the "time" the <code>Connection</code> was last used.
     */
    public long getTimeStamp();

    /**
     * Timestamps are used for connection management, in particular, for
     * reclaiming idle <code>Connection</code>s.
     *
     * @param time - the "time" the <code>Connection</code> was last used.
     */
    public void setTimeStamp(long time);

    /**
     * The "state" of the <code>Connection</code>.
     *
     * param state
     */
    public void setState(String state);

    /**
     * Grab a write lock on the <code>Connection</code>.
     *
     * If another thread already has a write lock then the calling
     * thread will block until the lock is released.  The calling
     * thread must call
     * {@link #writeUnlock}
     * when it is done.
     */
    public void writeLock();

    /**
     * Release a write lock on the <code>Connection</code>.
     */
    public void writeUnlock();

    /*
     * Send the data encoded in
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     * on the <code>Connection</code>.
     *
     * @param outputObject
     */
    public void sendWithoutLock(OutputObject outputObject);

    /**
     * Register an invocation's 
     * {@link com.sun.corba.se.pept.protocol.MessageMediator MessageMediator}
     * with the <code>Connection</code>.
     *
     * This is useful in protocols which support fragmentation.
     *
     * @param messageMediator
     */
    public void registerWaiter(MessageMediator messageMediator);

    /**
     * If a message expect's a response then this method is called.
     *
     * This method might block on a read (e.g., HTTP), put the calling
     * thread to sleep while another thread read's the response (e.g., GIOP),
     * or it may use the calling thread to perform the server-side work
     * (e.g., Solaris Doors).
     *
     * @param messageMediator
     */
    public InputObject waitForResponse(MessageMediator messageMediator);

    /**
     * Unregister an invocation's 
     * {@link com.sun.corba.se.pept.protocol.MessageMediator MessageMediator}
     * with the <code>Connection</code>.
     *
     * @param messageMediator
     */
    public void unregisterWaiter(MessageMediator messageMediator);

    public void setConnectionCache(ConnectionCache connectionCache);

    public ConnectionCache getConnectionCache();
}

// End of file.




