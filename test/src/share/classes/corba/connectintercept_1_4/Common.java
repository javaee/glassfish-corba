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
// Created       : 2000 Sep 26 (Tue) 16:26:48 by Harold Carr.
// Last Modified : 2004 Jun 06 (Sun) 12:21:53 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;

import com.sun.corba.se.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.se.spi.legacy.interceptor.UnknownType;

public abstract class Common
{
    public static String baseMsg = Common.class.getName();

    public static final String ORBClassKey = "org.omg.CORBA.ORBClass";

    public static final String DEFAULT_FACTORY_CLASS =
	"com.sun.corba.se.impl.legacy.connection.DefaultSocketFactory";

    public static final String CUSTOM_FACTORY_CLASS =
	MySocketFactory.class.getName();

    public static final String serverName1 = "ExI1";
    public static final String serverName2 = "ExI2";

    public static final String MyType1 = "MyType1";
    public static final String MyType2 = "MyType2";
    public static final String MyType3 = "MyType3";
    public static final int    MyType1TransientPort  = 2000;
    public static final int    MyType2TransientPort  = 2001;
    public static final int    MyType3TransientPort  =    0;
    public static final int    MyType1PersistentPort =    0;
    public static final int    MyType2PersistentPort =    0;
    public static final int    MyType3PersistentPort =    0;

    public static final int ListenPortsComponentID = 4545;

    public static final String DummyType = "DummyType";
    public static final String DummyHost = "DummyHost";
    public static final int    DummyPort = 65000;

    public static final String Transient  = "Transient";
    public static final String Persistent = "Persistent";

    // To test proprietary interceptor ordering.
    // These are used by both client and server, but in
    // different VMs - so no conflict.

    public static int currentOrder = 1;

    // Need to reset before each case (since destroy leaves it
    // at 3 but first invocation expects 1.
    public static void upDownReset() { currentOrder = 1; }

    public static void up(int x)
    {
	if (x >= currentOrder) {
	    currentOrder = x;
	} else {
	    throw new INTERNAL("Interceptor ordering (up): " 
			       + x + " " + currentOrder,
			       -45,
			       CompletionStatus.COMPLETED_MAYBE);
	}
    }

    public static void down(int x)
    {
	if (x <= currentOrder) {
	    currentOrder = x;
	} else {
	    throw new INTERNAL("Interceptor ordering (down): "
			       + x + " " + currentOrder,
			       -45,
			       CompletionStatus.COMPLETED_MAYBE);
	}
    }

    public static String createComponentData(String msg, Object o)
    {
	String ccd = baseMsg + ".createComponentData";
	int MyType1Port = -1;
	int MyType2Port = -1;
	int MyType3Port = -1;
	if (o instanceof IORInfoExt) {
	    try {
		MyType1Port = ((IORInfoExt)o).getServerPort(Common.MyType1);
		MyType2Port = ((IORInfoExt)o).getServerPort(Common.MyType2);
		MyType3Port = ((IORInfoExt)o).getServerPort(Common.MyType3);
	    } catch (UnknownType ex) {
		System.out.println(ccd + ": " + ex);
		System.exit(-1);
	    }
	} else {
	    throw new RuntimeException(ccd + 
				       ": unexpected type of object: " + o);
	}

	return createComponentData(msg, MyType1Port, MyType2Port, MyType3Port);
    }

    public static String createComponentData(String msg,
					     int MyType1Port,
					     int MyType2Port,
					     int MyType3Port)
    {
	String componentData =
	    Common.MyType1 + ":" + MyType1Port + "," +
	    Common.MyType2 + ":" + MyType2Port + "," +
	    Common.MyType3 + ":" + MyType3Port;
	System.out.println(msg + " componentData: " + componentData);

	return componentData;
    }

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

