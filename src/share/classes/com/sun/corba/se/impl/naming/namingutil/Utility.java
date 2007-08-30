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

import java.io.StringWriter;

import org.omg.CORBA.DATA_CONVERSION;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.se.impl.logging.NamingSystemException;

import com.sun.corba.se.spi.orb.ORB ;

/**
 *  Utility methods for Naming.
 *
 *  @Author Hemanth
 */
class Utility {
    private static NamingSystemException wrapper =
	ORB.getStaticLogWrapperTable().get_NAMING_Naming() ;

    /** 
     * cleanEscapes removes URL escapes as per IETF 2386 RFP.
     */
    static String cleanEscapes( String stringToDecode ) {
        StringWriter theStringWithoutEscape = new StringWriter();
        for( int i = 0; i < stringToDecode.length(); i++ ) {
            char c = stringToDecode.charAt( i ) ;
            if( c != '%' ) {
                theStringWithoutEscape.write( c );
            } else {
                // Get the two hexadecimal digits and convert that into int
                i++;
                int Hex1 = hexOf( stringToDecode.charAt(i) );
                i++;
                int Hex2 = hexOf( stringToDecode.charAt(i) );
                int value = (Hex1 * 16) + Hex2;
                // Convert the integer to ASCII
                theStringWithoutEscape.write( (char) value );
            }
        }
        return theStringWithoutEscape.toString();
    }

    /** 
     *  Converts an Ascii Character into Hexadecimal digit
     *  NOTE: THIS METHOD IS DUPLICATED TO DELIVER NAMING AS A SEPARATE 
     *  COMPONENT TO RI.
     **/
    static int hexOf( char x )
    {
        int val;

        val = x - '0';
        if (val >=0 && val <= 9)
            return val;

        val = (x - 'a') + 10;
        if (val >= 10 && val <= 15)
            return val;

        val = (x - 'A') + 10;
        if (val >= 10 && val <= 15)
            return val;

        throw new DATA_CONVERSION( );
    }

    /**
     * If GIOP Version is not correct, This method throws a BAD_PARAM
     * Exception.
     **/
    static void validateGIOPVersion( IIOPEndpointInfo endpointInfo ) {
        if ((endpointInfo.getMajor() > NamingConstants.MAJORNUMBER_SUPPORTED) ||
	    (endpointInfo.getMinor() > NamingConstants.MINORNUMBERMAX ) )
        {
	    throw wrapper.insBadAddress() ;
        }
    }
}
