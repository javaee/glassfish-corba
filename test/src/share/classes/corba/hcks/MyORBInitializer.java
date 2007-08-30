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
// Created       : 2000 Sep 22 (Fri) 11:34:53 by Harold Carr.
// Last Modified : 2001 Feb 05 (Mon) 15:01:52 by Harold Carr.
//

package corba.hcks;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;

public class MyORBInitializer 
    extends
        org.omg.CORBA.LocalObject
    implements
	org.omg.PortableInterceptor.ORBInitializer
{
    public static final String baseMsg = MyORBInitializer.class.getName();

    public void pre_init(ORBInitInfo info)
    {
	try {
	    MyInterceptor interceptor = new MyInterceptor();
	    info.add_client_request_interceptor(interceptor);
	    info.add_server_request_interceptor(interceptor);
	    U.sop(baseMsg + ".pre_init");
	} catch (Throwable t) {
	    U.sopUnexpectedException(baseMsg, t);
	}
    }

    public void post_init(ORBInitInfo info)
    {
	try {
	    Current piCurrent = 
		CurrentHelper.narrow(
                    info.resolve_initial_references(U.PICurrent));
	    NamingContext nameService =
		NamingContextHelper.narrow(
		   info.resolve_initial_references(U.NameService));;

	    SsPicInterceptor.sPic1ASlotId = info.allocate_slot_id();
	    SsPicInterceptor.sPic1BSlotId = info.allocate_slot_id();
	    int sPic2ASlotId = info.allocate_slot_id();
	    int sPic2BSlotId = info.allocate_slot_id();

	    SsPicInterceptor sPicAInt = 
		new SsPicInterceptor(SsPicInterceptor.sPic1AServiceContextId,
				     SsPicInterceptor.sPic2AServiceContextId,
				     SsPicInterceptor.sPic1ASlotId,
				     sPic2ASlotId,
				     piCurrent,
				     nameService,
				     "sPicA");
	    info.add_client_request_interceptor(sPicAInt);
	    info.add_server_request_interceptor(sPicAInt);
	    
	    
	    SsPicInterceptor sPicBInt = 
		new SsPicInterceptor(SsPicInterceptor.sPic1BServiceContextId,
				     SsPicInterceptor.sPic2BServiceContextId,
				     SsPicInterceptor.sPic1BSlotId,
				     sPic2BSlotId,
				     piCurrent,
				     nameService,
				     "sPicB");
	    info.add_client_request_interceptor(sPicBInt);
	    info.add_server_request_interceptor(sPicBInt);
	    U.sop(baseMsg + ".post_init");
	} catch (Throwable t) {
	    U.sopUnexpectedException(baseMsg, t);
	}
    }

}
 
// End of file.
