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

package com.sun.corba.se.impl.encoding;

import com.sun.corba.se.spi.logging.ORBUtilSystemException;
import com.sun.corba.se.spi.misc.ORBConstants;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.orb.ORB;

import org.omg.CORBA.INTERNAL;

/**
 * Creates read/write buffer managers to handle over/under flow
 * in CDR*putStream.
 */

public class BufferManagerFactory
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public static final int GROW    = 0;
    public static final int COLLECT = 1;
    public static final int STREAM  = 2;

    // The next two methods allow creation of BufferManagers based on GIOP version.
    // We may want more criteria to be involved in this decision.
    // These are only used for sending messages (so could be fragmenting)
    public static BufferManagerRead newBufferManagerRead(
            GIOPVersion version, byte encodingVersion, ORB orb) {

        // REVISIT - On the reading side, shouldn't we monitor the incoming
        // fragments on a given connection to determine what fragment size
        // they're using, then use that ourselves?

	if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
	    return new BufferManagerReadGrow();
	}

        switch (version.intValue()) 
        {
            case GIOPVersion.VERSION_1_0:
                return new BufferManagerReadGrow();
            case GIOPVersion.VERSION_1_1:
            case GIOPVersion.VERSION_1_2:
                // The stream reader can handle fragmented and
                // non fragmented messages
                return new BufferManagerReadStream(orb);
            default:
                // REVISIT - what is appropriate?
                throw new INTERNAL("Unknown GIOP version: "
                                   + version);
        }
    }

    public static BufferManagerRead newBufferManagerRead(
            int strategy, byte encodingVersion, ORB orb) {

	if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
	    if (strategy != BufferManagerFactory.GROW) {
		throw wrapper.invalidBuffMgrStrategy("newBufferManagerRead");
	    }
	    return new BufferManagerReadGrow();
	}
        switch (strategy) {
            case BufferManagerFactory.GROW:
                return new BufferManagerReadGrow();
            case BufferManagerFactory.COLLECT:
                throw new INTERNAL("Collect strategy invalid for reading");
            case BufferManagerFactory.STREAM:
                return new BufferManagerReadStream(orb);
            default:
                throw new INTERNAL("Unknown buffer manager read strategy: "
                                   + strategy);
        }
    }

    public static BufferManagerWrite newBufferManagerWrite(
            int strategy, byte encodingVersion,	ORB orb) {
	if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
	    if (strategy != BufferManagerFactory.GROW) {
		throw wrapper.invalidBuffMgrStrategy("newBufferManagerWrite");
	    }
	    return new BufferManagerWriteGrow(orb);
	}
        switch (strategy) {
            case BufferManagerFactory.GROW:
                return new BufferManagerWriteGrow(orb);
            case BufferManagerFactory.COLLECT:
                return new BufferManagerWriteCollect(orb);
            case BufferManagerFactory.STREAM:
                return new BufferManagerWriteStream(orb);
            default:
                throw new INTERNAL("Unknown buffer manager write strategy: "
                                   + strategy);
        }
    }

    public static BufferManagerWrite newBufferManagerWrite(
        GIOPVersion version, byte encodingVersion, ORB orb) {
	if (encodingVersion != ORBConstants.CDR_ENC_VERSION) {
	    return new BufferManagerWriteGrow(orb);
	}
        return BufferManagerFactory.newBufferManagerWrite(
	    orb.getORBData().getGIOPBuffMgrStrategy(version),
	    encodingVersion, orb);
    }

    public static BufferManagerRead defaultBufferManagerRead(ORB orb) {
        return new BufferManagerReadGrow();
    }
}
