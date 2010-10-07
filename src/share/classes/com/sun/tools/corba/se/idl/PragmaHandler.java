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
// - Add openScope and closeScope.

import java.io.IOException;

public abstract class PragmaHandler
{
  public abstract boolean process (String pragma, String currentToken) throws IOException;

  void init (Preprocessor p)
  {
    preprocessor = p;
  } // init

  // Utility methods.

  /** Get the current token. */
  protected String currentToken ()
  {
    return preprocessor.currentToken ();
  } // currentToken

  /** This method, given an entry name, returns the entry with that name.
      It can take fully or partially qualified names and returns the
      appropriate entry defined within the current scope.  If no entry
      exists, null is returned. */
  protected SymtabEntry getEntryForName (String string)
  {
    return preprocessor.getEntryForName (string);
  } // getEntryForName

  /** This method returns a string of all of the characters from the input
      file from the current position up to, but not including, the end-of-line
      character(s). */
  protected String getStringToEOL () throws IOException
  {
    return preprocessor.getStringToEOL ();
  } // getStringToEOL

  /** This method returns a string of all of the characters from the input
      file from the current position up to, but not including, the given
      character.  It encapsulates parenthesis and quoted strings, meaning
      it does not stop if the given character is found within parentheses
      or quotes.  For instance, given the input of `start(inside)end',
      getUntil ('n') will return "start(inside)e" */
  protected String getUntil (char c) throws IOException
  {
    return preprocessor.getUntil (c);
  } // getUntil

  /** This method returns the next token String from the input file. */
  protected String nextToken () throws IOException
  {
    return preprocessor.nextToken ();
  } // nextToken

  /** This method assumes that the current token marks the beginning
      of a scoped name.  It then parses the subsequent identifier and
      double colon tokens, builds the scoped name, and finds the symbol
      table entry with that name. */
  protected SymtabEntry scopedName () throws IOException
  {
    return preprocessor.scopedName ();
  } // scopedName

  /** Skip to the end of the line. */
  protected void skipToEOL () throws IOException
  {
    preprocessor.skipToEOL ();
  } // skipToEOL

  /** This method skips the data in the input file until the specified
      character is encountered, then it returns the next token. */
  protected String skipUntil (char c) throws IOException
  {
    return preprocessor.skipUntil (c);
  } // skipUntil

  /** This method displays a Parser Exception complete with line number
      and position information with the given message string. */
  protected void parseException (String message)
  {
    preprocessor.parseException (message);
  } // parseException

  /** This method is called when the parser encounters a left curly brace.
      An extender of PragmaHandler may find scope information useful.
      For example, the prefix pragma takes effect as soon as it is
      encountered and stays in effect until the current scope is closed.
      If a similar pragma extension is desired, then the openScope and
      closeScope methods are available for overriding.
      @param entry the symbol table entry whose scope has just been opened.
       Be aware that, since the scope has just been entered, this entry is
       incomplete at this point.  */
  protected void openScope (SymtabEntry entry)
  {
  } // openScope

  /** This method is called when the parser encounters a right curly brace.
      An extender of PragmaHandler may find scope information useful.
      For example, the prefix pragma takes effect as soon as it is
      encountered and stays in effect until the current scope is closed.
      If a similar pragma extension is desired, then the openScope and
      closeScope methods are available for overriding.
      @param entry the symbol table entry whose scope has just been closed. */
  protected void closeScope (SymtabEntry entry)
  {
  } // closeScope

  private Preprocessor preprocessor = null;
} // class PragmaHandler
