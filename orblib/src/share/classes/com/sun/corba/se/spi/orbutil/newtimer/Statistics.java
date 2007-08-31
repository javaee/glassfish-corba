/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.newtimer ;

import com.sun.corba.se.spi.orbutil.jmx.ManagedData ;
import com.sun.corba.se.spi.orbutil.jmx.ManagedAttribute ;

/** A simple read-only holder for accumulated statistics.
 */
@ManagedData( description="Statistics recorded for a series of time intervals" )
public class Statistics {
    private final long count ;
    private final double min ;
    private final double max ;
    private final double average ;
    private final double standardDeviation ;

    public Statistics( long count, double min, double max,
	double average, double standardDeviation ) {

	this.count = count ;
	this.min = min ;
	this.max = max ;
	this.average = average ;
	this.standardDeviation = standardDeviation ;
    }

    /** Return the number of data points recorded.
     */
    @ManagedAttribute( description="Total number of intervals recorded" ) 
    public long count() { return count ; }

    /** Return the minimum value of call data points records.
     */
    @ManagedAttribute( description="Minimum interval duration recorded" ) 
    public double min() { return min ; }

    /** Return the maximum value of call data points records.
     */
    @ManagedAttribute( description="Maximum interval duration recorded" ) 
    public double max() { return max ; }

    /** Return the current average of the data, or -1 if there is no
     * data.
     */
    @ManagedAttribute( description="Average interval duration recorded" ) 
    public double average() { return average ; }

    /** Return the standard deviation of the data, or -1 if there is
     * no data.
     */
    @ManagedAttribute( description="Standard deviation of all durations recorded" ) 
    public double standardDeviation() { return standardDeviation ; }

    public boolean equals( Object obj ) {
	if (obj == this)
	    return true ;

	if (!(obj instanceof Statistics))
	    return false ;

	Statistics other = Statistics.class.cast( obj ) ;
	return (count==other.count()) &&
	    (min==other.min()) &&
	    (max==other.max()) &&
	    (average==other.average()) &&
	    (standardDeviation==other.standardDeviation()) ;
    }

    public int hashCode() {
	double sum = min+max+average+standardDeviation ;
	sum += count ;
	Double dsum = Double.valueOf( sum ) ;
	return dsum.hashCode() ;
    }
}
