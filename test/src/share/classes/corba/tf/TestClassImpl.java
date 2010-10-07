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
package corba.tf;

import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;

/**
 *
 * @author ken
 */
@A @B @C
public class TestClassImpl implements TestClass {
    static {
        System.out.println( "Start of <clinit>" ) ;
    }

    @A
    private long increment( long x ) {
        return x + 1 ;
    }

    @A
    private long decrement( long x ) {
        return x - 1 ;
    }

    @A
    private boolean is0( long x ) {
        return x==0 ;
    }

    @A
    private boolean is1( long x ) {
        return x==1 ;
    }

    @InfoMethod
    private void bigAddValue( String msg, long value ) { }

    @InfoMethod
    private void bigMultValue( String msg, long value ) { }

    @B
    public long add( long a, long b ) {
        if ((a<0) || (b<0)) {
            throw new RuntimeException( "Negative not supported" ) ;
        }

        if (is0(b)) {
            return a ;
        } else {
            if (b > 100) {
                bigAddValue( "Large argument for add", b ) ;
            }

            return add( increment(a), decrement(b) ) ;
        }
    }

    @C
    public long mult( long a, long b ) {
        if ((a<0) || (b<0)) {
            throw new RuntimeException( "Negative not supported" ) ;
        }

        if (is0(b)) {
            return 0 ;
        } else if (is1(b)) {
            return a ;
        } else {
            if (b > 10) {
                bigMultValue( "Large argument for mult", b ) ;
            }

            long decRes = decrement(b) ;
            long multRes = mult( a, decRes ) ;
            return add( a, multRes ) ;
        }
    }
}
