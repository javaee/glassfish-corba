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

package com.sun.tools.corba.se.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for the #include statement.
 **/
public class IncludeEntry extends SymtabEntry
{
  protected IncludeEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } // ctor

  protected IncludeEntry (SymtabEntry that)
  {
    super (that, new IDLID ());
    module (that.name ());
    name ("");
  } // ctor

  protected IncludeEntry (IncludeEntry that)
  {
    super (that);
  } // ctor

  public Object clone ()
  {
    return new IncludeEntry (this);
  } // clone

  /** Invoke the Include type generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    includeGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the Include type generator.
      @returns an object which implements the IncludeGen interface.
      @see IncludeGen */
  public Generator generator ()
  {
    return includeGen;
  } // generator

  //d44810
  /** Set the fully-qualified file specification of this include file. */
  public void absFilename (String afn)
  {
    _absFilename = afn;
  }

  //d44810
  /** Access the fully-qualified file specification of this include.
      @returns a string containing the path of the include file. */
  public String absFilename ()
  {
    return _absFilename;
  }

  /** Add an IncludeEntry to the list of files which this included
      file includes. */
  public void addInclude (IncludeEntry entry)
  {
    includeList.addElement (entry);
  } // addInclude

  /** Get the list of files which this file includes. */
  public Vector includes ()
  {
    return includeList;
  } // includes

  static  IncludeGen includeGen;
  /** List of files this file includes */
  private Vector     includeList = new Vector ();
  //d44810 
  /** Absolute file name for .u file generation. */
  private String     _absFilename       = null;
} // class IncludeEntry
