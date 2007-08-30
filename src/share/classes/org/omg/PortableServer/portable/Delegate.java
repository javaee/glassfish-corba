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
package org.omg.PortableServer.portable;

import org.omg.PortableServer.Servant;
import org.omg.PortableServer.POA;

/**
 * The portability package contains interfaces and classes 
 * that are designed for and intended to be used by ORB 
 * implementor. It exposes the publicly defined APIs that 
 * are used to connect stubs and skeletons to the ORB.
 * The Delegate interface provides the ORB vendor specific 
 * implementation of PortableServer::Servant.
 * Conformant to spec CORBA V2.3.1, ptc/00-01-08.pdf
 */
public interface Delegate {
/**
 * Convenience method that returns the instance of the ORB
 * currently associated with the Servant.
 * @param Self the servant.
 * @return ORB associated with the Servant.
 */
    org.omg.CORBA.ORB orb(Servant Self);

/**
 * This allows the servant to obtain the object reference for
 * the target CORBA Object it is incarnating for that request.
 * @param Self the servant.
 * @return Object reference associated with the request.
 */
    org.omg.CORBA.Object this_object(Servant Self);

/**
 * The method _poa() is equivalent to
 * calling PortableServer::Current:get_POA.
 * @param Self the servant.
 * @return POA associated with the servant.
 */
    POA poa(Servant Self);

/**
 * The method _object_id() is equivalent
 * to calling PortableServer::Current::get_object_id.
 * @param Self the servant.
 * @return ObjectId associated with this servant.
 */
    byte[] object_id(Servant Self);

/**
 * The default behavior of this function is to return the
 * root POA from the ORB instance associated with the servant.
 * @param Self the servant.
 * @return POA associated with the servant class.
 */
    POA default_POA(Servant Self);

/**
 * This method checks to see if the specified repid is present
 * on the list returned by _all_interfaces() or is the
 * repository id for the generic CORBA Object.
 * @param Self the servant.
 * @param Repository_Id the repository_id to be checked in the
 *            repository list or against the id of generic CORBA
 *            object.
 * @return boolean indicating whether the specified repid is
 *         in the list or is same as that got generic CORBA
 *         object.
 */
    boolean is_a(Servant Self, String Repository_Id);

/**
 * This operation is used to check for the existence of the
 * Object.
 * @param Self the servant.
 * @return boolean true to indicate that object does not exist,
 *                 and false otherwise.
 */
    boolean non_existent(Servant Self);
    //Simon And Ken Will Ask About Editorial Changes
    //In Idl To Java For The Following Signature.

/**
 * This operation returns an object in the Interface Repository
 * which provides type information that may be useful to a program.
 * @param Self the servant.
 * @return type information corresponding to the object.
 */
    // The get_interface() method has been replaced by get_interface_def()
    //org.omg.CORBA.Object get_interface(Servant Self);

    org.omg.CORBA.Object get_interface_def(Servant self);
}

