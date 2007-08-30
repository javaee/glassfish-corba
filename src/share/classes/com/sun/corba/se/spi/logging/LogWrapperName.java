/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.spi.logging ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBData ;
import com.sun.corba.se.spi.presentation.rmi.PresentationDefaults ;

import com.sun.corba.se.impl.logging.CORBALogDomains ;

public abstract class LogWrapperName {
    private LogWrapperName() {}

    /** Returns the logger based on the category for an ORB.
     */
    public static String getLoggerName( ORB orb, String domain )  
    {
	ORBData odata = orb.getORBData() ;

	// Determine the correct ORBId.  There are 3 cases:
	// 1. odata is null, which happens if we are getting a logger before
	//    ORB initialization is complete.  In this case we cannot determine
	//    the ORB ID (it's not known yet), so we set the ORBId to
	//    _INITIALIZING_.
	// 2. odata is not null, so initialization is complete, but ORBId is set 
	//    to the default "".  To avoid a ".." in
	//    the log domain, we simply use _DEFAULT_ in this case.
	// 3. odata is not null, ORBId is not "": just use the ORBId.
	String ORBId ;
	if (odata == null)
	    ORBId = "_INITIALIZING_" ;
	else {
	    ORBId = odata.getORBId() ;
	    if (ORBId.equals(""))
		ORBId = "_DEFAULT_" ;
	}

	return getCORBALoggerName( ORBId, domain ) ;
    }

    /** Returns the logger based on the category.
     */
    public static String getLoggerName( String domain )
    {
	return getCORBALoggerName( "_CORBA_", domain ) ;
    }	
	
    private static String getCORBALoggerName( String ORBId, String domain ) 
    {
        StringBuffer sbuff = new StringBuffer() ;
	sbuff.append( CORBALogDomains.TOP_LEVEL_DOMAIN ) ;
	
	// It is really important to differentiate between the se and ee
	// ORBs here, as otherwise the following can happen:
	// 1. One part of an application creates an ORB using the JDK
        //    1.5 ORB, perhaps by calling ORB.init().create_any().
        //    A logger with name X is created somewhere in this ORB.
	// 2. Another part of an application creates an ORB using 
	//    the AS 9 (packaged in ee packages) ORB.  This ORB
	//    also needs logger X.
	// Step 1 created logger X with the se version of LogStrings.properties,
	// and step 2 attempts to create the same logger X with the ee
        // version of LogStrings.properties.  Since the names are different,
	// the Logger.getLogger call fails, which cascades through the application.
	// This failure has been observed in AS 9.
	sbuff.append( '.' ) ;
	if (PresentationDefaults.inAppServer())
	    sbuff.append( "ee" ) ;
	else
	    sbuff.append( "se" ) ;

	sbuff.append( '.' ) ;
	sbuff.append( ORBId ) ;
	sbuff.append( '.' ) ;
	sbuff.append( domain ) ;
	return sbuff.toString() ;
    }
	
}
