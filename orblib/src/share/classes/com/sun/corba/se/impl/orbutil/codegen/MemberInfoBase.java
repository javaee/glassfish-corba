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

package com.sun.corba.se.impl.orbutil.codegen;

import java.util.List ;
import java.util.ArrayList ;

import java.lang.reflect.Modifier ;

import com.sun.corba.se.spi.orbutil.codegen.Signature ;
import com.sun.corba.se.spi.orbutil.codegen.Type ;
import com.sun.corba.se.spi.orbutil.codegen.Variable ;
import com.sun.corba.se.spi.orbutil.codegen.MemberInfo ;
import com.sun.corba.se.spi.orbutil.codegen.ClassInfo ;

public class MemberInfoBase implements MemberInfo {
    private ClassInfo myClassInfo ;
    private int modifiers ;
    private String name ;

    public MemberInfoBase( ClassInfo myClassInfo, int modifiers,
	String name ) {

	this.myClassInfo = myClassInfo ;
	this.modifiers = modifiers ;
	this.name = name ;
    }

    public ClassInfo myClassInfo() {
	return this.myClassInfo ;
    }

    public int modifiers() {
	return this.modifiers ;
    }

    public String name() {
	return this.name ;
    }

    public boolean isAccessibleInContext( ClassInfo definingClass,
	ClassInfo accessClass ) {

	if (Modifier.isPublic( modifiers )) {
	    return true ;
	}

	if (Modifier.isPrivate( modifiers)) {
	    return myClassInfo.name().equals( definingClass.name() ) ;
	}

	if (Modifier.isProtected( modifiers)) {
	    if (myClassInfo.pkgName().equals( definingClass.pkgName())) {
		return true ;
	    } else {
		return definingClass.isSubclass( myClassInfo ) &&
		    accessClass.isSubclass( definingClass ) ;
	    }
	}

	// check default access
	return myClassInfo.pkgName().equals( definingClass.pkgName() ) ;
    }

    public int hashCode() {
	return name.hashCode() ^ modifiers ;
    }

    public boolean equals( Object obj ) {
	if (!(obj instanceof MemberInfo))
	    return false ;

	if (obj == this) 
	    return true ;

	MemberInfo other = MemberInfo.class.cast( obj ) ;

	return name.equals(other.name()) &&
	    modifiers == other.modifiers() ; 
    }

    public String toString() {
	return this.getClass().getName() + "[" + Modifier.toString( modifiers ) 
	    + name + "]" ;
    }
}

