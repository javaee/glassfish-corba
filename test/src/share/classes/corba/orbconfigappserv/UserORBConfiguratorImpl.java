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
//
// Created       : 2003 Apr 15 (Tue) 15:36:45 by Harold Carr.
// Last Modified : 2003 Apr 15 (Tue) 16:36:40 by Harold Carr.
//

package corba.orbconfigappserv;

import org.omg.CORBA.INITIALIZE;
import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.orb.ORBConfigurator;
import com.sun.corba.se.spi.orb.ParserImplBase;
import com.sun.corba.se.spi.orb.PropertyParser;
import com.sun.corba.se.spi.orb.DataCollector;
import com.sun.corba.se.spi.orb.OperationFactory;

public class UserORBConfiguratorImpl 
    implements 
	ORBConfigurator 
{
    public static String propertyName = "userConfigProperty";

    private static class ConfigParser extends ParserImplBase {
	public Class testclass = 
	    corba.orbconfigappserv.UserORBConfiguratorImpl.class;

	public PropertyParser makeParser()
	{
	    PropertyParser parser = new PropertyParser() ;
	    parser.add( propertyName,
			OperationFactory.classAction(), 
			"testclass" ) ;
	    return parser ;
	}
    }

    public void configure( DataCollector dc, ORB orb ) {
	ConfigParser parser = new ConfigParser();
	parser.init( dc );
	Class theTestclass = parser.testclass;

        if (theTestclass != null) {
            try {
                Object o = theTestclass.newInstance();
		System.out.println("UserORBConfiguratorImpl.configure: " + o);
            } catch (Exception ex) {
                throw new org.omg.CORBA.INITIALIZE(ex.toString());
            }
        }
    }	
}
