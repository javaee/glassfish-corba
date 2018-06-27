/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
// Created       : 2001 May 23 (Wed) 20:32:27 by Harold Carr.
// Last Modified : 2001 Oct 02 (Tue) 20:33:57 by Harold Carr.
//

package pi.serviceexample;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;


public class AServiceORBInitializer 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    private AServiceImpl aServiceImpl;
    private AServiceInterceptor aServiceInterceptor;

    public void pre_init(ORBInitInfo info)
    {
        try {
            int id = info.allocate_slot_id();

            aServiceInterceptor = new AServiceInterceptor(id);

            info.add_client_request_interceptor(aServiceInterceptor);
            info.add_server_request_interceptor(aServiceInterceptor);

            // Create and register a reference to the service to be
            // used by client code.

            aServiceImpl = new AServiceImpl(id);

            info.register_initial_reference("AService", aServiceImpl);

        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }

    public void post_init(ORBInitInfo info)
    {
        try {

            Current piCurrent =
                CurrentHelper.narrow(
                    info.resolve_initial_references("PICurrent"));
            aServiceImpl.setPICurrent(piCurrent);

            CodecFactory codecFactory =
                CodecFactoryHelper.narrow(
                    info.resolve_initial_references("CodecFactory"));
            Encoding encoding = new Encoding((short)0, (byte)1, (byte)2);
            Codec codec = codecFactory.create_codec(encoding);
            aServiceInterceptor.setCodec(codec);
            
            AServiceIORInterceptor aServiceIORInterceptor =
                new AServiceIORInterceptor(codec);
            info.add_ior_interceptor(aServiceIORInterceptor);

        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }

}
 
// End of file.
