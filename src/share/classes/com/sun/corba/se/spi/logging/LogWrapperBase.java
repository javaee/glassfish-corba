/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

import java.util.logging.Level ;
import java.util.logging.Logger ;
import java.util.logging.LogRecord ;

import com.sun.corba.se.spi.orb.ORB ;

public abstract class LogWrapperBase 
{
    private Logger logger ;
    protected String loggerName ;

    protected LogWrapperBase( String loggerName ) 
    {
	this.logger = null ;
        this.loggerName = loggerName ;
    }

    protected synchronized Logger getLogger()
    {
	if (logger == null)
	    logger = ORB.getLogger( loggerName ) ;

	return logger ;
    }

    protected void doLog( Level level, String key, Object[] params, Class wrapperClass,
	Throwable thr ) 
    {
	LogRecord lrec = new LogRecord( level, key ) ;
	if (params != null)
	    lrec.setParameters( params ) ;
	if (level != Level.INFO) {
	    inferCaller( wrapperClass, lrec ) ;
	    lrec.setThrown( thr ) ;
	}
        lrec.setLoggerName( loggerName );
	Logger lgr = getLogger() ;
	lrec.setResourceBundle( lgr.getResourceBundle() ) ;
	lgr.log( lrec ) ;
    }

    private void inferCaller( Class wrapperClass, LogRecord lrec ) 
    {
	// Private method to infer the caller's class and method names

	// Get the stack trace.
	StackTraceElement stack[] = (new Throwable()).getStackTrace();
	StackTraceElement frame = null ;
	String wcname = wrapperClass.getName() ;
	String baseName = LogWrapperBase.class.getName() ;

	// The top of the stack should always be a method in the wrapper class, 
	// or in this base class.
	// Search back to the first method not in the wrapper class or this class.
	int ix = 0;
	while (ix < stack.length) {
	    frame = stack[ix];
	    String cname = frame.getClassName();
	    if (!cname.equals(wcname) && !cname.equals(baseName))  {
		break;
	    }

	    ix++;
	}

	// Set the class and method if we are not past the end of the stack
	// trace
	if (ix < stack.length) {
	    lrec.setSourceClassName(frame.getClassName());
	    lrec.setSourceMethodName(frame.getMethodName());
	}
    }

    protected void doLog( Level level, String key, Class wrapperClass, Throwable thr ) 
    {
	doLog( level, key, null, wrapperClass, thr ) ;
    }
}
