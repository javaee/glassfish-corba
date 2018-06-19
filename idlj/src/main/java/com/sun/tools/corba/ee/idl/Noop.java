/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997 IBM Corp. All rights reserved.
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
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

public class Noop implements
        com.sun.tools.corba.ee.idl.AttributeGen, com.sun.tools.corba.ee.idl.ConstGen, com.sun.tools.corba.ee.idl.EnumGen, com.sun.tools.corba.ee.idl.ExceptionGen, com.sun.tools.corba.ee.idl.ForwardGen,
        com.sun.tools.corba.ee.idl.ForwardValueGen, com.sun.tools.corba.ee.idl.IncludeGen, com.sun.tools.corba.ee.idl.InterfaceGen, com.sun.tools.corba.ee.idl.ValueGen, com.sun.tools.corba.ee.idl.ValueBoxGen,
        com.sun.tools.corba.ee.idl.MethodGen, com.sun.tools.corba.ee.idl.ModuleGen, com.sun.tools.corba.ee.idl.NativeGen, com.sun.tools.corba.ee.idl.ParameterGen, com.sun.tools.corba.ee.idl.PragmaGen,
        com.sun.tools.corba.ee.idl.PrimitiveGen, com.sun.tools.corba.ee.idl.SequenceGen, com.sun.tools.corba.ee.idl.StringGen, com.sun.tools.corba.ee.idl.StructGen, com.sun.tools.corba.ee.idl.TypedefGen,
        com.sun.tools.corba.ee.idl.UnionGen, GenFactory
{
  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.AttributeEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ConstEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.EnumEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ExceptionEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ForwardEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ForwardValueEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.IncludeEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.InterfaceEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ValueEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ValueBoxEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.MethodEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ModuleEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.ParameterEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.PragmaEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.PrimitiveEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.SequenceEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.StringEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.StructEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.TypedefEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.UnionEntry entry, PrintWriter stream)
  {
  } // generate

  public void generate (Hashtable symbolTable, com.sun.tools.corba.ee.idl.NativeEntry entry, PrintWriter stream)
  {
  } // generate

  // For GenFactory
  public com.sun.tools.corba.ee.idl.AttributeGen createAttributeGen ()
  {
    return null;
  } // createAttributeGen

  public com.sun.tools.corba.ee.idl.ConstGen createConstGen ()
  {
    return null;
  } // createConstGen

  public com.sun.tools.corba.ee.idl.EnumGen createEnumGen ()
  {
    return null;
  } // createEnumGen

  public com.sun.tools.corba.ee.idl.ExceptionGen createExceptionGen ()
  {
    return null;
  } // createExceptionGen

  public com.sun.tools.corba.ee.idl.ForwardGen createForwardGen ()
  {
    return null;
  } // createForwardGen

  public com.sun.tools.corba.ee.idl.ForwardValueGen createForwardValueGen ()
  {
    return null;
  } // createForwardValueGen

  public com.sun.tools.corba.ee.idl.IncludeGen createIncludeGen ()
  {
    return null;
  } // createIncludeGen

  public com.sun.tools.corba.ee.idl.InterfaceGen createInterfaceGen ()
  {
    return null;
  } // createInterfaceGen

  public com.sun.tools.corba.ee.idl.ValueGen createValueGen ()
  {
    return null;
  } // createValueGen

  public com.sun.tools.corba.ee.idl.ValueBoxGen createValueBoxGen ()
  {
    return null;
  } // createValueBoxGen

  public com.sun.tools.corba.ee.idl.MethodGen createMethodGen ()
  {
    return null;
  } // createMethodGen

  public com.sun.tools.corba.ee.idl.ModuleGen createModuleGen ()
  {
    return null;
  } // createModuleGen

  public com.sun.tools.corba.ee.idl.NativeGen createNativeGen ()
  {
    return null;
  } // createNativeGen

  public com.sun.tools.corba.ee.idl.ParameterGen createParameterGen ()
  {
    return null;
  } // createParameterGen

  public com.sun.tools.corba.ee.idl.PragmaGen createPragmaGen ()
  {
    return null;
  } // createPragmaGen

  public com.sun.tools.corba.ee.idl.PrimitiveGen createPrimitiveGen ()
  {
    return null;
  } // createPrimitiveGen

  public com.sun.tools.corba.ee.idl.SequenceGen createSequenceGen ()
  {
    return null;
  } // createSequenceGen

  public com.sun.tools.corba.ee.idl.StringGen createStringGen ()
  {
    return null;
  } // createStringGen

  public com.sun.tools.corba.ee.idl.StructGen createStructGen ()
  {
    return null;
  } // createStructGen

  public com.sun.tools.corba.ee.idl.TypedefGen createTypedefGen ()
  {
    return null;
  } // createTypedefGen

  public com.sun.tools.corba.ee.idl.UnionGen createUnionGen ()
  {
    return null;
  } // createUnionGen
} // class Noop
