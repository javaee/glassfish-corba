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
// Created       : 2005 Sep 21 (Wed) 09:14:00 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 16:27:36 by Harold Carr.
//

package corba.folb;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;

import corba.framework.Controller;
import corba.hcks.U;

import org.testng.annotations.BeforeSuite ;
import org.testng.annotations.Test ;

import org.testng.Assert ;

/**
 * @author Harold Carr
 */
public class ClientMulti extends ClientBase {

    @BeforeSuite 
    public void clientSetup() {
        setup( getDefaultProperties() ) ;
    }

    @Test
    public void test() throws Exception
    {
        CallThread a =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        CallThread b =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        CallThread c =
            new CallThread(1000, testRfmWithAddressesWithLabel);
        a.start();
        b.start();
        c.start();

        do {
            gisPoaWithAddressesWithLabels.removeInstance(
                corba.folb_8_1.Common.Z);
            Thread.sleep(1000);
            gisPoaWithAddressesWithLabels.addInstance(
                corba.folb_8_1.Common.Z);
            Thread.sleep(1000);
        } while (!a.done || !b.done || !c.done);

        Assert.assertTrue( (a.failures + b.failures + c.failures) == 0 ) ;
    }

    public static void main(String[] av) {
        doMain( ClientCircular.class ) ;
    }
}

class CallThread extends Thread
{
    int iterations;
    int failures;
    EchoTest ref;
    boolean done;

    CallThread(int iterations, EchoTest ref)
    { 
        this.failures = 0 ;
        this.iterations = iterations;
        this.ref = ref;
        done = false;
    }

    public void run()
    {
        for (int i = 0; i < iterations; ++i) {
            try {
                ref.echo("FOO");
            } catch (java.rmi.RemoteException e) {
                failures++ ;
                System.out.println("CallThread.run FAILURE !!!!!");
                e.printStackTrace(System.out);
            }
        }
        done = true;
    }
}

// End of file.
