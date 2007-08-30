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

package com.sun.corba.se.spi.codegen;

import static com.sun.corba.se.spi.codegen.Wrapper.* ;

public class Primitives {
    private Primitives() {} 

    // If expr.type() is not a primitive, return expr.
    // If expr.type() is a primitive, return an expression
    // that wraps expr in the appropriate primitive wrapper class.
    public static Expression wrap( Expression expr ) {
	Type etype = expr.type() ;
	Signature sig = _s(_void(),etype) ;

	if (etype.equals( _boolean() ) )
	    return _new( _t("java.lang.Boolean"), sig, expr ) ; 
	else if (etype.equals( _byte() ) )
	    return _new( _t("java.lang.Byte"), sig, expr ) ; 
	else if (etype.equals( _char() ) )
	    return _new( _t("java.lang.Character"), sig, expr ) ; 
	else if (etype.equals( _short() ) )
	    return _new( _t("java.lang.Short"), sig, expr ) ; 
	else if (etype.equals( _int() ) )
	    return _new( _t("java.lang.Integer"), sig, expr ) ; 
	else if (etype.equals( _long() ) )
	    return _new( _t("java.lang.Long"), sig, expr ) ; 
	else if (etype.equals( _float() ) )
	    return _new( _t("java.lang.Float"), sig, expr ) ; 
	else if (etype.equals( _double() ) )
	    return _new( _t("java.lang.Double"), sig, expr ) ; 
	else
	    return expr ;
    }

    public static Type getWrapperTypeForPrimitive( Type type ) {
	if (type.equals( _boolean() ) )
	    return _t("java.lang.Boolean"); 
	else if (type.equals( _byte() ) )
	    return _t("java.lang.Byte"); 
	else if (type.equals( _char() ) )
	    return _t("java.lang.Character"); 
	else if (type.equals( _short() ) )
	    return _t("java.lang.Short"); 
	else if (type.equals( _int() ) )
	    return _t("java.lang.Integer"); 
	else if (type.equals( _long() ) )
	    return _t("java.lang.Long"); 
	else if (type.equals( _float() ) )
	    return _t("java.lang.Float"); 
	else if (type.equals( _double() ) )
	    return _t("java.lang.Double"); 
	else
	    return type ;
    }

    // If expr.type() is a primitive wrapper, unwrap it.
    // If expr.type() is not a primitive wrapper,
    // return it.
    public static Expression unwrap( Expression expr ) {
	Type etype = expr.type() ;

	if (etype.equals( _t("java.lang.Boolean")))
	    return _call( expr, "booleanValue", _s(_boolean()) ) ;
	else if (etype.equals( _t("java.lang.Byte") ))
	    return _call( expr, "byteValue", _s(_byte()) ) ;
	else if (etype.equals( _t("java.lang.Character") ))
	    return _call( expr, "charValue", _s(_char()) ) ;
	else if (etype.equals( _t("java.lang.Short") ))
	    return _call( expr, "shortValue", _s(_short()) ) ;
	else if (etype.equals( _t("java.lang.Integer") ))
	    return _call( expr, "intValue", _s(_int()) ) ;
	else if (etype.equals( _t("java.lang.Long") ))
	    return _call( expr, "longValue", _s(_long()) ) ;
	else if (etype.equals( _t("java.lang.Float") ))
	    return _call( expr, "floatValue", _s(_float()) ) ;
	else if (etype.equals( _t("java.lang.Double") ))
	    return _call( expr, "doubleValue", _s(_double()) ) ;
	else return expr ;
    }

    public static Type getPrimitiveTypeForWrapper( Type type) {
	if (type.equals( _t("java.lang.Boolean")))
	    return _boolean() ;
	else if (type.equals( _t("java.lang.Byte") ))
	    return _byte() ;
	else if (type.equals( _t("java.lang.Character") ))
	    return _char() ;
	else if (type.equals( _t("java.lang.Short") ))
	    return _short() ;
	else if (type.equals( _t("java.lang.Integer") ))
	    return _int() ;
	else if (type.equals( _t("java.lang.Long") ))
	    return _long() ;
	else if (type.equals( _t("java.lang.Float") ))
	    return _float() ;
	else if (type.equals( _t("java.lang.Double") ))
	    return _double() ;
	else 
	    return type ;
    }
}
