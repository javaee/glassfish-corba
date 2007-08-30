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

/** 
 *  The corbaname: URL definitions from the -ORBInitDef and -ORBDefaultInitDef's
 *  will be stored in this object. This object is capable of storing CorbaLoc
 *  profiles as defined in the CorbaName grammer.
 *
 *  @Author Hemanth
 */
public class CorbanameURL extends INSURLBase
{
    /**
     * This constructor takes a corbaname: url with 'corbaname:' prefix stripped
     * and initializes all the variables accordingly. If there are any parsing
     * errors then BAD_PARAM exception is raised.
     */
    public CorbanameURL( String aURL ) {
        String url = aURL;
  
        // First Clean the URL Escapes if there are any
        try {
            url = Utility.cleanEscapes( url );
        } catch( Exception e ) {
            badAddress( e );
        }

        int delimiterIndex = url.indexOf( '#' );
        String corbalocString = null;
        if( delimiterIndex != -1 ) {
                // Append corbaloc: for Grammar check, Get the string between
                // corbaname: and # which forms the corbaloc string
                corbalocString = "corbaloc:" +
                    url.substring( 0, delimiterIndex ) + "/";
        } else {
            // Build a corbaloc string to check the grammar.
            // 10 is the length of corbaname:
            corbalocString = "corbaloc:" + url.substring( 0, url.length() );
            // If the string doesnot end with a / then add one to end the
            // URL correctly
            if( corbalocString.endsWith( "/" ) != true ) {
                corbalocString = corbalocString + "/";
            }
        }
        try {
            // Check the corbaloc grammar and set the returned corbaloc
            // object to the CorbaName Object
            INSURL insURL = 
                INSURLHandler.getINSURLHandler().parseURL( corbalocString );
            copyINSURL( insURL );
            // String after '#' is the Stringified name used to resolve
            // the Object reference from the rootnaming context. If
            // the String is null then the Root Naming context is passed
            // back
            if((delimiterIndex > -1) &&
	       (delimiterIndex < (aURL.length() - 1)))
            {
		int start = delimiterIndex + 1 ;
		String result = url.substring(start) ;
		theStringifiedName = result ;
            } 
        } catch( Exception e ) {
            badAddress( e );
        }
    }

    /**
     * A Utility method to copy all the variables from CorbalocURL object to
     * this instance. 
     */
    private void copyINSURL( INSURL url ) {
        rirFlag = url.getRIRFlag( );
        theEndpointInfo = (java.util.ArrayList) url.getEndpointInfo( );
        theKeyString = url.getKeyString( );
        theStringifiedName = url.getStringifiedName( );
    }

    public boolean isCorbanameURL( ) {
        return true;
    }

}
