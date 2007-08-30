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
/*
 * Licensed Materials - Property of IBM
 * RMI-IIOP v1.0
 * Copyright IBM Corp. 1998 1999  All Rights Reserved
 *
 * US Government Users Restricted Rights - Use, duplication or
 * disclosure restricted by GSA ADP Schedule Contract with IBM Corp.
 */

package javax.rmi.CORBA;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.ValueHandler;
import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.SystemException;

/**
 * Supports delegation for method implementations in {@link Util}.  The
 * delegate is a singleton instance of a class that implements this
 * interface and provides a replacement implementation for all the
 * methods of <code>javax.rmi.CORBA.Util</code>.
 *
 * Delegation is enabled by providing the delegate's class name as the
 * value of the 
 * <code>javax.rmi.CORBA.UtilClass</code>
 * system property.
 *
 * @see Util
 */
public interface UtilDelegate {

    /**
     * Delegation call for {@link Util#mapSystemException}.
     */
    RemoteException mapSystemException(SystemException ex);

    /**
     * Delegation call for {@link Util#writeAny}.
     */
    void writeAny(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#readAny}.
     */
    java.lang.Object readAny(InputStream in);

    /**
     * Delegation call for {@link Util#writeRemoteObject}.
     */
    void writeRemoteObject(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#writeAbstractObject}.
     */
    void writeAbstractObject(OutputStream out, Object obj);

    /**
     * Delegation call for {@link Util#registerTarget}.
     */
    void registerTarget(Tie tie, Remote target);
    
    /**
     * Delegation call for {@link Util#unexportObject}.
     */
    void unexportObject(Remote target) throws java.rmi.NoSuchObjectException;
    
    /**
     * Delegation call for {@link Util#getTie}.
     */
    Tie getTie(Remote target);

    /**
     * Delegation call for {@link Util#createValueHandler}.
     */
    ValueHandler createValueHandler();

    /**
     * Delegation call for {@link Util#getCodebase}.
     */
    String getCodebase(Class clz);

    /**
     * Delegation call for {@link Util#loadClass}.
     */
    Class loadClass(String className, String remoteCodebase, ClassLoader loader) 
        throws ClassNotFoundException;

    /**
     * Delegation call for {@link Util#isLocal}.
     */
    boolean isLocal(Stub stub) throws RemoteException;

    /**
     * Delegation call for {@link Util#wrapException}.
     */
    RemoteException wrapException(Throwable obj);

    /**
     * Delegation call for {@link Util#copyObject}.
     */
    Object copyObject(Object obj, ORB orb) throws RemoteException;
    
    /**
     * Delegation call for {@link Util#copyObjects}.
     */
    Object[] copyObjects(Object[] obj, ORB orb) throws RemoteException;

}
	    
