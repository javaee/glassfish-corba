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
import static com.sun.corba.se.spi.codegen.Wrapper.* ;

import com.sun.corba.se.spi.codegen.Type ;
import com.sun.corba.se.spi.codegen.Expression ;

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;

import com.sun.corba.se.impl.codegen.ClassGenerator ;

import corba.codegen.ClassGeneratorFactory ;

public class _DImpl_Tie_gen implements ClassGeneratorFactory {
    public String className() {
	return "_DImpl_Tie" ;
    }

    // Some special types that we can't just _import
    // due to conflicts, or the type of an array.
    private static final Type STRING_ARR = _array( 
	_String() ) ;
    private static final Type ORB23 = _t( "org.omg.CORBA_2_3.ORB" ) ;
    private static final Type INPUT_STREAM23 = _t( 
	"org.omg.CORBA_2_3.portable.InputStream" ) ;
    private static final Type BYTE_ARR = _array(_byte()) ;
    private static final Type CORBA_OBJECT = _t(
	"org.omg.CORBA.Object" ) ;

    public ClassGenerator evaluate() {
	_package( "corba.codegen.gen" ) ;
	_import( "org.omg.PortableServer.Servant" ) ;
	_import( "javax.rmi.CORBA.Tie" ) ;
	_import( "corba.codegen.lib.DImpl" ) ;
	_import( "java.lang.String" ) ;
	_import( "java.rmi.Remote" ) ;
	_import( "java.lang.Object" ) ;
	_import( "org.omg.PortableServer.POAPackage.WrongPolicy" ) ;
	_import( "org.omg.PortableServer.POAPackage.ObjectNotActive" ) ;
	_import( "org.omg.PortableServer.POAPackage.ServantNotActive" ) ;
	_import( "javax.rmi.CORBA.Util" ) ;
	_import( "org.omg.CORBA.BAD_OPERATION" ) ;
	_import( "org.omg.CORBA.ORB" ) ;
	_import( "org.omg.CORBA.SystemException" ) ;
	_import( "org.omg.CORBA.portable.InputStream" ) ;
	_import( "org.omg.CORBA.portable.OutputStream" ) ;
	_import( "org.omg.CORBA.portable.ResponseHandler" ) ;
	_import( "org.omg.CORBA.portable.UnknownException" ) ;
	_import( "org.omg.PortableServer.POA" ) ;
	_import( "java.lang.ClassCastException" ) ;
	_import( "java.lang.Throwable" ) ;
	_import( "org.omg.CORBA.Any" ) ;
	_import( "org.omg.CORBA.BAD_PARAM" ) ;

	_class( PUBLIC, className(), _t("Servant"), _t("Tie")) ; 
	    _data( PRIVATE, _t("DImpl"), "target" ) ;
	    _data( PRIVATE|STATIC, STRING_ARR, "_type_ids" ) ;
	    
	    _initializer() ;
		_assign( _v("_type_ids"),
		    _new_array_init( _t("String"), 
			_const( "RMI:tests.D:0000000000000000" ),
			_const( "RMI:tests.B:0000000000000000" ),
			_const( "RMI:tests.A:0000000000000000" ))) ;
	    _end() ;

	    _constructor( PUBLIC ) ;
	    _body() ;
		_expr(_super(_s(_void()))) ;
		_assign(_v("target"),_null()) ;
	    _end() ;

	    _method( PUBLIC, _void(), "setTarget" ) ;
		_arg( _t("Remote"), "target" ) ;
	    _body() ;
		_assign( _field( _this(), "target" ), _cast( _t("DImpl"), _v("target"))) ;
	    _end() ;	

	    _method( PUBLIC, _t("Remote"), "getTarget" ) ;
	    _body() ;
		_return(_v("target")) ;
	    _end() ;

	    _method( PUBLIC, CORBA_OBJECT, "thisObject" ) ;
	    _body() ;
		_return( // _s("org.omg.CORBA.Object()") 
		    _call( _this(), "_this_object", 
			_s(_t("org.omg.CORBA.Object")))) ;
	    _end() ;

	    _method( PUBLIC, _void(), "deactivate" ) ;
	    _body() ;
		_try() ;
		    NullaryFunction<Expression> mpc = 
			new NullaryFunction<Expression>() {
			    public Expression evaluate() {
				return _call( _this(), "_poa", _s(_t("POA"))) ;
			    }
			} ;

		    _expr(
			_call( mpc.evaluate(), "deactivate_object",
			    _s(_void(),BYTE_ARR), _call( mpc.evaluate(), "servant_to_id", 
				    // _s("byte[](Servant)")
				    _s(BYTE_ARR,_t("Servant")), 
				    _this()))) ;
		_catch( _t("WrongPolicy"), "exception" ) ;
		_catch( _t("ObjectNotActive"), "exception" ) ;
		_catch( _t("ServantNotActive"), "exception" ) ;
		_end() ;
	    _end() ;

	    _method( PUBLIC, _t("ORB"), "orb" ) ;
	    _body() ;
		_return( _call( _this(), "_orb", _s(_t("ORB")))) ;
	    _end() ;

	    _method( PUBLIC, _void(), "orb" ) ;
		_arg( _t("ORB"), "orb" ) ;
	    _body() ;
		_try() ;
		    _expr( _call( _cast( ORB23, _v("orb")), "set_delegate",
			_s( _void(), _t("Object")), _this())) ;
		_catch( _t("ClassCastException"), "e" ) ;
		    _throw( _new( _t("BAD_PARAM"), _s(_void(),_String()), 
			_const( 
			    "POA Servant request an instance of org.omg.CORBA_2_3.ORB" ))) ;
		_end() ;
	    _end() ;

	    _method( PUBLIC, STRING_ARR, "_all_interfaces" ) ;
		_arg( _t("POA"), "poa" ) ;
		_arg( BYTE_ARR, "objectId" ) ;
	    _body() ;
		_return( _v("_type_ids") ) ;
	    _end() ;
	    // _s("void(OutputStream,Object)")

	    _method( PUBLIC, _t("OutputStream"), "_invoke" ) ; 
		_arg( _t("String"), "method" ) ; 
		_arg( _t("InputStream"), "_in" ) ;
		_arg( _t("ResponseHandler"), "reply" ) ;
	    _body() ;
		_try() ;
		    _define( INPUT_STREAM23, "in",
			_cast( INPUT_STREAM23, _v("_in"))) ;
		    _switch(_call( _v("method"), "length", _s(_int()))) ;
		    _case(5) ;
			_if( _call( _v("method"), "equals", 
			    _s(_boolean(),_Object()), _const("unary"))) ;
			    _define( _t("Object"), "arg0",
				_call( _t("Util"), "readAny", 
				    _s(_Object(),_t("InputStream")), _v("in"))) ;
			    _define( _t("Object"), "result", 
				_call( _v("target"), "unary", 
				    _s(_Object(),_Object()), _v("arg0"))) ;
			    _define( _t("OutputStream"), "out",
				_call( _v("reply"), "createReply", _s(_t("OutputStream")))) ;
			    _expr( 
				_call( _t("Util"), "writeAny", 
				    // _s("void(OutputStream,Object)")
				    _s(_void(),_t("OutputStream"), _Object()), 
					_v("out"), _v("result"))) ;
			    _return( _v("out")) ;
			_end() ; // _if
		    _end() ; // _switch 
		    _throw( 
			_new( _t("BAD_OPERATION"), 
			    _s(_void(),_String()), _const(""))) ;
		_catch( _t("SystemException"), "ex" ) ;
		    _throw( _v("ex") ) ;
		_catch( _t("Throwable"), "ex" ) ;
		    _throw( _new( _t("UnknownException"), 
			_s(_void(),_t("Throwable")), _v("ex"))) ;
		_end() ; // _try()
	    _end() ; // _body() of method _invoke
	_end() ; // of class

	return _classGenerator() ;
    }
}
