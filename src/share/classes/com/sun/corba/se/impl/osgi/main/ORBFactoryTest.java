/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.osgi.main ;

import org.osgi.framework.Bundle ;
import org.osgi.framework.BundleActivator ;
import org.osgi.framework.BundleEvent ;
import org.osgi.framework.BundleContext ;
import org.osgi.framework.SynchronousBundleListener ;
import org.osgi.framework.ServiceReference ;

import org.osgi.service.packageadmin.PackageAdmin ;
import org.osgi.service.packageadmin.ExportedPackage ;

import java.util.Properties ;

import com.sun.corba.se.spi.osgi.ORBFactory ;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orbutil.ORBConstants ;
import com.sun.corba.se.spi.oa.rfm.ReferenceFactoryManager ;

public class ORBFactoryTest implements BundleActivator, SynchronousBundleListener {
    private static PackageAdmin pkgAdmin ;
    
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

    private static void msg( String arg ) {
        System.out.println( "ORBFactoryTest: " + arg ) ;
    }

    private ORB orb = null ;

    public void start( BundleContext context ) {
        msg( "Starting ORBFactoryTest" ) ;
        context.addBundleListener( this ) ;

        try {
            ServiceReference sref = context.getServiceReference( "org.osgi.service.packageadmin.PackageAdmin" ) ;
            pkgAdmin = (PackageAdmin)context.getService( sref ) ;

            dumpInfo( context, pkgAdmin ) ;
        } catch (Exception exc) {
            msg( "Exception in getting PackageAdmin: " + exc ) ;
        }
    }

    private void dumpInfo( BundleContext context, PackageAdmin pkgAdmin ) {
        msg( "Dumping bundle information" ) ;
        for (Bundle bundle : context.getBundles()) {
            msg( "\tBundle: " + bundle.getSymbolicName() ) ;
            for (ExportedPackage ep : pkgAdmin.getExportedPackages( bundle ) ) {
                msg( "\t\tExport: " + ep.getName() ) ;
            }
        }
    }

    public void stop( BundleContext context ) {
        msg( "Stopping ORBFactoryTest" ) ;
        context.removeBundleListener( this ) ;
    }

    public void bundleChanged(BundleEvent event) {
        int type = event.getType() ;
        String name = event.getBundle().getSymbolicName() ;

        msg( "Received event type " 
            + getBundleEventType( type ) + " for bundle " + name ) ;

        // Only want to know when this bundle changes state, not the others.
        if (!name.equals( "glassfish-corba-osgi-test" )) {
            return ;
        }

        if ((type & (BundleEvent.STARTED | BundleEvent.STARTING)) != 0) {
            try {
                String[] args = {} ;
                Properties props = new Properties() ;
                props.setProperty( ORBConstants.RFM_PROPERTY, "dummy" ) ;
                orb = ORBFactory.create() ;
                ORBFactory.initialize( orb, args, props, true ) ;
                ReferenceFactoryManager rfm = 
                    (ReferenceFactoryManager)orb.resolve_initial_references(
                        ORBConstants.REFERENCE_FACTORY_MANAGER ) ;
                msg( "ORB successfully created" ) ;
            } catch (Exception exc) {
                exc.printStackTrace() ;
            }
        } else if ((type & (BundleEvent.STOPPED | BundleEvent.STOPPING)) != 0) {
            if (orb != null) {
                orb.destroy() ;
            }
            msg( "ORB destroyed" ) ;
        }
    }
}
