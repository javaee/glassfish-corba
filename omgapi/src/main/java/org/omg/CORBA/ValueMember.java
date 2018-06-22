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

/*
 * File: ./org/omg/CORBA/ValueMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package org.omg.CORBA;

/**
 * A description in the Interface Repository of
 * a member of a <code>value</code> object.
 */
// @SuppressWarnings({"serial"})
public final class ValueMember implements org.omg.CORBA.portable.IDLEntity {

    //  instance variables

    /**
     * The name of the <code>value</code> member described by this
     * <code>ValueMember</code> object.
     * @serial
     */
    public String name;

    /**
     * The repository ID of the <code>value</code> member described by
     * this <code>ValueMember</code> object;
     * @serial
     */
    public String id;

    /**
     * The repository ID of the <code>value</code> in which this member
     * is defined.
     * @serial
     */
    public String defined_in;

    /**
     * The version of the <code>value</code> in which this member is defined.
     * @serial
     */
    public String version;

    /**
     * The type of of this <code>value</code> member.
     * @serial
     */
    public org.omg.CORBA.TypeCode type;

    /**
     * The typedef that represents the IDL type of the <code>value</code> 
     * member described by this <code>ValueMember</code> object.
     * @serial
     */
    public org.omg.CORBA.IDLType type_def;

    /**
     * The type of access (public, private) for the <code>value</code> 
     * member described by this <code>ValueMember</code> object.
     * @serial
     */
    public short access;
    //  constructors

    /**
     * Constructs a default <code>ValueMember</code> object.
     */
    public ValueMember() { }

    /**
     * Constructs a <code>ValueMember</code> object initialized with
     * the given values.
     *
     *@param __name The name of the <code>value</code> member described by this
     * <code>ValueMember</code> object.
     *@param __id The repository ID of the <code>value</code> member described by
     * this <code>ValueMember</code> object;
     *@param __defined_in The repository ID of the <code>value</code> in which this member
     * is defined.
     *@param __version The version of the <code>value</code> in which this member is defined.
     *@param __type The type of of this <code>value</code> member.
     *@param __type_def The typedef that represents the IDL type of the <code>value</code> 
     * member described by this <code>ValueMember</code> object.
     *@param __access The type of access (public, private) for the <code>value</code> 
     * member described by this <code>ValueMember</code> object.
     */
    public ValueMember(String __name, String __id, String __defined_in, String __version, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def, short __access) {
        name = __name;
        id = __id;
        defined_in = __defined_in;
        version = __version;
        type = __type;
        type_def = __type_def;
        access = __access;
    }
}
