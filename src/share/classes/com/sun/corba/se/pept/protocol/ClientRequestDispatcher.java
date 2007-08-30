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

package com.sun.corba.se.pept.protocol;

import com.sun.corba.se.pept.broker.Broker;
import com.sun.corba.se.pept.encoding.InputObject;
import com.sun.corba.se.pept.encoding.OutputObject;
import com.sun.corba.se.pept.transport.ContactInfo;

/**
 * <code>ClientRequestDispatcher</code> coordinates the request (and possible
 * response) processing for a specific <em>protocol</em>.
 *
 * @author Harold Carr
 */
public interface ClientRequestDispatcher
{
    /**
     * At the beginning of a request the presentation block uses this
     * to obtain an
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     * to set data to be sent on a message.
     *
     * @param self -
     * @param methodName - the remote method name
     * @param isOneWay - <code>true</code> if the message is asynchronous
     * @param contactInfo - the
     * {@link com.sun.corba.se.pept.transport.ContactInfo ContactInfo}
     * which which created/chose this <code>ClientRequestDispatcher</code>
     *
     * @return
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     */
    public OutputObject beginRequest(Object self,
				     String methodName,
				     boolean isOneWay,
				     ContactInfo contactInfo);

    /**
     * After the presentation block has set data on the
     * {@link com.sun.corba.se.pept.encoding.OutputObject OutputObject}
     * it signals the PEPt runtime to send the encoded data by calling this
     * method.
     *
     * @param self -
     * @param outputObject
     *
     * @return
     * {@link com.sun.corba.se.pept.encoding.InputObject InputObject}
     * if the message is synchronous.
     *
     * @throws
     * {@link org.omg.CORBA.portable.ApplicationException ApplicationException}
     * if the remote side raises an exception declared in the remote interface.
     * 
     * @throws
     * {@link org.omg.CORBA.portable.RemarshalException RemarshalException}
     * if the PEPt runtime would like the presentation block to start over.
     */
    public InputObject marshalingComplete(java.lang.Object self,
					  OutputObject outputObject)
    // REVISIT EXCEPTIONS
	throws
	    org.omg.CORBA.portable.ApplicationException, 
	    org.omg.CORBA.portable.RemarshalException;

    /**
     * After the presentation block completes a request it signals
     * the PEPt runtime by calling this method.
     *
     * This method may release resources.  In some cases it may cause
     * control or error messages to be sent.
     *
     * @param broker -
     * @param inputObject -
     */
    public void endRequest(Broker broker,
			   java.lang.Object self, 
			   InputObject inputObject);
}

// End of file.
