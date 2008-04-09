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

/* This will print the CORBA RepositoryId for a specified Java class.
 *
 * If the rules laid down in the CORBA spec (section 10.6.2) and the
 * CORBA Java-to-IDL spec (section 1.3.5.6) are followed, I believe
 * the following results should be obtained:
 *
 *
 * java ShowRepid java.lang.Class
 * class java.lang.Class = RMI:javax.rmi.CORBA.ClassDesc:2BABDA04587ADCCC:CFBF02CF5294176B
 *
 * java ShowRepid java.util.GregorianCalendar
 * class java.util.GregorianCalendar = RMI:java.util.GregorianCalendar:450042FBA7A923B1:8F3DD7D6E5B0D0C1
 *
 * java ShowRepid java.io.ObjectStreamClass
 * class java.io.ObjectStreamClass = RMI:java.io.ObjectStreamClass:071DA8BE7F971128:AB0E6F1AEEFE7B88
 *
 * java ShowRepid ShowRepid
 * class ShowRepid = RMI:ShowRepid:AC117E28FE36587A:0000000000001234
 */
package corba.serialization.good;

import java.io.*;
import com.sun.corba.se.impl.util.RepositoryId;

public class ShowRepid implements Serializable {
    static final long serialVersionUID = 0x1234;

    private void writeObject(ObjectOutputStream s) throws IOException {
    }

    private static int runTest() {
	int rc = 0;

	String r1 = "RMI:javax.rmi.CORBA.ClassDesc:2BABDA04587ADCCC:CFBF02CF5294176B";
	String r2 = "RMI:java.util.GregorianCalendar:450042FBA7A923B1:8F3DD7D6E5B0D0C1";
	String r3 = "RMI:java.io.ObjectStreamClass:071DA8BE7F971128:AB0E6F1AEEFE7B88";
	String r4 = "RMI:ShowRepid:AC117E28FE36587A:0000000000001234";
	String r5 = "RMI:java.util.Hashtable:86573568A211C011:13BB0F25214AE4B8";

	String s1 = RepositoryId.createForAnyType(java.lang.Class.class);
	String s2 = RepositoryId.createForAnyType(java.util.GregorianCalendar.class);
	String s3 = RepositoryId.createForAnyType(java.io.ObjectStreamClass.class);
	String s4 = RepositoryId.createForAnyType(ShowRepid.class);
	String s5 = RepositoryId.createForAnyType(java.util.Hashtable.class);

	if (!s1.equals(r1)) {
	System.out.println("mismatch " + s1);
	++rc;
	}
	if (!s2.equals(r2)) {
	System.out.println("mismatch " + s2);
	++rc;
	}
	if (!s3.equals(r3)) {
	System.out.println("mismatch " + s3);
	++rc;
	}
	if (!s4.equals(r4)) {
	System.out.println("mismatch " + s4);
	++rc;
	}
	if (!s5.equals(r5)) {
	System.out.println("mismatch " + s5);
	++rc;
	}


	return rc;
    }

    public static void main(String[] args) {
	System.out.println("Server is ready.");
	if (args.length == 0) {
	    if (runTest() == 0)
		System.out.println("Test PASSED");
	    else {
		System.out.println("Test FAILED");
                System.exit(1) ;
            }
	} else {
	    try {
		Class clz = Class.forName(args[0]);
		System.out.print(clz + " = ");
		System.out.println(RepositoryId.createForAnyType(clz));
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
