/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package corba.simpledynamic;

import java.util.Map ;
import java.util.HashMap ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;
import javax.rmi.PortableRemoteObject ;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.orbutil.generic.Pair ;

import com.sun.corba.se.impl.logging.UtilSystemException ;

import corba.misc.BuckPasserAL  ;
import corba.misc.BuckPasserV  ;

public class EchoImpl extends PortableRemoteObject implements Echo {
    private String name ;
    private static UtilSystemException wrapper = 
        ORB.getStaticLogWrapperTable().get_UTIL_Util() ;
    
    private static class ThrowsSysEx {
        private void readObject( java.io.ObjectInputStream is ) {
            throw wrapper.testException( 42 ) ;
        }
    }

    private static class Foo {
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

    public EchoImpl( String name ) throws RemoteException {
	this.name = name ;
    }

    public String sayHello( Object obj ) throws RemoteException {
	return "Hello " + obj ;
    }

    public Echo say( Echo echo ) {
	return echo ;
    }

    public String name() {
	return name ;
    }

    public Object testExceptionContext() throws RemoteException {
        Object d1 = new Pair<String,String>( "foo", "bar" ) ;
        Object d2 = new Pair<String,ThrowsSysEx>( "baz", new ThrowsSysEx() ) ;
        Foo f1 = new Foo( "d1", d1, "d2", d2 ) ;
        Pair<String,Foo> result = new Pair<String,Foo>( "f1", f1 ) ;
        return result ;
    }

    public int[] echo( int[] arg ) {
	return arg ;
    }

    public Object echo( Object arg ) {
	return arg ;
    }

    public BuckPasserAL echo( BuckPasserAL arg ) {
        return arg ;
    }

    public BuckPasserV echo( BuckPasserV arg ) {
        return arg ;
    }

    public BuckPasserVectorOriginal echo( BuckPasserVectorOriginal arg ) throws RemoteException {
        return arg ;
    }
}
