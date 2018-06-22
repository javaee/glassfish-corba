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

//
// Created       : 2002 Jul 19 (Fri) 13:42:23 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:06:21 by Harold Carr.
//

package corba.iorintsockfact;

import org.omg.CosNaming.*;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.legacy.interceptor.ORBInitInfoExt ;

/**
 * @author Harold Carr
 */
public class ServerORBInitializer
    extends
        org.omg.CORBA.LocalObject
    implements
        ORBInitializer
{
    public static final String baseMsg = ServerORBInitializer.class.getName();

    public void pre_init(ORBInitInfo info) { }

    public void post_init(ORBInitInfo info)
    {
        ORB orb = ((ORBInitInfoExt)info).getORB() ;
        try {
            info.add_ior_interceptor(new IORInterceptor(orb));
        } catch (DuplicateName ex) {
            System.out.println(baseMsg + ex);
            System.exit(-1);
        }
    }
}

// End of file.
