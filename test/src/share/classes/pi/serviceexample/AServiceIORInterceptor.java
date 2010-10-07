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
// Created       : 2001 Sep 24 (Mon) 20:51:03 by Harold Carr.
// Last Modified : 2001 Oct 02 (Tue) 20:49:16 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

public class AServiceIORInterceptor
    extends org.omg.CORBA.LocalObject
    implements IORInterceptor
{
    private Codec codec;

    public AServiceIORInterceptor(Codec codec)
    {
	this.codec = codec;
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
	return "AServiceInterceptor";
    }

    public void destroy() 
    {
    }

    //
    // IOR Interceptor operations
    //

    public void establish_components(IORInfo info)
    {
	//
	// Note: typically, rather than just inserting a tagged component
	// this interceptor would check info.get_effective_policy(int)
	// to determine if a tagged component reflecting that policy
	// should be added to the IOR.  That is not shown in this example.
	// 

	ASERVICE_COMPONENT aServiceComponent = new ASERVICE_COMPONENT(true);
	Any any = ORB.init().create_any();
	ASERVICE_COMPONENTHelper.insert(any, aServiceComponent);
	byte[] value = null;
	try {
	    value = codec.encode_value(any);
	} catch (InvalidTypeForEncoding e) {
	    System.out.println("Exception handling not shown.");
	}
	TaggedComponent taggedComponent =
	    new TaggedComponent(TAG_ASERVICE_COMPONENT.value, value);
	info.add_ior_component(taggedComponent);
    }

}

// End of file.

