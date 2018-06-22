/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package corba.copyobjectpolicy;

import java.lang.reflect.Method ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBConfigurator ;
import com.sun.corba.ee.spi.orb.DataCollector ;

import com.sun.corba.ee.spi.copyobject.CopierManager ;
import com.sun.corba.ee.spi.copyobject.CopyobjectDefaults ;

import corba.framework.TraceAccumulator ;
import corba.framework.ProxyInterceptor ;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

public class UserConfigurator implements ORBConfigurator 
{
    // All of these statics are needed in the main test
    public static final int VALUE_INDEX = 1 ;
    public static final int REFERENCE_INDEX = 2 ;

    public static final String VALUE_NAME = "ValueInterceptor" ;
    public static final String REFERENCE_NAME = "ReferenceInterceptor" ;

    // The TraceAccumular connected to the ProxyInterceptors 
    // used in this test.
    public static final TraceAccumulator traceAccum =
        new TraceAccumulator() ;

    public static final Method makeMethod ;
    
    static {
        try {
            makeMethod = ObjectCopierFactory.class.
                getDeclaredMethod( "make" ) ;
        } catch (Exception exc) {
            throw new RuntimeException( 
                "Cannot find ObjectCopierFactory.make() method", exc ) ;
        }
    } 

    private ProxyInterceptor makePI( String name, ObjectCopierFactory factory ) 
    {
        ProxyInterceptor result = ProxyInterceptor.make(
            name,  new Class[] { ObjectCopierFactory.class }, factory ) ;
        result.addListener( traceAccum ) ;
        result.addMethod( makeMethod ) ;

        return result ;
    }

    /** Set up two copiers: the value copier, and the reference
     * copier.  Make the value copier the default .
     */
    public void configure( DataCollector dc, ORB orb ) 
    {
        CopierManager cm = orb.getCopierManager() ;
        cm.setDefaultId( VALUE_INDEX ) ;

        ObjectCopierFactory value =
            CopyobjectDefaults.makeORBStreamObjectCopierFactory( orb ) ;
        ProxyInterceptor valuePI = makePI( VALUE_NAME,
            value ) ;
        cm.registerObjectCopierFactory( 
            (ObjectCopierFactory)valuePI.getActual(), 
            VALUE_INDEX ) ;

        ObjectCopierFactory reference =
            CopyobjectDefaults.getReferenceObjectCopierFactory( ) ;
        ProxyInterceptor referencePI = makePI( REFERENCE_NAME,
            reference ) ;
        cm.registerObjectCopierFactory( 
            (ObjectCopierFactory)referencePI.getActual(), 
            REFERENCE_INDEX ) ;
    }
}
