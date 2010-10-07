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
// Created       : 2005 Jul 29 (Fri) 07:52:10 by Harold Carr.
// Last Modified : 2005 Aug 29 (Mon) 14:09:31 by Harold Carr.
//

package com.sun.corba.se.impl.folb;

import java.util.List;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import com.sun.corba.se.spi.folb.ClusterInstanceInfo;
import com.sun.corba.se.spi.folb.ClusterInstanceInfo;
import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.transport.SocketInfo;

/**
 * An implementation of this interface <code>org.omg.CORBA.LocalObject</code>.
 * An instance of this interface is plugged into the ORB via
 * <code>ORB.register_initial_reference(ORBConstants.CSIv2SSLTaggedComponentHandler, instance)</code>.
 *
 * @author Harold Carr
 */
public interface CSIv2SSLTaggedComponentHandler
{
    /**
     * @param iorInfo - from IORInterceptor.establish_components.
     * @param clusterInstanceInfo On the server-side, the FOLB system will pass all ClusterInstanceInfo
     * to the CSIv2/SSL system.  
     * @return null or org.omg.IOP.TaggedComponent.
     * The CSIv2SSL system returns <code>null</code> if no security
     * information is to be added to IORs.  Otherwise it returns the
     * CSIv2SSL <code>org.omg.IOP.TaggedComponent</code> that will be
     * added to IORs.
     */
    public TaggedComponent insert(IORInfo iorInfo, 
				  List<ClusterInstanceInfo> clusterInstanceInfo);

    /** Extract is called on each invocation of the IOR, so that the security code can
     * run properly.
     * If the given IOR contains CSIv2SSL host/port
     * info that should be used for this invocation then
     * extract should return a List of SocketInfo. 
     * Otherwise it should return null.
     * @param ior The target ior of the current invocation.
     * @return List of all SocketInfos found in the IOR.
     */
    public List<SocketInfo> extract(IOR ior); 
}

// End of file.


