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

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * This is the symbol table entry for parameters.
 **/
public class ParameterEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  /** This is a set of class constants.  A parameter can be passed
      as one of In, Out, or Inout. */
  public static final int In    = 0,
                          Inout = 1,
                          Out   = 2;

  protected ParameterEntry ()
  {
    super ();
  } // ctor

  protected ParameterEntry (ParameterEntry that)
  {
    super (that);
    _passType = that._passType;
  } // ctor

  protected ParameterEntry (com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public Object clone ()
  {
    return new ParameterEntry (this);
  } // clone

  /** Invoke the paramter generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    parameterGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the parameter generator.
      @returns an object which implements the ParameterGen interface.
      @see com.sun.tools.corba.ee.idl.ParameterGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return parameterGen;
  } // generator

  /** This indicates the pass type of this parameter. */
  public void passType (int passType)
  {
    if (passType >= In && passType <= Out)
      _passType = passType;
  } // passType

  /** This indicates the pass type of this parameter. */
  public int passType ()
  {
    return _passType;
  } // passType

  private int _passType = In;

  static com.sun.tools.corba.ee.idl.ParameterGen parameterGen;
} // class ParameterEntry
