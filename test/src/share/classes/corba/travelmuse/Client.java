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

package corba.travelmuse;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.transport.MessageData;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Properties;

import corba.util.TransportManagerUtil;
import org.testng.annotations.Test ;
import org.testng.annotations.Configuration ;
import org.testng.Assert ;

import corba.framework.TestngRunner;

/**
 *
 * @author Ken Cavanaugh
 * @author daraniramu
 */
public class Client {
    private Properties p = new Properties();
    private org.omg.CORBA.ORB orb;
    private com.sun.corba.ee.spi.orb.ORB myOrb;

    private static void msg( String msg ) {
        System.out.println( msg ) ;
    }

    public Client() {
    }

    @Configuration( beforeTest=true ) 
    public void setUp() {
        msg( "Configuring ORB" ) ;
        p.put("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl");
        p.put("com.sun.corba.ee.ORBDebug","cdr,streamFormatVersion,valueHandler");
        orb=  com.sun.corba.ee.spi.orb.ORB.init(new String[0],p);
        myOrb = (com.sun.corba.ee.spi.orb.ORB)orb ;
        myOrb.setDebugFlags( "cdr", "streamFormatVersion", "valueHandler" ) ;
    }

    @Configuration( afterTest=true ) 
    public void tearDown() {
        msg( "Cleaning up" ) ;
        orb.destroy();
        myOrb.destroy();
    }

   
    @Test
    public void travelMuse() {
        try {
            msg( "test case travelMuse" ) ;
            InputStream inputFile ;
            inputFile = new FileInputStream("../src/share/classes/corba/travelmuse/mtm.bin");
            ObjectInputStream in = new ObjectInputStream(inputFile);
            Object baResult=in.readObject();
            byte[][] baResult1=(byte[][])baResult;
            MessageData md = TransportManagerUtil.getMessageData(baResult1, myOrb);
            int bnum = 0 ;
            for (byte[] data : baResult1) {
                ByteBuffer bb = ByteBuffer.wrap( data ) ;
                bb.position( bb.capacity() ) ;
                ORBUtility.printBuffer( "Dumping buffer " + bnum++, bb, System.out ) ;
            }
            Object cdrstream1=javax.rmi.CORBA.Util.readAny( md.getStream());
        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( exc.toString() ) ;
        }
    }

    public static void main( String[] args ) {
        msg( "Test start: workding dir is " + System.getProperty( "user.dir" ) ) ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        try {
            runner.run() ;
        } catch (Exception exc ) {
            exc.printStackTrace() ;
        }
        runner.systemExit() ;
    }
}
