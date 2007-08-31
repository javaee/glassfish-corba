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

package com.sun.corba.se.impl.orbutil.codegen;

import com.sun.corba.se.spi.orbutil.misc.ORBClassLoader ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;

/** Class used to set and get the global class loader used
 * by the codegen library.  This is maintained in a ThreadLocal
 * to avoid concurrency problems.  All processing in the
 * codegen library takes place in the same thread in any case.
 */
public class CurrentClassLoader {
    private static ThreadLocal<ClassLoader> current = 
	new ThreadLocal() {
	    protected ClassLoader initialValue() {
		return ORBClassLoader.getClassLoader() ;
	    }
	} ;

    public static ClassLoader get() {
	return current.get() ;
    }

    public static void set( ClassLoader cl ) {
	if (cl == null)
	    cl = ORBClassLoader.getClassLoader() ;
	current.set( cl ) ;

	// This is essential for propert operation of codegen when multiple
	// ClassLoaders are in use.  The problem is that Type maintains a cache
	// the maps class names to Types.  The same class name may of course refer
	// to different classes in different ClassLoaders.  The Type interface
	// supports access to ClassInfo, which in the case of a Type for a loaded
	// class has a reflective implementation that includes all method and field
	// information from the class.  Thus we can have the situation where the class
	// name is mapped to a Type with ClassInfo from the wrong ClassLoader!
	// See bug 6562360 and GlassFish issue 3134 for the app server impact of getting
	// this wrong.
	Type.clearCaches() ;
    }
}
