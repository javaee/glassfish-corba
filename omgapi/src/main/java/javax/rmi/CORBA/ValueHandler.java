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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA;

/**
 * Defines methods which allow serialization of Java objects
 * to and from GIOP streams.
 **/
public interface ValueHandler {

    /**
     * Writes a value to the stream using Java semantics.
     * @param out the stream to write the value to.
     * @param value the value to be written to the stream.
     **/
    void writeValue(org.omg.CORBA.portable.OutputStream out,
                    java.io.Serializable value);

    /**
     * Reads a value from the stream using Java semantics.
     * @param in the stream to read the value from.
     * @param offset the current position in the input stream.
     * @param clz the type of the value to be read in.
     * @param repositoryID the RepositoryId of the value to be read in.
     * @param sender the sending context runtime codebase.
     * @return the value read from the stream.
     **/
    java.io.Serializable readValue(org.omg.CORBA.portable.InputStream in,
                                   int offset,
                                   java.lang.Class clz, 
                                   String repositoryID,
                                   org.omg.SendingContext.RunTime sender);

    /**
     * Returns the CORBA RepositoryId for the given Java class.
     * @param clz a Java class.
     * @return the CORBA RepositoryId for the class.
     **/
    java.lang.String getRMIRepositoryID(java.lang.Class clz);

    /**
     * Indicates whether the given class performs custom or
     * default marshaling.
     * @param clz the class to test for custom marshaling.
     * @return <code>true</code> if the class performs custom marshaling, <code>false</code>
     * if it does not.
     **/
    boolean isCustomMarshaled(java.lang.Class clz);

    /**
     * Returns the CodeBase for this ValueHandler.  This is used by
     * the ORB runtime.  The server sends the service context containing
     * the IOR for this CodeBase on the first GIOP reply.  The client
     * does the same on the first GIOP request.
     * @return the SendingContext.CodeBase of this ValueHandler.
     **/
    org.omg.SendingContext.RunTime getRunTimeCodeBase();

    /**
     * If the value contains a <code>writeReplace</code> method then the result
     * is returned.  Otherwise, the value itself is returned.
     * @param value the value to be marshaled.
     * @return the true value to marshal on the wire.
     **/
    java.io.Serializable writeReplace(java.io.Serializable value);

}
