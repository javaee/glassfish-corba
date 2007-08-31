/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.orbutil.codegen ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

/** Some utilities for dealing with Java identifiers.
 *
 * @Author Ken Cavanaugh
 */
public abstract class Identifier {
    private Identifier() {}

    /** Check that name is a valid Java identifier.  No packages
     * are permitted here.
     */
    public static boolean isValidIdentifier( String name ) {
	if ((name == null) || (name.length() == 0))
	    return false ;

	if (!Character.isJavaIdentifierStart( name.charAt(0) ))
	    return false ;

	for (int ctr=1; ctr<name.length(); ctr++) {
	    if (!Character.isJavaIdentifierPart( name.charAt(ctr) ))
		return false ;
	}

	return true ;
    }

    /** Check that name is a valid full qualified Java identifier.
     */
    public static boolean isValidFullIdentifier( String name ) {
	if ((name == null) || (name.length() == 0))
	    return false ;

	// String.split seems to ignore trailing separators
	if (name.charAt(name.length()-1) == '.')
	    return false ;

	String[] arr = name.split( "\\." ) ;
	for (String str : arr) {
	    if (!isValidIdentifier( str )) {
		return false ;
	    }
	}

	return true ;
    }

    /** Assuming that isValidFullIdentifier( pkg ) and 
     * isValidIdentifier( ident ), reurn a fully qualifed
     * name for the identifier in the package.
     */
    public static String makeFQN( String pkg, String ident ) {
	if ((pkg != null) && !pkg.equals( "" ))
	    return pkg + '.' + ident ;
	else
	    return ident ;
    }

    public static Pair<String,String> splitFQN( String fqn ) {
	int count = fqn.lastIndexOf( '.' ) ;
	String pkg = (count<0) ? "" : fqn.substring( 0, count ) ;
	String cls = fqn.substring( count+1 ) ;
	return new Pair<String,String>( pkg, cls ) ;
    }
}
