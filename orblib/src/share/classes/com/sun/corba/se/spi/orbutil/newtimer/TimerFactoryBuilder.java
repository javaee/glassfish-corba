/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.orbutil.newtimer ;

import java.util.Map ;
import java.util.HashMap ;
import java.util.Collection ;
import java.util.List ;
import java.util.ArrayList ;

import com.sun.corba.se.impl.orbutil.newtimer.TimerFactoryImpl ;

import org.glassfish.gmbal.ManagedObjectManager ;

/** TimerFactoryBuilder creates independent
 * instances of the TimerFactory interface.
 * Guarantees that all TimerFactory instances have unique names.
 */
public class TimerFactoryBuilder {
    private static Map<String,TimerFactory> fmap = 
	new HashMap<String,TimerFactory>() ;

    public synchronized static TimerFactory make( 
        String name, String description ) {

        return make( null, name, description, false ) ;
    }

    /** Create a new TimerFactory.  No two TimerFactory instances
     * can have the same name.
     */
    public synchronized static TimerFactory make( ManagedObjectManager mom, 
        String name, String description, boolean doGmbalRegistration ) {

	if (fmap.get( name ) != null)
	    throw new IllegalArgumentException(
		"There is currently a TimerFactory named " + name ) ;

	TimerFactory result = new TimerFactoryImpl( mom, name, description,
            doGmbalRegistration ) ;
	fmap.put( name, result ) ;
	return result ;
    }

    /** Remove a TimerFactory so that it may be collected.
     */
    public synchronized static void destroy( TimerFactory factory ) {
	fmap.remove( factory.name() ) ;
    }

    /** Return a list of the TimerFactory instances in this TimerFactoryBuilder.
     * The list represents the state of the instances at the time this method is 
     * called; any susbsequent make/destroy calls do NOT affect this list.
     */
    public synchronized static List<TimerFactory> contents() {
	Collection<TimerFactory> coll = fmap.values() ;
	ArrayList<TimerFactory> list = new ArrayList( coll ) ;
	return list ;
    }
}
