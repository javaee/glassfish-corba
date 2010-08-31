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
//
// Created       : 2003 May 18 (Sun) 22:16:39 by Harold Carr.
// Last Modified : 2003 May 20 (Tue) 07:50:28 by Harold Carr.
//

package corba.islocal;

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.IMP_LIMIT;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;

import corba.hcks.U;

public class MyServantLocator
    extends
	org.omg.CORBA.LocalObject
    implements
	ServantLocator
{
    public static final String baseMsg = MyServantLocator.class.getName();
    public static final String thisPackage = 
	MyServantLocator.class.getPackage().getName();


    public ORB orb;

    public MyServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
			     CookieHolder cookieHolder)
	throws
	    ForwardRequest
    {
	ClassLoader classLoader      = null;
	Class rmiiIServantPOAClass   = null;
	Object rmiiIServantPOAObject = null;
	Tie tie                      = null;
	try {
	    classLoader = new CustomClassLoader();
	    rmiiIServantPOAClass = 
		classLoader.loadClass(thisPackage + ".rmiiIServantPOA");
	    rmiiIServantPOAObject = rmiiIServantPOAClass.newInstance();
	    classLoader = rmiiIServantPOAObject.getClass().getClassLoader();
	    System.out.println("rmiiIServantPOAClass: "
			       + rmiiIServantPOAClass);
	    System.out.println("rmiiIServantPOAObject classLoader: " +
			       classLoader);
	    System.out.println("rmiiIServantPOAObject: " +
			       rmiiIServantPOAObject);
	    //tie = javax.rmi.CORBA.Util.getTie(rmiiIServantPOAObject);
	    tie = (Tie) Class.forName(thisPackage + "._rmiiIServantPOA_Tie")
		.newInstance();
	    reflect(tie.getClass());
	    reflect(java.rmi.Remote.class);
	    reflect(rmiiIServantPOAObject.getClass());
	    tie.setTarget((java.rmi.Remote)rmiiIServantPOAObject);
	    return (Servant) tie;
	} catch (Throwable t) {
	    U.sopUnexpectedException("preinvoke", t);
	    System.exit(-1);
	}
	return null;
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
			   java.lang.Object cookie, Servant servant)
    {
    }

    private void reflect(Class c)
    {
	reflect(c, 0);
    }

    private void reflect(Class c, int indent)
    {
	for (int i = 0; i < indent; i++) {
	    System.out.print(" ");
	}
	System.out.println(c + " " + c.getClassLoader());

	Class[] interfaces = c.getInterfaces();
	for (int j = 0; j < interfaces.length; j++) {
	    reflect(interfaces[j], indent + 2);
	}
	if (c.getSuperclass() != null) {
	    reflect(c.getSuperclass(), indent + 2);
	}
    }
}

// End of file.

