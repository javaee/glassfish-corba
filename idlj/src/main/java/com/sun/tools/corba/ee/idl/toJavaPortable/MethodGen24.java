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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
// -D62023   <klr> New file to implement CORBA 2.4 RTF
// -D62794   <klr> Fix problem with no-arg create functions

import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.ParameterEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 **/
public class MethodGen24 extends MethodGen
{
  /**
   * Public zero-argument constructor.
   **/
  public MethodGen24 ()
  {
  } // ctor

  /**
   * Print the parameter list for the factory method.
   * @param m The method to list parameters for
   * @param listTypes If try, declare the parms, otherwise just list them
   * @param stream The PrintWriter to print on
   */
  protected void writeParmList (MethodEntry m, boolean listTypes, PrintWriter stream) {
    boolean firstTime = true;
    Enumeration e = m.parameters ().elements ();
    while (e.hasMoreElements ())
    {
      if (firstTime)
        firstTime = false;
      else
        stream.print (", ");
      ParameterEntry parm = (ParameterEntry)e.nextElement ();
      if (listTypes) {
        writeParmType (parm.type (), parm.passType ());
        stream.print (' ');
      }
      // Print parm name
      stream.print (parm.name ());
      // end of parameter list
    }
  }

  void helperFactoryMethod(Hashtable symbolTable, MethodEntry m, SymtabEntry t, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m = m;
    this.stream = stream;
    String initializerName = m.name ();
    String typeName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(t);
    String factoryName = typeName + "ValueFactory";

    // Step 1. Print factory method decl up to parms.
    stream.print  ("  public static " + typeName + " " + initializerName +
            " (org.omg.CORBA.ORB $orb");
    if (!m.parameters ().isEmpty ())
      stream.print (", "); // <d62794>

    // Step 2. Print the declaration parameter list.
    writeParmList (m, true, stream);

    // Step 3. Print the body of the factory method
    stream.println (")");
    stream.println ("  {");
    stream.println ("    try {");
    stream.println ("      " + factoryName + " $factory = (" + factoryName + ")");
    stream.println ("          ((org.omg.CORBA_2_3.ORB) $orb).lookup_value_factory(id());");
    stream.print   ("      return $factory." + initializerName + " (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("    } catch (ClassCastException $ex) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("    }");
    stream.println ("  }");
    stream.println ();
  } // helperFactoryMethod

  void abstractMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    stream.print ("public abstract ");
    writeMethodSignature ();
    stream.println (";");
    stream.println ();
  } // abstractMethod

  void defaultFactoryMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    String typeName = m.container (). name ();
    stream.println ();
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print   ("  public " + typeName + " " + m.name () + " (");
    writeParmList  (m, true, stream);
    stream.println (")");
    stream.println ("  {");
    stream.print   ("    return new " + typeName + "Impl (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("  }");
  } // defaultFactoryMethod

  protected void writeMethodSignature ()
  {
    // Step 0.  Print the return type and name.
    // A return type of null indicates the "void" return type. If m is a
    // Valuetype factory method, it has a null return type,
    if (m.type () == null)
    {
        // if factory method, result type is container 
        if (isValueInitializer ())
            stream.print (m.container ().name ());
        else
            stream.print ("void");
    }
    else
    {
      stream.print (com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(m.type()));
    }
    stream.print (' ' + m.name () + " (");

    // Step 1.  Print the parameter list.
    boolean firstTime = true;
    Enumeration e = m.parameters ().elements ();
    while (e.hasMoreElements ())
    {
      if (firstTime)
        firstTime = false;
      else
        stream.print (", ");
      ParameterEntry parm = (ParameterEntry)e.nextElement ();

      writeParmType (parm.type (), parm.passType ());

      // Print parm name
      stream.print (' ' + parm.name ());
    }

    // Step 2.  Add the context parameter if necessary.
    if (m.contexts ().size () > 0)
    {
      if (!firstTime)
        stream.print (", ");
      stream.print ("org.omg.CORBA.Context $context");
    }

    // Step 3.  Print the throws clause (if necessary).
    if (m.exceptions ().size () > 0)
    {
      stream.print (") throws ");
      e = m.exceptions ().elements ();
      firstTime = true;
      while (e.hasMoreElements ())
      {
        if (firstTime)
          firstTime = false;
        else
          stream.print (", ");
        stream.print (com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName((SymtabEntry) e.nextElement()));
      }
    }
    else
      stream.print (')');
  } // writeMethodSignature

  protected void interfaceMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    writeMethodSignature ();
    stream.println (";");
  } // interfaceMethod
}
