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

package com.sun.tools.corba.se.idl.toJavaPortable ;

import com.sun.tools.corba.se.idl.toJavaPortable.NameModifier ;

public class NameModifierImpl implements NameModifier {
    private String prefix ;
    private String suffix ;

    public NameModifierImpl( )
    {
        this.prefix = null ;
        this.suffix = null ;
    }

    public NameModifierImpl( String prefix, String suffix ) 
    {
        this.prefix = prefix ;
        this.suffix = suffix ;
    }

    /** Construct a NameModifier from a pattern of the form xxx%xxx.
    * The pattern must consist of characters chosen from the
    * set [A-Za-z0-9%$_]. In addition, the pattern must contain 
    * exactly one % character.  Finally, if % is not the first char in 
    * the pattern, the pattern must not start with a number.
    * <p>
    * The semantics of makeName are very simply: just replace the
    * % character with the base in the pattern and return the result.
    */
    public NameModifierImpl( String pattern ) 
    {
        int first = pattern.indexOf( '%' ) ;
        int last  = pattern.lastIndexOf( '%' ) ;

        if (first != last)
            throw new IllegalArgumentException( 
                Util.getMessage( "NameModifier.TooManyPercent" ) ) ;

        if (first == -1)
            throw new IllegalArgumentException( 
                Util.getMessage( "NameModifier.NoPercent" ) ) ;

        for (int ctr = 0; ctr<pattern.length(); ctr++) {
            char ch = pattern.charAt( ctr ) ;
            if (invalidChar( ch, ctr==0 )) {
                char[] chars = new char[] { ch } ;
                throw new IllegalArgumentException( 
                    Util.getMessage( "NameModifier.InvalidChar", 
                        new String( chars )) ) ;
            }
        }

        // at this point, 0 <= first && first < pattern.length()
        prefix = pattern.substring( 0, first ) ;
        suffix = pattern.substring( first+1 ) ;
    }

    /** Return true if ch is invalid as a character in an 
    * identifier.  If ch is a number, it is invalid only if
    * isFirst is true.
    */
    private boolean invalidChar( char ch, boolean isFirst ) 
    {
        if (('A'<=ch) && (ch<='Z'))
            return false ;
        else if (('a'<=ch) && (ch<='z'))
            return false ;
        else if (('0'<=ch) && (ch<='9'))
            return isFirst ;
        else if (ch=='%')
            return false ;
        else if (ch=='$')
            return false ;
        else if (ch=='_')
            return false ;
        else
            return true ;
    }

    public String makeName( String base )
    {
        StringBuffer sb = new StringBuffer() ;

        if (prefix != null)
            sb.append( prefix ) ;

        sb.append( base ) ;

        if (suffix != null)
            sb.append( suffix ) ;

        return sb.toString() ;
    }
}
