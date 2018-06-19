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
* org/omg/CORBA/ContainerOperations.java .
* IGNORE Generated by the IDL-to-Java compiler (portable), version "3.2"
* from idlj/src/main/java/com/sun/tools/corba/ee/idl/ir.idl
* IGNORE Sunday, January 21, 2018 1:54:22 PM EST
*/


// orbos 98-01-18: Objects By Value -- end
public interface ContainerOperations  extends org.omg.CORBA.IRObjectOperations
{

  // read interface
  org.omg.CORBA.Contained lookup (String search_name);
  org.omg.CORBA.Contained[] contents (org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited);
  org.omg.CORBA.Contained[] lookup_name (String search_name, int levels_to_search, org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited);
  org.omg.CORBA.ContainerPackage.Description[] describe_contents (org.omg.CORBA.DefinitionKind limit_type, boolean exclude_inherited, int max_returned_objs);

  // write interface
  org.omg.CORBA.ModuleDef create_module (String id, String name, String version);
  org.omg.CORBA.ConstantDef create_constant (String id, String name, String version, org.omg.CORBA.IDLType type, org.omg.CORBA.Any value);
  org.omg.CORBA.StructDef create_struct (String id, String name, String version, org.omg.CORBA.StructMember[] members);
  org.omg.CORBA.UnionDef create_union (String id, String name, String version, org.omg.CORBA.IDLType discriminator_type, org.omg.CORBA.UnionMember[] members);
  org.omg.CORBA.EnumDef create_enum (String id, String name, String version, String[] members);
  org.omg.CORBA.AliasDef create_alias (String id, String name, String version, org.omg.CORBA.IDLType original_type);
  org.omg.CORBA.ExceptionDef create_exception (String id, String name, String version, org.omg.CORBA.StructMember[] members);
  org.omg.CORBA.InterfaceDef create_interface (String id, String name, String version, boolean is_abstract, org.omg.CORBA.InterfaceDef[] base_interfaces);

  // orbos 98-01-18: Objects By Value
  org.omg.CORBA.ValueDef create_value (String id, String name, String version, boolean is_custom, boolean is_abstract, byte flags, org.omg.CORBA.ValueDef base_value, boolean has_safe_base, org.omg.CORBA.ValueDef[] abstract_base_values, org.omg.CORBA.InterfaceDef[] supported_interfaces, org.omg.CORBA.Initializer[] initializers);

  // orbos 98-01-18: Objects By Value
  org.omg.CORBA.ValueBoxDef create_value_box (String id, String name, String version, org.omg.CORBA.IDLType original_type_def);
  org.omg.CORBA.NativeDef create_native (String id, String name, String version);
} // interface ContainerOperations