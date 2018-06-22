/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
// -D61056   <klr> Use Util.helperName

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

/**
 * Handles generation of CORBA strings as well as wstrings.  Be careful
 * not to forget the wstrings.
 **/
public class StringGen implements com.sun.tools.corba.ee.idl.StringGen, JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public StringGen ()
  {
  } // ctor

  /**
   * This should never be called.  This class exists for the
   * JavaGenerator interface.
   **/
  public void generate (Hashtable symbolTable, StringEntry e, PrintWriter stream)
  {
  } // generate

  ///////////////
  // From JavaGenerator

  public int helperType (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return type(index, indent, tcoffsets, name, entry, stream);
  } // helperType

  public int type (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    tcoffsets.set (entry);
    StringEntry stringEntry = (StringEntry)entry;
    String bound;
    if (stringEntry.maxSize () == null)
      bound = "0";
    else
      bound = com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(stringEntry.maxSize());

    // entry.name() is necessary to determine whether it is a
    // string or wstring

    stream.println (indent 
                    + name 
                    + " = org.omg.CORBA.ORB.init ().create_"
                    + entry.name()
                    + "_tc ("
                    + bound + ");");
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
    StringEntry string = (StringEntry)entry;
    String entryName = entry.name ();
    if (entryName.equals ("string"))
      stream.println (indent + name + " = istream.read_string ();");
    else if (entryName.equals ("wstring"))
      stream.println (indent + name + " = istream.read_wstring ();");
    if (string.maxSize () != null)
    {
      stream.println (indent + "if (" + name + ".length () > (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(string.maxSize()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    return index;
  } // read

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    StringEntry string = (StringEntry)entry;
    if (string.maxSize () != null)
    {
      stream.print (indent + "if (" + name + ".length () > (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(string.maxSize()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    String entryName = entry.name ();
    if (entryName.equals ("string"))
      stream.println (indent + "ostream.write_string (" + name + ");");
    else if (entryName.equals ("wstring"))
      stream.println (indent + "ostream.write_wstring (" + name + ");");
    return index;
  } // write

  // From JavaGenerator
  ///////////////
} // class StringGen
