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
// Last Modified : 2005 Aug 09 (Tue) 16:31:38 by Harold Carr.
//

package com.sun.corba.ee.impl.folb;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;

import com.sun.corba.ee.spi.trace.Folb ;

import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.folb.GroupInfoServiceObserver;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
@Folb
public abstract class GroupInfoServiceBase
    extends org.omg.CORBA.LocalObject
    implements GroupInfoService
{
    private List<GroupInfoServiceObserver> observers =
        new LinkedList<GroupInfoServiceObserver>();

    @Folb
    public boolean addObserver(GroupInfoServiceObserver x) {
        return observers.add(x);
    }

    @InfoMethod
    private void observerInfo( GroupInfoServiceObserver obs ) { }

    @Folb
    public void notifyObservers() {
        for (GroupInfoServiceObserver observer : observers) {
            observerInfo( observer ) ;
            observer.membershipChange();
        }
    }

    @Folb
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName) {

        // Make a copy of the internal data
        return new ArrayList( internalClusterInstanceInfo() ) ;
    }

    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName, List<String> endpoints ) {

        // Make a copy of the internal data
        return new ArrayList( internalClusterInstanceInfo( endpoints ) ) ;
    }

    @Folb
    public boolean shouldAddAddressesToNonReferenceFactory(
        String[] adapterName) {
        return false ;
    }

    @Folb
    public boolean shouldAddMembershipLabel (String[] adapterName) {
        return true ;
    }

    public List<ClusterInstanceInfo> internalClusterInstanceInfo() {
        final List<String> endpoints = new ArrayList<String>() ;
        return internalClusterInstanceInfo( endpoints ) ;
    }

    public abstract List<ClusterInstanceInfo> internalClusterInstanceInfo( List<String> endpoints ) ;
}

// End of file.
