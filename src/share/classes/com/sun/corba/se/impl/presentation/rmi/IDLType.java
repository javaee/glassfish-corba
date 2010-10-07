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

package com.sun.corba.se.impl.presentation.rmi ;

/**
 * Holds information about the OMG IDL mapping of a Java type.
 */
public class IDLType {

    private Class cl_;

    // terminology for OMG IDL type package name
    private String[] modules_;

    // name of element within module
    private String memberName_;


    public IDLType(Class cl, String[] modules, String memberName) {
        cl_ = cl;
        modules_ = modules;
        memberName_ = memberName;
    }

    public IDLType(Class cl, String memberName) {
	this( cl, new String[0], memberName ) ;
    }

    public Class getJavaClass() {
        return cl_;
    }

    public String[] getModules()
    {
	return modules_ ;
    }
    
    public String makeConcatenatedName( char separator, boolean fixIDLKeywords ) {
	StringBuffer sbuff = new StringBuffer() ;
	for (int ctr=0; ctr<modules_.length; ctr++) {
	    String mod = modules_[ctr] ;
	    if (ctr>0)
		sbuff.append( separator ) ;
	    
	    if (fixIDLKeywords && IDLNameTranslatorImpl.isIDLKeyword(mod))
		mod = IDLNameTranslatorImpl.mangleIDLKeywordClash( mod ) ;

	    sbuff.append( mod ) ;
	}

        return sbuff.toString() ;
    }
   
    public String getModuleName() {
	// Note that this should probably be makeConcatenatedName( '/', true )
	// for spec compliance,
	// but rmic does it this way, so we'll leave this.
	// The effect is that an overloaded method like
	// void foo( bar.typedef.Baz ) 
	// will get an IDL name of foo__bar_typedef_Baz instead of
	// foo__bar__typedef_Baz (note the extra _ before typedef).
	return makeConcatenatedName( '_', false ) ;
    }

    public String getExceptionName() {
	// Here we will check for IDL keyword collisions (see bug 5010332).
	// This means that the repository ID for 
	// foo.exception.SomeException is
	// "IDL:foo/_exception/SomeEx:1.0" (note the underscore in front
	// of the exception module name).
	String modName = makeConcatenatedName( '/', true ) ;

	String suffix = "Exception" ;
	String excName = memberName_ ;
	if (excName.endsWith( suffix )) {
	    int last = excName.length() - suffix.length() ;
	    excName = excName.substring( 0, last ) ;
	}
   
	// See bug 4989312: we must always add the Ex.
	excName += "Ex" ;

	if (modName.length() == 0)
	    return "IDL:" + excName + ":1.0" ; 
	else
	    return "IDL:" + modName + '/' + excName + ":1.0" ; 
    }

    public String getMemberName() {
        return memberName_;
    }
    
    /**
     * True if this type doesn't have a containing module.  This
     * would be true of a java type defined in the default package
     * or a primitive.
     */
    public boolean hasModule() {
        return (modules_.length > 0) ;
    }
}
