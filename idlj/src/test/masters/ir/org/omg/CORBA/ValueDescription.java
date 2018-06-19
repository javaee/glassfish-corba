/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.omg.CORBA;

/**
* org/omg/CORBA/ValueDescription.java .
* IGNORE Generated by the IDL-to-Java compiler (portable), version "3.2"
* from idlj/src/main/java/com/sun/tools/corba/ee/idl/ir.idl
* IGNORE Sunday, January 21, 2018 1:54:24 PM EST
*/

public final class ValueDescription implements org.omg.CORBA.portable.IDLEntity
{
  public String name = null;
  public String id = null;
  public boolean is_abstract = false;
  public boolean is_custom = false;
  public byte flags = (byte)0;

  // always 0
  public String defined_in = null;
  public String version = null;
  public String supported_interfaces[] = null;
  public String abstract_base_values[] = null;
  public boolean has_safe_base = false;
  public String base_value = null;

  public ValueDescription ()
  {
  } // ctor

  public ValueDescription (String _name, String _id, boolean _is_abstract, boolean _is_custom, byte _flags, String _defined_in, String _version, String[] _supported_interfaces, String[] _abstract_base_values, boolean _has_safe_base, String _base_value)
  {
    name = _name;
    id = _id;
    is_abstract = _is_abstract;
    is_custom = _is_custom;
    flags = _flags;
    defined_in = _defined_in;
    version = _version;
    supported_interfaces = _supported_interfaces;
    abstract_base_values = _abstract_base_values;
    has_safe_base = _has_safe_base;
    base_value = _base_value;
  } // ctor

} // class ValueDescription
