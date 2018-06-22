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
// Created       : 2001 May 23 (Wed) 11:57:05 by Harold Carr.
// Last Modified : 2001 Sep 20 (Thu) 21:39:43 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

class AServiceImpl
    extends LocalObject
    implements AService
{
    private int slotId;

    private int currentServiceId = 0;

    private Current piCurrent;

    private Any NOT_IN_EFFECT;

    public AServiceImpl(int slotId)
    {
        this.slotId = slotId;
        NOT_IN_EFFECT = ORB.init().create_any();
    }

    // Package protected so the AService ORBInitializer can access this
    // non-IDL defined method.
    void setPICurrent(Current piCurrent)
    {
        this.piCurrent = piCurrent;
    }

    public void begin()
    {
        Any any = ORB.init().create_any();
        any.insert_long(++currentServiceId);
        setSlot(any);
    }

    public void end()
    {
        setSlot(NOT_IN_EFFECT);
    }

    public void verify()
    {
        try {
            Any any = piCurrent.get_slot(slotId);
            if (any.type().kind().equals(TCKind.tk_long)) {
                System.out.println("Service present: " + any.extract_long());
            } else {
                System.out.println("Service not present");
            }
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }
    }

    // Synchronized because two threads in the same ORB could be
    // sharing this object.
    synchronized private void setSlot(Any any)
    {
        try {
            piCurrent.set_slot(slotId, any);
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }
    }
}

// End of file.

