/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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


package org.omg.CORBA;


/** 
 * An interface defined in the OMG Transactions Service Specification 
 * that provides methods to allow the JTS to register
 * its Sender and Receiver interfaces with the ORB. <code>TSIdentification</code> 
 * methods are always called from the same address space (i.e. it is 
 * a pseudo-object), hence it is not necessary to define any stubs/skeletons. 
 * During initialization, an instance of <code>TSIdentification</code> is provided 
 * to the JTS by the ORB using the method 
 * <code>com.sun.corba.se.spi.costransactions.TransactionService.identify_ORB</code>.
*/

public interface TSIdentification { 

    /** 
     * Called by the OTS 
     * during initialization in order to register its Sender 
     * callback interface with the ORB. This method
     * may throw an <code>AlreadyIdentified</code> exception if
     * the registration has already been done previously.
	 * @param senderOTS the <code>Sender</code> object to be
	 *                  registered
	 * @throws org.omg.CORBA.TSIdentificationPackage.NotAvailable
	 *         if the ORB is unavailable to register the given <code>Sender</code>
	 *         object
	 * @throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
	 *         if the given <code>Sender</code> object has already been registered
	 *         with the ORB
	 *         
    */
    public void 
	identify_sender(org.omg.CosTSPortability.Sender senderOTS)
	throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, 
	       org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified ;


    /** 
     * Called by the OTS during 
     * initialization to register its <code>Receiver</code> callback interface 
     * with the ORB.  This operation may throw an <code> AlreadyIdentified</code> 
     * exception if the registration has already been done previously.
	 * @param receiverOTS the <code>Receiver</code> object to register with the ORB
	 * @throws org.omg.CORBA.TSIdentificationPackage.NotAvailable
	 *         if the ORB is unavailable to register the given <code>Receiver</code>
	 *         object
	 * @throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
	 *         if the given <code>Receiver</code> object has already been registered
	 *         with the ORB
    */
    public void 
	identify_receiver(org.omg.CosTSPortability.Receiver receiverOTS)
	throws org.omg.CORBA.TSIdentificationPackage.NotAvailable, 
	       org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified ;
}


