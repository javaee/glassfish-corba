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

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.ConstEntry;
import com.sun.tools.corba.ee.idl.ModuleEntry;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;

/**
 *
 **/
public class ConstGen implements com.sun.tools.corba.ee.idl.ConstGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ConstGen ()
  {
  } // ctor

  /**
   * Generate Java code for an IDL constant.  A constant is written to
   * a new class only when it is not a member of an interface; otherwise
   * it written to the interface class in which it resides.
   **/
  public void generate (Hashtable symbolTable, ConstEntry c, PrintWriter s)
  {
    this.symbolTable = symbolTable;
    this.c           = c;
    this.stream      = s;
    init ();
    
    if (c.container () instanceof ModuleEntry)
      generateConst ();
    else if (stream != null)
      writeConstExpr ();
  } // generate

  /**
   * Initialize members unique to this generator.
   **/
  protected void init ()
  {
  } // init

  /**
   * Generate the class defining the constant.
   **/
  protected void generateConst ()
  {
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } // generateConst

  /**
   * Open a new print stream only if the constant is not a member
   * of an interface.
   **/
  protected void openStream ()
  {
    stream = Util.stream(c, ".java");
  } // openStream

  /**
   * Write the heading for the class defining the constant.
   **/
  protected void writeHeading ()
  {
    Util.writePackage(stream, c);
    Util.writeProlog(stream, ((GenFileStream) stream).name());
    stream.println ("public interface " + c.name ()); 
        // should not be done according to the mapping
        // + " extends org.omg.CORBA.portable.IDLEntity");
    stream.println ("{");
  } // writeHeading

  /**
   * Write the constant expression and any comment, if present.
   **/
  protected void writeBody ()
  {
    writeConstExpr ();
  } // writeBody

  /**
   * Write the entire constant expression and any comment, if present.
   **/
  protected void writeConstExpr ()
  {
    if (c.comment () != null)
      c.comment ().generate ("  ", stream);
    if (c.container () instanceof ModuleEntry) {
        
      stream.print ("  public static final " + Util.javaName(c.type()) + " value = ");
    } else {
      stream.print ("  public static final " + Util.javaName(c.type()) + ' ' + c.name () + " = ");
    }
    writeConstValue (c.type ());
  } // writeConstExpr

  /**
   * Write the constant's value according to its type.
   **/
  private void writeConstValue (SymtabEntry type)
  {
    if (type instanceof PrimitiveEntry)
      stream.println ('(' + Util.javaName(type) + ")(" + Util.parseExpression(c.value()) + ");");
    else if (type instanceof StringEntry)
      stream.println (Util.parseExpression(c.value()) + ';');
    else if (type instanceof TypedefEntry)
    {
      while (type instanceof TypedefEntry)
        type = type.type ();
      writeConstValue (type);
    }
    else
      stream.println (Util.parseExpression(c.value()) + ';');
  } // writeValue

  /**
   * Generate any last words and close the class.
   **/
  protected void writeClosing ()
  {
    stream.println ("}");
  } // writeClosing

  /**
   * Close the print stream, causing the file to be written.
   **/
  protected void closeStream ()
  {
    stream.close ();
  } // closeStream

  protected java.util.Hashtable  symbolTable = null;
  protected ConstEntry           c           = null;
  protected PrintWriter          stream      = null;
} // class ConstGen


/*=======================================================================================
  DATE-AUTHOR   ACTION
  ---------------------------------------------------------------------------------------
  11sep1997daz  Return when print stream is null and container is NOT a module. Fixes
                -keep option, which causes null print stream to be sent to ConstGen.
  31jul1997daz  Write source comment immediately preceding constant declaration.
  =======================================================================================*/

