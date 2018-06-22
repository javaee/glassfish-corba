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

package com.sun.corba.ee.spi.ior;

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.IncludeSubclass ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;

/** TaggedProfile represents a tagged profile in an IOR.
 * A profile contains all of the information necessary for an invocation.
 * It contains one or more endpoints that may be used for an invocation.
 * A TaggedProfile conceptually has three parts: A TaggedProfileTemplate,
 * an ObjectKeyTemplate, and an ObjectId.  
 */
@ManagedData
@Description( "A TaggedProfile contained in an IOR" )
@IncludeSubclass( { com.sun.corba.ee.spi.ior.iiop.IIOPProfile.class } ) 
public interface TaggedProfile extends Identifiable, MakeImmutable
{
    @ManagedAttribute
    @Description( "Template for this TaggedProfile" ) 
    TaggedProfileTemplate getTaggedProfileTemplate() ;

    @ManagedAttribute
    @Description( "The ObjectId used in the IIOPProfile in this IOR" )
    ObjectId getObjectId() ;

    @ManagedAttribute
    @Description( "The template for the ObjectKey in the IIOPProfile in this IOR" ) 
    ObjectKeyTemplate getObjectKeyTemplate() ;

    ObjectKey getObjectKey() ;

    /** Return true is prof is equivalent to this TaggedProfile.
     * This means that this and prof are indistinguishable for 
     * the purposes of remote invocation.  Typically this means that
     * the profile data is identical and both profiles contain exactly
     * the same components (if components are applicable).
     * isEquivalent( prof ) should imply that getObjectId().equals( 
     * prof.getObjectId() ) is true, and so is
     * getObjectKeyTemplate().equals( prof.getObjectKeyTemplate() ).
     */
    boolean isEquivalent( TaggedProfile prof ) ;

    /** Return the TaggedProfile as a CDR encapsulation in the standard
     * format.  This is required for Portable interceptors.
     */
    org.omg.IOP.TaggedProfile getIOPProfile();

    /** Return true if this TaggedProfile was created in orb.  
     *  Caches the result.
     */
    boolean isLocal() ;
}
