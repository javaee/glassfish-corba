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

package pi.ort;

import java.io.*;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import org.omg.IOP.CodecPackage.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TAG_INTERNET_IOP;

/**
 * Thoroughly tests IORInterceptor support.
 */
public class SampleIORInterceptor 
    extends LocalObject 
    implements IORInterceptor_3_0 
{

    // The name for this interceptor
    private String name;

    // Destination for all output.  This is set in the constructor, which
    // is called by ServerTestInitializer.
    private PrintStream out;

    public SampleIORInterceptor( String name, PrintStream out ) {
        this.name = name;
        this.out = out;
        out.println( "    - IORInterceptor " + name + " created." );
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void establish_components (IORInfo info) {
    }

    /**
     * Check ORBId and ORBServerId are propogated correctly.
     */
    public void components_established( IORInfo info )
    {
        com.sun.corba.ee.impl.interceptors.IORInfoImpl iorInfoImpl =
            (com.sun.corba.ee.impl.interceptors.IORInfoImpl) info;
        ObjectReferenceTemplate ort = iorInfoImpl.adapter_template();
        if( !ort.orb_id().equals( Constants.ORB_ID )  && 
            !ort.orb_id().equals(com.sun.corba.ee.impl.ior.ObjectKeyTemplateBase.JIDL_ORB_ID)) {
            System.err.println( 
                "ORBId is not passed to components_established correctly..");
            System.exit( -1 );
        }

        if( !ort.server_id().equals( Constants.ORB_SERVER_ID ) ) {
            System.err.println( 
                "ORBServerId is not passed to components_established correctly..");
            System.exit( -1 );
        }
    }

    public void adapter_state_changed( ObjectReferenceTemplate[] templates, 
        short state )
    {
        ORTStateChangeEvaluator.getInstance( ).registerAdapterStateChange( 
            templates, state );
    }

    public void adapter_manager_state_changed( int managedId, short state )
    {
        ORTStateChangeEvaluator.getInstance().registerAdapterManagerStateChange(
            managedId, state );
    }
}


