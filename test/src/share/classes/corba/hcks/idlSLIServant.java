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
//
// Created       : 2000 Nov 07 (Tue) 12:16:39 by Harold Carr.
// Last Modified : 2001 Feb 07 (Wed) 17:19:31 by Harold Carr.
//

package corba.hcks;

import org.omg.CORBA.ORB;
import org.omg.CORBA.NO_MEMORY;

class idlSLIServant
    extends 
	idlSLIPOA
{
    public static String baseMsg = idlSLIServant.class.getName();

    public ORB orb;

    public idlSLIServant ( ORB orb ) { this.orb = orb; }
    public String raiseForwardRequestInPreinvoke ( String a ) { return a; }
    public String raiseObjectNotExistInPreinvoke ( String a ) { return a; }
    public String raiseSystemExceptionInPreinvoke ( String a ) { return a; }
    public String raiseSystemExceptionInPostinvoke ( String a ) { return a; }

    public String raiseSystemInServantThenPOThenSE ( )
    {
	throw new NO_MEMORY();
    }

    public String raiseUserInServantThenSystemInPOThenSE ( )
	throws 
	    idlExampleException
    {
	C.throwUserException(baseMsg +
			     C.raiseUserInServantThenSystemInPOThenSE);
	// return for compiler
	return U.SHOULD_NOT_SEE_THIS;
    }

    public String makeColocatedCallFromServant ( )
    {
	return C.makeColocatedCallFromServant(C.idlSLI1, orb, baseMsg);
    }
    public String colocatedCallFromServant ( String a )
    {
	return C.colocatedCallFromServant(a, orb, baseMsg);
    }

    public String throwThreadDeathInReceiveRequestServiceContexts( String a )
    {
	U.sop(U.servant(a));
	return a; 
    }
    public String throwThreadDeathInPreinvoke ( String a )
    {
	U.sop(U.servant(a));
	return a; 
    }
    public String throwThreadDeathInReceiveRequest ( String a )
    {
	U.sop(U.servant(a));
	return a;
    }

    public String throwThreadDeathInServant ( String a )
    {
	U.sop(U.servant(a));
	throw new ThreadDeath();
    }
    public String throwThreadDeathInPostinvoke ( String a ) 
    {
	U.sop(U.servant(a));
	return a; 
    }
    public String throwThreadDeathInSendReply ( String a ) 
    { 
	U.sop(U.servant(a));
	return a; 
    }
    public String throwThreadDeathInServantThenSysInPostThenSysInSendException ( String a )
    {
	U.sop(U.servant(a));
	throw new ThreadDeath();
    }

    public void sPic1()
    {
	C.testAndIncrementPICSlot(true, C.sPic1,
				  SsPicInterceptor.sPic1ASlotId, 3, orb);
	C.testAndIncrementPICSlot(true, C.sPic1,
				  SsPicInterceptor.sPic1BSlotId, 3, orb);
    }
    public void sPic2()
    {
    }
}

// End of file.
