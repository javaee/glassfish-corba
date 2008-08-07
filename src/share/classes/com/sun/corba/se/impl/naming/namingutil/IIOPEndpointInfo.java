/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.naming.namingutil;

import com.sun.corba.se.spi.orbutil.ORBConstants;

/** 
 *  EndpointInfo is used internally by CorbaLoc object to store the
 *  host information used in creating the Service Object reference
 *  from the -ORBInitDef and -ORBDefaultInitDef definitions.
 *
 *  @Author Hemanth
 */
public class IIOPEndpointInfo
{
    // Version information
    private int major, minor;

    // Host Name and Port Number
    private String host;
    private int port;

    public String toString() {
        return "IIOPEndpointInfo[" + major + "." + minor
            + " " + host + ":" + port + "]" ;
    }

    IIOPEndpointInfo( ) {
	// Default IIOP Version 
	major = ORBConstants.DEFAULT_INS_GIOP_MAJOR_VERSION;
	minor = ORBConstants.DEFAULT_INS_GIOP_MINOR_VERSION;
	// Default host is localhost
	host = ORBConstants.DEFAULT_INS_HOST;
	// Default INS Port
	port = ORBConstants.DEFAULT_INS_PORT;
    }

    public void setHost( String theHost ) {
	host = theHost;
    }

    public String getHost( ) {
	return host;
    }

    public void setPort( int thePort ) {
	port = thePort;
    }

    public int getPort( ) {
	return port;
    }

    public void setVersion( int theMajor, int theMinor ) {
	major = theMajor;
	minor = theMinor;
    }

    public int getMajor( ) {
	return major;
    }

    public int getMinor( ) {
	return minor;
    }

    /** Internal Debug Method.
     */
    public void dump( ) {
	System.out.println( " Major -> " + major + " Minor -> " + minor );
	System.out.println( "host -> " + host );
	System.out.println( "port -> " + port );
    }
}

