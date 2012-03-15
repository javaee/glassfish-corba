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

package org.omg.CORBA;

/**
* A user exception thrown when a policy error occurs.  A <code>PolicyError</code>
* exception may include one of the following policy error reason codes
* defined in the org.omg.CORBA package: BAD_POLICY, BAD_POLICY_TYPE,
* BAD_POLICY_VALUE, UNSUPPORTED_POLICY, UNSUPPORTED_POLICY_VALUE.
*/

// @SuppressWarnings({"serial"})
public final class PolicyError extends org.omg.CORBA.UserException {

    /** 
     * The reason for the <code>PolicyError</code> exception being thrown.
     * @serial
     */
    public short reason;

    /**
     * Constructs a default <code>PolicyError</code> user exception
     * with no reason code and an empty reason detail message.
     */
    public PolicyError() {
        super();
    }

    /**
     * Constructs a <code>PolicyError</code> user exception
     * initialized with the given reason code and an empty reason detail message.
     * @param __reason the reason code.
     */
    public PolicyError(short __reason) {
        super();
        reason = __reason;
    }

    /**
     * Constructs a <code>PolicyError</code> user exception
     * initialized with the given reason detail message and reason code.
     * @param reason_string the reason detail message.
     * @param __reason the reason code.
     */
    public PolicyError(String reason_string, short __reason) {
        super(reason_string);
        reason = __reason;
    }
}
