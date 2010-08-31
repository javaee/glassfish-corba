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

package com.sun.corba.se.spi.orbutil.misc ;

import java.util.EmptyStackException ;

// We implement a Stack here instead of using java.util.Stack because
// java.util.Stack is thread-safe, negatively impacting performance.
// We use an ArrayList instead since it is not thread-safe.  
// RequestInfoStack is used quite frequently.
public class StackImpl {
    // The stack for RequestInfo objects.  
    private Object[] data = new Object[3] ;
    private int top = -1 ;

    // Tests if this stack is empty.
    public final boolean empty() {
	return top == -1;
    }

    // Looks at the object at the top of this stack without removing it
    // from the stack.
    public final Object peek() {
	if (empty()) 
	    throw new EmptyStackException();

	return data[ top ];
    }

    // Removes the object at the top of this stack and returns that 
    // object as the value of this function.
    public final Object pop() {
	Object obj = peek() ;
	data[top] = null ;
	top-- ;
	return obj;
    }

    private void ensure() 
    {
	if (top == (data.length-1)) {
	    int newSize = 2*data.length ;
	    Object[] newData = new Object[ newSize ] ;
	    System.arraycopy( data, 0, newData, 0, data.length ) ;
	    data = newData ;
	}
    }

    // Pushes an item onto the top of the stack
    public final Object push( Object item ) {
	ensure() ;
	top++ ;
	data[top] = item;
	return item;
    }
}
