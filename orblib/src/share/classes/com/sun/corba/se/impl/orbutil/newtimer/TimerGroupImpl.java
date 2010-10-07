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

package com.sun.corba.se.impl.orbutil.newtimer ;

import java.util.Set ;
import java.util.HashSet ;
import java.util.Collections ;

import com.sun.corba.se.spi.orbutil.newtimer.TimerFactory ;
import com.sun.corba.se.spi.orbutil.newtimer.TimerGroup ;
import com.sun.corba.se.spi.orbutil.newtimer.Controllable ;

/** A TimerGroup is a collection of Controllables, which includes
 * Timers and TimerGroups.
 */
public class TimerGroupImpl extends ControllableBase implements TimerGroup {
    private Set<ControllableBase> contents ;
    private Set<ControllableBase> roContents ;
    private long contentVersion ;

    private Set<ControllableBase> tcContents ;	// the transitive closure for this element
						// of the relation created by contents.
    private Set<ControllableBase> roTcContents ;

    private long tcContentVersion ;

    TimerGroupImpl( int id, TimerFactoryImpl factory, String name, String description ) {
	super( id, name, description, factory ) ;

	contents = new HashSet<ControllableBase>() ;
	roContents = Collections.unmodifiableSet( contents ) ;
	contentVersion = 0 ;

	tcContents = new HashSet<ControllableBase>() ;
	roTcContents = Collections.unmodifiableSet( tcContents ) ;
	tcContentVersion = -1 ;
    }

    public Set<ControllableBase> contents() {
	return roContents ;
    }

    public boolean add( Controllable con ) {
	synchronized (factory()) {
	    boolean result = contents.add( ControllableBase.class.cast( con ) ) ;
	    if (result)
		contentVersion++ ;
	    return result ;
	}
    }

    public boolean remove( Controllable con ) {
	synchronized (factory()) {
	    boolean result = contents.remove( ControllableBase.class.cast( con ) ) ;
	    if (result)
		contentVersion++ ;
	    return result ;
	}
    }

    // Get the transitive closure.  This is cached, and updated if the
    // contents have changed since the last access to tcContents.
    Set<ControllableBase> tcContents() {
	if (contentVersion != tcContentVersion) {
	    tcContents.clear() ;
	    transitiveClosure( tcContents ) ;
	}

	return roTcContents ;
    }
}

