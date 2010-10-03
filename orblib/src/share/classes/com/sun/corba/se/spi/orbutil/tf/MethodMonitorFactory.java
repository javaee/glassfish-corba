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

package com.sun.corba.se.spi.orbutil.tf;

import java.util.Collection;

/** Since each MethodMonitor generally needs a reference to the class it is
 * monitoring, we actually work with MethodMonitorFactory instances instead
 * of simply using MethodMonitor.
 *
 * @author ken
 */
public interface MethodMonitorFactory {
    /** Return an instance of a MethodMonitor suitable for use in the given
     * class cls, according to the currently registered MethodMonitorFactory 
     * instances in the MethodMonitorRegistry.
     * 
     * @param cls The class for which we need the MethodMonitor.
     * @return The MethodMonitor for cls.
     */
    MethodMonitor create( Class<?> cls ) ;

    /** Returns the contents of this method monitor factory.  If it is a composite
     * method monitor factory, all the component MethoMonitorFactory instances are 
     * returned.  If it is a single MethodMonitorFactory, it just returns itself.
     */
    Collection<MethodMonitorFactory> contents() ;

    String name() ;
}
