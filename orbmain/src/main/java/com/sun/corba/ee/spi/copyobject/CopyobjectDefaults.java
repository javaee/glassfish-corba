/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.corba.ee.spi.copyobject ;

import com.sun.corba.ee.spi.orb.ORB ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory ;
import org.glassfish.pfl.dynamic.copyobject.impl.FallbackObjectCopierImpl ;

import com.sun.corba.ee.impl.copyobject.ReferenceObjectCopierImpl ;
import com.sun.corba.ee.impl.copyobject.ORBStreamObjectCopierImpl ;
import com.sun.corba.ee.impl.copyobject.JavaStreamORBObjectCopierImpl ;
import com.sun.corba.ee.impl.copyobject.OldReflectObjectCopierImpl ;
import com.sun.corba.ee.impl.copyobject.ReflectObjectCopierImpl ;

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

    /** Obtain the old version of the reflective copier factory.  This is provided only
     * for benchmarking purposes.
     */
    public static ObjectCopierFactory makeOldReflectObjectCopierFactory( final ORB orb ) 
    {
        return new ObjectCopierFactory() {
            public ObjectCopier make()
            {
                return new OldReflectObjectCopierImpl( orb ) ;
            }
        } ;
    }

    /** Obtain the new reflective copier factory.  This is 3-4 times faster than the stream
     * copier, and about 10% faster than the old reflective copier.  It should
     * normally be used with a fallback copier, as there are some classes that simply
     * cannot be copied reflectively.
     */
    public static ObjectCopierFactory makeReflectObjectCopierFactory( final ORB orb ) 
    {
        return new ObjectCopierFactory() {
            public ObjectCopier make( )
            {
                return new ReflectObjectCopierImpl( orb ) ;
            }
        } ;
    }
}
