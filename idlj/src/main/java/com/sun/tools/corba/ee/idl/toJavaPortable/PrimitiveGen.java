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

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

/**
 *
 **/
public class PrimitiveGen implements com.sun.tools.corba.ee.idl.PrimitiveGen, JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public PrimitiveGen ()
  {
  } // ctor

  /**
   * This method should never be called; this class exists for
   * the JavaGenerator interface.
   **/
  public void generate (Hashtable symbolTable, PrimitiveEntry e, PrintWriter stream)
  {
  } // generate

  ///////////////
  // From JavaGenerator

  public int helperType (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return type (index, indent, tcoffsets, name, entry, stream);
  } // helperType

  public int type (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    tcoffsets.set (entry);
    String emit = "tk_null";
    if (entry.name ().equals ("null"))
      emit = "tk_null";
    else if (entry.name ().equals ("void"))
      emit = "tk_void";
    else if (entry.name ().equals ("short"))
      emit = "tk_short";
    else if (entry.name ().equals ("long"))
      emit = "tk_long";
    else if (entry.name ().equals ("long long"))
      emit = "tk_longlong";
    else if (entry.name ().equals ("unsigned short"))
      emit = "tk_ushort";
    else if (entry.name ().equals ("unsigned long"))
      emit = "tk_ulong";
    else if (entry.name ().equals ("unsigned long long"))
      emit = "tk_ulonglong";
    else if (entry.name ().equals ("float"))
      emit = "tk_float";
    else if (entry.name ().equals ("double"))
      emit = "tk_double";
    else if (entry.name ().equals ("boolean"))
      emit = "tk_boolean";
    else if (entry.name ().equals ("char"))
      emit = "tk_char";
    else if (entry.name ().equals ("octet"))
      emit = "tk_octet";
    else if (entry.name ().equals ("any"))
      emit = "tk_any";
    else if (entry.name ().equals ("TypeCode"))
      emit = "tk_TypeCode";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    else if (entry.name ().equals ("Principal")) // <d61961>
      emit = "tk_Principal";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind." + emit + ");");
    return index;
  } // type

  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
  } // helperRead

  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
  } // helperWrite

  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + name + " = " + "istream.read_" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.collapseName(entry.name()) + " ();");
    return index;
  } // read

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + "ostream.write_" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.collapseName(entry.name()) + " (" + name + ");");
    return index;
  } // write

  // From JavaGenerator
  ///////////////
} // class PrimitiveGen
