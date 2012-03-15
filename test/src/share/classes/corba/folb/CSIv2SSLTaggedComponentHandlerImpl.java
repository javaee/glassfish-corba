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
// Created       : 2005 Jul 29 (Fri) 08:23:33 by Harold Carr.
// Last Modified : 2005 Sep 23 (Fri) 15:08:31 by Harold Carr.
//

package corba.folb;

import java.util.List;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.impl.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * @author Harold Carr
 */
public class CSIv2SSLTaggedComponentHandlerImpl
    extends org.omg.CORBA.LocalObject
    implements CSIv2SSLTaggedComponentHandler,
               ORBConfigurator
{
    private boolean debug = true;
    private ORB orb;

    ////////////////////////////////////////////////////
    //
    // CSIv2SSLTaggedComponentHandler
    //

    public TaggedComponent insert(IORInfo iorInfo, 
                                  List<ClusterInstanceInfo> clusterInstanceInfo)
    {
        if (debug) { dprint(".insert: " + iorInfo); }
        return null;
    }

    public List<SocketInfo> extract(IOR ior)
    {
        if (debug) { dprint(".extract"); }
        return null;
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    public void configure(DataCollector collector, ORB orb) 
    {
        if (debug) { dprint(".configure->:"); }

        this.orb = orb;
        try {
            orb.register_initial_reference(
                ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER,
                this);
        } catch (InvalidName e) {
            dprint(".configure: !!!!! FAILURE");
            e.printStackTrace(System.out);
            System.exit(1);
        }

        if (debug) { dprint(".configure<-:"); }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private static void dprint(String msg)
    {
        ORBUtility.dprint("CSIv2SSLTaggedComponentHandlerImpl", msg);
    }

}

// End of file.


