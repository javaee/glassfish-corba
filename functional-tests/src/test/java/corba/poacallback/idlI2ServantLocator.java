/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package corba.poacallback;


import org.omg.CORBA.ORB;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;


public class idlI2ServantLocator
    extends
        org.omg.CORBA.LocalObject
    implements
        ServantLocator
{
    public ORB orb;

    public idlI2ServantLocator(ORB orb) { this.orb = orb; }

    public Servant preinvoke(byte[] oid, POA poa, String operation,
                             CookieHolder cookieHolder)
    {
        System.out.println( "idlI2ServantLocator.preinvoke called for " +
            operation );
        System.out.flush( );
        try { 
            org.omg.CORBA.Object obj = 
                orb.resolve_initial_references( "NameService" );
            org.omg.CosNaming.NamingContextExt nctx = 
                org.omg.CosNaming.NamingContextExtHelper.narrow( obj );
            obj = nctx.resolve_str( "idlI1" );
            idlI1 i1 = idlI1Helper.narrow( obj );
            i1.o2( "Call From idlI2ServantLocator" );
        } catch ( Exception e ) {
            e.printStackTrace( );
        }
        return new idlI2Servant( );
    }

    public void postinvoke(byte[] oid, POA poa, String operation,
                           java.lang.Object cookie, Servant servant)
    {
    }
}


