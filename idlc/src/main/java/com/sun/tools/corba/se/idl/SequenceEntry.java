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

import com.sun.tools.corba.se.idl.constExpr.Expression;

/**
 * This is the symbol table entry for sequences.
 **/
public class SequenceEntry extends SymtabEntry
{
  protected SequenceEntry ()
  {
    super ();
    repositoryID (Util.emptyID);
  } // ctor

  protected SequenceEntry (SequenceEntry that)
  {
    super (that);
    _maxSize = that._maxSize;
  } // ctor

  protected SequenceEntry (SymtabEntry that, IDLID clone)
  {
    super (that, clone);
    if (!(that instanceof SequenceEntry))
      // If that is a SequenceEntry, then it is a container of this sequence, but it is not a module of this sequence.  It's name doesn't belong in the module name.
      if (module ().equals (""))
        module (name ());
      else if (!name ().equals (""))
        module (module () + "/" + name ());
    repositoryID (Util.emptyID);
  } // ctor

  public Object clone ()
  {
    return new SequenceEntry (this);
  } // clone

  public boolean isReferencable()
  {
    // A sequence is referencable if its component
    // type is.
    return type().isReferencable() ;
  }

  public void isReferencable( boolean value ) 
  {
    // NO-OP: this cannot be set for a sequence.
  }

  /** Invoke the sequence generator.
      @param symbolTable the symbol table is a hash table whose key is
       a fully qualified type name and whose value is a SymtabEntry or
       a subclass of SymtabEntry.
      @param stream the stream to which the generator should sent its output.
      @see SymtabEntry */
  public void generate (Hashtable symbolTable, PrintWriter stream)
  {
    sequenceGen.generate (symbolTable, this, stream);
  } // generate

  /** Access the sequence generator.
      @returns an object which implements the SequenceGen interface.
      @see SequenceGen */
  public Generator generator ()
  {
    return sequenceGen;
  } // generator

  /** the constant expression defining the maximum size of the sequence.
      If it is null, then the sequence is unbounded. */
  public void maxSize (Expression expr)
  {
    _maxSize = expr;
  } // maxSize

  /** the constant expression defining the maximum size of the sequence.
      If it is null, then the sequence is unbounded. */
  public Expression maxSize ()
  {
    return _maxSize;
  } // maxSize

  /** Only sequences can be contained within sequences. */
  public void addContained (SymtabEntry entry)
  {
    _contained.addElement (entry);
  } // addContained

  /** Only sequences can be contained within sequences. */
  public Vector contained ()
  {
    return _contained;
  } // contained

  static SequenceGen sequenceGen;

  private Expression _maxSize   = null;
  private Vector     _contained = new Vector ();
} // class SequenceEntry
