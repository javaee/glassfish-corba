/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
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

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.sun.corba.ee.spi.transport.TemporarySelectorState;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * Encapsulates a temporary Selector and temporary Selector state
 */
public class TemporarySelector {
    
    private TemporarySelectorState itsState;
    private Selector itsSelector;

    /** Creates a new instance of TemporarySelector */
    public TemporarySelector(SelectableChannel theSelectableChannel) throws IOException {
        itsSelector = theSelectableChannel.provider().openSelector();
        itsState = new TemporarySelectorStateOpen();
    }
    
    /**
     * NOTE: There is a potential for a situation, (albiet very remote), that
     *       some other thread may be initiating an explicit "close" of a 
     *       Connection (if someone overrides the implementation of
     *       SocketOrChannelConnectionImpl and an explicit call to "close"
     *       the Connection), that call to close the Connection may also
     *       attempt to close a TemporarySelector.  If that TemporarySelector
     *       is currently in the select(long theTimeout), then the closing
     *       of that TemporarySelector will not occur until the 
     *       select(long theTimeout) method exits, (i.e. maximum blocking wait
     *       time for the close will be theTimeout milliseconds which by
     *       default is 2000 milliseconds).
     *       This artifact occurs as a result of the TemporarySelector's
     *       select() and close() operations being atomic operations.
     *       However, this potential issue does not exist in the current
     *       implementation of SocketOrChannelConnectionImpl. It may arise
     *       if someone chooses to extend the implementation of the
     *       SocketOrChannelConnectionImpl and make explicit calls to
     *       close the Connection. An example of this potential scenario
     *       can be found in the "no connection cache" plug-in implementation.
     *       To avoid this potential scenario, the "no connection
     *       cache" plug-in disables the read optimization to always
     *       enter a blocking read.  
     *       See com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl.java
     *       to see how the 'always enter blocking read' optimization is
     *       disabled.
     */
    synchronized public int select(long theTimeout) throws IOException {
        return itsState.select(itsSelector, theTimeout);
    }
    
    synchronized public SelectionKey registerChannel(SelectableChannel theSelectableChannel, int theOps) throws IOException {
        return itsState.registerChannel(itsSelector, theSelectableChannel, theOps);
    }
 
    /**
     * NOTE: There is a potential for a situation, (albiet very remote), that
     *       some other thread may be in this TemporarySelector's select()
     *       method while another thread is trying to call this "close" method 
     *       as a result of an explicit close of a Connection (if someone 
     *       overrides the implementation of SocketOrChannelConnectionImpl 
     *       and makes an explicit call to "close" the Connection), that call 
     *       to close the Connection may also attempt to call this close method.
     *       If that other thread is currently in this TemporarySelector's 
     *       select(long theTimeout) method, then the call to this close method
     *       will block until the select(long theTimeout) method exits, (i.e. 
     *       maximum blocking wait time for this close will be theTimeout 
     *       milliseconds which by default is 2000 milliseconds).
     *       This artifact occurs as a result of the TemporarySelector's
     *       select() and close() operations being atomic operations.
     *       However, this potential issue does not exist in the current
     *       implementation of SocketOrChannelConnectionImpl. It may arise
     *       if someone chooses to extend the implementation of the
     *       SocketOrChannelConnectionImpl and make explicit calls to
     *       close the Connection. An example of this potential scenario
     *       exists in the "no connection cache" plug-in.  To avoid this 
     *       scenario, the "no connection cache" plug-in disables the read
     *       optimization to always enter a blocking read.
     *       See com.sun.corba.ee.impl.plugin.hwlb.NoConnectionCacheImpl.java
     *       to see how the 'always enter blocking read' optimization is
     *       disabled.
     */
    synchronized public void close() throws IOException {
        itsState = itsState.close(itsSelector);
    }
    
    synchronized public void removeSelectedKey(SelectionKey theSelectionKey) throws IOException {
        itsState = itsState.removeSelectedKey(itsSelector, theSelectionKey);
    }

    synchronized public void cancelAndFlushSelector(SelectionKey theSelectionKey) throws IOException {
        itsState = itsState.cancelKeyAndFlushSelector(itsSelector, theSelectionKey);
    }

}
