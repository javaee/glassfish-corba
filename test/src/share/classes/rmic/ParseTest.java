/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1999-2007 Sun Microsystems, Inc. All rights reserved.
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

/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package rmic;

import test.Util;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import sun.tools.java.MemberDefinition;
import sun.rmi.rmic.iiop.Type;
import sun.rmi.rmic.iiop.ArrayType;
import sun.rmi.rmic.iiop.ValueType;
import sun.rmi.rmic.iiop.ImplementationType;
import sun.rmi.rmic.iiop.Constants;
import sun.rmi.rmic.iiop.CompoundType;
import sun.rmi.rmic.iiop.RemoteType;
import sun.rmi.rmic.iiop.AbstractType;
import sun.rmi.rmic.iiop.BatchEnvironment;
import sun.rmi.rmic.iiop.ContextStack;
import sun.tools.java.ClassPath;
import java.lang.reflect.Method;

import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.AfterGroups ;
import org.testng.annotations.BeforeGroups ;
import org.testng.annotations.AfterSuite ;
import org.testng.annotations.BeforeSuite ;

import corba.framework.TestngRunner ;

public class ParseTest extends test.Test implements Constants {
    public static ClassPath createClassPath() {
            
        String path = System.getProperty("java.class.path");

        // Use reflection to call sun.rmi.rmic.BatchEnvironment.createClassPath(path)
        // so that we can leave classes.zip at the front of the classpath for
        // the build environment. Don't ask.

        try {
            Class env = sun.rmi.rmic.BatchEnvironment.class;
            Method method = env.getMethod("createClassPath",new Class[]{java.lang.String.class});
            return (ClassPath) method.invoke(null,new Object[]{path});
        } catch (Throwable e) {
            if (e instanceof ThreadDeath) 
                throw (ThreadDeath)e;
            throw new Error("ParseTest.createClassPath() caught "+e);
        }
    }
    
    public void run( ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( TestExecutor.class ) ;
        runner.run() ;
        if (runner.hasFailure()) 
            status = new Error( "test failed" ) ;
    }
}
