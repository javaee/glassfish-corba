/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2002 Jul 19 (Fri) 14:47:13 by Harold Carr.
// Last Modified : 2004 Jun 06 (Sun) 12:21:47 by Harold Carr.
//

package corba.iorintsockfact;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import com.sun.corba.se.spi.orbutil.ORBConstants;

/**
 * @author Harold Carr
 */
public abstract class Common
{
    public static final String SOCKET_FACTORY_CLASS_PROPERTY =
	ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY;

    public static final String CUSTOM_FACTORY_CLASS =
	SocketFactory.class.getName();

    public static final String serverName1 = "I1";

    public static NamingContext getNameService(ORB orb)
    {
        org.omg.CORBA.Object objRef = null;
	try {
	    objRef = orb.resolve_initial_references("NameService");
	} catch (Exception ex) {
	    System.out.println("Common.getNameService: " + ex);
	    ex.printStackTrace(System.out);
	    System.exit(-1);
	}
        return NamingContextHelper.narrow(objRef);
    }

    public static NameComponent[] makeNameComponent(String name)
    {
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
	return path;
    }
}

// End of file.

