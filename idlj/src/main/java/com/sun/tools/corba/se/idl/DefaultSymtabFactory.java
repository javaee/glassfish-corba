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

package com.sun.tools.corba.se.idl;

// NOTES:

/**
 * This factory constructs the default symbol table entries, namely,
 * those declared within the package com.sun.tools.corba.se.idl.
 **/
public class DefaultSymtabFactory implements SymtabFactory
{
  public AttributeEntry attributeEntry ()
  {
    return new AttributeEntry ();
  } // attributeEntry

  public AttributeEntry attributeEntry (InterfaceEntry container, IDLID id)
  {
    return new AttributeEntry (container, id);
  } // attributeEntry

  public ConstEntry constEntry ()
  {
    return new ConstEntry ();
  } // constEntry

  public ConstEntry constEntry (SymtabEntry container, IDLID id)
  {
    return new ConstEntry (container, id);
  } // constEntry

  public NativeEntry nativeEntry ()
  {
    return new NativeEntry ();
  } // interfaceEntry

  public NativeEntry nativeEntry (SymtabEntry container, IDLID id)
  {
    return new NativeEntry (container, id);
  } // interfaceEntry

  public EnumEntry enumEntry ()
  {
    return new EnumEntry ();
  } // enumEntry

  public EnumEntry enumEntry (SymtabEntry container, IDLID id)
  {
    return new EnumEntry (container, id);
  } // enumEntry

  public ExceptionEntry exceptionEntry ()
  {
    return new ExceptionEntry ();
  } // exceptionEntry

  public ExceptionEntry exceptionEntry (SymtabEntry container, IDLID id)
  {
    return new ExceptionEntry (container, id);
  } // exceptionEntry

  public ForwardEntry forwardEntry ()
  {
    return new ForwardEntry ();
  } // forwardEntry

  public ForwardEntry forwardEntry (ModuleEntry container, IDLID id)
  {
    return new ForwardEntry (container, id);
  } // forwardEntry

  public ForwardValueEntry forwardValueEntry ()
  {
    return new ForwardValueEntry ();
  } // forwardValueEntry

  public ForwardValueEntry forwardValueEntry (ModuleEntry container, IDLID id)
  {
    return new ForwardValueEntry (container, id);
  } // forwardValueEntry

  public IncludeEntry includeEntry ()
  {
    return new IncludeEntry ();
  } // includeEntry

  public IncludeEntry includeEntry (SymtabEntry container)
  {
    return new IncludeEntry (container);
  } // includeEntry

  public InterfaceEntry interfaceEntry ()
  {
    return new InterfaceEntry ();
  } // interfaceEntry

  public InterfaceEntry interfaceEntry (ModuleEntry container, IDLID id)
  {
    return new InterfaceEntry (container, id);
  } // interfaceEntry

  public ValueEntry valueEntry ()
  {
    return new ValueEntry ();
  } // valueEntry

  public ValueEntry valueEntry (ModuleEntry container, IDLID id)
  {
    return new ValueEntry (container, id);
  } // valueEntry

  public ValueBoxEntry valueBoxEntry ()
  {
    return new ValueBoxEntry ();
  } // valueBoxEntry

  public ValueBoxEntry valueBoxEntry (ModuleEntry container, IDLID id)
  {
    return new ValueBoxEntry (container, id);
  } // valueBoxEntry

  public MethodEntry methodEntry ()
  {
    return new MethodEntry ();
  } // methodEntry

  public MethodEntry methodEntry (InterfaceEntry container, IDLID id)
  {
    return new MethodEntry (container, id);
  } // methodEntry

  public ModuleEntry moduleEntry ()
  {
    return new ModuleEntry ();
  } // moduleEntry

  public ModuleEntry moduleEntry (ModuleEntry container, IDLID id)
  {
    return new ModuleEntry (container, id);
  } // moduleEntry

  public ParameterEntry parameterEntry ()
  {
    return new ParameterEntry ();
  } // parameterEntry

  public ParameterEntry parameterEntry (MethodEntry container, IDLID id)
  {
    return new ParameterEntry (container, id);
  } // parameterEntry

  public PragmaEntry pragmaEntry ()
  {
    return new PragmaEntry ();
  } // pragmaEntry

  public PragmaEntry pragmaEntry (SymtabEntry container)
  {
    return new PragmaEntry (container);
  } // pragmaEntry

  public PrimitiveEntry primitiveEntry ()
  {
    return new PrimitiveEntry ();
  } // primitiveEntry

  /** "name" can be, but is not limited to, the primitive idl type names:
      'char', 'octet', 'short', 'long', etc.  The reason it is not limited
      to these is that, as an extender, you may wish to override these names.
      For instance, when generating Java code, octet translates to byte, so
      there is an entry in Compile.overrideNames:  <"octet", "byte"> and a
      PrimitiveEntry in the symbol table for "byte". */
  public PrimitiveEntry primitiveEntry (String name)
  {
    return new PrimitiveEntry (name);
  } // primitiveEntry

  public SequenceEntry sequenceEntry ()
  {
    return new SequenceEntry ();
  } // sequenceEntry

  public SequenceEntry sequenceEntry (SymtabEntry container, IDLID id)
  {
    return new SequenceEntry (container, id);
  } // sequenceEntry

  public StringEntry stringEntry ()
  {
    return new StringEntry ();
  } // stringEntry

  public StructEntry structEntry ()
  {
    return new StructEntry ();
  } // structEntry

  public StructEntry structEntry (SymtabEntry container, IDLID id)
  {
    return new StructEntry (container, id);
  } // structEntry

  public TypedefEntry typedefEntry ()
  {
    return new TypedefEntry ();
  } // typedefEntry

  public TypedefEntry typedefEntry (SymtabEntry container, IDLID id)
  {
    return new TypedefEntry (container, id);
  } // typedefEntry

  public UnionEntry unionEntry ()
  {
    return new UnionEntry ();
  } // unionEntry

  public UnionEntry unionEntry (SymtabEntry container, IDLID id)
  {
    return new UnionEntry (container, id);
  } // unionEntry

} // interface DefaultSymtabFactory
