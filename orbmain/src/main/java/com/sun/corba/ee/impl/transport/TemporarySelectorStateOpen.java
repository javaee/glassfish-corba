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
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.TemporarySelectorState;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * A class which models temporary Selector in an open state.
 */
@Transport
public class TemporarySelectorStateOpen implements TemporarySelectorState {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    final static AtomicInteger tsCount = new AtomicInteger(0);

    @Transport
    private void reportNumTemporarySelectors( int num ) {
    }

    /** Creates a new instance of TemporarySelectorStateOpen */
    public TemporarySelectorStateOpen() {
        reportNumTemporarySelectors( tsCount.incrementAndGet() ) ;
    }

    @Transport
    public int select(Selector theSelector, long theTimeout) throws IOException {
        int result = 0;
        if (theSelector.isOpen()) {
            if (theTimeout > 0) {
                result = theSelector.select(theTimeout);
            } else {
                throw wrapper.temporarySelectorSelectTimeoutLessThanOne(
                    theSelector, theTimeout);
            }
        } else {
            throw new TemporarySelectorClosedException(
                "Selector " + theSelector.toString() + " is closed.");
        }

        return result;
    }

    @Transport
    public SelectionKey registerChannel(Selector theSelector, 
        SelectableChannel theSelectableChannel, int theOps) throws IOException {

        SelectionKey key = null;
        if (theSelector.isOpen()) {
            key = theSelectableChannel.register(theSelector, theOps);
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                                                        theSelector.toString() +
                                                       " is closed.");
        }
        return key;
    }

    @Transport
    public TemporarySelectorState cancelKeyAndFlushSelector(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {

        if (theSelectionKey != null) {
            theSelectionKey.cancel();
        }

        if (theSelector.isOpen()) {
            theSelector.selectNow();
        } else {
            throw new TemporarySelectorClosedException(
                "Selector " + theSelector.toString() + " is closed."); }

        return this;
    }

    @Transport
    public TemporarySelectorState close(Selector theSelector) throws IOException {
        theSelector.close();
        reportNumTemporarySelectors( tsCount.decrementAndGet() );
        return new TemporarySelectorStateClosed();
    }

    @Transport
    public TemporarySelectorState removeSelectedKey(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {
        if (theSelector.isOpen()) {
            theSelector.selectedKeys().remove(theSelectionKey);
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                                                        theSelector.toString() +
                                                       " is closed.");
        }
        return this;
    }
}
