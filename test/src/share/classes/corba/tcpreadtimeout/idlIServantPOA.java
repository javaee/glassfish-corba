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

//
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Sep 02 (Tue) 12:55:39 by Harold Carr.
//

package corba.tcpreadtimeout;

import com.sun.corba.se.spi.orb.ORB;

import corba.hcks.U;

class idlIServantPOA
    extends 
	idlIPOA
{
    public static final String baseMsg = idlIServantPOA.class.getName();

    public ORB orb;

    public idlIServantPOA(ORB orb)
    {
      this.orb = orb;
    }

    public String o(String arg1)
    {
	String result = arg1 + " (echo from server)";
	U.sop(result);
	return result;
    }

    public String return_after_client_gives_up(String arg1)
    {
	U.sop("return_after_client_gives_up about to wait");
	try {
	    Thread.sleep(4 * orb.getORBData().getClientTCPReadTimeout());
	} catch (InterruptedException e) {
	    RuntimeException rte = new RuntimeException();
	    rte.initCause(e);
	    throw rte;
	}
	String result = arg1 + " (echo from server)";
	U.sop(result);
	return result;
    }
}

// End of file.

