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

package com.sun.corba.se.spi.orbutil.misc ;

import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ken
 */
public class DprintUtil {
    private static final boolean USE_LOGGER = false ;
    
    private final String sourceClassName ;
    private final String loggerName ;
    private final ThreadLocal<Stack<String>> currentMethod =
	new ThreadLocal<Stack<String>>() {
            @Override
            public Stack<String> initialValue() {
                return new Stack<String>() ;
            }
        } ;

    private static Map<String,DprintUtil> dpuMap =
	new WeakHashMap<String,DprintUtil>() ;

    public static DprintUtil getDprintUtil( Class<?> cls ) {
	String cname = cls.getName() ;
	DprintUtil result = dpuMap.get( cname ) ;
	if (result == null) {
	    result = new DprintUtil( cls ) ;
	    dpuMap.put( cname, result ) ;
	}

	return result ;
    }

    private DprintUtil( Class selfClass ) {
        sourceClassName = compressClassName( selfClass.getName() ) ;  
        if (USE_LOGGER) {
            loggerName = selfClass.getPackage().getName() ;
        } else {
            loggerName = null ;
        }
    }        
    
    private static String compressClassName( String name ) {
	// Note that this must end in . in order to be renamed correctly.
	String prefix = "org.glassfish.gmbal." ;
	if (name.startsWith( prefix ) ) {
	    return "(GMBAL)." + name.substring( prefix.length() ) ;
	} else {
            return name;
        }
    }
 
    public synchronized void dprint(String msg) {
        String prefix = "(" + Thread.currentThread().getName() + "): " ;
  
        if (USE_LOGGER) {
            String mname = currentMethod.get().peek() ;
            Logger.getLogger( loggerName ).
                logp( Level.INFO, prefix + msg, sourceClassName, mname ) ;
        } else {
            System.out.println( prefix + sourceClassName + msg ) ;
        }
    }
    
    private String makeString( Object... args ) {
        if (args.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder() ;
        sb.append( '(' ) ;
        boolean first = true ;
        for (Object obj : args) {
            if (first) {
                first = false ;
            } else {
                sb.append( ' ' ) ;
            }

	    sb.append( OperationTracer.convertToString(obj)) ;
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
        String mname = currentMethod.get().peek() ;
        dprint( "." + mname + "<-" ) ;
        currentMethod.get().pop() ;
    }

    public void exit( Object retVal ) {
        String mname = currentMethod.get().peek() ;
        dprint( "." + mname + "<-(" + retVal + ")" ) ;
        currentMethod.get().pop() ;
    }
}
