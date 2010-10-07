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
package com.sun.corba.se.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.ior.TaggedComponentBase;

/**
 * Tagged component that contains a value that indicates the Java 
 * serialization version supported by the ORB.
 *
 * ORB Java serialization uses IIOP as the transport protocol, but uses
 * Java serialization mechanism and its accompanying encodings, instead
 * of IIOP CDR serialization mechanism. Java serialization is generally
 * observed to be faster than CDR.
 */ 
public class JavaSerializationComponent extends TaggedComponentBase {

    private byte version;

    private static JavaSerializationComponent singleton;

    static {
	singleton = new JavaSerializationComponent(
					       ORBConstants.JAVA_ENC_VERSION);
    }

    public static JavaSerializationComponent singleton() {
	return singleton;
    }

    public JavaSerializationComponent(byte version) {
        this.version = version;
    }

    public byte javaSerializationVersion() {
	return this.version;
    }

    public void writeContents(OutputStream os) {
        os.write_octet(version);
    }
    
    public int getId() {
	return ORBConstants.TAG_JAVA_SERIALIZATION_ID;
    }

    public boolean equals(Object obj) {
	if (!(obj instanceof JavaSerializationComponent)) {
	    return false;
	}
	JavaSerializationComponent other = (JavaSerializationComponent) obj;
	return this.version == other.version;
    }

    public int hashCode() {
	return this.version;
    }
}
