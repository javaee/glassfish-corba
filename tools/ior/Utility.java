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
package tools.ior;

import java.util.Map;
import java.io.Serializable;
import org.omg.CORBA.Any;
import org.omg.IOP.*;
import org.omg.IOP.CodecFactoryPackage.*;
import org.omg.IIOP.Version;

/**
 * Interface to provide helpful methods to
 * EncapsHandler implementations.
 */
public interface Utility
{
    /**
     * Constants for Codec selection. See getCDREncapsCodec.
     */
    public Version GIOP_1_0 = new Version((byte)1, (byte)0);
    public Version GIOP_1_1 = new Version((byte)1, (byte)1);
    public Version GIOP_1_2 = new Version((byte)1, (byte)2);

    /**
     * If writing one's own EncapsHandler, use Codecs to
     * interpret the given byte array.
     */
    public CodecFactory getCodecFactory();

    public Codec getCDREncapsCodec(Version giopVersion)
        throws UnknownEncoding;

    /**
     * Get the ORB instance.  Useful for generating TypeCodes.
     */
    org.omg.CORBA.ORB getORB();

    /**
     * Pretty print the given byte buffer as hex with
     * ASCII interpretation on the side.
     */
    public void printBuffer(byte[] buffer, TextOutputHandler out);

    /**
     * Recursively display the fields of the given Object.
     *
     * Breaks apart array types.  All core Java classes (classes
     * with names beginning with "java") are directly printed
     * with toString.
     */
    public void recursiveDisplay(String name, Object object, TextOutputHandler out);
}
