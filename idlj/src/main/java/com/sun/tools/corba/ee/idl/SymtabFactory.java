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

package com.sun.tools.corba.ee.idl;

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
  AttributeEntry attributeEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ConstEntry constEntry ();
  com.sun.tools.corba.ee.idl.ConstEntry constEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.NativeEntry nativeEntry ();
  com.sun.tools.corba.ee.idl.NativeEntry nativeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.EnumEntry enumEntry ();
  com.sun.tools.corba.ee.idl.EnumEntry enumEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry ();
  com.sun.tools.corba.ee.idl.ExceptionEntry exceptionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry ();
  com.sun.tools.corba.ee.idl.ForwardEntry forwardEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry ();
  com.sun.tools.corba.ee.idl.ForwardValueEntry forwardValueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.IncludeEntry includeEntry ();
  com.sun.tools.corba.ee.idl.IncludeEntry includeEntry (com.sun.tools.corba.ee.idl.SymtabEntry container);

  com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry ();
  com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ValueEntry valueEntry ();
  com.sun.tools.corba.ee.idl.ValueEntry valueEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry ();
  com.sun.tools.corba.ee.idl.ValueBoxEntry valueBoxEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.MethodEntry methodEntry ();
  com.sun.tools.corba.ee.idl.MethodEntry methodEntry (com.sun.tools.corba.ee.idl.InterfaceEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry ();
  com.sun.tools.corba.ee.idl.ModuleEntry moduleEntry (com.sun.tools.corba.ee.idl.ModuleEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry ();
  com.sun.tools.corba.ee.idl.ParameterEntry parameterEntry (com.sun.tools.corba.ee.idl.MethodEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry ();
  com.sun.tools.corba.ee.idl.PragmaEntry pragmaEntry (com.sun.tools.corba.ee.idl.SymtabEntry container);

  com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry ();
  /** name can be, but is not limited to, the primitive idl type names:
      char, octet, short, long, etc.  The reason it is not limited to
      these is that, as an extender, you may wish to override these names.
      For instance, when generating Java code, octet translates to byte,
      so there is an entry in Compile.overrideNames:  &lt;"octet", "byte"&gt;
      and a PrimitiveEntry in the symbol table for "byte". */
  com.sun.tools.corba.ee.idl.PrimitiveEntry primitiveEntry (String name);

  com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry ();
  com.sun.tools.corba.ee.idl.SequenceEntry sequenceEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.StringEntry stringEntry ();

  com.sun.tools.corba.ee.idl.StructEntry structEntry ();
  com.sun.tools.corba.ee.idl.StructEntry structEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry ();
  com.sun.tools.corba.ee.idl.TypedefEntry typedefEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);

  com.sun.tools.corba.ee.idl.UnionEntry unionEntry ();
  com.sun.tools.corba.ee.idl.UnionEntry unionEntry (com.sun.tools.corba.ee.idl.SymtabEntry container, com.sun.tools.corba.ee.idl.IDLID id);
} // interface SymtabFactory
