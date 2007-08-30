/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2000-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
package com.sun.corba.se.spi.monitoring;

import com.sun.corba.se.impl.monitoring.MonitoredObjectFactoryImpl;
import com.sun.corba.se.impl.monitoring.MonitoredAttributeInfoFactoryImpl;
import com.sun.corba.se.impl.monitoring.MonitoringManagerFactoryImpl;

/**
 * <p>
 *
 * @author Hemanth Puttaswamy
 * </p>
 * <p>
 *  This is used for getting the default factories for 
 *  MonitoredObject, MonitoredAttributeInfo and MonitoringManager. We do not
 *  expect users to use the MonitoredAttributeInfo factory most of the time
 *  because the Info is automatically built by StringMonitoredAttributeBase
 *  and LongMonitoredAttributeBase. 
 *  </p>
 */   
public class MonitoringFactories {
    ///////////////////////////////////////
    // attributes
    private static final MonitoredObjectFactoryImpl monitoredObjectFactory =
        new MonitoredObjectFactoryImpl( );
    private static final MonitoredAttributeInfoFactoryImpl 
        monitoredAttributeInfoFactory = 
        new MonitoredAttributeInfoFactoryImpl( );
    private static final MonitoringManagerFactoryImpl monitoringManagerFactory = 
        new MonitoringManagerFactoryImpl( );


    ///////////////////////////////////////
    // operations

/**
 * <p>
 * Gets the MonitoredObjectFactory 
 * </p>
 * <p>
 *
 * @return a MonitoredObjectFactory 
 * </p>
 */
    public static MonitoredObjectFactory getMonitoredObjectFactory( ) {
        return monitoredObjectFactory;
    }

/**
 * <p>
 * Gets the MonitoredAttributeInfoFactory. The user is not expected to use this
 * Factory, since the MonitoredAttributeInfo is internally created by 
 * StringMonitoredAttributeBase, LongMonitoredAttributeBase and 
 * StatisticMonitoredAttribute. If User wants to create a MonitoredAttribute
 * of some other special type like a DoubleMonitoredAttribute, they can
 * build a DoubleMonitoredAttributeBase like LongMonitoredAttributeBase
 * and build a MonitoredAttributeInfo required by MonitoredAttributeBase 
 * internally by using this Factory. 
 * </p>
 * <p>
 *
 * @return a MonitoredAttributeInfoFactory
 * </p>
 */
    public static MonitoredAttributeInfoFactory 
        getMonitoredAttributeInfoFactory( ) 
    {
        return monitoredAttributeInfoFactory;
    }

/**
 * <p>
 * Gets the MonitoredManagerFactory. The user is not expected to use this
 * Factory, since the ORB will be automatically initialized with the 
 * MonitoringManager. 
 *
 * User can get hold of MonitoringManager associated with ORB by calling
 * orb.getMonitoringManager( )
 * </p>
 * <p>
 *
 * @return a MonitoredManagerFactory
 * </p>
 */
    public static MonitoringManagerFactory getMonitoringManagerFactory( ) {
        return monitoringManagerFactory;
    }
}
