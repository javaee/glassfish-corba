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
import org.osgi.framework.ServiceReference ;

import org.osgi.service.packageadmin.PackageAdmin ;
import org.osgi.service.packageadmin.ExportedPackage ;

import com.sun.corba.se.spi.orb.ClassCodeBaseHandler ;

import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

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
    private static ORBUtilSystemException wrapper =
        com.sun.corba.se.spi.orb.ORB.getStaticLogWrapperTable().get_UTIL_ORBUtil() ;

    private static final String ORB_PROVIDER_KEY = "ORB-Class-Provider" ;

    private static PackageAdmin pkgAdmin ;

    // Map from class name to Bundle, which identifies all known 
    // ORB-Class-Providers.
    private static Map<String,Bundle> classNameMap =
        new HashMap<String,Bundle>() ;

    // Map from package name to Bundle, which identifies all known
    // exported packages.
    private static Map<String,Bundle> packageNameMap = 
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
            Bundle bundle = getBundleForClass( arg ) ;
            if (bundle == null) {
                wrapper.classNotFoundInBundle( arg ) ;
                return null ;
            } else {
                wrapper.foundClassInBundle( arg, bundle ) ;
            }

            try {
                return bundle.loadClass(arg);
            } catch (ClassNotFoundException ex) {
                throw wrapper.bundleCouldNotLoadClass( ex, arg, bundle ) ;
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

    private static class ClassCodeBaseHandlerImpl implements ClassCodeBaseHandler {
        private static final String PREFIX = "osgi://" ;

        public String getCodeBase( Class cls ) {
            if (cls == null) {
                return null ;
            }

            if (pkgAdmin == null) {
                return null ;
            }

            Bundle bundle = pkgAdmin.getBundle( cls ) ;
            if (bundle == null) {
                wrapper.classNotFoundInBundle( cls ) ;
                return null ;
            }
            
            String name = bundle.getSymbolicName() ;

            Dictionary headers = bundle.getHeaders() ;
            String version = "0.0.0" ;
            if (headers != null) {
                String hver = (String)headers.get( "Bundle-Version" ) ;
                if (hver != null)
                    version = hver ;
            }

            wrapper.foundClassInBundleVersion( cls, name, version ) ;

            return PREFIX + name + "/" + version ;
        }

        public Class loadClass( String codebase, String className ) {
            if (codebase == null) {
                Bundle bundle = getBundleForClass( className ) ;
                if (bundle != null) {
                    try {
                        return bundle.loadClass( className ) ;
                    } catch (ClassNotFoundException exc) {
                        wrapper.couldNotLoadClassInBundle( exc, className, 
                            bundle.getSymbolicName() ) ;
                        return null ;
                    }
                }
            }

            if (codebase.startsWith( PREFIX )) {
                String rest = codebase.substring( PREFIX.length() ) ;
                int index = rest.indexOf( "/" ) ;
                if (index > 0) {
                    String name = rest.substring( 0, index ) ;
                    String version = rest.substring( index+1 ) ;
                    // version is a version range
                    if (pkgAdmin != null) {
                        Bundle[] defBundles = pkgAdmin.getBundles( name, version ) ;
                        if (defBundles != null) {
                            // I think this is the highest available version
                            try {
                                wrapper.foundClassInBundleVersion( className, name, version ) ;
                                return defBundles[0].loadClass( className ) ;
                            } catch (ClassNotFoundException cnfe) {
                                wrapper.classNotFoundInBundleVersion( className, name, version ) ;
                                // fall through to return null
                            }
                        }
                    }
                }
            }

            return null ;
        }
    }

    private static ClassCodeBaseHandler ccbHandler = new ClassCodeBaseHandlerImpl() ;

    public static ClassCodeBaseHandler classCodeBaseHandler() {
        return ccbHandler ;
    }

    private static synchronized void insertClasses( Bundle bundle ) {
        final Dictionary dict = bundle.getHeaders() ;
        final String name = bundle.getSymbolicName() ;
        if (dict != null) {
            final String orbProvider = (String)dict.get( ORB_PROVIDER_KEY ) ;
            if (orbProvider != null) {
                for (String className : orbProvider.split(",") ) {
                    classNameMap.put( className, bundle ) ;
                    wrapper.insertOrbProvider( className, name ) ;
                }
            }
        }

        if (pkgAdmin != null) {
            ExportedPackage[] epkgs = pkgAdmin.getExportedPackages( bundle ) ;
            for (ExportedPackage ep : epkgs) {
                packageNameMap.put( ep.getName(), bundle ) ;
            }
        }
    }

    private static synchronized void removeClasses( Bundle bundle ) {
        final Dictionary dict = bundle.getHeaders() ;
        final String name = bundle.getSymbolicName() ;
        if (dict != null) {
            final String orbProvider = (String)dict.get( ORB_PROVIDER_KEY ) ;
            if (orbProvider != null) {
                for (String className : orbProvider.split(",") ) {
                    classNameMap.remove( className ) ;
                    wrapper.removeOrbProvider( className, name ) ;
                }
            }
        }

        if (pkgAdmin != null) {
            ExportedPackage[] epkgs = pkgAdmin.getExportedPackages( bundle ) ;
            for (ExportedPackage ep : epkgs) {
                packageNameMap.remove( ep.getName() ) ;
            }
        }
    }

    private static synchronized Bundle getBundleForClass( String className ) {
        Bundle result = classNameMap.get( className ) ;
        if (result == null) {
            wrapper.classNotFoundInClassNameMap( className ) ;
            // Get package prefix
            final int index = className.lastIndexOf( "." ) ;
            if (index > 0) {
                final String packageName = className.substring( 0, index ) ;
                result = packageNameMap.get( packageName ) ;
                if (result == null) {
                    wrapper.classNotFoundInPackageNameMap( className ) ;
                } else {
                    wrapper.classFoundInPackageNameMap( className, 
                        result.getSymbolicName() ) ;
                }
            }
        } else {
            wrapper.classFoundInClassNameMap( className, 
                result.getSymbolicName() ) ;
        }

        return result ;
    }

    public void start( BundleContext context ) {
        context.addBundleListener(this);
        // Probe all existing bundles for ORB providers
        wrapper.probeBundlesForProviders() ;
        for (Bundle bundle : context.getBundles()) {
            insertClasses( bundle ) ;
        }
        mapContents() ;

        final ServiceReference sref = context.getServiceReference( 
            "org.osgi.service.packageadmin.PackageAdmin" ) ;
        pkgAdmin = (PackageAdmin)context.getService( sref ) ;
        if (pkgAdmin == null) {
            wrapper.packageAdminServiceNotAvailable() ;
        }
    }

    public void stop( BundleContext context ) {
        final Bundle myBundle = context.getBundle() ;
        removeClasses( myBundle ) ;
        mapContents() ;
    }

    public void bundleChanged(BundleEvent event) {
        final int type = event.getType() ;
        final Bundle bundle = event.getBundle() ;
        final String name = bundle.getSymbolicName() ;

        wrapper.receivedBundleEvent( getBundleEventType( type ), name ) ;

        if (type == Bundle.INSTALLED) {
            insertClasses( bundle ) ;
        } else if (type == Bundle.UNINSTALLED) {
            removeClasses( bundle ) ;
        }
        mapContents() ;
    }
}
