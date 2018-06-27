/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.impl.txpoa;

import org.omg.CORBA.TSIdentification;

public class TSIdentificationImpl extends org.omg.CORBA.LocalObject
        implements TSIdentification {

    private org.omg.CosTSPortability.Sender sender=null;
    private org.omg.CosTSPortability.Receiver receiver=null;

    /** identify_sender is called by the OTS during initialization
        to register its Sender callback interface with the ORB.
        identify_sender may throw a AlreadyIdentified exception if
        the registration has already been done previously.
    */
    public void
        identify_sender(org.omg.CosTSPortability.Sender senderOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable,
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
    {
        if ( sender == null )
            sender = senderOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }


    /** identify_receiver is called by the OTS during initialization
        to register its Receiver callback interface with the ORB.
        identify_receiver may throw a AlreadyIdentified exception if
        the registration has already been done previously.
    */
    public void
        identify_receiver(org.omg.CosTSPortability.Receiver receiverOTS)
        throws org.omg.CORBA.TSIdentificationPackage.NotAvailable,
               org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
    {
        if ( receiver == null )
            receiver = receiverOTS;
        else
            throw new org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified();
    }


    /** getSender is not defined in the OTS spec. It is just a convenience
        method to allow the ORB to access the Sender subsequent to
        initialization.
    */
    public org.omg.CosTSPortability.Sender
        getSender()
    {
        return sender;
    }

    /** getReceiver is not defined in the OTS spec. It is just a convenience
        method to allow the ORB to access the Receiver subsequent to
        initialization.
    */
    public org.omg.CosTSPortability.Receiver
        getReceiver()
    {
        return receiver;
    }
}
