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

package com.sun.corba.se.impl.protocol;

import com.sun.corba.se.spi.protocol.RequestId;
import com.sun.corba.se.spi.logging.ORBUtilSystemException;

/**
 * Represents a protocol request id.  Currently used to ensure proper
 * sequencing of fragmented messages.
 *
 * @author Charlie Hunt
 */
public class RequestIdImpl implements RequestId {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    final private int value;
    final private boolean defined;
    final static private String UNDEFINED = "?";
    final static public 
            RequestId UNKNOWN_CORBA_REQUEST_ID = new RequestIdImpl();

    /** Creetes a new instance of CorbaRequestIdImpl */
    public RequestIdImpl(int requestId) {
        this.value = requestId;
        this.defined = true;
    }

    /** Creates a new instance of CorbaRequestIdImpl */
    private RequestIdImpl() {
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
            throw wrapper.undefinedCorbaRequestIdNotAllowed();
        }
    }

    /** Is there a numeric identifier for this CorbaRequestId ? */
    public boolean isDefined() {
        return defined;
    }

    /** Does this CorbaRequestId equal another CorbaRequestId ? */
    @Override
    public boolean equals(Object requestId) {

        if (requestId == null || !(requestId instanceof RequestId)) {
            return false;
        }
        
        if (this.isDefined()) {
            if (((RequestId)requestId).isDefined()) {
                return this.value == ((RequestId)requestId).getValue();
            } else { // requestId is not defined and "this" is defined
                return false;
            }
        } else {
            // "this" is not defined
            // simply return result of NOT requestId.isDefined()
            return !((RequestId)requestId).isDefined();
        }
    }

    /** Return this CorbaRequestId's hashCode */
    @Override
    public int hashCode() {
        return this.value;
    }
    
    /** String representing this CorbaRequestId */
    @Override
    public String toString() {
        if (defined) {
            return Integer.toString(this.value);
        } else {
            return UNDEFINED;
        }
    }
}
