/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016-2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.corba.ee.impl.encoding;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;
import com.sun.corba.ee.spi.transport.Connection;

/**
 * Factory for creating various output streams with AccessController
 * 
 * @author jwells
 */
public class OutputStreamFactory {
	public static TypeCodeOutputStream newTypeCodeOutputStream(final ORB orb) {
		return AccessController.doPrivileged(new PrivilegedAction<TypeCodeOutputStream>() {

			@Override
			public TypeCodeOutputStream run() {
				return new TypeCodeOutputStream(orb);
			}
			
		});
    }
	
	public static EncapsOutputStream newEncapsOutputStream(final ORB orb,
			final GIOPVersion version) {
		return AccessController.doPrivileged(new PrivilegedAction<EncapsOutputStream>() {

			@Override
			public EncapsOutputStream run() {
				return new EncapsOutputStream(orb, version);
			}
			
		});	
	}
	
	public static EncapsOutputStream newEncapsOutputStream(final ORB orb) {
		return AccessController.doPrivileged(new PrivilegedAction<EncapsOutputStream>() {

			@Override
			public EncapsOutputStream run() {
				return new EncapsOutputStream(orb);
			}
			
		});
		
	}
	
	public static CDROutputObject newCDROutputObject(final ORB orb,
			final MessageMediator mediator,
			final GIOPVersion giopVersion,
            final Connection connection,
            final Message header,
            final byte streamFormatVersion) {
		return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

			@Override
			public CDROutputObject run() {
				return new CDROutputObject(orb, mediator, giopVersion, connection, header, streamFormatVersion);
			}
		});
	}
	
	public static CDROutputObject newCDROutputObject(final ORB orb,
			final MessageMediator messageMediator,
			final Message header,
			final byte streamFormatVersion) {
		return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

			@Override
			public CDROutputObject run() {
				return new CDROutputObject(orb, messageMediator, header, streamFormatVersion);
			}
		});
		
	}
	
	public static CDROutputObject newCDROutputObject(final ORB orb,
			final MessageMediator messageMediator,
            final Message header,
            final byte streamFormatVersion,
            final int strategy) {
		return AccessController.doPrivileged(new PrivilegedAction<CDROutputObject>() {

			@Override
			public CDROutputObject run() {
				return new CDROutputObject(orb, messageMediator, header, streamFormatVersion, strategy);
			}
		});
		
	}
}
