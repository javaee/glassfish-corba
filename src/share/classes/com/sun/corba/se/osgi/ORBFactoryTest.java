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
package com.sun.corba.se.osgi ;

import org.osgi.framework.BundleActivator ;
import org.osgi.framework.ServiceListener ;
import org.osgi.framework.BundleContext ;
import org.osgi.framework.ServiceEvent ;

import java.util.Properties ;

import com.sun.corba.se.spi.orb.ORBFactory ;
import com.sun.corba.se.spi.orb.ORB ;

public class ORBFactoryTest implements BundleActivator, ServiceListener {
    private ORB orb ;

    public void start( BundleContext context ) {
        System.out.println( "Starting ORBFactoryTest" ) ;
        context.addServiceListener( this ) ;
        try {
            String[] args = {} ;
            orb = ORBFactory.create( args, new Properties() ) ;
            System.out.println( "ORB successfully created" ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
        }
    }

    public void stop( BundleContext context ) {
        System.out.println( "Stopping ORBFactoryTest" ) ;
        orb.destroy() ;
        System.out.println( "ORB destroyed" ) ;
        context.removeServiceListener( this ) ;
    }

    /**
     * Implements ServiceListener.serviceChangedi().
     * Prints the details of any service event from the framework.
     * @param event the fired service event.
    **/
    public void serviceChanged(ServiceEvent event) {
        String[] objectClass = (String[])
            event.getServiceReference().getProperty("objectClass");

        if (event.getType() == ServiceEvent.REGISTERED) {
            System.out.println(
                "Ex1: Service of type " + objectClass[0] + " registered.");
        } else if (event.getType() == ServiceEvent.UNREGISTERING) {
            System.out.println(
                "Ex1: Service of type " + objectClass[0] + " unregistered.");
        } else if (event.getType() == ServiceEvent.MODIFIED) {
            System.out.println(
                "Ex1: Service of type " + objectClass[0] + " modified.");
        }
    }
}
