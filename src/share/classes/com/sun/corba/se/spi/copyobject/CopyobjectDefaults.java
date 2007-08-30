/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.spi.copyobject ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopierFactory ;
import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier ;

import com.sun.corba.se.impl.orbutil.copyobject.FallbackObjectCopierImpl ;

import com.sun.corba.se.impl.copyobject.ReferenceObjectCopierImpl ;
import com.sun.corba.se.impl.copyobject.ORBStreamObjectCopierImpl ;
import com.sun.corba.se.impl.copyobject.JavaStreamORBObjectCopierImpl ;

public abstract class CopyobjectDefaults
{
    private CopyobjectDefaults() { }

    /** Obtain the ORB stream copier factory.  Note that this version behaves differently
     * than the others: each ObjectCopier produced by the factory only preserves aliasing
     * within a single call to copy.  The others copiers all preserve aliasing across
     * all calls to copy (on the same ObjectCopier instance).
     */
    public static ObjectCopierFactory makeORBStreamObjectCopierFactory( final ORB orb ) 
    {
	return new ObjectCopierFactory() {
	    public ObjectCopier make( )
	    {
		return new ORBStreamObjectCopierImpl( orb ) ;
	    }
	} ;
    }

    public static ObjectCopierFactory makeJavaStreamObjectCopierFactory( final ORB orb ) 
    {
	return new ObjectCopierFactory() {
	    public ObjectCopier make( )
	    {
		return new JavaStreamORBObjectCopierImpl( orb ) ;
	    }
	} ;
    }

    private static final ObjectCopier referenceObjectCopier = new ReferenceObjectCopierImpl() ;

    private static ObjectCopierFactory referenceObjectCopierFactory = 
	new ObjectCopierFactory() {
	    public ObjectCopier make() 
	    {
		return referenceObjectCopier ;
	    }
	} ;

    /** Obtain the reference object "copier".  This does no copies: it just
     * returns whatever is passed to it.
     */
    public static ObjectCopierFactory getReferenceObjectCopierFactory()
    {
	return referenceObjectCopierFactory ;
    }

    /** Create a fallback copier factory from the two ObjectCopierFactory
     * arguments.  This copier makes an ObjectCopierFactory that creates
     * instances of a fallback copier that first tries an ObjectCopier
     * created from f1, then tries one created from f2, if the first
     * throws a ReflectiveCopyException.
     */
    public static ObjectCopierFactory makeFallbackObjectCopierFactory( 
	final ObjectCopierFactory f1, final ObjectCopierFactory f2 )
    {
	return new ObjectCopierFactory() {
	    public ObjectCopier make() 
	    {
		ObjectCopier c1 = f1.make() ;
		ObjectCopier c2 = f2.make() ;
		return new FallbackObjectCopierImpl( c1, c2 ) ;
	    }
	} ;
    }
}
