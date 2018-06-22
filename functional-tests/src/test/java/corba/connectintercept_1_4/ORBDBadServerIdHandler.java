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
// Created       : 1999 by Harold Carr.
// Last Modified : 2004 Apr 29 (Thu) 16:28:35 by Harold Carr.
//

package corba.connectintercept_1_4;

import com.sun.corba.ee.spi.activation.IIOP_CLEAR_TEXT;
import com.sun.corba.ee.spi.activation.EndPointInfo;
import com.sun.corba.ee.spi.activation.Locator;
import com.sun.corba.ee.spi.activation.LocatorHelper;
import com.sun.corba.ee.spi.activation.LocatorPackage.ServerLocationPerORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory ;

import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion; 

import com.sun.corba.ee.spi.protocol.ForwardException; 

import com.sun.corba.ee.spi.orb.ORB; 

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

import com.sun.corba.ee.impl.ior.IORImpl;
import com.sun.corba.ee.impl.ior.POAObjectKeyTemplate ;

import com.sun.corba.ee.impl.oa.poa.BadServerIdHandler;

import com.sun.corba.ee.impl.util.Utility;

import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.OBJECT_NOT_EXIST;

public class ORBDBadServerIdHandler
    implements
        BadServerIdHandler
{
    public static final String baseMsg =
        ORBDBadServerIdHandler.class.getName();

    private ORB orb;

    public ORBDBadServerIdHandler(org.omg.CORBA.ORB orb)
    {
        this.orb = (com.sun.corba.ee.spi.orb.ORB) orb;
    }

    public void handle(ObjectKey okey)
    {
        Locator locator = null;
        try {
            locator = LocatorHelper.narrow(orb.resolve_initial_references(ORBConstants.SERVER_LOCATOR_NAME));
        } catch (InvalidName ex) {
            // Should never happen.
            System.out.println("ORBDBadServerIdHandler.handle: " + ex);
            System.exit(-1);
        }

        IOR newIOR = null;
        ServerLocationPerORB location;

        POAObjectKeyTemplate poktemp = (POAObjectKeyTemplate)
            (okey.getTemplate());
        int serverId = poktemp.getServerId() ;
        String orbId = poktemp.getORBId() ;

        try {
            location  = locator.locateServerForORB(serverId, orbId);

            int clearPort = 
                locator.getServerPortForType(location, IIOP_CLEAR_TEXT.value);

            int myType1Port
                = locator.getServerPortForType(location, Common.MyType1);
            int myType2Port
                = locator.getServerPortForType(location, Common.MyType2);
            int myType3Port
                = locator.getServerPortForType(location, Common.MyType3);

            String componentData =
                Common.createComponentData(baseMsg + ".handle: ",
                                           myType1Port,
                                           myType2Port,
                                           myType3Port);

            /*
              1. Use ObjectKeyFactory.create( byte[]) to convert byte[]
              object key to ObjectKey (if it's not already ObjectKey).
              Note that the arg type will change to a stream in my next
              putback.
              2. Use host and port to construct an IIOPAddress.
              3. Use address from 2, object key template from 1, and GIOP
              version info to construct IIOPProfileTemplate.
              4. Add tagged components to IIOPProfileTemplate.
              5. Use IIOPProfileTemplate from 4 and ObjectId from 1
              to construct IIOPProfile.
              6. Construct IOR from ORB and repid.
              7. Add IIOPProfile to IOR.
              8. Make IOR immutable.
            */

            IIOPProfileTemplate sipt = 
                IIOPFactories.makeIIOPProfileTemplate(
                    (com.sun.corba.ee.spi.orb.ORB)orb,
                    GIOPVersion.V1_2,
                    IIOPFactories.makeIIOPAddress( location.hostname, clearPort));
            sipt.add(new ORBDListenPortsComponent(componentData));
            IORTemplate iortemp = IORFactories.makeIORTemplate( poktemp ) ;
            iortemp.add( sipt ) ;
            newIOR = iortemp.makeIOR( (com.sun.corba.ee.spi.orb.ORB)orb, 
                "IDL:org/omg/CORBA/Object:1.0", okey.getId() );

            /*
            // REVISIT - add component data.

            newIOR = new IOR((com.sun.corba.ee.spi.orb.ORB)orb,
                             "IDL:org/omg/CORBA/Object:1.0",
                             location.hostname,
                             myType2Port, // REVISIT - clearPort
                             objectKey);
            */
        } catch (Exception e) {
            // For this example, all exceptions map to:
            throw new OBJECT_NOT_EXIST();
        }

        throw new ForwardException( (com.sun.corba.ee.spi.orb.ORB)orb, newIOR);
    }
}

// End of file.
