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
// Created       : 2002 Jul 19 (Fri) 13:43:29 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:07:06 by Harold Carr.
//

package corba.folb_8_1;

import java.net.InetAddress;

import org.omg.CORBA.Any;
import org.omg.IOP.TaggedComponent;

import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt ;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * @author Harold Carr
 */
public class IORInterceptorImpl
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer,
        org.omg.PortableInterceptor.IORInterceptor
{
    private ORB orb ;

    public IORInterceptorImpl()
    {
    }

    public IORInterceptorImpl( ORB orb ) 
    {
        this.orb = orb ;
    }

    public final String baseMsg = IORInterceptorImpl.class.getName();
    public String name()    { return baseMsg; }
    public void   destroy() { }

    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) { }

    public void post_init(ORBInitInfo info)
    {
        orb = ((ORBInitInfoExt)info).getORB() ;
        try {
            info.add_ior_interceptor(new IORInterceptorImpl(orb));
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ex);
            System.exit(1);
        }
    }

    //
    // IORInterceptor
    //

    public void   establish_components(IORInfo iorInfo)
    {
        try {
            IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;

            String localAddress = InetAddress.getLocalHost().getHostAddress();

            for (int i = 0; i < Common.socketTypes.length; i++) {

                TaggedCustomSocketInfo socketInfo = 
                    new TaggedCustomSocketInfo(
                        Common.socketTypes[i], 
                        localAddress,
                        iorInfoExt.getServerPort(Common.socketTypes[i]));

                if (orb.transportDebugFlag) {
                    dprint(".establish_components:" 
                           + " " + Common.socketTypes[i]
                           + " " + localAddress
                           + " " + iorInfoExt.getServerPort(Common.socketTypes[i]));
                }

                Any any = orb.create_any();
                TaggedCustomSocketInfoHelper.insert(any, socketInfo);
                byte[] data = Common.getCodec(orb).encode(any);
                TaggedComponent tc =
                    new TaggedComponent(TAG_TAGGED_CUSTOM_SOCKET_INFO.value,
                                        data);
                iorInfo.add_ior_component(tc);
            }
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            System.exit(1);
        }
    }

    public void components_established( IORInfo iorInfo )
    {
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) 
    {
    }

    private void dprint(String msg)
    {
        ORBUtility.dprint("IORInterceptor", msg);
    }
}

// End of file.
