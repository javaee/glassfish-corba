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

package pi.iorinterceptor;

import java.io.*;

import org.omg.CORBA.LocalObject;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ObjectReferenceTemplate;

/**
 * An IORInterceptor that throws a NullPointerException during its
 * establish_components.
 */
public class NPEIORInterceptor 
    extends LocalObject 
    implements IORInterceptor 
{

    // The name for this interceptor
    private String name;

    // Destination for all output, set from constructor upon construction
    // by ServerTestInitializer:
    private PrintStream out;

    // True if any instances of NPEIORInterceptor were ever registered, or
    // false if not.
    public static boolean registered = false;

    // True if establish_components was ever called on this interceptor,
    // or false if not
    public static boolean establishComponentsCalled = false;

    public NPEIORInterceptor( String name, PrintStream out ) {
	this.name = name;
	this.out = out;
	out.println( "    - NPEIORInterceptor " + name + " created." );
	registered = true;
    }

    public String name() {
	return name;
    }

    public void destroy() {
    }

    public void establish_components (IORInfo info) {
	out.println( "    - NPEIORInterceptor: establish_components called." );
	establishComponentsCalled = true;
	throw new NullPointerException();
    }

    public void components_established( IORInfo info )
    {
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates, 
	short state )
    {
    }

    public void adapter_manager_state_changed( int managerId, short state )
    {
    }
}


