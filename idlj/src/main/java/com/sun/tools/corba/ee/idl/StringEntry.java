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

import com.sun.tools.corba.ee.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Hashtable;

public class StringEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected StringEntry ()
  {
    super ();
    String override = (String) com.sun.tools.corba.ee.idl.Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (com.sun.tools.corba.ee.idl.Util.emptyID);
  } // ctor

  protected StringEntry (StringEntry that)
  {
    super (that);
    _maxSize = that._maxSize;
  } // ctor

  protected StringEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    module ("");

    String override = (String) com.sun.tools.corba.ee.idl.Parser.overrideNames.get ("string");
    if (override == null)
      name ("string");
    else
      name (override);
    repositoryID (com.sun.tools.corba.ee.idl.Util.emptyID);
  } // ctor

  public Object clone ()
  {
    return new StringEntry (this);
  } // clone

  /** Invoke the string type generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    stringGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the primitive type generator.
      @return an object which implements the PrimitiveGen interface.
      @see com.sun.tools.corba.ee.idl.PrimitiveGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return stringGen;
  } // generator

  /** The constant expression defining the maximum size of the string.
      If it is null, then the string is unbounded. */
  public void maxSize (Expression expr)
  {
    _maxSize = expr;
  } // maxSize

  /** The constant expression defining the maximum size of the string.
      If it is null, then the string is unbounded. */
  public Expression maxSize ()
  {
    return _maxSize;
  } // maxSize

  static com.sun.tools.corba.ee.idl.StringGen stringGen;

  private Expression _maxSize = null;
} // class StringEntry
