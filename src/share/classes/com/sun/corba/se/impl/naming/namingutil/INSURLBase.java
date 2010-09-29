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

package com.sun.corba.se.impl.naming.namingutil;

import com.sun.corba.se.spi.logging.OMGSystemException ;

import com.sun.corba.se.spi.orb.ORB ;

/** The corbaloc: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's
 *  will be stored in this object. This object is capable of storing multiple
 *  Host profiles as defined in the CorbaLoc grammer.
 *
 *  @author  Hemanth
 */
public abstract class INSURLBase implements INSURL {
    private static OMGSystemException wrapper =
        OMGSystemException.self ;

    // If rirFlag is set to true that means internal
    // boot strapping technique will be used. If set to
    // false then the EndpointInfo will be used to create the
    // Service Object reference.
    protected boolean rirFlag = false ;
    protected java.util.ArrayList theEndpointInfo = null ;
    protected String theKeyString = "NameService" ;
    protected String theStringifiedName = null ;

    /**
     *  A Utility method to throw BAD_PARAM exception to signal malformed
     *  INS URL.
     */
    protected void badAddress( String name )
    {
	throw wrapper.soBadAddress( name ) ;
    }

    protected void badAddress( java.lang.Throwable e, String name )
    {
	throw wrapper.soBadAddress( e, name ) ;
    }

    public boolean getRIRFlag( ) {
        return rirFlag;
    } 

    public java.util.List getEndpointInfo( ) {
        return theEndpointInfo;
    }

    public String getKeyString( ) {
        return theKeyString;
    }

    public String getStringifiedName( ) {
        return theStringifiedName;
    }

    public abstract boolean isCorbanameURL( );

    public void dPrint( ) {
        System.out.println( "URL Dump..." );
        System.out.println( "Key String = " + getKeyString( ) );
        System.out.println( "RIR Flag = " + getRIRFlag( ) );
        System.out.println( "isCorbanameURL = " + isCorbanameURL() );
        for( int i = 0; i < theEndpointInfo.size( ); i++ ) {
            ((IIOPEndpointInfo) theEndpointInfo.get( i )).dump( );
        }
        if( isCorbanameURL( ) ) {
            System.out.println( "Stringified Name = " + getStringifiedName() );
        }
    }
    
}

