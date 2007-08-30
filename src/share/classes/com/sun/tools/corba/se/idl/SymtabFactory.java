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
/*
 * COMPONENT_NAME: idl.parser
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.tools.corba.se.idl;

// NOTES:

/**
 * Each entry must have three ways in which it can be instantiated:
 * <ul>
 * <li>with no parameters;
 * <li>cloned from a copy of itself;
 * <li>the normal-use instantiation (usually with 2 parameters:  the container and the id of the container).
 * </ul>
 **/
public interface SymtabFactory
{
  AttributeEntry attributeEntry ();
  AttributeEntry attributeEntry (InterfaceEntry container, IDLID id);

  ConstEntry constEntry ();
  ConstEntry constEntry (SymtabEntry container, IDLID id);

  NativeEntry nativeEntry ();
  NativeEntry nativeEntry (SymtabEntry container, IDLID id);

  EnumEntry enumEntry ();
  EnumEntry enumEntry (SymtabEntry container, IDLID id);

  ExceptionEntry exceptionEntry ();
  ExceptionEntry exceptionEntry (SymtabEntry container, IDLID id);

  ForwardEntry forwardEntry ();
  ForwardEntry forwardEntry (ModuleEntry container, IDLID id);

  ForwardValueEntry forwardValueEntry ();
  ForwardValueEntry forwardValueEntry (ModuleEntry container, IDLID id);

  IncludeEntry includeEntry ();
  IncludeEntry includeEntry (SymtabEntry container);

  InterfaceEntry interfaceEntry ();
  InterfaceEntry interfaceEntry (ModuleEntry container, IDLID id);

  ValueEntry valueEntry ();
  ValueEntry valueEntry (ModuleEntry container, IDLID id);

  ValueBoxEntry valueBoxEntry ();
  ValueBoxEntry valueBoxEntry (ModuleEntry container, IDLID id);

  MethodEntry methodEntry ();
  MethodEntry methodEntry (InterfaceEntry container, IDLID id);

  ModuleEntry moduleEntry ();
  ModuleEntry moduleEntry (ModuleEntry container, IDLID id);

  ParameterEntry parameterEntry ();
  ParameterEntry parameterEntry (MethodEntry container, IDLID id);

  PragmaEntry pragmaEntry ();
  PragmaEntry pragmaEntry (SymtabEntry container);

  PrimitiveEntry primitiveEntry ();
  /** name can be, but is not limited to, the primitive idl type names:
      char, octet, short, long, etc.  The reason it is not limited to
      these is that, as an extender, you may wish to override these names.
      For instance, when generating Java code, octet translates to byte,
      so there is an entry in Compile.overrideNames:  <"octet", "byte">
      and a PrimitiveEntry in the symbol table for "byte". */
  PrimitiveEntry primitiveEntry (String name);

  SequenceEntry sequenceEntry ();
  SequenceEntry sequenceEntry (SymtabEntry container, IDLID id);

  StringEntry stringEntry ();

  StructEntry structEntry ();
  StructEntry structEntry (SymtabEntry container, IDLID id);

  TypedefEntry typedefEntry ();
  TypedefEntry typedefEntry (SymtabEntry container, IDLID id);

  UnionEntry unionEntry ();
  UnionEntry unionEntry (SymtabEntry container, IDLID id);
} // interface SymtabFactory
