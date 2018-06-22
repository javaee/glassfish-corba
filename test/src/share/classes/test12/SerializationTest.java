/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package test12;

import java.util.Properties ;
import org.glassfish.pfl.test.JUnitReportHelper;

public class SerializationTest extends test.Test {
    public void run() {
        JUnitReportHelper helper = new JUnitReportHelper( SerializationTest.class.getName() ) ;

        try {        
            helper.start( "test1" ) ;
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            org.omg.CORBA.ORB orb = 
                org.omg.CORBA.ORB.init(getArgsAsArgs(),props);
            org.omg.CORBA_2_3.portable.OutputStream sos =
                (org.omg.CORBA_2_3.portable.OutputStream)orb.create_output_stream();


            ARectangle rect = new ARectangle(1,3,5,7);
            sos.write_value(rect);

            /***************************************************************/
            /*********************** READ DATA BACK IN *********************/
            /***************************************************************/

            org.omg.CORBA_2_3.portable.InputStream sis = 
                (org.omg.CORBA_2_3.portable.InputStream)sos.create_input_stream();

            ARectangle _rect = (ARectangle)sis.read_value();
            if (!rect.equals(_rect))
                throw new Error("ARectangle test failed!");

            helper.pass() ;
        } catch(Throwable e) {
            helper.fail( e ) ;
            status = new Error(e.getMessage());
            e.printStackTrace();
        } finally {
            helper.done() ;
        }
    }
}
