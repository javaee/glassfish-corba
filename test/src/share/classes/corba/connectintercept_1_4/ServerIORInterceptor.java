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
// Created       : 2000 Oct 13 (Fri) 09:48:05 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:10:21 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;
import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;

public class ServerIORInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        IORInterceptor
{
    public final String baseMsg = ServerIORInterceptor.class.getName();
    public final String estMsg  = baseMsg + ".establish_components";

    public String name()    { return baseMsg; }
    public void   destroy() { }
    public void   establish_components(IORInfo iorInfo)
    {
        IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
        String componentData = Common.createComponentData(estMsg, iorInfoExt);
        TaggedComponent taggedComponent =
            new TaggedComponent(Common.ListenPortsComponentID,
                                componentData.getBytes());
        iorInfo.add_ior_component(taggedComponent);
        System.out.println(estMsg + ": add_ior_component completed");
    }

    public void components_established( IORInfo iorInfo )
    {
        // NO-OP
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
        // NO-OP
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates,
        short state ) 
    {
        // NO-OP
    }
}

// End of file.
