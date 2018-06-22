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
// Created       : 2002 Jul 19 (Fri) 13:43:29 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:06:35 by Harold Carr.
//

package corba.iorintsockfact;

import java.net.InetAddress;
import java.util.Iterator;

import org.omg.CORBA.Any;
import org.omg.IOP.Codec;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.IORInfo;
// This one is only necessary when running in current development workspace.
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

import com.sun.corba.ee.spi.ior.TaggedProfileTemplate;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.legacy.interceptor.IORInfoExt;
import com.sun.corba.ee.spi.oa.ObjectAdapter;
import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.iiop.AlternateIIOPAddressComponent;

/**
 * @author Harold Carr
 */
public class IORInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.IORInterceptor
{
    private ORB orb ;

    public IORInterceptor( ORB orb ) 
    {
        this.orb = orb ;
    }

    public final String baseMsg = IORInterceptor.class.getName();
    public String name()    { return baseMsg; }
    public void   destroy() { }

    public void   establish_components(IORInfo iorInfo)
    {
        try {
            IORInfoExt iorInfoExt = (IORInfoExt) iorInfo;
            ObjectAdapter adapter = iorInfoExt.getObjectAdapter();

            String localAddress = InetAddress.getLocalHost().getHostAddress();
            int port =
                iorInfoExt.getServerPort(ORBSocketFactory.IIOP_CLEAR_TEXT);

            InetAddress[] allAddresses =
                InetAddress.getAllByName(localAddress);

            for (int i = 0; i < allAddresses.length; i++) {
                String address = allAddresses[0].getHostAddress();

                IIOPAddress iiopAddress = 
                    IIOPFactories.makeIIOPAddress(address, port);
                AlternateIIOPAddressComponent iiopAddressComponent =
                    IIOPFactories.makeAlternateIIOPAddressComponent(iiopAddress);
                Iterator iterator = adapter.getIORTemplate().iteratorById(
                    org.omg.IOP.TAG_INTERNET_IOP.value);
                
                while (iterator.hasNext()) {
                    TaggedProfileTemplate taggedProfileTemplate =
                        (TaggedProfileTemplate) iterator.next();
                    taggedProfileTemplate.add(iiopAddressComponent);
                }
            }
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            System.exit(-1);
        }
    }

    // Thses are only necessary when running in current development workspace.
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
}

// End of file.
