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

package org.omg.CORBA;

/** 
 * An object that represents an ORB service: its <code>service_detail_type</code>
 * field contains the type of the ORB service, and its <code>service_detail</code>
 * field contains a description of the ORB service.

 *
 * @author RIP Team
 * @version 1.11 11/15/00
 */
// @SuppressWarnings({"serial"})
public final class ServiceDetail implements org.omg.CORBA.portable.IDLEntity
{
    /**
     * The type of the ORB service that this <code>ServiceDetail</code> 
     * object represents.
     */
    public int service_detail_type;

    /** 
     * The data describing the ORB service that this <code>ServiceDetail</code>
     * object represents.
     */
    public byte[] service_detail;

    /**
     * Constructs a <code>ServiceDetail</code> object with 0 for the type of
     * ORB service and an empty description.
     */
    public ServiceDetail() { }

    /**
     * Constructs a <code>ServiceDetail</code> object with the given 
     * ORB service type and the given description.
     *
     * @param service_detail_type an <code>int</code> specifying the type of 
     *                            ORB service
     * @param service_detail a <code>byte</code> array describing the ORB service
     */
    public ServiceDetail(int service_detail_type, byte[] service_detail) {
	this.service_detail_type = service_detail_type;
	this.service_detail = service_detail;
    }
}
