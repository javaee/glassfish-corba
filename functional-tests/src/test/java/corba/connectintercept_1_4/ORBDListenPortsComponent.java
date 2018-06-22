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

//
// Created       : 2000 Oct 17 (Tue) 08:36:51 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:00:53 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;

public class ORBDListenPortsComponent
    extends
        TaggedComponentBase
{
    private String listenPorts;

    public ORBDListenPortsComponent(String listenPorts)
    {
        this.listenPorts = listenPorts;
    }

    public boolean equals(Object o)
    {
        if (o == null) {
            return false;
        }
        if (!(o instanceof ORBDListenPortsComponent)) {
            return false;
        }
        return listenPorts.equals(((ORBDListenPortsComponent)o).listenPorts);
    }

    public void writeContents(OutputStream os)
    {
        os.write_string(listenPorts);
    }

    public int getId()
    {
        return Common.ListenPortsComponentID;
    }
}

// End of file.
