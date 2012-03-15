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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package sun.rmi.rmic;

import sun.tools.java.Identifier;

public interface RMIConstants extends sun.rmi.rmic.Constants {

    /*
     * identifiers for RMI classes referenced by rmic
     */
    public static final Identifier idRemoteObject =
        Identifier.lookup("java.rmi.server.RemoteObject");
    public static final Identifier idRemoteStub =
        Identifier.lookup("java.rmi.server.RemoteStub");
    public static final Identifier idRemoteRef =
        Identifier.lookup("java.rmi.server.RemoteRef");
    public static final Identifier idOperation =
        Identifier.lookup("java.rmi.server.Operation");
    public static final Identifier idSkeleton =
        Identifier.lookup("java.rmi.server.Skeleton");
    public static final Identifier idSkeletonMismatchException =
        Identifier.lookup("java.rmi.server.SkeletonMismatchException");
    public static final Identifier idRemoteCall =
        Identifier.lookup("java.rmi.server.RemoteCall");
    public static final Identifier idMarshalException =
        Identifier.lookup("java.rmi.MarshalException");
    public static final Identifier idUnmarshalException =
        Identifier.lookup("java.rmi.UnmarshalException");
    public static final Identifier idUnexpectedException =
        Identifier.lookup("java.rmi.UnexpectedException");

    /*
     * stub protocol versions
     */
    public static final int STUB_VERSION_1_1  = 1;
    public static final int STUB_VERSION_FAT  = 2;
    public static final int STUB_VERSION_1_2  = 3;

    /** serialVersionUID for all stubs that can use 1.2 protocol */
    public static final long STUB_SERIAL_VERSION_UID = 2;

    /** version number used to seed interface hash computation */
    public static final int INTERFACE_HASH_STUB_VERSION = 1;
}
