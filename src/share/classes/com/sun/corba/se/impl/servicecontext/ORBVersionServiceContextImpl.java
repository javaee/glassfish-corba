/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.servicecontext;

import org.omg.CORBA.SystemException;
import org.omg.CORBA_2_3.portable.InputStream ;
import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.orb.ORBVersion ;
import com.sun.corba.se.spi.orb.ORBVersionFactory ;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.servicecontext.ServiceContextBase ;
import com.sun.corba.se.spi.servicecontext.ORBVersionServiceContext ;

import com.sun.corba.se.impl.orbutil.ORBConstants ;

public class ORBVersionServiceContextImpl extends ServiceContextBase
    implements ORBVersionServiceContext 
{
    // current ORB Version
    private ORBVersion version = ORBVersionFactory.getORBVersion() ;

    public static final ORBVersionServiceContext singleton =
	new ORBVersionServiceContextImpl() ;

    public ORBVersionServiceContextImpl( )
    {
        version = ORBVersionFactory.getORBVersion() ;
    }

    public ORBVersionServiceContextImpl( ORBVersion ver )
    {
	this.version = ver ;
    }

    public ORBVersionServiceContextImpl(InputStream is, GIOPVersion gv)
    {
	super(is) ;
	// pay particular attention to where the version is being read from!
	// is contains an encapsulation, ServiceContext reads off the
	// encapsulation and leaves the pointer in the variable "in",
	// which points to the long value.

	version = ORBVersionFactory.create( in ) ;
    }

    public int getId() 
    { 
	return SERVICE_CONTEXT_ID ; 
    }

    public void writeData( OutputStream os ) throws SystemException
    {
	version.write( os ) ;
    }

    public ORBVersion getVersion() 
    {
	return version ;
    }

    public String toString() 
    {
	return "ORBVersionServiceContextImpl[ version=" + version + " ]" ;
    }
}
