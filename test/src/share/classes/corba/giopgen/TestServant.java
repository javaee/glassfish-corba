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
//
// Created       : 2005 Oct 05 (Wed) 13:50:17 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 15:17:44 by Harold Carr.
//

package corba.giopgen;

import java.rmi.RemoteException;
import javax.rmi.PortableRemoteObject;

import java.util.Map ;
import java.util.HashMap ;

import java.io.Serializable ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CORBA.CompletionStatus ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.generic.SPair ;

import com.sun.corba.se.impl.logging.UtilSystemException ;

public class TestServant
    extends PortableRemoteObject
    implements Test
{
    public static final String baseMsg = TestServant.class.getName();

    public TestServant()
	throws RemoteException
    {
    }

    public int echo(int x, float y, short[] z, String str, Map m )
	throws RemoteException
    {
	System.out.println(baseMsg + ".echo: " + x);
        return x;
    }

    private static UtilSystemException wrapper = 
        ORB.getStaticLogWrapperTable().get_UTIL_Util() ;
    
    private static class ThrowsSysEx implements Serializable {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.testException( 42 ) ;
        }
    }

    private static class ThrowsSimpleSysEx implements Serializable {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.simpleTestException( CompletionStatus.COMPLETED_MAYBE, new Exception() ) ;
        }
    }

    private static class Foo implements Serializable {
        private Map m ;

        public Foo( Object... args ) {
            m = new HashMap() ;
            boolean atKey = true ;
            Object key = null ;
            Object value = null ;
            for (Object obj : args) {
                if (atKey) {
                    key = obj ;
                } else {
                    value = obj ;
                    m.put( key, value ) ;
                }

                atKey = !atKey ;
            }
        }
    }

    public Object testExceptionContext() throws RemoteException {
        Object d1 = new SPair<String,String>( "foo", "bar" ) ;
        Object d2 = new SPair<String,ThrowsSysEx>( "baz", new ThrowsSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        SPair<String,Foo> result = new SPair<String,Foo>( "f1", f1 ) ;
        return result ;
    }

    public Object testSimpleExceptionContext() throws RemoteException {
        Object d1 = new SPair<String,String>( "foo", "bar" ) ;
        Object d2 = new SPair<String,ThrowsSimpleSysEx>( "baz", new ThrowsSimpleSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        SPair<String,Foo> result = new SPair<String,Foo>( "f1", f1 ) ;
        return result ;
    }
}

// End of file.
