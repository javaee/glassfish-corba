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

package javax.rmi.CORBA;

/**
 * Java to IDL ptc 02-01-12 1.5.1.5
 */
public interface ValueHandlerMultiFormat extends ValueHandler {

    /**
     * Returns the maximum stream format version for
     * RMI/IDL custom value types that is supported
     * by this ValueHandler object. The ValueHandler
     * object must support the returned stream format version and
     * all lower versions.
     *
     * An ORB may use this value to include in a standard
     * IOR tagged component or service context to indicate to other
     * ORBs the maximum RMI-IIOP stream format that it
     * supports.  If not included, the default for GIOP 1.2
     * is stream format version 1, and stream format version
     * 2 for GIOP 1.3 and higher.
     */
    byte getMaximumStreamFormatVersion();

    /**
     * Allows the ORB to pass the stream format
     * version for RMI/IDL custom value types. If the ORB
     * calls this method, it must pass a stream format version
     * between 1 and the value returned by the
     * getMaximumStreamFormatVersion method inclusive,
     * or else a BAD_PARAM exception with standard minor code
     * will be thrown.
     *
     * If the ORB calls the older ValueHandler.writeValue(OutputStream,
     * Serializable) method, stream format version 1 is implied.
     *
     * The ORB output stream passed to the ValueHandlerMultiFormat.writeValue
     * method must implement the ValueOutputStream interface, and the
     * ORB input stream passed to the ValueHandler.readValue method must
     * implement the ValueInputStream interface.
     */
    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value,
                    byte streamFormatVersion);
}
