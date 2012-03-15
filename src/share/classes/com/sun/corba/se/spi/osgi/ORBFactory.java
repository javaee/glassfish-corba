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

package com.sun.corba.ee.spi.osgi;

import java.util.Properties ;

import org.glassfish.external.amx.AMXGlassfish ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ClassCodeBaseHandler ;

import com.sun.corba.ee.impl.orb.ORBImpl ;

import com.sun.corba.ee.impl.osgi.loader.OSGIListener;
import com.sun.corba.ee.spi.misc.ORBConstants;

/** A simple factory for creating our ORB that avoids the ClassLoader
 * problems with org.omg.CORBA.ORB.init, which must load the ORB impl class.
 * The usual OSGi configuration prevents this, so we just directly use a
 * static factory method here.  Note that this also assumes that the created
 * ORB should be suitable for running inside GlassFish v3.
 */
public class ORBFactory {   
    private ORBFactory() {} 

    public static ORB create( String[] args, Properties props, boolean useOSGi ) {
        ORB result = create() ;
        initialize( result, args, props, useOSGi ) ;
        return result ;
    }

    /** Create but do not initialize an ORB instance.
     * @return The newly created uninitialized ORB.
     */
    public static ORB create() {
        ORB result = new ORBImpl() ;
        return result ;
    }

    /** Complete the initialization of the ORB.  
     * useOSGi if true will cause an ORB initialization
     * suitable for use in GlassFish v3.
     * @param orb The orb to initialize.
     * @param args Usual args passed to an ORB.init() call.
     * @param props Usual props passed to an ORB.init() call.
     * @param useOSGi true if the ORB is running in GFv3 or later (generally means an OSGI environment).
     */
    @SuppressWarnings("static-access")
    public static void initialize( ORB orb, String[] args, Properties props, boolean useOSGi ) {
        // Always disable ORBD if coming through the ORBFactory.
        // Anyone that wants ORBD must use ORB.init as usual.
        // Actually we assume that we are running in GFv3 if this method is called,
        // regardless of whether OSGi is used or not.
        props.setProperty( ORBConstants.DISABLE_ORBD_INIT_PROPERTY, "true" ) ;

        if (useOSGi) {
            orb.classNameResolver(
                orb.makeCompositeClassNameResolver(
                    OSGIListener.classNameResolver(),
                    orb.defaultClassNameResolver()
                ) );

            ClassCodeBaseHandler ccbh = OSGIListener.classCodeBaseHandler() ;
            orb.classCodeBaseHandler( ccbh ) ;
        }

        orb.setRootParentObjectName( AMXGlassfish.DEFAULT.serverMonForDAS() ) ;

        orb.setParameters( args, props ) ;
    }
}

// End of file.
