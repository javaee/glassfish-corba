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

package pi.ort;

import org.omg.PortableInterceptor.*;

/** 
 * ORTStateChangeEvaluator is a Singleton used for
 * 1. registering Adapter and AdapterState Changes from the IORInterceptor
 * 2. evaluating that the statechanges happed as expected
 * 3. notifying DelayServant.method() completion 
 */
public class ORTStateChangeEvaluator {
    private static ORTStateChangeEvaluator ortStateChangeEvaluatorInstance = 
        new ORTStateChangeEvaluator ( );

    String[] poasWhoseStateChangesAreReported;
    short currentAdapterState;
    int managerId;
    short currentAdapterManagerState;
    String notificationToken = null;
    

    boolean registerAdapterStateChangeCalled;

    private ORTStateChangeEvaluator( ) {
        resetAllStates( );
    }

    public static ORTStateChangeEvaluator getInstance( ) {
        return ortStateChangeEvaluatorInstance;
    }

    /**
     *  The AdapterStateChange from the IORInterceptor is notified here.
     */
    public void registerAdapterStateChange(ObjectReferenceTemplate[] templates,
        short  state ) 
    {
        System.out.println( "registerAdapterState Change called...." );
        System.out.flush( );
        if( ( templates == null )
          ||( templates.length == 0 ) )
        {
            System.err.println(
                "Adapter State Change called with no templates");
            System.exit( -1 );
        }
        this.poasWhoseStateChangesAreReported = new String[templates.length];
        for( int i = 0; i < templates.length; i++ ) {
            String[] adapterNames = templates[i].adapter_name( );
            poasWhoseStateChangesAreReported[i] = 
                adapterNames[ adapterNames.length - 1 ]; 
            System.out.println("\t- POAs Whose State Change Is Reported : " +
                poasWhoseStateChangesAreReported[i] );
            System.out.println("\t  - POA Parent to Child list.." );
            for( int j = 0; j < adapterNames.length; j++ ) {
                System.out.println( "\t\t + " + adapterNames[j] );
                System.out.flush( );
            }
            System.out.flush( );
        }
        this.currentAdapterState = state;
        System.out.println( "\t  - State Changed to " + state );
        System.out.flush( );
        registerAdapterStateChangeCalled = true; 
    }

    

    /**
     *  The AdapterManagerStateChange from the IORInterceptor is notified here.
     */
    public void registerAdapterManagerStateChange( int managerId, short state )
    {
        this.managerId = managerId;
        this.currentAdapterManagerState = state;
        System.out.println( 
            "\t- AdapterManagerStateChange Manager Id = " + managerId +
            " state = " + state );
        System.out.flush( );
    }

    /**
     * Compares the list of POAs whose destroyed notifications are expected Vs.
     * the list of POAs whose destroyed notifications are actually recieved.
     * If it doesn't match it returns false, else the evaluation passed.
     */
    public boolean evaluateAdapterStateChange( String[] poas ) {
        if( currentAdapterState != NON_EXISTENT.value ) 
        {
            System.err.println( "AdapterStateChange reported = " + 
                currentAdapterState );
            System.err.println( "AdapterStateChange Expected = " + 
                NON_EXISTENT.value );
            return false;
        }
        boolean check =  checkAllPOAStateChangesReported( poas );
        return check;
         
    }

    /**
     * Very similar to the previous method with an extra check to see if the
     * token passed matches the notificationToken recieved from DelayServant.
     */
    public boolean evaluateAdapterStateChange( String[] poas, String token ) {
        if( ( notificationToken == null )
          ||( !notificationToken.equals( token ) ) ) {
            System.err.println( "POA Destroy is notified before completing " +
                " invocations...." );
            System.err.flush( );
            return false;
        }
        return evaluateAdapterStateChange( poas );
    }


    /**
     * A simple utility to compare two arrays.
     * _REVISIT_: Ken might have an utility to compare two arrays, just use
     * that.
     */
    private boolean checkAllPOAStateChangesReported( String[] poas ) {
        if( poas.length != poasWhoseStateChangesAreReported.length ) {
            return false;
        }
        int i = 0, j = 0;
        boolean matchFound;
        for( i = 0; i < poas.length; i++ ) {
            matchFound = false;
            for( j = 0; j < poas.length; j++ ) {
                if( poasWhoseStateChangesAreReported[j].equals( poas[i] ) ) {
                    matchFound = true;
                    break;
                }
            }
            if( !matchFound ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares the AdapterManagerState notified from IOR interceptor with
     * passed in state. If it doesn't match then, it returns false.
     */
    public boolean evaluateAdapterManagerState( short state ) {
        if( currentAdapterManagerState == state ) {
            return true;
        }
        System.err.println( "In evaluateAdapterManagerState " );
        System.err.print( "currentAdapterManagerState = " + 
            currentAdapterManagerState );
        System.err.print( " is Not Equal To " + state );
        return false;
    }

    /**
     * resets all the variable states to allow for a new test to start.
     */
    void resetAllStates( ) {
        currentAdapterState = 0;
        currentAdapterManagerState = 0;
        notificationToken = null;
        poasWhoseStateChangesAreReported = null;
        managerId = 0;
        registerAdapterStateChangeCalled = false;
    }

    /**
     * DelayServant will invoke this method with a notificationToken passed to
     * it to signal the method completion.
     */
    public void notificationTokenFromDelayServant( String token ) {
        System.out.println( "notificationTokenFromDelayServant called with " +
            " the token = " + token );
        System.out.flush( );
        notificationToken = token;
    }
}


  
