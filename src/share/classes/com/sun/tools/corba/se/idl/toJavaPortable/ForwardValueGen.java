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
// -D61056   <klr> Use Util.helperName

import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.tools.corba.se.idl.GenFileStream;
import com.sun.tools.corba.se.idl.InterfaceEntry;
import com.sun.tools.corba.se.idl.SymtabEntry;
import com.sun.tools.corba.se.idl.TypedefEntry;
import com.sun.tools.corba.se.idl.ForwardValueEntry;
import com.sun.tools.corba.se.idl.InterfaceState;
import com.sun.tools.corba.se.idl.MethodEntry;

/**
 *
 **/
public class ForwardValueGen implements com.sun.tools.corba.se.idl.ForwardValueGen, JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public ForwardValueGen ()
  {
  } // ctor

  /**
   *
   **/
  public void generate (Hashtable symbolTable, ForwardValueEntry v, PrintWriter str)
  {
    this.symbolTable = symbolTable;
    this.v = v;
    
    openStream ();
    if (stream == null)
      return;
    generateHelper ();
    generateHolder ();
    generateStub ();
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } // generate

  /**
   *
   **/
  protected void openStream ()
  {
    stream = Util.stream (v, ".java");
  } // openStream

  /**
   *
   **/
  protected void generateHelper ()
  {
    ((Factories)Compile.compiler.factories ()).helper ().generate (symbolTable, v);
  } // generateHelper

  /**
   *
   **/
  protected void generateHolder ()
  {
    ((Factories)Compile.compiler.factories ()).holder ().generate (symbolTable, v);
  } // generateHolder

  /**
   *
   **/
  protected void generateStub ()
  {
  } // generateStub

  /**
   *
   **/
  protected void writeHeading ()
  {
    Util.writePackage (stream, v);
    Util.writeProlog (stream, ((GenFileStream)stream).name ());

    if (v.comment () != null)
      v.comment ().generate ("", stream);

    stream.print ("public class " + v.name () + " implements org.omg.CORBA.portable.IDLEntity");
      // There should ALWAYS be at least one:  ValueBase

    stream.println ("{");
  } // writeHeading

  /**
   *
   **/
  protected void writeBody ()
  {
  } // writeBody

  /**
   *
   **/
  protected void writeClosing ()
  {
   stream.println ("} // class " + v.name ());
  } // writeClosing

  /**
   *
   **/
  protected void closeStream ()
  {
    stream.close ();
  } // closeStream

  ///////////////
  // From JavaGenerator

  public int helperType (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return index;
  } // helperType

  public int type (int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    stream.println (indent + name + " = " + Util.helperName (entry, true) + ".type ();"); // <d61056>
    return index;
  } // type

  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    stream.println ("    " + entryName + " value = new " + entryName + " ();");
    read (0, "    ", "value", entry, stream);
    stream.println ("    return value;");
  } // helperRead

  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    return index;
  } // read

  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    write (0, "    ", "value", entry, stream);
  } // helperWrite

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    return index;
  } // write

  // From JavaGenerator
  ///////////////

  /**
   *
   **/
  protected void writeAbstract ()
  {
  } // writeAbstract

  protected Hashtable  symbolTable = null;
  protected ForwardValueEntry v = null;
  protected PrintWriter stream = null;
} // class ForwardValueGen
