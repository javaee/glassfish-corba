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
// - What does oneway mean?

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for methods.
 **/
public class MethodEntry extends com.sun.tools.corba.ee.idl.SymtabEntry
{
  protected MethodEntry ()
  {
    super ();
  } // ctor

  protected MethodEntry (MethodEntry that)
  {
    super (that);
    _exceptionNames = (Vector)that._exceptionNames.clone ();
    _exceptions     = (Vector)that._exceptions.clone ();
    _contexts       = (Vector)that._contexts.clone ();
    _parameters     = (Vector)that._parameters.clone ();
    _oneway         = that._oneway;
  } // ctor

  protected MethodEntry (InterfaceEntry that, com.sun.tools.corba.ee.idl.IDLID clone)
  {
    super (that, clone);
    if (module ().equals (""))
      module (name ());
    else if (!name ().equals (""))
      module (module () + "/" + name ());
  } // ctor

  public Object clone ()
  {
    return new MethodEntry (this);
  } // clone

  /** Invoke the method generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see com.sun.tools.corba.ee.idl.SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    methodGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the method generator.
      @returns an object which implements the MethodGen interface.
      @see com.sun.tools.corba.ee.idl.MethodGen */
  public com.sun.tools.corba.ee.idl.Generator generator ()
  {
    return methodGen;
  } // generator

  public void type (com.sun.tools.corba.ee.idl.SymtabEntry newType)
  {
    super.type (newType);
    if (newType == null)
      typeName ("void");
  } // type

  /** Add an exception to the exception list. */
  public void addException (com.sun.tools.corba.ee.idl.ExceptionEntry exception)
  {
    _exceptions.addElement (exception);
  } // addException

  /** This a a vector of the exceptions which this method raises. */
  public Vector exceptions ()
  {
    return _exceptions;
  } // exceptions

  /** Add an exception name to the list of exception names. */
  public void addExceptionName (String name)
  {
    _exceptionNames.addElement (name);
  } // addExceptionName

  /** This is a vector of strings, each of which is the full name of an
      exception which this method throws.  This vector corresponds to the
      exceptions vector.  The first element of this vector is the name
      of the first element of the exceptions vector, etc. */
  public Vector exceptionNames ()
  {
    return _exceptionNames;
  } // exceptionNames

  /* Add a context to the context list. */
  public void addContext (String context)
  {
    _contexts.addElement (context);
  } // addContext

  /** This is a vector of strings, each of which is the name of a context. */
  public Vector contexts ()
  {
    return _contexts;
  } // contexts

  /** Add a parameter to the parameter list. */
  public void addParameter (com.sun.tools.corba.ee.idl.ParameterEntry parameter)
  {
    _parameters.addElement (parameter);
  } // addParameter

  /** This is a vector of ParameterEntry's.  They are the parameters on
      this method and their order in the vector is the order they appear
      on the method. */
  public Vector parameters ()
  {
    return _parameters;
  } // parameters

  /** Is this a oneway method? */
  public void oneway (boolean yes)
  {
    _oneway = yes;
  } // oneway

  /** Is this a oneway method? */
  public boolean oneway ()
  {
    return _oneway;
  } // oneway

  /** Is this a value method? */
  public void valueMethod (boolean yes)
  {
    _valueMethod = yes;
  } // valueMethod

  /** Is this a value method? */
  public boolean valueMethod ()
  {
    return _valueMethod;
  } // valueMethod

  void exceptionsAddElement (com.sun.tools.corba.ee.idl.ExceptionEntry e)
  {
    addException (e);
    addExceptionName (e.fullName ());
  } // exceptionsAddElement

  private Vector  _exceptionNames = new Vector ();
  private Vector  _exceptions     = new Vector ();
  private Vector  _contexts       = new Vector ();
  private Vector  _parameters     = new Vector ();
  private boolean _oneway         = false;
  private boolean _valueMethod    = false;

  static com.sun.tools.corba.ee.idl.MethodGen methodGen;
} // class MethodEntry
