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

package com.sun.corba.se.spi.ior;

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.se.spi.orb.ORBVersion ;
import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.protocol.CorbaServerRequestDispatcher ;

import com.sun.jmxa.ManagedData ;
import com.sun.jmxa.ManagedAttribute ;
import com.sun.jmxa.Description ;

/** An ObjectKeyTemplate represents the part of an Object Key
 * that corresponds to the object adapter used to create an
 * object reference.  The template is shared between many
 * object references.
 */
@ManagedData
@Description( "The template used to represent all IORs created by the same Object adapter" )
public interface ObjectKeyTemplate extends Writeable
{
    @ManagedAttribute
    @Description( "The ORB version that created this template" )
    public ORBVersion getORBVersion() ;

    /** An ID used to determine how to perform operations on this
     * ObjectKeyTemplate.  This id determines how to process requests
     * on this object reference, and what object adapter type to use.
     */
    @ManagedAttribute
    @Description( "The subcontract ID which identifies a particular type-independent " 
        + " implementation of an IOR" )
    public int getSubcontractId();

    /** Return the server ID for this template.
    * For CORBA 3.0, this should be a String, but it is currently
    * an int in the object key template.
    */
    @ManagedAttribute
    @Description( "The ID of the server that handles requests to this IOR" )
    public int getServerId() ;

    /** Return the ORB ID for this template.
    */
    @ManagedAttribute
    @Description( "the ORB ID that created this IOR" )
    public String getORBId() ;

    /** Return the object adapter ID for this template.
    */
    @ManagedAttribute
    @Description( "The ObjectAdapterId that identifies the ObjectAdapter that created this IOR" )
    public ObjectAdapterId getObjectAdapterId() ;

    /** Compute an adapter ID for this template than includes
    * all of the template information.
    * This value is cached to avoid the expense of recomputing
    * it.
    */
    public byte[] getAdapterId() ;

    public void write(ObjectId objectId, OutputStream os);
    
    public CorbaServerRequestDispatcher getServerRequestDispatcher( ORB orb, ObjectId id ) ;
}
