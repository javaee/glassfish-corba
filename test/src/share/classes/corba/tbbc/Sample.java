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

package corba.tbbc ;

import org.glassfish.pfl.dynamic.codegen.spi.Expression;
import org.glassfish.pfl.dynamic.codegen.spi.Type;
import java.lang.reflect.Method ;

import static java.lang.reflect.Modifier.* ;
import static org.glassfish.pfl.dynamic.codegen.spi.Wrapper.* ;

public class Sample {

    public static void main( String[] args ) {
        Type ArrayList = _import( "java.util.ArrayList" ) ;
        Type StringArray = Type._array( Type._String() ) ;
        Type System = _import( "java.lang.String" ) ;

        _class( PUBLIC, "MyClass", Type._Object() ) ; {
            final Expression list = _data( PUBLIC, ArrayList, "list" ) ;

            _constructor( PUBLIC )  ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                final Expression b = _arg( ArrayList, "b" ) ;
                _body() ;
                    _super() ;
                    _assign( list, _call( _this(), "bar", a, b ) ) ;
                _end() ; // of constructor
            }

            _method( PUBLIC|STATIC, _thisClass(), "foo" ) ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                _body() ;
                    _return( _new( _thisClass(), a, _new( ArrayList ) ) ) ;
                _end() ; // of method
            }

            _method( PUBLIC, ArrayList, "bar" ) ; {
                final Expression a = _arg( Type._String(), "a" ) ;
                final Expression b = _arg( ArrayList, "b" ) ;
                _body() ;
                    _call( b, "add", _call( a, "toLowerCase" ) ) ;
                    _return( b ) ;
                _end() ; // of method
            }

            _method( PUBLIC, ArrayList, "getList" ) ; {
                _body() ;
                    _return( list ) ;
                _end() ; // of method
            }

            _method( PUBLIC|STATIC, Type._void(), "main" ) ; {
                final Expression margs = _arg( StringArray, "args" ) ;
                _body() ;
                    Expression sout = _field( System, "out" ) ;
                    Expression fooArgs0 = _call( _thisClass(), "foo", _index( margs, _const( 0 ) ) ) ;
                    _call( sout, "println", _call(  fooArgs0, "getList" ) ) ;
                _end() ; // of method
            }

            _end() ; // of class
        }

        Class genClass = Sample.class ;
        Class cls = _generate( genClass.getClassLoader(), genClass.getProtectionDomain(), null ) ;
        
        try {
            Method m = cls.getDeclaredMethod( "main", String[].class ) ;
            m.invoke( null, (Object[])args ) ;
        } catch (Exception exc) {
            java.lang.System.out.println( "Exception: " + exc ) ;
            exc.printStackTrace() ;
        }
    }
}
