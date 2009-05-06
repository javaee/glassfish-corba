/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.osgi.loader ;

import com.sun.corba.se.spi.orbutil.generic.UnaryFunction;
import com.sun.corba.se.spi.orbutil.ORBConstants;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle ;
import org.osgi.framework.BundleActivator ;
import org.osgi.framework.SynchronousBundleListener ;
import org.osgi.framework.BundleContext ;
import org.osgi.framework.BundleEvent ;

/** OSGi class that monitors which bundles provide classes that the ORB
 * needs to instantiate for initialization.  This class is part of the
 * glassfish-corba-osgi bundle.  Note that the glassfish-corba-osgi module
 * may itself be an ORB class provider.
 * <p>
 * Note that OSGIListener must be a Bundle-Activator in the glassfish-corba-osgi
 * bundle.
 * <p>
 * Any bundle that provides ORB classes to the ORB initialization code must
 * declare all such classes in a comma-separated list in the bundle manifest
 * with the keywork ORB-Class-Provider.
 *
 * @author ken
 */
public class OSGIListener implements BundleActivator, SynchronousBundleListener {
    private static final String ORB_PROVIDER_KEY = "ORB-Class-Provider" ;

    private static Map<String,Bundle> classNameMap =
        new HashMap<String,Bundle>() ;

    private static final boolean DEBUG = Boolean.getBoolean( ORBConstants.DEBUG_OSGI_LISTENER ) ;

    private static synchronized void mapContents() {
        if (DEBUG) {
            msg( "Contents of classNameMap:" ) ;

            for (Map.Entry<String,Bundle> entry : classNameMap.entrySet()) {
                msg( entry.getKey() + "=>" + entry.getValue().getSymbolicName() ) ;
            }
        }
    }

    private static void msg( String arg ) {
        ClassLoader cl = OSGIListener.class.getClassLoader() ;
        System.out.println( "OSGIListener(" + cl + "): " + arg ) ;
    }

    private static String getBundleEventType( int type ) {
        if (type == BundleEvent.INSTALLED) 
            return "INSTALLED" ;
        else if (type == BundleEvent.LAZY_ACTIVATION)
            return "LAZY_ACTIVATION" ;
        else if (type == BundleEvent.RESOLVED)
            return "RESOLVED" ;
        else if (type == BundleEvent.STARTED)
            return "STARTED" ;
        else if (type == BundleEvent.STARTING)
            return "STARTING" ;
        else if (type == BundleEvent.STOPPED)
            return "STOPPED" ;
        else if (type == BundleEvent.STOPPING)
            return "STOPPING" ;
        else if (type == BundleEvent.UNINSTALLED)
            return "UNINSTALLED" ;
        else if (type == BundleEvent.UNRESOLVED)
            return "UNRESOLVED" ;
        else if (type == BundleEvent.UPDATED)
            return "UPDATED" ;
        else 
            return "ILLEGAL-EVENT-TYPE" ;
    }

    private static class ClassNameResolverImpl implements
        UnaryFunction<String,Class<?>> {

        public Class<?> evaluate(String arg) {
            if (DEBUG) {
                msg( "Looking for class \"" + arg + "\"" ) ;
            }

            Bundle bundle = getBundleForClass( arg ) ;
            if (bundle == null) {
                if (DEBUG) {
                    msg( "Class not found" ) ;
                }

                return null ;
            } else {
                if (DEBUG) {
                    msg( "Found class in bundle " + bundle ) ;
                }
            }

            try {
                return bundle.loadClass(arg);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException( ex ) ;
            }
        }

        public String toString() {
            return "OSGiClassNameResolver" ;
        }
    }

    private static UnaryFunction<String,Class<?>> classNameResolver =
        new ClassNameResolverImpl() ;

    public static UnaryFunction<String,Class<?>> classNameResolver() {
        return classNameResolver ;
    }

    private static synchronized void insertORBProviders( Bundle bundle ) {
        Dictionary dict = bundle.getHeaders() ;
        String name = bundle.getSymbolicName() ;
        if (dict != null) {
            String orbProvider = (String)dict.get( ORB_PROVIDER_KEY ) ;
            if (orbProvider != null) {
                for (String className : orbProvider.split(",") ) {
                    if (DEBUG) {
                        msg( "Inserting: Bundle " + name + " provides ORB class \"" + className + "\"" ) ;
                    }
                    classNameMap.put( className, bundle ) ;
                }
            }
        }
    }

    private static synchronized void removeORBProviders( Bundle bundle ) {
        Dictionary dict = bundle.getHeaders() ;
        String name = bundle.getSymbolicName() ;
        if (dict != null) {
            String orbProvider = (String)dict.get( ORB_PROVIDER_KEY ) ;
            if (orbProvider != null) {
                for (String className : orbProvider.split(",") ) {
                    classNameMap.remove( className ) ;
                    if (DEBUG) {
                        msg( "Removing: Bundle " + name + " provides ORB class \"" + className + "\"" ) ;
                    }
                }
            }
        }
    }

    private static synchronized Bundle getBundleForClass( String className ) {
        return classNameMap.get( className ) ;
    }

    public void start( BundleContext context ) {
        context.addBundleListener(this);
        // Probe all existing bundles for ORB providers
        if (DEBUG) {
            msg( "Starting up: probing all existing bundles for providers" ) ;
        }
        for (Bundle bundle : context.getBundles()) {
            insertORBProviders( bundle ) ;
        }
        mapContents() ;
    }

    public void stop( BundleContext context ) {
        Bundle myBundle = context.getBundle() ;
        removeORBProviders( myBundle ) ;
        mapContents() ;
    }

    public void bundleChanged(BundleEvent event) {
        int type = event.getType() ;
        Bundle bundle = event.getBundle() ;
        String name = bundle.getSymbolicName() ;

        if (DEBUG) {
            msg( "Event type " + getBundleEventType( type ) 
                + " on bundle " + name ) ;
        }

        if (type == Bundle.INSTALLED) {
            insertORBProviders( bundle ) ;
        } else if (type == Bundle.UNINSTALLED) {
            removeORBProviders( bundle ) ;
        }
        mapContents() ;
    }
}
