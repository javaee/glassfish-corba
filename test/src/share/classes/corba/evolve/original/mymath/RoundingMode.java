/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2001 IBM Corp. All rights reserved.
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

package mymath;

/**
 * Specifies a <i>rounding behavior</i> for numerical operations
 * capable of discarding precision. Each rounding mode indicates how
 * the least significant returned digit of a rounded result is to be
 * calculated.  If fewer digits are returned than the digits needed to
 * represent the exact numerical result, the discarded digits will be
 * referred to as the <i>discarded fraction</i> regardless the digits'
 * contribution to the value of the number.  In other words,
 * considered as a numerical value, the discarded fraction could have
 * an absolute value greater than one.
 *
 * <p>Each rounding mode description includes a table listing how
 * different two-digit decimal values would round to a one digit
 * decimal value under the rounding mode in question.  The result
 * column in the tables could be gotten by creating a
 * <tt>BigDecimal</tt> number with the specified value, forming a
 * {@link MathContext} object with the proper settings
 * (<tt>precision</tt> set to <tt>1</tt>, and the
 * <tt>roundingMode</tt> set to the rounding mode in question), and
 * calling {@link BigDecimal#round round} on this number with the
 * proper <tt>MathContext</tt>.  A summary table showing the results
 * of these rounding operations for all rounding modes appears below.
 *
 *<p>
 *<table border>
 * <caption top><h3>Summary of Rounding Operations Under Different Rounding Modes</h3></caption>
 * <tr><th></th><th colspan=8>Result of rounding input to one digit with the given 
 *                           rounding mode</th>
 * <tr valign=top>
 * <th>Input Number</th>         <th><tt>UP</tt></th> 
 *                                           <th><tt>DOWN</tt></th> 
 *                                                        <th><tt>CEILING</tt></th>
 *                                                                       <th><tt>FLOOR</tt></th>
 *                                                                                    <th><tt>HALF_UP</tt></th>
 *                                                                                                   <th><tt>HALF_DOWN</tt></th>
 *                                                                                                                    <th><tt>HALF_EVEN</tt></th>
 *                                                                                                                                     <th><tt>UNNECESSARY</tt></th>
 *    
 * <tr align=right><td>5.5</td>  <td>6</td>  <td>5</td>    <td>6</td>    <td>5</td>  <td>6</td>      <td>5</td>       <td>6</td>       <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>2.5</td>  <td>3</td>  <td>2</td>    <td>3</td>    <td>2</td>  <td>3</td>      <td>2</td>       <td>2</td>       <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>1.6</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>2</td>      <td>2</td>       <td>2</td>       <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>1.1</td>  <td>2</td>  <td>1</td>    <td>2</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>1.0</td>  <td>1</td>  <td>1</td>    <td>1</td>    <td>1</td>  <td>1</td>      <td>1</td>       <td>1</td>       <td>1</td>                                
 * <tr align=right><td>-1.0</td> <td>-1</td> <td>-1</td>   <td>-1</td>   <td>-1</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>-1</td>                               
 * <tr align=right><td>-1.1</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-1</td>     <td>-1</td>      <td>-1</td>      <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>-1.6</td> <td>-2</td> <td>-1</td>   <td>-1</td>   <td>-2</td> <td>-2</td>     <td>-2</td>      <td>-2</td>      <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>-2.5</td> <td>-3</td> <td>-2</td>   <td>-2</td>   <td>-3</td> <td>-3</td>     <td>-2</td>      <td>-2</td>      <td>throw <tt>ArithmeticException</tt></td>
 * <tr align=right><td>-5.5</td> <td>-6</td> <td>-5</td>   <td>-5</td>   <td>-6</td> <td>-6</td>     <td>-5</td>      <td>-6</td>      <td>throw <tt>ArithmeticException</tt></td>
 *</table>
 *
 * 
 * <p>This <tt>enum</tt> is intended to replace the integer-based
 * enumeration of rounding mode constants in {@link BigDecimal}
 * ({@link BigDecimal#ROUND_UP}, {@link BigDecimal#ROUND_DOWN},
 * etc. ).
 *
 * @see     BigDecimal
 * @see     MathContext
 * @version 1.x 01/xx/xx
 * @author  Josh Bloch
 * @author  Mike Cowlishaw
 * @author  Joseph D. Darcy
 * @since 1.5
 */
public enum RoundingMode {

        /**
         * Rounding mode to round away from zero.  Always increments the
         * digit prior to a non-zero discarded fraction.  Note that this
         * rounding mode never decreases the magnitude of the calculated
         * value.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>UP</tt> rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    UP(BigDecimal.ROUND_UP),
            
        /**
         * Rounding mode to round towards zero.  Never increments the digit
         * prior to a discarded fraction (i.e., truncates).  Note that this
         * rounding mode never increases the magnitude of the calculated value.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>DOWN</tt> rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    DOWN(BigDecimal.ROUND_DOWN),
            
        /**
         * Rounding mode to round towards positive infinity.  If the
         * result is positive, behaves as for <tt>RoundingMode.UP</tt>;
         * if negative, behaves as for <tt>RoundingMode.DOWN</tt>.  Note
         * that this rounding mode never decreases the calculated value.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>CEILING</tt> rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>2</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-1</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    CEILING(BigDecimal.ROUND_CEILING),

        /**
         * Rounding mode to round towards negative infinity.  If the
         * result is positive, behave as for <tt>RoundingMode.DOWN</tt>;
         * if negative, behave as for <tt>RoundingMode.UP</tt>.  Note that
         * this rounding mode never increases the calculated value.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>FLOOR</tt> rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>1</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-2</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    FLOOR(BigDecimal.ROUND_FLOOR),

        /**
         * Rounding mode to round towards &quot;nearest neighbor&quot;
         * unless both neighbors are equidistant, in which case round up.
         * Behaves as for <tt>RoundingMode.UP</tt> if the discarded
         * fraction is &gt;= 0.5; otherwise, behaves as for
         * <tt>RoundingMode.DOWN</tt>.  Note that this is the rounding
         * mode commonly taught at school.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>HALF_UP</tt> rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>3</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-3</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_UP(BigDecimal.ROUND_HALF_UP),

        /**
         * Rounding mode to round towards &quot;nearest neighbor&quot;
         * unless both neighbors are equidistant, in which case round
         * down.  Behaves as for <tt>RoundingMode.UP</tt> if the discarded
         * fraction is &gt; 0.5; otherwise, behaves as for
         * <tt>RoundingMode.DOWN</tt>.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>HALF_DOWN</tt> rounding
         *<tr align=right><td>5.5</td>  <td>5</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-5</td>
         *</table>
         */
    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),

        /**
         * Rounding mode to round towards the &quot;nearest neighbor&quot;
         * unless both neighbors are equidistant, in which case, round
         * towards the even neighbor.  Behaves as for
         * <tt>RoundingMode.HALF_UP</tt> if the digit to the left of the
         * discarded fraction is odd; behaves as for
         * <tt>RoundingMode.HALF_DOWN</tt> if it's even.  Note that this
         * is the rounding mode that statistically minimizes cumulative
         * error when applied repeatedly over a sequence of calculations.
         * It is sometimes known as &quot;Banker's rounding,&quot; and is
         * chiefly used in the USA.  This rounding mode is analogous to
         * the rounding policy used for <tt>float</tt> and <tt>double</tt>
         * arithmetic in Java.
         *
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>HALF_EVEN</tt> rounding
         *<tr align=right><td>5.5</td>  <td>6</td>
         *<tr align=right><td>2.5</td>  <td>2</td>
         *<tr align=right><td>1.6</td>  <td>2</td>
         *<tr align=right><td>1.1</td>  <td>1</td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>-1</td>
         *<tr align=right><td>-1.6</td> <td>-2</td>
         *<tr align=right><td>-2.5</td> <td>-2</td>
         *<tr align=right><td>-5.5</td> <td>-6</td>
         *</table>
         */
    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),

        /**
         * Rounding mode to assert that the requested operation has an exact
         * result, hence no rounding is necessary.  If this rounding mode is
         * specified on an operation that yields an inexact result, an
         * <tt>ArithmeticException</tt> is thrown.
         *<p>Example:
         *<table border>
         *<tr valign=top><th>Input Number</th>
         *    <th>Input rounded to one digit<br> with <tt>UNNECESSARY</tt> rounding
         *<tr align=right><td>5.5</td>  <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>2.5</td>  <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>1.6</td>  <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>1.1</td>  <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>1.0</td>  <td>1</td>
         *<tr align=right><td>-1.0</td> <td>-1</td>
         *<tr align=right><td>-1.1</td> <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>-1.6</td> <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>-2.5</td> <td>throw <tt>ArithmeticException</tt></td>
         *<tr align=right><td>-5.5</td> <td>throw <tt>ArithmeticException</tt></td>     
         *</table>
         */
    UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);

    // Corresponding BigDecimal rounding constant
    final int oldMode;

    /**
     * Constructor
     *
     * @param oldMode The <tt>BigDecimal</tt> constant corresponding to 
     *        this mode
     */
    private RoundingMode(int oldMode) {
        this.oldMode = oldMode;
    }

    /**
     * Returns the <tt>RoundingMode</tt> object corresponding to a
     * legacy integer rounding mode constant in {@link BigDecimal}.
     *
     * @param  rm legacy integer rounding mode to convert
     * @return <tt>RoundingMode</tt> corresponding to the given integer.
     * @throws IllegalArgumentException integer is out of range
     */
    public static RoundingMode valueOf(int rm) {
        switch(rm) {

        case BigDecimal.ROUND_UP:
            return UP;

        case BigDecimal.ROUND_DOWN:
            return DOWN;

        case BigDecimal.ROUND_CEILING:
            return CEILING;
            
        case BigDecimal.ROUND_FLOOR:
            return FLOOR;

        case BigDecimal.ROUND_HALF_UP:
            return HALF_UP;
            
        case BigDecimal.ROUND_HALF_DOWN:
            return HALF_DOWN;
            
        case BigDecimal.ROUND_HALF_EVEN:
            return HALF_EVEN;
            
        case BigDecimal.ROUND_UNNECESSARY:
            return UNNECESSARY;
            
        default:
            throw new IllegalArgumentException("argument out of range");                
        }
    }
}
