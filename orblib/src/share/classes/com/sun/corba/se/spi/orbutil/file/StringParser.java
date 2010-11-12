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

package com.sun.corba.se.spi.orbutil.file ;

public class StringParser {
    private String data ;
    private int pos ;
    private char current ;

    public StringParser( String str ) {
        if (str.length() == 0) {
            throw new RuntimeException("Empty string not allowed");
        }

        this.data = str ;
        this.pos = 0 ;
        this.current = str.charAt( pos ) ;
    }

    private void setPos( int newPos ) {
        if (newPos < data.length() ) {
            pos = newPos ;
            current = data.charAt( newPos ) ;
        }
    }

    private boolean next() {
        if (data.length() > pos) {
            setPos( pos + 1 ) ;
            return true ;
        } else {
            return false ;
        }
    }

    /** skip everything until str is found.  Returns true if found, otherwise
     * false.
     * @param str String for which we are looking
     * @return whether or not str was found
     */
    public boolean skipToString( String str ) {
        int index = data.indexOf( str ) ;
        if (index >= 0) {
            setPos( index ) ;
            return true ;
        } else {
            return false ;
        }
    }

    /** skip over str, if str is at the current position.
     * @param string to skip (must be at current position)
     * @return whether or not str was at current position
     */
    public boolean skipString( String str ) {
        String cstr = data.substring( pos, pos+str.length() ) ;
        if (cstr.equals( str )) {
            setPos( pos+str.length() ) ;
            return true ;
        } else {
            return false ;
        }
    }

    /** Skip over whitespace.  Returns true if some whitespace skipped.
     * @return whether some whitespace was skipped.
     */
    public boolean skipWhitespace() {
        boolean hasSkipped = false ;
        while (Character.isWhitespace(current)) { 
            hasSkipped = true ;
            if (!next()) {
                break ;
            }
        }

        return hasSkipped ;
    }

    /** Return int matched at current position as a string.
     */
    public String parseInt() {
        int first = pos ;
        boolean atStart = true ;
        while ((current >= '0') && (current <= '9')) {
            atStart = false ;
            if (!next()) {
                break ;
            }
        }

        if (atStart) {
            return null ;
        } else {
            return data.substring( first, pos ) ;
        }
    }
}
