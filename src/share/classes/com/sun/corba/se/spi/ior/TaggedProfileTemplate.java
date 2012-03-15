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

package com.sun.corba.ee.spi.ior;

import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;

import java.util.List ;
import java.util.Iterator ;

import org.omg.CORBA_2_3.portable.OutputStream ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.Writeable ;
import com.sun.corba.ee.spi.ior.ObjectId ;
import com.sun.corba.ee.spi.ior.WriteContents ;

import com.sun.corba.ee.spi.orb.ORB ;

import org.glassfish.gmbal.ManagedData ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.InheritedAttribute ;
import org.glassfish.gmbal.IncludeSubclass ;

/** Base template for creating TaggedProfiles.  A TaggedProfile will often contain
* tagged components.  A template that does not contain components acts like 
* an empty immutable list.
*
* @author Ken Cavanaugh
*/
@ManagedData
@Description( "A template for creating a TaggedProfile" ) 
@IncludeSubclass( { com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate.class } )
public interface TaggedProfileTemplate extends List<TaggedComponent>, 
    Identifiable, WriteContents, MakeImmutable
{    
    @ManagedAttribute
    @Description( "The list of TaggedComponents in this TaggedProfileTemplate" ) 
    public Iterator<TaggedComponent> getTaggedComponents() ;

    /** Return an iterator that iterates over tagged components with
    * identifier id.  It is not possible to modify the list through this
    * iterator.  
    */
    public Iterator<TaggedComponent> iteratorById( int id ) ;

    public <T extends TaggedComponent> Iterator<T> iteratorById( int id, 
        Class<T> cls )  ;

    /** Create a TaggedProfile from this template.
    */
    TaggedProfile create( ObjectKeyTemplate oktemp, ObjectId id ) ;

    /** Write the profile create( oktemp, id ) to the OutputStream os.
    */
    void write( ObjectKeyTemplate oktemp, ObjectId id, OutputStream os) ;

    /** Return true if temp is equivalent to this template.  Equivalence
     * means that in some sense an invocation on a profile created by this
     * template has the same results as an invocation on a profile
     * created from temp.  Equivalence may be weaker than equality.  
     */
    boolean isEquivalent( TaggedProfileTemplate temp );

    /** Return the tagged components in this profile (if any)
     * in the GIOP marshalled form, which is required for Portable
     * Interceptors.  Returns null if either the profile has no 
     * components, or if this type of profile can never contain
     * components.
     */
    org.omg.IOP.TaggedComponent[] getIOPComponents( 
        ORB orb, int id );
}
