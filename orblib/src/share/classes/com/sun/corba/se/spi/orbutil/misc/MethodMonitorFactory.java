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
import java.util.WeakHashMap;

/** Contains various factory methods for constructing MethodMonitors,
 *
 * @author ken_admin
 */
public class MethodMonitorFactory {
    private MethodMonitorFactory() {}

    private abstract static class MethodMonitorBase implements MethodMonitor {
	private String name ;

	MethodMonitorBase( String name ) {
	    this.name = name ;
	}

	@Override
	public String toString() {
	    return name ;
	}

	public abstract void enter(boolean enabled, String name,
	    Object... args) ;

	public void info( boolean enabled, Object... args) {
	    // Default does nothing: override if used
	}

	public abstract void exit( boolean enabled, Object result) ;

	public void exit( boolean enabled ) {
	    exit( enabled, null ) ;
	}

	public void clear() {
	    // No-op
	}
    }

    private static final MethodMonitor noopImpl = new MethodMonitorBase(
	"NoOpImpl" ) {

	@Override
	public void enter(boolean enabled, String name, Object... args) {
	    // No-op
	}

	@Override
	public void exit( boolean enabled, Object result) {
	    // No-op
	}
    } ;

    public static MethodMonitor getNoOp() {
	return noopImpl ;
    }

    public static MethodMonitor compose( final MethodMonitor... mms) {
	return new MethodMonitorBase( "ComposeImpl" ) {

	    @Override
	    public void enter(boolean enabled, String name, Object... args) {
		for (MethodMonitor mm : mms ) {
		    mm.enter( enabled, name, args ) ;
		}
	    }

	    @Override
	    public void info( boolean enabled, Object... args ) {
		for (MethodMonitor mm : mms ) {
		    mm.info( enabled, args ) ;
		}
	    }

	    @Override
	    public void exit(boolean enabled, Object result) {
		for (MethodMonitor mm : mms ) {
		    mm.exit( enabled, result ) ;
		}
	    }
	} ;
    }

    public static final MethodMonitor operationTracer = new MethodMonitorBase(
	"OperationTracer" ) {

	@Override
	public void enter(boolean enabled, String name, Object... args) {
	    OperationTracer.enter( name, args ) ;
	}

	@Override
	public void exit( boolean enabled, Object result) {
	    OperationTracer.exit();
	}

	@Override
	public void clear() {
	    OperationTracer.clear() ;
	}
    } ;

    public static MethodMonitor dprintUtil( final Class cls ) {
	final DprintUtil dputil = DprintUtil.getDprintUtil( cls ) ;

	return new MethodMonitorBase( "DprintUtil" ) {
	    @Override
	    public void enter(boolean enabled, String name, Object... args) {
		if (enabled) {
		    dputil.enter( name, args ) ;
		}
	    }

	    @Override
	    public void info( boolean enabled, Object... args ) {
		if (enabled) {
		    dputil.info( args ) ;
		}
	    }

	    @Override
	    public void exit(boolean enabled, Object result) {
		if (enabled) {
		    dputil.exit( result ) ;
		}
	    }
	} ;
    }

    // This is the same as compose( operationTracer, dprintUtil ), but
    // it is created so often that this avoid some significant overhead.
    private static MethodMonitor standardImpl( final Class cls ) {
	final DprintUtil dputil = DprintUtil.getDprintUtil( cls ) ;

	return new MethodMonitorBase( "StandardImpl" ) {
	    @Override
	    public void enter(boolean enabled, String name, Object... args) {
		OperationTracer.enter( name, args ) ;
		if (enabled) {
		    dputil.enter( name, args ) ;
		}
	    }

	    @Override
	    public void info( boolean enabled, Object... args ) {
		if (enabled) {
		    dputil.info( args ) ;
		}
	    }

	    @Override
	    public void exit(boolean enabled, Object result) {
	        OperationTracer.exit();
		if (enabled) {
		    dputil.exit( result ) ;
		}
	    }

	    @Override
	    public void clear() {
	        OperationTracer.clear() ;
	    }
	} ;
    }

    private static Map<Class<?>,MethodMonitor> stdMap =
	new WeakHashMap<Class<?>,MethodMonitor>() ;

    public static MethodMonitor makeStandard( Class cls ) {
	 MethodMonitor result = stdMap.get( cls ) ;
	 if (result == null) {
	     result = standardImpl( cls ) ;
	     stdMap.put( cls, result ) ;
	 }

	 return result ;
    }
}
