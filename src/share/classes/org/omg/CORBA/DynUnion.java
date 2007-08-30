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
 * The <code>DynUnion</code> interface represents a <code>DynAny</code> object
 * that is associated with an IDL union.
 * Union values can be traversed using the operations defined in <code>DynAny</code>.
 * The first component in the union corresponds to the discriminator;
 * the second corresponds to the actual value of the union.
 * Calling the method <code>next()</code> twice allows you to access both components.
 * @deprecated Use the new <a href="../DynamicAny/DynUnion.html">DynUnion</a> instead
 */

// @Deprecated
public interface DynUnion extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{
    /**
     * Determines whether the discriminator associated with this union has been assigned
     * a valid default value.
     * @return <code>true</code> if the discriminator has a default value;
     * <code>false</code> otherwise
     */
    public boolean set_as_default();

    /**
    * Determines whether the discriminator associated with this union gets assigned
    * a valid default value.
    * @param arg <code>true</code> if the discriminator gets assigned a default value
    */
    public void set_as_default(boolean arg);

    /**
    * Returns a DynAny object reference that must be narrowed to the type
    * of the discriminator in order to insert/get the discriminator value.
    * @return a <code>DynAny</code> object reference representing the discriminator value
    */
    // @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.DynAny discriminator();

    /**
    * Returns the TCKind object associated with the discriminator of this union.
    * @return the <code>TCKind</code> object associated with the discriminator of this union
    */
    public org.omg.CORBA.TCKind discriminator_kind();

    /**
    * Returns a DynAny object reference that is used in order to insert/get
    * a member of this union.
    * @return the <code>DynAny</code> object representing a member of this union
    */
    // @SuppressWarnings({"deprecation"})
    public org.omg.CORBA.DynAny member();

    /**
    * Allows for the inspection of the name of this union member
    * without checking the value of the discriminator.
    * @return the name of this union member
    */
    public String member_name();

    /**
    * Allows for the assignment of the name of this union member.
    * @param arg the new name of this union member
    */
    public void member_name(String arg);

    /**
    * Returns the TCKind associated with the member of this union.
    * @return the <code>TCKind</code> object associated with the member of this union
    */
    public org.omg.CORBA.TCKind member_kind();
}
