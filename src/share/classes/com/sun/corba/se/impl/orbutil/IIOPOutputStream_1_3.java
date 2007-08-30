/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2002-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.corba.se.impl.orbutil;

import java.io.*;

/**
 * Implements legacy behavior from before Ladybird to maintain
 * backwards compatibility.
 */
public class IIOPOutputStream_1_3 extends com.sun.corba.se.impl.io.IIOPOutputStream
{
    // We can't assume that the superclass's putFields
    // member will be non-private.  We must allow
    // the RI to run on JDK 1.3.1 FCS as well as
    // the JDK 1.3.1_01 patch.
    private ObjectOutputStream.PutField putFields_1_3;

    // The newer version in the io package correctly writes a wstring instead.
    // This concerns bug 4379597.
    protected void internalWriteUTF(org.omg.CORBA.portable.OutputStream stream, 
                                    String data)
    {
        stream.write_string(data);
    }

    public IIOPOutputStream_1_3()
	throws java.io.IOException {
	super();
    }

    /**
     * Before JDK 1.3.1_01, the PutField/GetField implementation
     * actually sent a Hashtable.
     */
    public ObjectOutputStream.PutField putFields()
	throws IOException {
	putFields_1_3 = new LegacyHookPutFields();
	return putFields_1_3;
    }

    public void writeFields()
	throws IOException {
	putFields_1_3.write(this);
    }
}
