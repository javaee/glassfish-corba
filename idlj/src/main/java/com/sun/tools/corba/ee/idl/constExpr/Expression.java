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

package com.sun.tools.corba.ee.idl.constExpr;

// NOTES:

import java.math.BigInteger;

public abstract class Expression
{
  /**
   * Compute the value of this expression.
   **/
  public abstract Object evaluate () throws EvaluationException;

  /**
   * Set the value of this expression.
   **/
  public void value (Object value)
  {
    _value = value;
  }
  /**
   * Get the value of this expression.
   **/
  public Object value ()
  {
    return _value;
  }

  /**
   * Set the representation of this expression.
   **/
  public void rep (String rep)
  {
    _rep = rep;
  }
  /**
   * Get the representation of this expression.
   **/
  public String rep ()
  {
    return _rep;
  }

  /**
   * Set the target type of this expression.
   **/
  public void type (String type)
  {
    _type = type;
  }
  /**
   * Get the target type of this expression.
   **/
  public String type ()
  {
    return _type;
  }

  /**
   * Return the default computation type for the given target type.
   **/
  protected static String defaultType (String targetType)
  {
    return (targetType == null) ? new String ("") : targetType;
  } // defaultType

  // BigInteger is a multi-precision number whose representation contains
  // a signum (sign-number = 1, -1) and a magnitude.  To support "long long",
  // all integer expressions are now performed over BigInteger and stored as
  // such.  During the evaluation of an integer expression, the signum of its
  // value may toggle, which may cause the value of an expression to conflict
  // with its target type: [Case 1] If the resulting value is negative
  // (signum=-1) and the target type is unsigned; or [Case 2] if the resulting
  // value is positive (signum=1) and greater than 2**(target-type-length - 1),
  // and the target type is signed, then the resulting value will be out of
  // range.  However, this value is correct and must be coerced to the target
  // type.  E.G., After appying "not" to a BigInteger, the result is
  // a BigInteger that represents its 2's-complement (~5 => -6 in a byte-space).
  // In this example, the signum toggles and the magnatude is 6.  If the target
  // type of this value were unsigned short, it must be coerced to a positive
  // number whose bits truly represent -6 in 2's-complement (250 in a byte-space).
  //
  // Also, floating types may now be intialized with any integer expression.
  // The result must be coerced to Double.
  //
  // Use the following routines to coerce this expression's value to its
  // "target" type.

  /**
   * Coerces a number to the target type of this expression.
   * @param  number  The number to coerce.
   * @return  the value of number coerced to the (target) type of
   *  this expression.
   **/
  public Object coerceToTarget (Object obj)
  {
    if (obj instanceof BigInteger)
    {
      if (type ().indexOf ("unsigned") >= 0)
        return toUnsignedTarget ((BigInteger)obj);
      else
        return toSignedTarget ((BigInteger)obj);
    }
    return obj;
  } // coerceToTarget

  /**
   * Coerces an integral value (BigInteger) to its corresponding unsigned
   * representation, if the target type of this expression is unsigned.
   * @param b The BigInteger to be coerced.
   * @return the value of an integral type coerced to its corresponding
   *  unsigned integral type, if the target type of this expression is
   *  unsigned.
   **/
  protected BigInteger toUnsignedTarget (BigInteger b)
  {
    if (type ().equals ("unsigned short")) // target type of this expression
    {
      if (b != null && b.compareTo (zero) < 0) // error if value < min = -(2**(l-1)).
        return b.add (twoPow16);
    }
    else if (type ().equals ("unsigned long"))
    {
      if (b != null && b.compareTo (zero) < 0)
        return b.add (twoPow32);
    }
    else if (type ().equals ("unsigned long long"))
    {
      if (b != null && b.compareTo (zero) < 0)
        return b.add (twoPow64);
    }
    return b;
  } // toUnsignedTarget

  /**
   * Coerces an integral value (BigInteger) to its corresponding signed
   * representation, if the target type of this expression is signed.
   * @param  b  The BigInteger to be coerced.
   * @return  the value of an integral type coerced to its corresponding
   *  signed integral type, if the target type of this expression is
   *  signed.
   **/
  protected BigInteger toSignedTarget (BigInteger b)
  {
    if (type ().equals ("short"))
    {
      if (b != null && b.compareTo (sMax) > 0)
        return b.subtract (twoPow16);
    }
    else if (type ().equals ("long"))
    {
      if (b != null && b.compareTo (lMax) > 0)
        return b.subtract (twoPow32);
    }
    else if (type ().equals ("long long"))
    {
      if (b != null && b.compareTo (llMax) > 0)
        return b.subtract (twoPow64);
    }
    return b;
  } // toSignedTarget

  /**
   * Return the unsigned value of a BigInteger.
   **/
  protected BigInteger toUnsigned (BigInteger b)
  {
    if (b != null && b.signum () == -1)
      if (type ().equals ("short"))
        return b.add (twoPow16);
      else if (type ().equals ("long"))
        return b.add (twoPow32);
      else if (type ().equals ("long long"))
        return b.add (twoPow64);
    return b;
  }

  // Integral-type boundaries.

  public static final BigInteger negOne = BigInteger.valueOf (-1);
  public static final BigInteger zero   = BigInteger.valueOf (0);
  public static final BigInteger one    = BigInteger.valueOf (1);
  public static final BigInteger two    = BigInteger.valueOf (2);

  public static final BigInteger twoPow15 = two.pow (15);
  public static final BigInteger twoPow16 = two.pow (16);
  public static final BigInteger twoPow31 = two.pow (31);
  public static final BigInteger twoPow32 = two.pow (32);
  public static final BigInteger twoPow63 = two.pow (63);
  public static final BigInteger twoPow64 = two.pow (64);

  public static final BigInteger sMax = BigInteger.valueOf (Short.MAX_VALUE);
  public static final BigInteger sMin = BigInteger.valueOf (Short.MAX_VALUE);

  public static final BigInteger usMax = sMax.multiply (two).add (one);
  public static final BigInteger usMin = zero;

  public static final BigInteger lMax = BigInteger.valueOf (Integer.MAX_VALUE);
  public static final BigInteger lMin = BigInteger.valueOf (Integer.MAX_VALUE);

  public static final BigInteger ulMax = lMax.multiply (two).add (one);
  public static final BigInteger ulMin = zero;

  public static final BigInteger llMax = BigInteger.valueOf (Long.MAX_VALUE);
  public static final BigInteger llMin = BigInteger.valueOf (Long.MIN_VALUE);

  public static final BigInteger ullMax = llMax.multiply (two).add (one);
  public static final BigInteger ullMin = zero;

  /**
   * Value of this expression: Boolean, Char, Byte, BigInteger, Double,
   * String, Expression, ConstEntry.
   **/
  private Object _value = null;
  /**
   * String representation of this expression.
   **/
  private String _rep   = null;
  /**
   * Computation type of this (sub)expression = Target type for now.
   **/
  private String _type  = null;
} // abstract class Expression
