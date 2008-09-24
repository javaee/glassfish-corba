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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.corba.se.impl.orbutil ;

import java.util.Stack ;

import com.sun.corba.se.impl.orbutil.ORBUtility ;

/** Simple little utility class for tracing significant
 * events and method entry/exit.  Calls to these methods in
 * an instance of this class should be guarded with a debug flag
 * check, especially in the dispatch path.
 */
public class DprintUtil {
    private Object client ;
    private ThreadLocal<Stack<String>> currentMethod = new ThreadLocal<Stack<String>>() {
        public Stack<String> initialValue() {
            return new Stack<String>() ;
        }
    } ;

    public DprintUtil( Object self ) {
        client = self ;
    }

    public void dprint( String msg ) {
        ORBUtility.dprint( client, msg ) ;
    }

    private String makeString( Object... args ) {
        if (args.length == 0)
            return "" ;

        StringBuilder sb = new StringBuilder() ;
        sb.append( '(' ) ;
        boolean first = true ;
        for (Object obj : args) {
            if (first) {
                first = false ;
            } else {
                sb.append( ' ' ) ;
            }
            sb.append( obj.toString() ) ;
        }
        sb.append( ')' ) ;

        return sb.toString() ;
    }

    public void enter( String mname, Object... args ) {
        currentMethod.get().push( mname ) ;
        String str = makeString( args ) ;
        dprint( "." + mname + "->" + str ) ;
    }

    public void info( Object... args ) {
        String mname = currentMethod.get().peek() ;
        String str = makeString( args ) ;
        dprint( "." + mname + "::" + str ) ;
    }

    public void exit() {
        String mname = currentMethod.get().pop() ;
        dprint( "." + mname + "<-" ) ;
    }

    public void exit( Object retVal ) {
        String mname = currentMethod.get().pop() ;
        dprint( "." + mname + "<-(" + retVal + ")" ) ;
    }
}
