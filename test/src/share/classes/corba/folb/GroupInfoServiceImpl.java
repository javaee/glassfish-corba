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
// Created       : 2005 Jun 13 (Mon) 11:04:09 by Harold Carr.
// Last Modified : 2005 Sep 26 (Mon) 22:39:12 by Harold Carr.
//

package corba.folb;

import java.net.InetAddress ;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.impl.folb.GroupInfoServiceBase;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import com.sun.corba.ee.spi.folb.SocketInfo;

import com.sun.corba.ee.impl.misc.ORBUtility;
import java.util.ArrayList;

/**
 * @author Harold Carr
 */
public class GroupInfoServiceImpl
    extends org.omg.CORBA.LocalObject
    implements GroupInfoService
{
    private List<String> currentInstances;
    private GIS gis;
    private boolean debug = true; // REVISIT - get from ORB

    private class GIS extends GroupInfoServiceBase
    {
        public List<ClusterInstanceInfo> internalClusterInstanceInfo(
            List<String> endpoints ) { 
            throw new RuntimeException( "Should not be called" ) ;
        }

        @Override
        public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName, List<String> endpoints )
        {
            return getClusterInstanceInfo( adapterName ) ;
        }

        @Override
        public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName)
        {
            String adapter_name = ORBUtility.formatStringArray(adapterName);

            try {
                if (debug) dprint(".getMemberAddresses->: " + adapter_name);
                if (debug) dprint(".getMemberAddresses: " + adapter_name 
                       + ": current members: " + currentInstances);

                List<ClusterInstanceInfo> info =
                    new LinkedList<ClusterInstanceInfo>();
                ClusterInstanceInfo instanceInfo;


                String hostName = "";
                try {
                    hostName = InetAddress.getLocalHost().getHostAddress();
                } catch (UnknownHostException e) {
                    dprint(".getMemberAddresses: " + adapter_name 
                           + ": exception: " + e);
                    e.printStackTrace(System.out);
                    System.exit(1);
                }

                for (int i=0; i<corba.folb_8_1.Common.socketTypes.length; ++i){

                    if (! currentInstances.contains(corba.folb_8_1.Common.socketTypes[i])) {
                        if (debug) dprint(".getMemberAddresses: " + adapter_name 
                               + ": NOT in current members: " + 
                               corba.folb_8_1.Common.socketTypes[i]);
                        continue;
                    }

                    if (debug) dprint(".getMemberAddresses: " + adapter_name 
                           + ":IN current members: " + 
                           corba.folb_8_1.Common.socketTypes[i]);

                    //
                    // A BAD Address.
                    //

                    SocketInfo siBad =
                        new SocketInfo("t" + i, "bad" + i, i + 1);


                    //
                    // A Good Address.
                    //

                    SocketInfo si = 
                        new SocketInfo(corba.folb_8_1.Common.socketTypes[i],
                                       hostName,
                                       corba.folb_8_1.Common.socketPorts[i]);

                    //
                    // One fake instance.
                    //
                    List<SocketInfo> socketInfos = new ArrayList<SocketInfo>() ;
                    socketInfos.add( siBad ) ;
                    socketInfos.add( si ) ;
                    instanceInfo = 
                        new ClusterInstanceInfo("instance-" + i, i + 1,
                                                socketInfos);
                    info.add(instanceInfo);

                    //
                    // REVISIT: this is not used in testing - remove
                    //
                    // Only add one good address in test ReferenceFactory.
                    //

                    if (isNoLabelName(adapterName)) {
                        if (debug) dprint(".getMemberAddresses: " + adapter_name
                               + ": no label ReferenceFactory - only added one good address");
                        break;
                    }
                }

                return info;

            } catch (RuntimeException e) {
                dprint(".getMemberAddresses: " + adapter_name 
                       + ": exception: " + e);
                e.printStackTrace(System.out);
                System.exit(1);
                throw e;
            } finally {
                if (debug) dprint(".getMemberAddresses<-: " + adapter_name);
            }
        }

        @Override
        public boolean shouldAddAddressesToNonReferenceFactory(
            String[] adapterName)
        {
            return Common.POA_WITH_ADDRESSES_WITH_LABEL.equals(
                adapterName[adapterName.length-1]);
        }

        @Override
        public boolean shouldAddMembershipLabel (String[] adapterName)
        {
            return ! isNoLabelName(adapterName);
        }

        ////////////////////////////////////////////////////
        //
        // Implementation
        //

        private boolean isNoLabelName(String[] adapterName)
        {
            return Common.RFM_WITH_ADDRESSES_WITHOUT_LABEL.equals(
                adapterName[adapterName.length-1]);
        }
    }

    public GroupInfoServiceImpl()
    {
        gis = new GIS();
        currentInstances = new LinkedList<String>();
        for (int i = 0; i < corba.folb_8_1.Common.socketTypes.length; ++i){
            currentInstances.add(corba.folb_8_1.Common.socketTypes[i]);
        }
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoService
    //

    public boolean addObserver(GroupInfoServiceObserver x) 
    {
        return gis.addObserver(x);
    }

    public void notifyObservers()
    {
        gis.notifyObservers();
    }

    @Override
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName, List<String> endpoints )
    {
        return gis.getClusterInstanceInfo(adapterName,endpoints);
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName)
    {
        return gis.getClusterInstanceInfo(adapterName);
    }

    public boolean shouldAddAddressesToNonReferenceFactory(
        String[] adapterName)
    {
        return gis.shouldAddAddressesToNonReferenceFactory(adapterName);
    }

    public boolean shouldAddMembershipLabel (String[] adapterName)
    {
        return gis.shouldAddMembershipLabel(adapterName);
    }

    ////////////////////////////////////////////////////
    //
    // Implementation used by GroupInfoServiceTestServant
    //

    public boolean add(String x)
    {
        if (debug) dprint(".add->: " + x);
        if (debug) dprint(".add: current members before: " + currentInstances);
        boolean result = currentInstances.add(x);
        if (debug) dprint(".add: current members after : " + currentInstances);
        notifyObservers();
        if (debug) dprint(".add<-: " + x + " " + result);
        return result;
    }


    public boolean remove(String x)
    {
        if (debug) dprint(".remove->: " + x);
        if (debug) dprint(".remove: current members before: " + currentInstances);
        boolean result = currentInstances.remove(x);
        if (debug) dprint(".remove: current members after : " + currentInstances);
        notifyObservers();
        if (debug) dprint(".remove<-: " + x + " " + result);
        return result;
    }

    private static void dprint(String msg)
    {
        ORBUtility.dprint("GroupInfoServiceImpl", msg);
    }
}

// End of file.
