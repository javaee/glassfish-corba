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

package com.sun.corba.se.spi.costransactions;

/** The TransactionService interface must be implemented by all 
    Java Transaction Services. It provides a procedure for initialization
    of a JTS in association with an ORB.
    TransactionService is not intended to be visible to the application
    programmer; it is only an interface between the ORB and the JTS.
    During initialization, the application programmer must provide the
    ORB with a JTS implementation through the property 
    "com.sun.corba.se.spi.costransactions.TransactionServiceClass". The ORB
    then instantiates the JTS and calls identify_ORB() on it.
    The following is an example of the 
    initialization steps required in the application code:
    <p>
    // Create a properties object. The properties may also be given as <br>
    // command line arguments, applet parameters, etc. <br>
    Properties p = new Properties(); <br>
    p.put("org.omg.CORBA.ORBClass", "com.sun.corba.se.impl.orb.ORBImpl");
    p.put("com.sun.corba.se.spi.costransactions.ORBJTSClass", 
    					"com.xyz.SomeTransactionService");
    // This property is given to the JTS in the Properties parameter in identify_ORB().
    p.put("com.xyz.CosTransactions.SomeProperty", "SomeValue");

    <p>    
    // Get an ORB object. During initialization, the JTS is also <br>
    // instantiated, and the JTS registers its callbacks with the ORB.<br>
    org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(null, p);
    <p>    
    // Get the Current instance from the ORB <br>
    org.omg.CosTransactions.Current current = (Current)orb.resolve_initial_references("TransactionCurrent");
    <p>
    current.begin(); <br>
    ... <br>
    current.commit(...);  <br>
    <p>

    Note: The package name for TransactionService and the property may change.
*/

public interface TransactionService {

    /** get_current() is called by the ORB during initialization to
	obtain the transaction-service's implementation of the Current
	pseudo-object. Current is available to the application programmer
	thru orb.resolve_initial_references("TransactionCurrent").
    */
    public org.omg.CosTransactions.Current get_current();

    /** identify_ORB is called by the ORB during initialization to
        provide the JTS implementation with an instance of the ORB object, 
	and a TSIdentification object. The ORB instance is necessary so
	that client JTS implementations running in applets do not have
	to store static data which may cause security flaws.
	The TSIdentification object is used by the JTS to register its Sender 
	and Receiver callbacks. The Properties object allows the application
	to pass information to the JTS implementation.
    */
    public void identify_ORB(org.omg.CORBA.ORB orb, 
			     org.omg.CORBA.TSIdentification tsi,
			     java.util.Properties props);
}
