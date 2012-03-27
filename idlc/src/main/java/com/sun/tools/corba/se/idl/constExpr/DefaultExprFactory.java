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

package com.sun.tools.corba.se.idl.constExpr;

// NOTES:

import com.sun.tools.corba.se.idl.ConstEntry;
import java.math.BigInteger;

public class DefaultExprFactory implements ExprFactory
{
  public And and (Expression left, Expression right)
  {
    return new And (left, right);
  } // and

  public BooleanAnd booleanAnd (Expression left, Expression right)
  {
    return new BooleanAnd (left, right);
  } // booleanAnd

  public BooleanNot booleanNot (Expression operand)
  {
    return new BooleanNot (operand);
  } // booleanNot

  public BooleanOr booleanOr (Expression left, Expression right)
  {
    return new BooleanOr (left, right);
  } // booleanOr

  public Divide divide (Expression left, Expression right)
  {
    return new Divide (left, right);
  } // divide

  public Equal equal (Expression left, Expression right)
  {
    return new Equal (left, right);
  } // equal

  public GreaterEqual greaterEqual (Expression left, Expression right)
  {
    return new GreaterEqual (left, right);
  } // greaterEqual

  public GreaterThan greaterThan (Expression left, Expression right)
  {
    return new GreaterThan (left, right);
  } // greaterThan

  public LessEqual lessEqual (Expression left, Expression right)
  {
    return new LessEqual (left, right);
  } // lessEqual

  public LessThan lessThan (Expression left, Expression right)
  {
    return new LessThan (left, right);
  } // lessThan

  public Minus minus (Expression left, Expression right)
  {
    return new Minus (left, right);
  } // minus

  public Modulo modulo (Expression left, Expression right)
  {
    return new Modulo (left, right);
  } // modulo

  public Negative negative (Expression operand)
  {
    return new Negative (operand);
  } // negative

  public Not not (Expression operand)
  {
    return new Not (operand);
  } // not

  public NotEqual notEqual (Expression left, Expression right)
  {
    return new NotEqual (left, right);
  } // notEqual

  public Or or (Expression left, Expression right)
  {
    return new Or (left, right);
  } // or

  public Plus plus (Expression left, Expression right)
  {
    return new Plus (left, right);
  } // plus

  public Positive positive (Expression operand)
  {
    return new Positive (operand);
  } // positive

  public ShiftLeft shiftLeft (Expression left, Expression right)
  {
    return new ShiftLeft (left, right);
  } // shiftLeft

  public ShiftRight shiftRight (Expression left, Expression right)
  {
    return new ShiftRight (left, right);
  } // shiftRight

  public Terminal terminal (String representation, Character charValue,
    boolean isWide )
  {
    return new Terminal (representation, charValue, isWide );
  } // ctor

  public Terminal terminal (String representation, Boolean booleanValue)
  {
    return new Terminal (representation, booleanValue);
  } // ctor

  // Support long long <daz>
  public Terminal terminal (String representation, BigInteger bigIntegerValue)
  {
    return new Terminal (representation, bigIntegerValue);
  } // ctor

  //daz  public Terminal terminal (String representation, Long longValue)
  //       {
  //       return new Terminal (representation, longValue);
  //       } // ctor

  public Terminal terminal (String representation, Double doubleValue)
  {
    return new Terminal (representation, doubleValue);
  } // ctor

  public Terminal terminal (String stringValue, boolean isWide )
  {
    return new Terminal (stringValue, isWide);
  } // ctor

  public Terminal terminal (ConstEntry constReference)
  {
    return new Terminal (constReference);
  } // ctor

  public Times times (Expression left, Expression right)
  {
    return new Times (left, right);
  } // times

  public Xor xor (Expression left, Expression right)
  {
    return new Xor (left, right);
  } // xor
} // class DefaultExprFactory
