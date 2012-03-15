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

package com.sun.corba.ee.impl.copyobject ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import java.util.IdentityHashMap ;
import java.util.Map ;

import org.omg.CORBA.portable.ObjectImpl ;
import org.omg.CORBA.portable.Delegate ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.impl.misc.ClassInfoCache ;

import com.sun.corba.ee.impl.util.Utility ;
import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopier;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopierBase;
import org.glassfish.pfl.dynamic.copyobject.impl.ClassCopierFactory;
import org.glassfish.pfl.dynamic.copyobject.impl.DefaultClassCopierFactories;
import org.glassfish.pfl.dynamic.copyobject.impl.FastCache;
import org.glassfish.pfl.dynamic.copyobject.impl.PipelineClassCopierFactory;
import org.glassfish.pfl.dynamic.copyobject.spi.CopyobjectDefaults;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopier;
import org.glassfish.pfl.dynamic.copyobject.spi.ReflectiveCopyException;

/** Class used to deep copy arbitrary data.  A single 
 * ReflectObjectCopierImpl
 * instance will preserve all object aliasing across multiple calls
 * to copy.
 */
public class ReflectObjectCopierImpl implements ObjectCopier {
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Note that this class is the only part of the copyObject
    // framework that is dependent on the ORB.  This is in
    // fact specialized just for CORBA objrefs and RMI-IIOP stubs.

    // This thread local holds an ORB that is used when
    // a Remote needs to be copied, because the autoConnect
    // call requires an ORB.  We do not want to pass an ORB
    // everywhere because that would make the ClassCopier instances
    // ORB dependent, which would prevent them from being 
    // statically scoped.  Note that this is package private so that
    // ObjectCopier can access this data member.
    static final ThreadLocal localORB = new ThreadLocal() ;

    // Special ClassCopier instances needed for CORBA
    
    // For java.rmi.Remote, we need to call autoConnect, 
    // which requires an orb.
    private static ClassCopier remoteClassCopier =
        new ClassCopierBase( "remote" ) {
            public Object createCopy( Object source ) {
                ORB orb = (ORB)localORB.get() ;
                return Utility.autoConnect( source, orb, true ) ;
            }
        } ;

    private static ClassCopier identityClassCopier =
        new ClassCopierBase( "identity" ) {
            public Object createCopy( Object source ) {
                return source ;
            } 
        } ;

    // For ObjectImpl, we just make a shallow copy, since the Delegate
    // is mostly immutable.
    private static ClassCopier corbaClassCopier = 
        new ClassCopierBase( "corba" ) {
            public Object createCopy( Object source) {
                ObjectImpl oi = (ObjectImpl)source ;
                Delegate del = oi._get_delegate() ;

                try {
                    // Create a new object of the same type as source
                    ObjectImpl result = (ObjectImpl)source.getClass().newInstance() ;
                    result._set_delegate( del ) ;

                    return result ;
                } catch (Exception exc) {
                    throw wrapper.exceptionInCreateCopy( exc ) ;

                }
            }
        } ;

    private static ClassCopierFactory specialClassCopierFactory = 
        new ClassCopierFactory() {
            public ClassCopier getClassCopier( Class cls 
            ) throws ReflectiveCopyException
            {
                ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get( cls ) ;
                
                // Handle Remote: this must come before CORBA.Object,
                // since a corba Object may also be a Remote.
                if (cinfo.isARemote(cls)) {
                    return remoteClassCopier;
                }

                // Handle org.omg.CORBA.portable.ObjectImpl
                if (cinfo.isAObjectImpl(cls)) {
                    return corbaClassCopier;
                }

                // Need this case to handle TypeCode.
                if (cinfo.isAORB(cls)) {
                    return identityClassCopier ;
                }

                return null ;
            }
        } ;

    // It is very important that ccf be static.  This means that
    // ccf is shared across all instances of the object copier,
    // so that any class is analyzed only once, instead of once per 
    // copier instance.  This is worth probably 20%+ in microbenchmark 
    // performance.
    private static PipelineClassCopierFactory ccf = 
        DefaultClassCopierFactories.getPipelineClassCopierFactory() ; 
    
    static {
        ccf.setSpecialClassCopierFactory( specialClassCopierFactory ) ;
    }

    private Map oldToNew ;

    /** Create an ReflectObjectCopierImpl for the given ORB.
     * The orb is used for connection Remote instances.
     */
    public ReflectObjectCopierImpl( ORB orb )
    {
        localORB.set( orb ) ;
        if (DefaultClassCopierFactories.USE_FAST_CACHE) {
            oldToNew =
                new FastCache(new IdentityHashMap());
        } else {
            oldToNew = new IdentityHashMap();
        }
    }

    /** Return a deep copy of obj.  Aliasing is preserved within
     * obj and between objects passed in multiple calls to the
     * same instance of ReflectObjectCopierImpl.
     */
    public Object copy( Object obj ) throws ReflectiveCopyException
    {
        return copy( obj, false ) ;
    }

    public Object copy( Object obj, boolean debug ) throws ReflectiveCopyException
    {
        if (obj == null) {
            return null;
        }

        OperationTracer.begin( "ReflectObjectCopierImpl" ) ;
        Class<?> cls = obj.getClass() ;
        ClassCopier copier = ccf.getClassCopier( cls ) ;
        return copier.copy( oldToNew, obj) ;
    }
}
