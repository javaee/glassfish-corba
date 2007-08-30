/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.spi.transport;

/** Interface that supports capturing the GIOP messages for the
 * last non-co-located invocation in the current thread.
 * This enables easy capture of the GIOP messages for testing
 * purposes.
 */
public interface MessageTraceManager {
    /** Discard all messages accumulated since the last call to
     * clear.
     */
    void clear() ;

    /** Returns true if messages are to be captured on this thread, otherwise
     * false.
     */
    boolean isEnabled() ;

    /** Called with flag=true to enable capture of messages.
     */
    void enable( boolean flag ) ;

    /** Return an array of messages (represented as byte[]) for the 
     * message(s) sent on this thread since the last call to clear(). 
     * If there is a Location Forward in this invocation, the 
     * the data returned will include all requests sent starting
     * with the first request.
     */
    byte[][] getDataSent() ;

    /** Return an array of messages (represented as byte[]) for the 
     * message(s) received since the last call to clear().  
     */
    byte[][] getDataReceived() ;
}
