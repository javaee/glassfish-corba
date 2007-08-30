/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2006-2007 Sun Microsystems, Inc. All rights reserved.
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

import com.sun.corba.se.impl.codegen.ClassGenerator ;

import com.sun.corba.se.spi.codegen.Expression ;
import com.sun.corba.se.spi.codegen.Type ;

import corba.codegen.ClassGeneratorFactory ;

import static java.lang.reflect.Modifier.* ;
import static com.sun.corba.se.spi.codegen.Wrapper.* ;

public class MyRemote__Adapter_Simplified_gen implements ClassGeneratorFactory {
    public String className() {
	return "MyRemote__Adapter_Simplified" ;
    }
    
    public ClassGenerator evaluate() {
	_clear() ;
	_package( "corba.codegen.gen" ) ;
	Type Exception = _import( "java.lang.Exception" ) ;
	Type Throwable = _import( "java.lang.Throwable" ) ;
	Type Serializable = _import( "java.io.Serializable" ) ;
	Type EJBObject = _import( "javax.ejb.EJBObject" ) ;
	Type EJBException = _import( "javax.ejb.EJBException" ) ;
	Type RemoteException = _import( "java.rmi.RemoteException" ) ;
	Type AppException = _import( "corba.codegen.lib.AppException" ) ;
	Type MyBusinessIntf = _import( "corba.codegen.lib.MyBusinessIntf" ) ;
	Type EJBObjectBase = _import( "corba.codegen.lib.EJBObjectBase" ) ;
	Type MyRemote = _import( "corba.codegen.gen.MyRemote" ) ;

	_class( PUBLIC, className(), EJBObjectBase,
	    EJBObject, MyBusinessIntf, Serializable) ; {
	    
	    Expression myRemote = _data( PRIVATE, MyRemote, "myRemote" ) ;

	    _constructor( PUBLIC ) ; {
		Expression arg = _arg( MyRemote, "arg" ) ;
	    _body() ;
		_expr(_super()) ;
		_assign(myRemote,arg) ;
	    _end() ; }

	    _method( PUBLIC, _void(), "doSomething" ) ; {
	    _body() ;
		_try() ;
		    _expr( _call( myRemote, "doSomething")) ;
		Expression re = _catch( RemoteException, "re" ) ;
		    _throw( _new( EJBException, re ) ) ;
		_end() ;
	    _end() ; }

	    _method( PUBLIC, _int(), "doSomethingElse", AppException) ; {
	    _body() ;
		_try() ;
		    _return( _call( myRemote, "doSomethingElse")) ;
		Expression re = _catch( RemoteException, "re" ) ;
		    Expression exc = _define( EJBException, "exc", 
			_new( EJBException )) ;
		    _expr( _call( exc, "initCause", re )) ;
		    _throw( exc ) ;
		_end() ;
	    _end() ; }

	    _method( PUBLIC, _int(), "echo") ; {
		Expression arg = _arg( _int(), "arg" ) ;
	    _body() ;
		_try() ;
		    _return( _call( myRemote, "echo", arg)) ;
		Expression re = _catch( RemoteException, "re" ) ;
		    _throw( _new( EJBException, re ) ) ;
		_end() ;
	    _end() ; }
	_end() ; }

	return _classGenerator() ;
    }
}
