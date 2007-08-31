/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2005-2007 Sun Microsystems, Inc. All rights reserved.
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

package corba.codegen.test ;

import static java.lang.reflect.Modifier.* ;
import static com.sun.corba.se.spi.orbutil.codegen.Wrapper.* ;

import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Expression ;
import com.sun.corba.se.spi.orbutil.codegen.Signature ;

import com.sun.corba.se.impl.orbutil.codegen.ClassGenerator ;

import corba.codegen.ClassGeneratorFactory ;

public class Flow_gen implements ClassGeneratorFactory {
    private static final Signature traceSignature = 
	_s( _boolean(), _int() ) ;
    private static final Signature expectClassSignature = 
	_s( _void(), _Object(), _Class() ) ;
    private static final Signature expectIntSignature = 
	_s( _void(), _int(), _int() ) ;

    public String className() {
	return "Flow" ;
    }

    private void startTestMethod( String name ) {
	_method( PUBLIC, _void(), name ) ;
	// method has no arguments
	_body() ;
    }

    private Expression traceCall( int arg ) {
	return _call( _this(), "trace", traceSignature, _const(arg)) ;
    }

    private Expression expectClassCall( Expression obj, Type classType ) {
	return _call( _this(), "expect", expectClassSignature, obj,
	    _const(classType) ) ;
    }
	    
    private void simpleIfMethod() {
	startTestMethod( "simpleIf" ) ;
	    _if( traceCall(1) ) ;
		_expr(traceCall(2)) ;
	    _else() ;
		_expr(traceCall(3)) ;
	    _end() ;
	    _expr(traceCall(4)) ;
	_end() ;
    }

    private void complexIfMethod() {
	startTestMethod( "complexIf" ) ;
	    _if( traceCall(1) ) ;
		_if( traceCall(2) ) ;
		    _expr(traceCall(3)) ;
		_else() ;
		    _expr(traceCall(4)) ;
		_end() ;
		_expr(traceCall(5)) ;
		_if( traceCall(6) ) ;
		    _expr(traceCall(7)) ;
		    _if( traceCall(8) ) ;
			_expr(traceCall(9)) ;
		    _else() ;
			_expr(traceCall(10)) ;
		    _end() ;
		_else() ;
		    _expr(traceCall(11)) ;
		_end() ;
	    _else() ;
		_if( traceCall(12) ) ;
		    _expr(traceCall(13)) ;
		_else() ;
		    _expr(traceCall(14)) ;
		    _if( traceCall(15) ) ;
			_expr(traceCall(16)) ;
		    _else() ;
			_expr(traceCall(17)) ;
		    _end() ;
		_end() ;
	    _end() ;
	    _expr(traceCall(18));
	_end() ;
    }

    private void simpleTryCatchMethod() {
	startTestMethod( "simpleTryCatch" ) ;
	    _expr(traceCall(1)) ;
	    _try() ;
		_expr(traceCall(2)) ;
		_expr(traceCall(3)) ;
	    _catch( _t("FirstException"), "exc" ) ;
		_expr(expectClassCall( _v("exc"), _t("FirstException"))) ;
		_expr(traceCall(4)) ;
		_expr(traceCall(5)) ;
	    _end() ;
	    _expr(traceCall(6)) ;
	_end() ;
    }

    private void simpleTryCatchFinallyMethod() {
	startTestMethod( "simpleTryCatchFinally" ) ;
	    _expr(traceCall(1)) ;
	    _try() ;
		_expr(traceCall(2)) ;
		_expr(traceCall(3)) ;
	    _catch( _t("FirstException"), "exc" ) ;
		_expr(expectClassCall( _v("exc"), _t("FirstException"))) ;
		_expr(traceCall(4)) ;
		_expr(traceCall(5)) ;
	    _finally() ;
		_expr(traceCall(6)) ;
		_expr(traceCall(7)) ;
	    _end() ;
	    _expr(traceCall(8)) ;
	_end() ;
    }

    public ClassGenerator evaluate() {
	_clear() ;
	_package( "corba.codegen.gen" ) ;
	_import( "corba.codegen.ControlBase" ) ;
	_import( "corba.codegen.BaseException" ) ;
	_import( "corba.codegen.FirstException" ) ;
	_import( "corba.codegen.SecondException" ) ;

	_class( PUBLIC, className(), _t("ControlBase") ) ;
	    // Simple default constructor
	    _constructor( PUBLIC ) ;
	    _body() ;
		_expr(_super(_s(_void()))) ;
	    _end() ;

	    // generate all of the test methods
	    simpleIfMethod() ;
	    complexIfMethod() ;
	    simpleTryCatchMethod() ;
	    simpleTryCatchFinallyMethod() ;
	_end() ; // of Flow_gen class

	return _classGenerator() ;
   }
}
