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
package corba.monitoring;

import java.io.PrintStream ;

import java.util.Collection ;
import java.util.Iterator ;
import java.util.Properties ;

import com.sun.corba.se.spi.monitoring.*;

import com.sun.corba.se.spi.orbutil.newtimer.StatisticsAccumulator ;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.RequestDispatcherRegistry;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import com.sun.corba.se.spi.orbutil.test.JUnitReportHelper ;

public class Client 
{
    private JUnitReportHelper helper = new JUnitReportHelper( Client.class.getName() ) ;
    private PrintStream out ;
    private PrintStream err ;
    private ORB orb ;

    private static final String CHILD_LEVEL1_NAME = 
        "ChildLevel1MonitoredObject";
    private static final String CHILD_LEVEL1_DESCRIPTION = 
        "Monitored Object For Child Level 1 Set For Testing";

    private static final String CHILD_LEVEL2_NAME1 = 
        "ChildLevel2MonitoredObject1";
    private static final String CHILD_LEVEL2_DESCRIPTION1 = 
        "Monitored Object For Child Level 2 Set For Testing 1";

    private static final String CHILD_LEVEL2_NAME2 = 
        "ChildLevel2MonitoredObject2";
    private static final String CHILD_LEVEL2_DESCRIPTION2 = 
        "Monitored Object For Child Level 2 Set For Testing 2";

    public static void main(String args[])
    {
	System.out.println( "Starting Monitoring Manager test" ) ;
        try{
	    Properties props = new Properties( System.getProperties() ) ;
	    props.put( "org.omg.CORBA.ORBClass", 
		"com.sun.corba.se.impl.orb.ORBImpl" ) ;
	    new Client( props, args, System.out, System.err ) ;
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public Client( Properties props, String args[], PrintStream out,
	PrintStream err )
    {
	this.orb = (ORB)ORB.init( args, props ) ;
	this.out = System.out ;
	this.err = System.err ;

        try {
            runTests() ;
        } finally {
            helper.done() ;
        }
    }

// *************************************************
// ***************   Utilities   *******************
// *************************************************

    private void error( String msg )
    {
	RuntimeException exc = new RuntimeException( msg ) ;
        helper.fail( exc ) ;
	throw exc ;
    }
    
    private void info( String msg )
    {
	out.println( msg ) ;
    }

    /**
     * Compare MonitoredAttribute's Meta Information with the expected values
     * and raise an exception if there are differences.
     */
    private void validateMonitoredAttribute( MonitoredAttribute ma, 
        Class expectedType, String expectedDescription, Object expectedValue )
    {
        MonitoredAttributeInfo maInfo = ma.getAttributeInfo( );
        if( ( maInfo.type() != expectedType )
          ||( !maInfo.getDescription().equals( expectedDescription ) ) )
        {
            error( "Monitored Attribute Info Does Not Match For: " +
                expectedDescription );
        }
        // If it's a statistic then we just return, special util method will
        // be used to test statistic
        if( maInfo.isStatistic( ) ) return;
        if( !ma.getValue().equals( expectedValue ) ) {
            error( "Monitored Attribute Value Does Not Match For: " + 
                expectedDescription );
        }
    }


    /**
     *  For this test we are setting the values of MonitoredAttributes as 
     *  null. This utility method traverses through the Attributes Iterator
     *  and checks to make sure that every attribute value is null. If not,
     *  it will throw a runtime exception.
     */
    private void validateForClearedState( Iterator attributesIterator ) {
        while( attributesIterator.hasNext() ) {
            MonitoredAttribute attr = 
                (MonitoredAttribute) attributesIterator.next();
            if( !attr.getAttributeInfo().isStatistic() ) {
                if( attr.getValue( ) != null ) {
                    error("Clear State Did Not Work Correctly..." );
                }
            }
        }
    }


    private double computeAverage( double[] samples ) {
        double sum = 0;
        int i; 
        for( i = 0; i < samples.length; i++ ) sum += samples[i];
        return (sum/samples.length); 
    }

    /**
     * Computes Standard Deviation from the standard Method. i.e., using the
     * formula SqRt( (Sample-Average)^2) / (N- 1) )
     */
    private double computeStandardDeviation( double[] samples ) {
        double average = 0;
        int i; 
        for( i = 0; i < samples.length; i++ ) average += samples[i];
        average = (average/samples.length); 
        double sampleMinusAverageSum = 0;
        for( i = 0; i < samples.length; i++ ) { 
            double t = samples[i] - average;
            sampleMinusAverageSum += (t*t);
        } 
        return Math.sqrt( sampleMinusAverageSum / (samples.length - 1));
    }

    private boolean equal( byte[] arr1, byte[] arr2 ) 
    {
	if ((arr1 == null) || (arr2 == null))	
	    return arr1==arr2 ;
	
	int len = arr1.length ;
	if (len != arr2.length)
	    return false ;

	for (int ctr = 0; ctr<len; ctr++ )
	    if (arr1[ctr] != arr2[ctr])
		return false ;

	return true ;
    }
    
    private boolean equal( Object[] arr1, Object[] arr2 ) 
    {
	if ((arr1 == null) || (arr2 == null))	
	    return arr1==arr2 ;
	
	int len = arr1.length ;
	if (len != arr2.length)
	    return false ;

	for (int ctr = 0; ctr<len; ctr++ )
	    if (!arr1[ctr].equals( arr2[ctr] ))
		return false ;

	return true ;
    }
    

// *************************************************
// ***************   TESTS   ***********************
// *************************************************

    private void runTests()
    {
        // NOTE: These tests are order sensitive
	testPopulateMonitoredObject() ;
	testPopulateMonitoredAttribute() ;
	testClearState() ;
        testStatisticsAccumulator();
	testRootName() ;
    }

    


    /**
     *  This test Populates 3 Monitored Objects in the following hierarchy
     *          ChildLevel1MonitoredObject 
     *                     |
     *  -----------------------------------------------
     *  |                                             |       
     * ChildLevel2MonitoredObject1         ChildLevel2MonitoredObject2
     *
     * and validates that the Hierarchy is set correctly.
     */
    private void testPopulateMonitoredObject( ) {
        System.out.println( "Starting TestPopulateMonitoredObject" );
        helper.start( "TestPopulateMonitoredObject" ) ;
        MonitoredObject root = 
            orb.getMonitoringManager().getRootMonitoredObject( );
        MonitoredObjectFactory f = 
            MonitoringFactories.getMonitoredObjectFactory( );

        MonitoredObject ChildLevel1MonitoredObject = 
            f.createMonitoredObject( CHILD_LEVEL1_NAME,
               CHILD_LEVEL1_DESCRIPTION ); 

        MonitoredObject ChildLevel2MonitoredObject1 = 
            f.createMonitoredObject( CHILD_LEVEL2_NAME1,
               CHILD_LEVEL2_DESCRIPTION1 ); 

        MonitoredObject ChildLevel2MonitoredObject2 = 
            f.createMonitoredObject( CHILD_LEVEL2_NAME2,
               CHILD_LEVEL2_DESCRIPTION2 ); 
 
        root.addChild( ChildLevel1MonitoredObject );

        ChildLevel1MonitoredObject.addChild( ChildLevel2MonitoredObject1 ); 
        ChildLevel1MonitoredObject.addChild( ChildLevel2MonitoredObject2 ); 

        // Now Validate It
        Collection list = root.getChildren( );
        Iterator iterator = list.iterator( );
        boolean found = false;
        MonitoredObject m = null;
        while( iterator.hasNext() ) {
            m = (MonitoredObject) iterator.next( );	
            if( m.getName().equals( CHILD_LEVEL1_NAME ) ) {
                found = true;
                break;
            }
        }
        if( !found ) {
            error( "Couldn't find " + CHILD_LEVEL1_NAME + 
                "In The Monitored Object Hierarchy" );
        }
        iterator = m.getChildren( ).iterator();
        boolean foundChild1 = false, foundChild2 = false; 
        while( iterator.hasNext() ) {
            m = (MonitoredObject) iterator.next( );	
            if( m.getName().equals( CHILD_LEVEL2_NAME1 ) ) {
                foundChild1 = true;
            }
            if( m.getName().equals( CHILD_LEVEL2_NAME2 ) ) {
                foundChild2 = true;
            }
        }
        if( !foundChild1 ) {
             error( "Couldn't find " + CHILD_LEVEL2_NAME1 +
                "In The Monitored Object Hierarchy" );
        }
        if( !foundChild2 ) {
             error( "Couldn't find " + CHILD_LEVEL2_NAME2 +
                "In The Monitored Object Hierarchy" );
        }
        System.out.println( "TestPopulateMonitoredObject: PASSED" );
        helper.pass() ;
    }

    private static final String STRINGATTRIBUTE1_LEVEL1_NAME = 
        "StringAttribute1 Level1";
    private static final String STRINGATTRIBUTE1_LEVEL1_DESCRIPTION =
        "A String Attribute Added For Testing Purpose";
    private static final String STRINGATTRIBUTE1_LEVEL1_VALUE = 
        "THIS IS THE VALUE FOR STRINGATTRIBUTE1_LEVEL1";

    private static final String STRINGATTRIBUTE1_LEVEL2_NAME = 
        "StringAttribute1 Level2";
    private static final String STRINGATTRIBUTE1_LEVEL2_DESCRIPTION =
        "A String Attribute Added For Testing Purpose";
    private static final String STRINGATTRIBUTE1_LEVEL2_VALUE = 
        "THIS IS THE VALUE FOR STRINGATTRIBUTE1_LEVEL2";

    private static final String LONGATTRIBUTE1_LEVEL2_NAME = 
        "LongAttribute1 Level2";
    private static final String LONGATTRIBUTE1_LEVEL2_DESCRIPTION =
        "A Long Attribute Added For Testing Purpose";
    private static final Long LONGATTRIBUTE1_LEVEL2_VALUE = new Long(2038179);

    private static final String LONGATTRIBUTE2_LEVEL2_NAME = 
        "LongAttribute2 Level2";
    private static final String LONGATTRIBUTE2_LEVEL2_DESCRIPTION =
        "A Long Attribute Added For Testing Purpose";
    private static final Long LONGATTRIBUTE2_LEVEL2_VALUE = new Long(62526251);

    private static final String STATISTICATTRIBUTE1_LEVEL2_NAME = 
        "StatisticAttribute1 Level2";
    private static final String STATISTICATTRIBUTE1_LEVEL2_DESCRIPTION =
        "A Statistic Attribute Added For Testing Purpose";

    private static final String UNIT_FOR_STATISTICS = "MicroSeconds";

    /**
     *  This method 
     *  adds 1 String Monitored Attribute to ChildLevel1MonitoredObject with 
     *  the name "StringAttribute Level1"
     *
     *  adds 1 String and 1 Long Monitored Attribute to Child2MonitoredObject
     *  with the names "StringAttribute1 Level2" and "LongAttribute1 Level2"
     *
     *  adds 1 Long Monitored Attribute and 1 Static Monitored Attribute
     *  with the names "LongAttribute2 Level2" and 
     *  "StatisticAttribute1 Level2" 
     *
     *  and Validates the attributes
     *
     */ 
    private void testPopulateMonitoredAttribute( ) {
        System.out.println( "Starting testPopulateMonitoredAttribute( )" );
        helper.start( "testPopulateMonitoredAttribute" ) ;
        MonitoredObject level1MonitoredObject =
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME );
                 
        System.out.println( "Populating Level1 Monitored Object..." );
        // Populate Level1 Monitored Object 
        StringMonitoredAttribute sAttribute = 
            new StringMonitoredAttribute( STRINGATTRIBUTE1_LEVEL1_NAME,
                STRINGATTRIBUTE1_LEVEL1_DESCRIPTION ); 
        sAttribute.internalSetValue( STRINGATTRIBUTE1_LEVEL1_VALUE ); 
        level1MonitoredObject.addAttribute( sAttribute );


        System.out.println( "Populating Level2 Monitored Object1..." );
        // Populate Level2 1st Monitored Object
        MonitoredObject level2MonitoredObject = 
            level1MonitoredObject.getChild( CHILD_LEVEL2_NAME1 );

        sAttribute = 
            new StringMonitoredAttribute( STRINGATTRIBUTE1_LEVEL2_NAME,
                STRINGATTRIBUTE1_LEVEL2_DESCRIPTION ); 
        sAttribute.internalSetValue( STRINGATTRIBUTE1_LEVEL2_VALUE ); 
        level2MonitoredObject.addAttribute( sAttribute );

        LongMonitoredAttribute lAttribute = 
            new LongMonitoredAttribute( LONGATTRIBUTE1_LEVEL2_NAME,
                LONGATTRIBUTE1_LEVEL2_DESCRIPTION ); 
        lAttribute.internalSetValue( LONGATTRIBUTE1_LEVEL2_VALUE ); 
        level2MonitoredObject.addAttribute( lAttribute );

        System.out.println( "Populating Level2 Monitored Object2..." );

        // Populate Level2 2nd Monitored Object
        level2MonitoredObject = 
            level1MonitoredObject.getChild( CHILD_LEVEL2_NAME2 );

        lAttribute = 
            new LongMonitoredAttribute( LONGATTRIBUTE2_LEVEL2_NAME,
                LONGATTRIBUTE2_LEVEL2_DESCRIPTION ); 
        lAttribute.internalSetValue( LONGATTRIBUTE2_LEVEL2_VALUE ); 
        level2MonitoredObject.addAttribute( lAttribute );

        StatisticsAccumulator sa = new StatisticsAccumulator( 
            UNIT_FOR_STATISTICS );
        StatisticMonitoredAttribute sm = new StatisticMonitoredAttribute( 
            STATISTICATTRIBUTE1_LEVEL2_NAME, 
            STATISTICATTRIBUTE1_LEVEL2_DESCRIPTION, sa, this );
        level2MonitoredObject.addAttribute( sm );


        validatePopulatedMonitoredAttributes( );
        System.out.println("testPopulateMonitoredAttribute(): PASSED" );
        helper.pass() ;
    }


    /**
     *  Validates all 5 attributes populated by testPopulateMonitoredAttributes
     *  method.
     */ 
    private void validatePopulatedMonitoredAttributes( ) {
        System.out.println("Starting validatePopulatedMonitoredAttributes()" );
        MonitoredObject level1MonitoredObject =
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME );

        System.out.println( "Validating Level1 Monitored Object..." );
        // Validate the attributes for Level1 Monitored Object
        validateMonitoredAttribute( 
            level1MonitoredObject.getAttribute( STRINGATTRIBUTE1_LEVEL1_NAME ),
            new String("").getClass(), STRINGATTRIBUTE1_LEVEL1_DESCRIPTION,
            STRINGATTRIBUTE1_LEVEL1_VALUE );

         
        System.out.println( "Validating Level2 Monitored Object 1..." );
        MonitoredObject level2MonitoredObject =
            level1MonitoredObject.getChild( CHILD_LEVEL2_NAME1 );

        // Validate the attributes for Level2 1st Monitored Object
        validateMonitoredAttribute( 
            level2MonitoredObject.getAttribute( STRINGATTRIBUTE1_LEVEL2_NAME ),
            new String("").getClass(), STRINGATTRIBUTE1_LEVEL2_DESCRIPTION,
            STRINGATTRIBUTE1_LEVEL2_VALUE );
        validateMonitoredAttribute( 
            level2MonitoredObject.getAttribute( LONGATTRIBUTE1_LEVEL2_NAME ),
            new Long(0).getClass(), LONGATTRIBUTE1_LEVEL2_DESCRIPTION,
            LONGATTRIBUTE1_LEVEL2_VALUE );

        System.out.println( "Validating Level2 Monitored Object 1..." );
        level2MonitoredObject =
            level1MonitoredObject.getChild( CHILD_LEVEL2_NAME2 );

        // Validate the attributes for Level2 2nd Monitored Object
        validateMonitoredAttribute( 
            level2MonitoredObject.getAttribute( LONGATTRIBUTE2_LEVEL2_NAME ),
            new Long(0).getClass(), LONGATTRIBUTE2_LEVEL2_DESCRIPTION,
            LONGATTRIBUTE2_LEVEL2_VALUE );

        validateMonitoredAttribute( 
            level2MonitoredObject.getAttribute(STATISTICATTRIBUTE1_LEVEL2_NAME),
            new String("").getClass(), STATISTICATTRIBUTE1_LEVEL2_DESCRIPTION,
            null );
    }


    /**
     * This method calls clearState() on the RootMonitoredObject and then tests
     * that the call is propogated to every element in the tree.
     */
    private void testClearState( ) {
        System.out.println("Starting testClearState()" ); 
        helper.start( "testClearState" ) ;
        orb.getMonitoringManager().clearState( );

        System.out.println("Validating Level 1 Child1 clear State..." );
        Iterator attributesIterator = 
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME ).getAttributes().iterator();

        validateForClearedState( attributesIterator );

        System.out.println("Validating Level2 Child1  clear State..." );
        attributesIterator = 
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME ).getChild( 
                CHILD_LEVEL2_NAME1 ).getAttributes( ).iterator( );

        validateForClearedState( attributesIterator );

        System.out.println("Validating Level 2 Child2 clear State..." );
        attributesIterator = 
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME ).getChild( 
                CHILD_LEVEL2_NAME2 ).getAttributes( ).iterator( );

        validateForClearedState( attributesIterator );
        System.out.println("Starting testClearState(): PASSED" ); 
        helper.pass() ;
    }

    /**
     *  This method tests StatisticsAccumulator by doing the following
     *  1. Accumulates 5 sample values and computes the statistics locally
     *     and compares it with the values computed by the accumulator. If
     *     the values are not the same then a Runtime exception is thrown from
     *     StatisticsAccumulator.unitTestValidate
     *
     *  2. Tests clearState()
     *
     *  3. Accumulates 7 fresh samples and again does same as (1)
     *
     *  4. clear the State again
     *
     *  5. Accumulate 7 fresh samples and all the samples will have the same
     *     value (1111). Make sure that the Statistics Accumulator works.
     */
    private void testStatisticsAccumulator( ) {
        System.out.println( "Starting testStatisticsAccumulator()" );
        helper.start( "testStatisticsAccumulator" ) ;
        StatisticMonitoredAttribute  sma = (StatisticMonitoredAttribute)
            orb.getMonitoringManager().getRootMonitoredObject().getChild(
                CHILD_LEVEL1_NAME ).getChild(
                CHILD_LEVEL2_NAME2 ).getAttribute( 
                STATISTICATTRIBUTE1_LEVEL2_NAME );
        StatisticsAccumulator sa = sma.getStatisticsAccumulator( );
        double[] sample1 = {2321, 2223, 2451, 2232, 3426};
        int i;
        for(i = 0; i < sample1.length; i++ ) {
            sa.sample( sample1[i] );
        } 
        sa.unitTestValidate( UNIT_FOR_STATISTICS, (double) 2223, (double) 3426,
            5, computeAverage(sample1), computeStandardDeviation(sample1) );
        System.out.println( "**************************" );
        System.out.println( sa.getValue() );
        System.out.println( "**************************" );


        // Now Clear the state and start fresh for next 7 samples	
        sma.clearState( );
 
        double[] sample2 = {521265, 627268, 726272, 565265, 536352.65, 
            663563.98, 563536.65};
        for(i = 0; i < sample2.length; i++ ) {
            sa.sample( sample2[i] );
        } 
        sa.unitTestValidate( UNIT_FOR_STATISTICS, (double)521265, 
            (double) 726272, 7, computeAverage(sample2), 
            computeStandardDeviation(sample2) );
        System.out.println( "**************************" );
        System.out.println( sa.getValue() );
        System.out.println( "**************************" );

        // Now Clear the state and start fresh for next 7 samples	
        sma.clearState( );
 
        double[] sample3 = {1111, 1111, 1111, 1111, 1111, 
            1111, 1111};
        for(i = 0; i < sample3.length; i++ ) {
            sa.sample( sample3[i] );
        } 
        sa.unitTestValidate( UNIT_FOR_STATISTICS, (double)1111, 
            (double) 1111, 7, computeAverage(sample3), 
            computeStandardDeviation(sample3) );
        System.out.println( "**************************" );
        System.out.println( sa.getValue() );
        System.out.println( "**************************" );
        System.out.println( "testStatisticsAccumulator(): PASSED" );
        helper.pass() ;
    }

    private void testRootName()
    {
        helper.start( "testRootName" ) ;
	// Make sure that the internal ORBSingleton full ORB has been
	// created.
	RequestDispatcherRegistry rdr = 
	    ((ORB)ORB.init()).getRequestDispatcherRegistry() ;

	// We have already created one ORB (see the constructor),
	// so the rootNames should start with orb__2.
	String[] orbIds = { "", "", "ID1", "ID2", "ID1" } ;
	String[] rootNames = { "orb__2", "orb__3", "orb_ID1_1", "orb_ID2_1",
	    "orb_ID1_2" } ;
	
	for (int ctr=0; ctr<orbIds.length; ctr++) {
	    Properties props = new Properties() ;
	    props.put( ORBConstants.ORB_ID_PROPERTY, orbIds[ctr] ) ;
	    ORB orb = (ORB)ORB.init( new String[0], props ) ;
	    MonitoringManager mm = orb.getMonitoringManager() ;

	    // Check the name of the root
	    String rname = mm.getRootMonitoredObject().getName() ;
	    if (!rname.equals( rootNames[ctr] ))
		error( "Expected " + rootNames[ctr] + " got " +
		    rname + " for name of root monitored object for " +
		    "ORB id " + orbIds[ctr] ) ;

	    MonitoringManagerFactory mmf = 
		MonitoringFactories.getMonitoringManagerFactory() ;
	}

        helper.pass() ;
    }
}
