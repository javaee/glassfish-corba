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
package tools.ior;

import java.io.IOException;
import org.omg.CORBA.portable.*;
import org.omg.IOP.*;
import org.omg.IIOP.*;
import org.omg.CORBA.Any;

/**
 * Handles the IIOP Profile.  Correctly recognizes 1.0 and 1.1
 * versions, delegating to the appropriate ProfileBody helper.
 *
 * This is necessary since we have to decide what to do
 * depending on the Version at the beginning.
 */
public class IIOPProfileHandler implements EncapsHandler
{
    public void display(byte[] data, 
                        TextOutputHandler out,
                        Utility util)
        throws DecodingException {

        try {

            // Assumes that all IIOPProfiles contain only GIOP 1.0
            // primitives.
            Codec codec = util.getCDREncapsCodec(Utility.GIOP_1_0);

            // Check the version of the profile
            Any versionAny = codec.decode_value(data,
                                                VersionHelper.type());

            Version version = VersionHelper.extract(versionAny);

            // This assumes that the profile will change
            // after GIOP 1.2.  Currently this is handled
            // since the caller dumps the data when getting
            // a DecodingException.
            if (version.major != 1 || version.minor > 2)
                throw new DecodingException("Unknown IIOP Profile version: "
                                            + version.major
                                            + '.'
                                            + version.minor);

            if (version.minor == 0) {
                Any bodyAny = codec.decode_value(data,
                                                 ProfileBody_1_0Helper.type());
                java.lang.Object body
                    = ProfileBody_1_0Helper.extract(bodyAny);

                util.recursiveDisplay("ProfileBody_1_0", body, out);

            } else {
                // GIOP 1.1 and 1.2 use the same
                // profile body
                Any bodyAny = codec.decode_value(data,
                                                 ProfileBody_1_1Helper.type());

                java.lang.Object body
                    = ProfileBody_1_1Helper.extract(bodyAny);

                util.recursiveDisplay("ProfileBody_1_1", body, out);
            }
        } catch (Exception ex) {
            throw new DecodingException(ex.getMessage());
        }
    }
}
