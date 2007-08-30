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

import java.util.*;

import com.sun.corba.se.spi.orbutil.newtimer.StatisticsAccumulator ;

/**
 * <p>
 * 
 * @author Hemanth Puttaswamy
 * </p>
 * <p>
 * StatisticsMonitoredAttribute is provided as a convenience to collect the
 * Statistics of any entity. The getValue() call will be delegated to the
 * StatisticsAccumulator set by the user.
 * </p>
 */
public class StatisticMonitoredAttribute extends MonitoredAttributeBase {


    // Every StatisticMonitoredAttribute will have a StatisticAccumulator. User
    // will use Statisticsaccumulator to accumulate the samples associated with
    // this Monitored Attribute 
    private StatisticsAccumulator statisticsAccumulator; 

    // Mutex is passed from the user class which is providing the sample values.
    // getValue() and clearState() is synchronized on this user provided mutex
    private Object  mutex; 


  ///////////////////////////////////////
  // operations


/**
 * <p>
 * Constructs the StaisticMonitoredAttribute, builds the required
 * MonitoredAttributeInfo with Long as the class type and is always
 * readonly attribute.
 * </p>
 * <p>
 * 
 * @param name Of this attribute
 * </p>
 * <p>
 * @return a StatisticMonitoredAttribute 
 * </p>
 * <p>
 * @param desc should provide a good description on the kind of statistics 
 * collected, a good example is "Connection Response Time Stats will Provide the
 * detailed stats based on the samples provided from every request completion
 * time"
 * </p>
 * <p>
 * @param s is the StatisticsAcumulator that user will use to accumulate the 
 * samples and this Attribute Object will get the computed statistics values
 * from.
 * </p>
 * <p>
 * @param mutex using which clearState() and getValue() calls need to be locked.
 * </p>
 */
    public  StatisticMonitoredAttribute(String name, String desc, 
        StatisticsAccumulator s, Object mutex) 
    {        
        super( name );
        MonitoredAttributeInfoFactory f = 
            MonitoringFactories.getMonitoredAttributeInfoFactory();
        MonitoredAttributeInfo maInfo = f.createMonitoredAttributeInfo(
                desc, String.class, false, true );

        this.setMonitoredAttributeInfo( maInfo );
        this.statisticsAccumulator = s;
        this.mutex = mutex;
    } // end StatisticMonitoredAttribute        



/**
 *  Gets the value from the StatisticsAccumulator, the value will be a formatted
 *  String with the computed statistics based on the samples accumulated in the
 *  Statistics Accumulator.
 */ 
    public Object getValue( ) {
        synchronized( mutex ) {
            return statisticsAccumulator.getValue( );
        }
    }

/**
 *  Clears the state on Statistics Accumulator, After this call all samples are
 *  treated fresh and the old sample computations are disregarded.
 */
    public void clearState( ) {
        synchronized( mutex ) {
            statisticsAccumulator.clearState( );
        }
    }

/**
 *  Gets the statistics accumulator associated with StatisticMonitoredAttribute.
 *  Usually, the user don't need to use this method as they can keep the handle
 *  to Accumulator to collect the samples.
 */
    public StatisticsAccumulator getStatisticsAccumulator( ) {
        return statisticsAccumulator;
    }
} // end StatisticMonitoredAttribute



