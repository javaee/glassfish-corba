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
// Created       : 2001 Sep 18 (Tue) 11:16:00 by Harold Carr.
// Last Modified : 2002 Apr 25 (Thu) 16:45:11 by Harold Carr.
//

package corba.genericRPCMSGFramework;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class Constants
{
    public static final String Header1 = "Header1";
    public static final String Header2 = "Header2";
    public static final String Header3 = "Header3";

    public static final String BasePort1 = "BasePort1";
    public static final String BasePort2 = "BasePort2";
    public static final String BasePort3 = "BasePort3";  

    public static boolean jdkIsHopperOrGreater() 
    {
        // Should probably use Merlin's new perl-like
        // feature.

        try {

            String version
                = System.getProperty("java.version");

            System.out.println(" JDK version: " + version);

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

        } catch (Exception e) {
	    throw new RuntimeException(e.toString());
	}
    } 
}

// End of file
