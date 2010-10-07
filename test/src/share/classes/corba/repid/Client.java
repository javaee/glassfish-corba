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
package corba.repid;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.se.spi.orbutil.ORBConstants ;

public class Client {
    
    public static void main(String args[]) {

        try {

	    Properties props = new Properties(System.getProperties());
	    props.setProperty(ORBConstants.USE_REP_ID, "false");
	    ORB orb = ORB.init(args, props);

            com.sun.corba.se.spi.orb.ORB ourORB
                = (com.sun.corba.se.spi.orb.ORB) orb;

	    if (ourORB.getORBData().useRepId() != false) {
		throw new RuntimeException("ORB.useRepId flag is not false");
	    }

	    String[] inStrSeq = new String[] { "Hello", "World" };
	    OutputStream ostr = (org.omg.CORBA_2_3.portable.OutputStream)
		                    ourORB.create_output_stream();
	    ostr.write_value(inStrSeq, new test.StringSeqHelper());
	    InputStream istr = (org.omg.CORBA_2_3.portable.InputStream)
		ostr.create_input_stream();
	    String[] outStrSeq = (String[]) 
		istr.read_value(new test.StringSeqHelper());
	    for (int i = 0; i < outStrSeq.length; i++) {
		if (!(outStrSeq[i].equals(inStrSeq[i]))) {
		    throw new RuntimeException("Input/output value mismatch");
		}
	    }

        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
