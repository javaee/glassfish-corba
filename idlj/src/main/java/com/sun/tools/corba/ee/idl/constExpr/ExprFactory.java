/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

public interface ExprFactory
{
  And and (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.BooleanAnd booleanAnd (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.BooleanNot booleanNot (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.BooleanOr booleanOr (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Divide divide (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Equal equal (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.GreaterEqual greaterEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.GreaterThan greaterThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.LessEqual lessEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.LessThan lessThan (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Minus minus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Modulo modulo (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Negative negative (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.Not not (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.NotEqual notEqual (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Or or (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Plus plus (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Positive positive (com.sun.tools.corba.ee.idl.constExpr.Expression operand);
  com.sun.tools.corba.ee.idl.constExpr.ShiftLeft shiftLeft (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.ShiftRight shiftRight (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Character charValue,
                         boolean isWide );
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Boolean booleanValue);
  //daz  Terminal     terminal (String representation, Long longValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, Double doubleValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String representation, BigInteger bigIntegerValue);
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (String stringValue, boolean isWide );
  com.sun.tools.corba.ee.idl.constExpr.Terminal terminal (ConstEntry constReference);
  com.sun.tools.corba.ee.idl.constExpr.Times times (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
  com.sun.tools.corba.ee.idl.constExpr.Xor xor (com.sun.tools.corba.ee.idl.constExpr.Expression left, com.sun.tools.corba.ee.idl.constExpr.Expression right);
} // interface ExprFactory
