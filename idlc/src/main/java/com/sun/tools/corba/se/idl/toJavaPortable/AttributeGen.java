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
 * COMPONENT_NAME: idl.toJava
 *
 * ORIGINS: 27
 *
 * Licensed Materials - Property of IBM
 * 5639-D57 (C) COPYRIGHT International Business Machines Corp. 1997, 1999
 * RMI-IIOP v1.0
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package com.sun.tools.corba.se.idl.toJavaPortable;

// NOTES:

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.tools.corba.se.idl.AttributeEntry;
import com.sun.tools.corba.se.idl.InterfaceEntry;
import com.sun.tools.corba.se.idl.MethodEntry;
import com.sun.tools.corba.se.idl.ParameterEntry;
import com.sun.tools.corba.se.idl.SymtabEntry;

/**
 *
 **/
public class AttributeGen extends MethodGen implements com.sun.tools.corba.se.idl.AttributeGen
{
  /**
   * Public zero-argument constructor.
   **/
  public AttributeGen ()
  {
  } // ctor

  /**
   *
   **/
  private boolean unique (InterfaceEntry entry, String name)
  {
    // Compare the name to the methods of this interface
    Enumeration methods = entry.methods ().elements ();
    while (methods.hasMoreElements ())
    {
      SymtabEntry method = (SymtabEntry)methods.nextElement ();
      if (name.equals (method.name ()))
        return false;
    }

    // Recursively call unique on each derivedFrom interface
    Enumeration derivedFrom = entry.derivedFrom ().elements ();
    while (derivedFrom.hasMoreElements ())
      if (!unique ((InterfaceEntry)derivedFrom.nextElement (), name))
        return false;

    // If the name isn't in any method, nor in any method of the
    // derivedFrom interfaces, then the name is unique.
    return true;
  } // unique

  /**
   * Method generate() is not used in MethodGen.  They are replaced by the
   * more granular interfaceMethod, stub, skeleton, dispatchSkeleton.
   **/
  public void generate (Hashtable symbolTable, AttributeEntry m, PrintWriter stream)
  {
  } // generate

  /**
   *
   **/
  protected void interfaceMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    AttributeEntry a = (AttributeEntry)m;

    // Generate for the get method
    super.interfaceMethod (symbolTable, a, stream);

    // Generate for the set method if the attribute is not readonly
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.interfaceMethod (symbolTable, a, stream);
      clear ();
    }
  } // interfaceMethod

  /**
   *
   **/
  protected void stub (String className, boolean isAbstract, Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;

    // Generate for the get method
    super.stub (className, isAbstract, symbolTable, a, stream, index);

    // Generate for the set method if the attribute is not readonly
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.stub (className, isAbstract, symbolTable, a, stream, index + 1);
      clear ();
    }
  } // stub

  /**
   *
   **/
  protected void skeleton (Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;

    // Generate for the get method
    super.skeleton (symbolTable, a, stream, index);

    // Generate for the set method if the attribute is not readonly
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.skeleton (symbolTable, a, stream, index + 1);
      clear ();
    }
  } // skeleton

  /**
   *
   **/
  protected void dispatchSkeleton (Hashtable symbolTable, MethodEntry m, PrintWriter stream, int index)
  {
    AttributeEntry a = (AttributeEntry)m;

    // Generate for the get method
    super.dispatchSkeleton (symbolTable, a, stream, index);

    // Generate for the set method if the attribute is not readonly
    if (!a.readOnly ())
    {
      setupForSetMethod ();
      super.dispatchSkeleton (symbolTable, m, stream, index + 1);
      clear ();
    }
  } // dispatchSkeleton

  private SymtabEntry realType = null;

  /**
   *
   **/
  protected void setupForSetMethod ()
  {
    ParameterEntry parm = Compile.compiler.factory.parameterEntry ();
    parm.type (m.type ());
    parm.name ("new" + Util.capitalize (m.name ()));
    m.parameters ().addElement (parm);
    realType = m.type ();
    m.type (null);
  } // setupForSetMethod

  /**
   *
   **/
  protected void clear ()
  {
    // Set back to normal
    m.parameters ().removeAllElements ();
    m.type (realType);
  } // clear
} // class AttributeGen
