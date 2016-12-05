/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.corba.ee.impl.encoding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.org.omg.SendingContext.CodeBase;

/**
 * @author jwells
 *
 */
public class EncapsInputStreamFactory {
	public static EncapsInputStream newEncapsInputStream(final EncapsInputStream eis) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(eis);
					}
					
				});
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] buf,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, buf, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size,
			final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb,
			final ByteBuffer byteBuffer,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, byteBuffer, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static EncapsInputStream newEncapsInputStream(final org.omg.CORBA.ORB orb, 
            final byte[] data, 
            final int size, 
            final GIOPVersion version, 
            final CodeBase codeBase) {
		return AccessController.
				doPrivileged(new PrivilegedAction<EncapsInputStream>() {

					@Override
					public EncapsInputStream run() {
						return new EncapsInputStream(orb, data, size, version, codeBase);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
            final byte[] data,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, data, size, byteOrder, version);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
			final byte[] data,
			final int size) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, data, size);
					}
					
				});
		
	}
	
	public static TypeCodeInputStream newTypeCodeInputStream(final org.omg.CORBA.ORB orb,
            final ByteBuffer byteBuffer,
            final int size,
            final ByteOrder byteOrder,
            final GIOPVersion version) {
		return AccessController.
				doPrivileged(new PrivilegedAction<TypeCodeInputStream>() {

					@Override
					public TypeCodeInputStream run() {
						return new TypeCodeInputStream(orb, byteBuffer, size, byteOrder, version);
					}
					
				});
		
	}
}
