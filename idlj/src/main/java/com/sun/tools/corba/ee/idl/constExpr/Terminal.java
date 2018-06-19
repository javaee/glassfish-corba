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

package com.sun.tools.corba.ee.idl.constExpr;

// NOTES:

import com.sun.tools.corba.ee.idl.ConstEntry;

import java.math.BigInteger;

/**
 * This class contains values.  Objects of this class are the terminal
 * nodes of an expression tree.
 * <b>
 * Note that there is a constructor for Double values, but not Float.
 * CORBA defines that all floating point expressions are evaluated as
 * double, and that the result is coerced back to float if necessary.
 * </b>
 * Note also that there is a constructor for long values, but not for
 * int or short.  CORBA defines that all integral expressions are evaluated
 * as unsigned long.  A CORBA long is a Java int.  There is no unsigned int
 * in Java, so the next larger type, long, is used.
 **/
public class Terminal extends Expression
{
  protected Terminal (String representation, Character charValue, 
    boolean isWide)
  {
    rep (representation);
    value (charValue);
    if (isWide)
        type( "wchar" ) ;
    else
        type( "char" ) ;
  } // ctor

  protected Terminal (String representation, Boolean booleanValue)
  {
    rep (representation);
    value (booleanValue);
  } // ctor

  // Support long long <daz>
  protected Terminal (String representation, BigInteger bigIntegerValue)
  {
    rep (representation);
    value (bigIntegerValue);
  } // ctor

  protected Terminal (String representation, Long longValue)
  {
    long lv = longValue.longValue ();
    rep (representation);
    if (lv > Integer.MAX_VALUE || lv < Integer.MIN_VALUE)
      value (longValue);
    else
      value (Integer.valueOf (longValue.intValue ()));
  } // ctor

  protected Terminal (String representation, Double doubleValue)
  {
    rep (representation);
    value (doubleValue);
  } // ctor

  protected Terminal (String stringValue, boolean isWide )
  {
    rep (stringValue);
    value (stringValue);
    if (isWide)
        type( "wstring" ) ;
    else
        type( "string" ) ;
  } // ctor

  protected Terminal (ConstEntry constReference)
  {
    rep (constReference.fullName ());
    value (constReference);
  } // ctor

  ///// INSTANCE METHODS
  public Object evaluate () throws com.sun.tools.corba.ee.idl.constExpr.EvaluationException
  {
    if (value () instanceof ConstEntry)
      return ((ConstEntry)value ()).value ().evaluate ();
    else
      return value ();
  } // evaluate
} // class Terminal
