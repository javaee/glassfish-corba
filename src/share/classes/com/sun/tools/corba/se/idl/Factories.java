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

/**
 * If the framework is being extended, this class must be extended.
 * At very least, the genFactory method must be overridden to return
 * the code generator extensions.  The remaining methods may be overridden
 * if necessary:
 * <dl>
 * <dt>symtabFactory
 * <dd>If you wish to extend the symbol table entries, this method must return the factory which constructs those extensions.  If you only want to extend a few of the symbol table entries, it may be useful to extend com.sun.tools.corba.se.idl.DefaultSymtabFactory and only override the pertinent methods.
 * <dt>exprFactory
 * <dd>If you wish to extend the expression classes, this method must return the factory which constructs those extensions.  If you only want to extend a few of the expression classes, it may be useful to extend com.sun.tools.corba.se.idl.constExpr.DefaultSymtabFactory and only override the pertinent methods.
 * <dt>arguments
 * <dd>If you wish to add additional arguments to the base set of arguments, extend com.sun.tools.corba.se.idl.Arguments and override this method to return that class.
 * <dt>languageKeywords
 * <dd>If the language you are generating code in has keywords other than IDL keywords, these keywords should be returned by this method.  The framework will prepend any IDL identifiers it encounters which are in this list with an underscore (`_') to avoid compilation errors.  For instance, `catch' is a Java keyword.  If the generators are emitting Java code for the following IDL, emitting `catch' as is will cause compile errors, so it is changed to `_catch':
 * <br>
 * IDL:
 * <br>
 * const long catch = 22;
 * <br>
 * Possible generated code:
 * <br>
 * public static final int _catch = 22;
 * </dl>
 **/
public class Factories
{
  /** Return the implementation of the GenFactory interface.  If this
      returns null, then the compiler cannot generate anything. */
  public GenFactory genFactory ()
  {
    return null;
  } // genFactory

  /** Return the implementation of the SymtabFactory interface.  If this
      returns null, the default symbol table entries will be used. */
  public SymtabFactory symtabFactory ()
  {
    return new DefaultSymtabFactory ();
  } // symtabFactory

  /** Return the implementation of the ExprFactory interface.  If this
      returns null, the default expressions will be used. */
  public com.sun.tools.corba.se.idl.constExpr.ExprFactory exprFactory ()
  {
    return new com.sun.tools.corba.se.idl.constExpr.DefaultExprFactory ();
  } // exprFactory

  /** Return a subclass of the Arguments class.  If this returns null,
      the default will be used. */
  public Arguments arguments ()
  {
    return new Arguments ();
  } // arguments

  /** Return the list of keywords in the generated language.
      Note that these keywords may contain the following wildcards:
      <dl>
      <dt>`*'
      <dd>matches zero or more characters
      <dt>`+'
      <dd>matches one or more characters
      <dt>`.'
      <dd>matches any single character
      </dl> */
  public String[] languageKeywords ()
  {
    return null;
  } // languageKeywords
} // interface Factories
