/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.spi.btrace ;

/** Static utility methods that can be targeted by btrace to report on results of method
 * calls.  This is a simple workaround for an annoying btrace problem: while btrace can
 * intercept arguments, it cannot see results.  So, to make a result visible, simple 
 * change
 * <p>
 * return x ;
 * <p>
 * to
 * <p>
 * return value(x) ;
 * <p>
 * and then make sure that the btrace script prints out the arguments passed into a value call
 * in the Return class.
 * <p>
 * This can also be used to report values in the middle of a method.  Here it's
 * useful to know which local variable is being reported, so we can just call
 * <p>
 * Return.value( "foo", foo ) ;
 * <p>
 * to report the value of variable foo at some point in a method.
 */
public class Return {
    private Return() {} 

    public static boolean value( boolean arg ) {
        return arg ;
    }

    public static byte value( byte arg ) {
        return arg ;
    }

    public static char value( char arg ) {
        return arg ;
    }

    public static short value( short arg ) {
        return arg ;
    }

    public static int value( int arg ) {
        return arg ;
    }

    public static long value( long arg ) {
        return arg ;
    }

    public static float value( float arg ) {
        return arg ;
    }

    public static double value( double arg ) {
        return arg ;
    }

    public static <T> T value( T arg ) {
        return arg ;
    }

    public static boolean value( String msg, boolean arg ) {
        return arg ;
    }

    public static byte value( String msg, byte arg ) {
        return arg ;
    }

    public static char value( String msg, char arg ) {
        return arg ;
    }

    public static short value( String msg, short arg ) {
        return arg ;
    }

    public static int value( String msg, int arg ) {
        return arg ;
    }

    public static long value( String msg, long arg ) {
        return arg ;
    }

    public static float value( String msg, float arg ) {
        return arg ;
    }

    public static double value( String msg, double arg ) {
        return arg ;
    }

    public static <T> T value( String msg, T arg ) {
        return arg ;
    }
}
