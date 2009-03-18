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

package corba.codegen ;

import java.lang.reflect.Method ;

import com.sun.corba.se.spi.orbutil.generic.NullaryFunction ;

import static com.sun.corba.se.spi.orbutil.codegen.Wrapper.* ;

import com.sun.corba.se.impl.orbutil.codegen.ClassGeneratorImpl ;

public abstract class CodeGeneratorBase implements SimpleCodeGenerator {
    private String className ;
    private long astTime ;

    protected CodeGeneratorBase( ClassGeneratorFactory cgf ) {
	_clear() ;

	long start = System.nanoTime() ;
	try {
	    // Create the ClassGeneratorImpl as a side effect of this
	    // call.  It is available through the Wrapper API.
	    cgf.evaluate() ;
	} finally {
	    astTime = (System.nanoTime() - start) / 1000 ;
	}

	className = _classGenerator().name() ;
    }

    public String className() {
	return className ;
    }

    public long astConstructionTime() {
	return astTime ;
    }

    public String toString() {
	return getClass().getName() + "[" + className + "]" ;
    }

    public void reportTimes() {
	Class cls = this.getClass() ;
	Method[] methods = cls.getMethods() ;
	System.out.println( "Timings for generator for class " + className ) ;
	System.out.println( "=======================================================" ) ;
	for (Method method : methods) {
	    String name = method.getName() ;
	    String suffix = "Time" ;
	    if (name.endsWith( suffix ) 
		&& method.getParameterTypes().length == 0
		&& method.getReturnType().equals( long.class )) {
		try {
		    long time = (Long)method.invoke( this ) ;
		    String title = name.substring( 0, name.length() - suffix.length() ) ;
		    System.out.printf( "%32s : %10d microseconds\n", name, time ) ;
		} catch (Exception exc) {
		    System.out.println( "Error in calling method " + name + ":" + exc ) ;
		}
	    }
	}
	System.out.println( "=======================================================" ) ;
    }
}
