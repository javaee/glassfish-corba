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

package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.CorbaRequestId;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;

/**
 * Represents a protocol request id.  Currently used to ensure proper
 * sequencing of fragmented messages.
 *
 * @author Charlie Hunt
 */
public class CorbaRequestIdImpl implements CorbaRequestId {
    final private int value;
    final private boolean defined;
    final static private String UNDEFINED = "?";
    final static public 
            CorbaRequestId UNKNOWN_CORBA_REQUEST_ID = new CorbaRequestIdImpl();

    /** Creetes a new instance of CorbaRequestIdImpl */
    public CorbaRequestIdImpl(int requestId) {
        this.value = requestId;
        this.defined = true;
    }

    /** Creates a new instance of CorbaRequestIdImpl */
    private CorbaRequestIdImpl() {
        this.defined = false;
        // initialize value, but note it is a meaningless value
        // and should not be used via getValue()!
        this.value = -1;
    }

    /** Return the value of this CorbaRequestId */
    public int getValue() {
        if (defined) {
            return this.value;
        } else {
            ORBUtilSystemException wrapper =
                    ORB.getStaticLogWrapperTable().get_RPC_PROTOCOL_ORBUtil();
            throw wrapper.undefinedCorbaRequestIdNotAllowed();
        }
    }

    /** Is there a numeric identifier for this CorbaRequestId ? */
    public boolean isDefined() {
        return defined;
    }

    /** Does this CorbaRequestId equal another CorbaRequestId ? */
    public boolean equals(Object requestId) {

        if (requestId == null || !(requestId instanceof CorbaRequestId)) {
            return false;
        }
        
        if (this.isDefined()) {
            if (((CorbaRequestId)requestId).isDefined()) {
                return this.value == ((CorbaRequestId)requestId).getValue();
            } else { // requestId is not defined and "this" is defined
                return false;
            }
        } else {
            // "this" is not defined
            // simply return result of NOT requestId.isDefined()
            return !((CorbaRequestId)requestId).isDefined();
        }
    }

    /** Return this CorbaRequestId's hashCode */
    public int hashCode() {
        return this.value;
    }
    
    /** String representing this CorbaRequestId */
    public String toString() {
        if (defined) {
            return Integer.toString(this.value);
        } else {
            return UNDEFINED;
        }
    }
}
