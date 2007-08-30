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
// Created       : 2000 Sep 27 (Wed) 17:37:35 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:00:16 by Harold Carr.
//

package corba.connectintercept_1_4;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.impl.orb.ORBImpl;

public class MyPIORB 
    extends
        ORBImpl 
{
    public static final String baseMsg = 
	MyPIORB.class.getName() + ".objectReferenceCreated: ";

    protected IOR objectReferenceCreated (IOR ior) 
    {
	String componentData = Common.createComponentData(baseMsg, this);

	// This test puts the information in the IOR via
	// the ServerIORInterceptor. The example here is just to
	// show how to use the old hooks to get the info.
	// You would put that info in the given IOR similar to
	// the ServerIORInterceptor code then return the augmented
	// ior.
	return ior ;
    }
}
 
// End of file.

