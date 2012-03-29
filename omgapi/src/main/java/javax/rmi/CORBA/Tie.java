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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA;

import java.rmi.Remote;
import java.util.Hashtable;

import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ObjectImpl; 
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.ORB;

/**
 * Defines methods which all RMI-IIOP server side ties must implement.
 */
public interface Tie extends org.omg.CORBA.portable.InvokeHandler {
    /**
     * Returns an object reference for the target object represented by
     * this tie.
     * @return an object reference for the target object.
     */
    org.omg.CORBA.Object thisObject();
    
    /**
     * Deactivates the target object represented by this tie.
     */
    void deactivate() throws java.rmi.NoSuchObjectException;
    
    /**
     * Returns the ORB for this tie.
     * @return the ORB.
     */
    ORB orb();
    
    /**
     * Sets the ORB for this tie.
     * @param orb the ORB.
     */
    void orb(ORB orb);
    
    /**
     * Called by {@link Util#registerTarget} to set the target
     * for this tie.
     * @param target the object to use as the target for this tie.
     */
    void setTarget(java.rmi.Remote target);
   
    /**
     * Returns the target for this tie.
     * @return the target.
     */
    java.rmi.Remote getTarget();
}
