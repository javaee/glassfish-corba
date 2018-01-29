package org.glassfish.rmic.classes.primitives;
/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiTestRemote extends Remote {
	public static final String JNDI_NAME = "IIOP_RmiTestRemote";
	public static final boolean A_BOOLEAN = true;
	public static final char A_CHAR = 'x';
	public static final byte A_BYTE = 0x34;
	public static final short A_SHORT = 12;
	public static final int AN_INT = 17;
	public static final long A_LONG = 1234567;
	public static final float A_FLOAT = 123.5f;
	public static final double A_DOUBLE = 123.567;

   	void test_ping() throws RemoteException;

   	int test_int(int x) throws RemoteException;
}
