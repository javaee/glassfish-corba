/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.transport.TemporarySelectorState;

import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.orbutil.ORBUtility;

/**
 *
 * @author Charlie Hunt
 */

/**
 *
 * A class which models temporary Selector in an open state.
 */
public class TemporarySelectorStateOpen implements TemporarySelectorState {

    final static AtomicInteger tsCount = new AtomicInteger(0);
    final private boolean debug;
    private ORB itsOrb;

    private TemporarySelectorStateOpen () {
        // must be initialized to rid of compiler complaint
        debug = true;
    }

    /** Creates a new instance of TemporarySelectorStateOpen */
    public TemporarySelectorStateOpen(ORB theOrb) {
        itsOrb = theOrb;
        debug = itsOrb.transportDebugFlag;
        if (debug) {
            dprint("(): number of open temporary selectors : " + 
                    tsCount.incrementAndGet());
        }
    }

    public int select(Selector theSelector, long theTimeout) throws IOException {
        if (debug) {
            dprint("select()->: selector: " + theSelector + ", timeout: " +
                    theTimeout);
        }
        int result = 0;
        if (theSelector.isOpen()) {
            if (theTimeout > 0) {
                result = theSelector.select(theTimeout);
            } else {
                ORBUtilSystemException wrapper =
		     itsOrb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;
                throw wrapper.temporarySelectorSelectTimeoutLessThanOne(theSelector, theTimeout);
            }
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                                                        theSelector.toString() +
                                                       " is closed.");
        }

        if (debug) {
            dprint("select()<-: selector: " + theSelector +
                   ", number selected: " + result);
        }
        return result;
    }

    public SelectionKey registerChannel(Selector theSelector,
                                        SelectableChannel theSelectableChannel,
                                        int theOps) throws IOException {

        if (debug) {
            dprint("registerChannel()->: selector: " + theSelector);
        }
        SelectionKey key = null;
        if (theSelector.isOpen()) {
            key = theSelectableChannel.register(theSelector, theOps);
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                                                        theSelector.toString() +
                                                       " is closed.");
        }
        if (debug) {
            dprint("registerChannel()<-: selector: " + theSelector +
                   ", SelectionKey: " + key);
        }
        return key;
    }

    public TemporarySelectorState cancelKeyAndFlushSelector(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {
        if (debug) {
            dprint("cancelKeyAndFlushSelector()->: selector: " + theSelector);
        }
        if (theSelectionKey != null) {
            if (debug) {
                dprint("cancelKeyAndFlushSelector(): cancel key: " +
                        theSelectionKey);
            }
            theSelectionKey.cancel();
        }
        if (theSelector.isOpen()) {
            theSelector.selectNow();
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                    theSelector.toString() +
                    " is closed.");
        }
        if (debug) {
            dprint("cancelKeyAndFlushSelector()<-: cancelled and flushed");
        }
        return this;
    }

    public TemporarySelectorState close(Selector theSelector) throws IOException {
        if (debug) {
            dprint("close()->: selector: " + theSelector);
        }
        theSelector.close();
        if (debug) {
            dprint("close(): number of open temporary selectors : " + 
                    tsCount.decrementAndGet());
            dprint("close()<-: changing to closed state");
        }
        return new TemporarySelectorStateClosed(itsOrb);
    }

    public TemporarySelectorState removeSelectedKey(Selector theSelector,
                              SelectionKey theSelectionKey) throws IOException {
        if (debug) {
            dprint("removeSelectedKey()->: selector: " + theSelector +
                   ", key to remove: " + theSelectionKey);
        }
        if (theSelector.isOpen()) {
            theSelector.selectedKeys().remove(theSelectionKey);
        } else {
            throw new TemporarySelectorClosedException("Selector " +
                                                        theSelector.toString() +
                                                       " is closed.");
        }
        if (debug) {
            dprint("removeSelectedKey()<-: selector: " + theSelector +
                   ", key removed: " + theSelectionKey);
        }
        return this;
    }

    private void dprint(String theMsg) {
	ORBUtility.dprint("TemporarySelectorStateOpen", theMsg);
    }
}
