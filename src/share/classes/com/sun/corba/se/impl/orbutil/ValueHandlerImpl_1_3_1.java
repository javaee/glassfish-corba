/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.orbutil;

import java.io.IOException ;

import org.omg.CORBA.TCKind;

import com.sun.corba.se.impl.io.IIOPInputStream;
import com.sun.corba.se.impl.io.IIOPOutputStream;

import com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3_1 ;
import com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3_1 ;

/**
 * This class overrides behavior of our current ValueHandlerImpl to
 * provide backwards compatibility with JDK 1.3.1.
 */
public class ValueHandlerImpl_1_3_1 
    extends com.sun.corba.se.impl.io.ValueHandlerImpl 
{
    public ValueHandlerImpl_1_3_1() {}

    /**
     * Our JDK 1.3 and JDK 1.3.1 behavior subclasses override this.
     * The correct behavior is for a Java char to map to a CORBA wchar,
     * but our older code mapped it to a CORBA char.
     */
    protected TCKind getJavaCharTCKind() {
        return TCKind.tk_char;
    }

    /**
     * RepositoryId_1_3_1 performs an incorrect repId calculation
     * when using serialPersistentFields and one of the fields no longer
     * exists on the class itself.
     */
    public boolean useFullValueDescription(Class clazz, String repositoryID) 
	throws java.io.IOException
    {        
        return RepositoryId_1_3_1.useFullValueDescription(clazz, repositoryID);
    }
    
    /**
     * Installs the legacy IIOPOutputStream_1_3_1 which does
     * PutFields/GetFields incorrectly.  Bug 4407244.
     */
    protected IIOPOutputStream createOutputStream() {
	try {
	    return new IIOPOutputStream_1_3_1() ;
	} catch (IOException exc) {
	    throw utilWrapper.exceptionInCreateIiopOutputStream( exc ) ;
	}
    }

    /**
     * Installs the legacy IIOPInputStream_1_3_1 which does
     * PutFields/GetFields incorrectly.  Bug 4407244.
     */
    protected IIOPInputStream createInputStream() {
	try {
	    return new IIOPInputStream_1_3_1() ;
	} catch (IOException exc) {
	    throw utilWrapper.exceptionInCreateIiopInputStream( exc ) ;
	}
    }
}
