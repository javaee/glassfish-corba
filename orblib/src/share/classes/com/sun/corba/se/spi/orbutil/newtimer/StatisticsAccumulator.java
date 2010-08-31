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
package com.sun.corba.se.spi.orbutil.newtimer ;

/**
 * <p>
 * 
 * @author Hemanth Puttaswamy
 * </p>
 * <p>
 * StatisticsAccumulator accumulates the samples provided by the user and
 * computes the value of minimum, maximum, sum and sample square sum. When
 * the StatisticMonitoredAttribute calls getValue(), it will compute all
 * the statistics for the collected samples (Which are Minimum, Maximum,
 * Average, StandardDeviation) and provides a nice printable record as a
 * String.
 *
 * Users can easily extend this class and provide the implementation of 
 * toString() method to format the stats as desired. By default all the stats
 * are printed in a single line.
 * </p>
 */
public class StatisticsAccumulator {
    private double max ;
    private double min ;

    // These are exposed to implement augment().
    double sampleSum; 
    double sampleSquareSum; 

    private long sampleCount; 
    private String unit;
    private Statistics stats ;

    public String unit() { return unit ; }

    public long count() { return sampleCount ; } 

    public double min() { return min ; }

    public double max() { return max ; }

    public double average( ) { return sampleSum/sampleCount ; }

    public double standardDeviation( ) {
        double sampleSumSquare = sampleSum * sampleSum;
        return Math.sqrt( 
            (sampleSquareSum-((sampleSumSquare)/sampleCount))/(sampleCount-1));
    }

    public void sample(double value) {        
        sampleCount++;
        if (value < min) 
	    min = value;
        if (value > max) 
	    max = value;
        sampleSum += value;
        sampleSquareSum += (value * value);
    }

    public synchronized Statistics getStats() {
	if ((stats == null) || (stats.count() != sampleCount)) {
	    stats = new Statistics( sampleCount, min, max, average(), standardDeviation() ) ; 
	}

	return stats ;
    }

    public void augment( StatisticsAccumulator acc ) {
	if (!unit.equals( acc.unit ))
	    throw new IllegalArgumentException( "Units must match: this = "
		+ unit + " other = " + acc.unit ) ;

	sampleCount += acc.count() ;
	if (acc.min < min)
	    min = acc.min ;
	if (acc.max() > max) 
	    max = acc.max ;
	sampleSum += acc.sampleSum ;
	sampleSquareSum += acc.sampleSquareSum ;
    }

    /**
     *  Computes the Standard Statistic Results based on the samples collected
     *  so far and provides the complete value as a formatted String
     */
    public String getValue( ) {
        return toString();
    }

    /**
     *  Users can extend StatisticsAccumulator to provide the complete
     *  Stats in the format they prefer, if the default format doesn't suffice.
     */
    public String toString( ) {
        return "Minimum Value = " + min + " " + unit + " " +
            "Maximum Value = " + max + " " + unit + " " +
            "Average Value = " + average() + " " +  unit + " " +
            "Standard Deviation = " + standardDeviation() + " " + unit + 
            " " + "Samples Collected = " + sampleCount;
    }


/** Construct the Statistics Accumulator by providing the unit as a String.
 * The examples of units are &quot;Hours&quot;, &quot;Minutes&quot;, 
 * &quot;Seconds&quot;, &quot;MilliSeconds&quot;, &quot;Micro Seconds&quot; 
 * etc.,
 * <p>
 * @param unit a String representing the units for the samples collected
 */
    public StatisticsAccumulator( String unit ) {
        this.unit = unit;
	clearState() ;
    }


    /**
     *  Clears the samples and starts fresh on new samples.
     */
    public void clearState( ) {
        min = Double.MAX_VALUE;
        max = Double.MIN_VALUE;
        sampleCount = 0;
        sampleSum = 0;
        sampleSquareSum = 0;

	synchronized (this) {
	    stats = null ;
	}
    }

    /**
     *  This is an internal API to test StatisticsAccumulator...
     */
    public void unitTestValidate( String expectedUnit, double expectedMin, 
        double expectedMax, long expectedSampleCount, double expectedAverage, 
        double expectedStandardDeviation ) 
    {
        if( !expectedUnit.equals( unit ) ){
            throw new RuntimeException( 
                "Unit is not same as expected Unit" +
                "\nUnit = " + unit + "ExpectedUnit = " + expectedUnit );
        } 
        if( min != expectedMin ) {
            throw new RuntimeException( 
                "Minimum value is not same as expected minimum value" +
                "\nMin Value = " + min + "Expected Min Value = " + expectedMin);
        } 
        if( max != expectedMax ) {
            throw new RuntimeException( 
                "Maximum value is not same as expected maximum value" + 
                "\nMax Value = " + max + "Expected Max Value = " + expectedMax);
        } 
        if( sampleCount != expectedSampleCount ) {
            throw new RuntimeException( 
                "Sample count is not same as expected Sample Count" + 
                "\nSampleCount = " + sampleCount + "Expected Sample Count = " + 
                expectedSampleCount);
        } 
        if( average() != expectedAverage ) {
            throw new RuntimeException( 
                "Average is not same as expected Average" + 
                "\nAverage = " + average() + "Expected Average = " + 
                expectedAverage);
        } 
        // We are computing Standard Deviation from two different methods
        // for comparison. So, the values will not be the exact same to the last
        // few digits. So, we are taking the difference and making sure that
        // the difference is not greater than 1.
        double difference = Math.abs(
            standardDeviation() - expectedStandardDeviation);
        if( difference > 1 ) {
            throw new RuntimeException( 
                "Standard Deviation is not same as expected Std Deviation" + 
                "\nStandard Dev = " + standardDeviation() + 
                "Expected Standard Dev = " + expectedStandardDeviation);
        } 
    }
} // end StatisticsAccumulator

