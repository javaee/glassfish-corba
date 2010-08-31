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
// Created       : 2005 Oct 05 (Wed) 14:11:24 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:07:07 by Harold Carr.
//

package corba.lb;

import java.util.Vector;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

public class Common
{
    public static final String ReferenceName = "Test";
    private static final String NameService = "NameService";

    public static org.omg.CORBA.Object resolve(String name, ORB orb)
	throws 
	    Exception
    {
	return getNameService(orb).resolve(makeNameComponent(name));
    }

    public static org.omg.CORBA.Object rebind(String name,
					      org.omg.CORBA.Object ref,
					      ORB orb)
	throws 
	    Exception
    {
	NamingContext nc = getNameService(orb);
	nc.rebind(makeNameComponent(name), ref);
	return ref;
    }
  
    public static NameComponent[] makeNameComponent(String name)
    {
	Vector result = new Vector();
	StringTokenizer tokens = new StringTokenizer(name, "/");
	while (tokens.hasMoreTokens()) {
	    result.addElement(tokens.nextToken());
	}
	NameComponent path[] = new NameComponent[result.size()];
	for (int i = 0; i < result.size(); ++i) {
	    path[i] = new NameComponent((String)result.elementAt(i), "");
	}
	return path;
    }

    public static NamingContext getNameService(ORB orb)
	throws
	    Exception
    {
	return NamingContextHelper.narrow(
            orb.resolve_initial_references(NameService));
    }
}

// End of file.
