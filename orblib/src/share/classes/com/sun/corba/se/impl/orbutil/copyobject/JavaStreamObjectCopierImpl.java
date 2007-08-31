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

package com.sun.corba.se.impl.orbutil.copyobject ;

import java.io.InputStream ;
import java.io.OutputStream ;
import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;

import com.sun.corba.se.spi.orbutil.copyobject.ObjectCopier ;

public class JavaStreamObjectCopierImpl implements ObjectCopier {
    public Object copy(Object obj, boolean debug ) {
	return copy( obj ) ;
    }

    public Object copy(Object obj) {
	try {
	    ByteArrayOutputStream os = new ByteArrayOutputStream( 10000 ) ;
	    ObjectOutputStream oos = new ObjectOutputStream( os ) ;
	    oos.writeObject( obj ) ;

	    byte[] arr = os.toByteArray() ;
	    InputStream is = new ByteArrayInputStream( arr ) ;
	    ObjectInputStream ois = new ObjectInputStream( is ) ;

	    return ois.readObject();
	} catch (Exception exc) {
	    System.out.println( "Failed with exception:" + exc ) ;
	    return null ;
	}
    }
}
