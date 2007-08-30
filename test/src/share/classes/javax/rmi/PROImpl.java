/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1998-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.naming.InitialContext;

public class PROImpl extends PortableRemoteObject implements PROHello {
    
    public PROImpl () throws RemoteException {
        super();
    }
    
    public String sayHello () throws RemoteException {
        return HELLO;
    }
    
    public Dog getDogValue () throws RemoteException {
	return new DogImpl ("Bow wow!");
    }

    public Dog getDogServer () throws RemoteException {
	return new DogServer ("Yip Yip Yip!");
    }

    public void unexport () throws RemoteException {
	PortableRemoteObject.unexportObject(this);
    }
    
    public static void main (String[] args) {
        
        // args[0] == 'iiop' || 'jrmp'
        // args[1] == publishName
        
        try {
          
            if (args[0].equalsIgnoreCase("iiop")) {
                System.getProperties().put("java.naming.factory.initial","com.sun.jndi.cosnaming.CNCtxFactory");
            } else if (args[0].equalsIgnoreCase("jrmp")) {
                System.getProperties().put("java.naming.factory.initial","com.sun.jndi.registry.RegistryContextFactory");
            }
            
            InitialContext context = new InitialContext ();
            context.rebind (args[1], new PROImpl());
          
        } catch (Exception e) {
            System.out.println ("Caught: " + e.getMessage());
        }
    }
}
