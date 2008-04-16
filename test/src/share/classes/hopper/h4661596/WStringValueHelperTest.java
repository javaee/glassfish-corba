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
package hopper.h4661596;

import corba.framework.CORBATest;
import java.util.*;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.WStringValueHelper;

/**
 * Checks to make sure WStringValueHelper.type() returns
 * a TypeCode reflecting a boxed wstring.
 *
 * Since we can't get the WStringValueHelper from rip-int
 * without Xbootclasspath, this will only run on 1.4.1 or
 * greater workspaces.
 */
public class WStringValueHelperTest extends CORBATest
{
    public static final String INDENT = "      ";

    // The actual test:

    public void checkWStringValueHelper() throws Exception {
        System.out.print(INDENT
                         + "  Checking WStringValueHelper from JDK...");

        TypeCode tc = WStringValueHelper.type();

        while (tc.kind().equals(TCKind.tk_alias))
            tc = tc.content_type();

        if (!tc.kind().equals(TCKind.tk_value_box) ||
            !tc.content_type().kind().equals(TCKind.tk_wstring)) {
            Exception exc = new Exception("Bad TypeCode from WStringValueHelper: "
                                + " kind: " 
                                + tc.content_type().kind().value());
            throw exc ;
        }

        System.out.println("PASSED");
    }


    // This has nothing to do with the logic of the 
    // test.  It is only to allow us to only run this on
    // hopper or greater JDKs.
    public boolean jdkIsHopperOrGreater() throws Exception {
        
        // Should probably use Merlin's new perl-like
        // feature.

        try {

            String version 
                = System.getProperty("java.version");

            System.out.println(INDENT
                               + " JDK version: " + version);

            StringTokenizer stok
                = new StringTokenizer(version, ". -_b", false);

            int major = Integer.parseInt(stok.nextToken());
            if (major > 1)
                return true;

            if (!stok.hasMoreTokens())
                return false;

            int dot1 = Integer.parseInt(stok.nextToken());
            if (dot1 > 4)
                return true;

            if (!stok.hasMoreTokens())
                return false;

            int dot2 = Integer.parseInt(stok.nextToken());
            if (dot2 == 0)
                return false;

            return true;

        } catch (NoSuchElementException nsee) {
            throw new Exception("Error determining version: "
                                + nsee);
        } catch (NumberFormatException nfe) {
            throw new Exception("Error determining version: "
                                + nfe);
        }
    }

    protected void doTest() throws Throwable {
        System.out.println();

        System.out.println(INDENT
                           + "Verifying JDK is Hopper or greater...");
        try {
            if (!jdkIsHopperOrGreater()) {
                System.out.println(INDENT
                                   + "* WARNING: "
                                   + " This test can only be run on Hopper or greater JDKs.  Skipping test.");
                return;
            }
                
        } catch (Exception ex) {
            System.out.println(INDENT
                               + "* Error determing JDK version.  Can only run on Hopper or greater JDKs.  Skipping test.  Error was: " + ex);
            return;
        }

        checkWStringValueHelper();
    }
}
