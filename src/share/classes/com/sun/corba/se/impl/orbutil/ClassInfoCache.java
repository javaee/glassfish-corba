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

import java.lang.reflect.Proxy ;

import java.io.Serializable ;
import java.io.Externalizable ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.Map ;
import java.util.WeakHashMap ;

import org.omg.CORBA.UserException ;

import org.omg.CORBA.portable.ObjectImpl ;
import org.omg.CORBA.portable.Streamable ;
import org.omg.CORBA.portable.StreamableValue ;
import org.omg.CORBA.portable.CustomValue ;
import org.omg.CORBA.portable.ValueBase ;
import org.omg.CORBA.portable.IDLEntity ;

import com.sun.corba.se.spi.orb.ORB ;
import com.sun.corba.se.spi.logging.LogWrapperBase ;

/** This class caches information about classes that is somewhat expensive
 * to obtain, notably the results of isInterface(), isArray(), and isAssignableFrom.
 * A user simply calls ClassInfoCache.get( Class ) to get the information about
 * a class.
 */
public class ClassInfoCache {
    // Do NOT put a reference to the Class in ClassInfo: we don't want to
    // pin ClassLoaders in memory!
    public static class ClassInfo {
	private boolean isARemote ;
	private boolean isARemoteException ;
	private boolean isAUserException ;
	private boolean isAObjectImpl ;
	private boolean isAORB ;
	private boolean isALogWrapperBase ;
	private boolean isAIDLEntity ;
	private boolean isAStreamable ;
	private boolean isAStreamableValue ;
	private boolean isACustomValue ;
	private boolean isAValueBase ;
	private boolean isACORBAObject ;
	private boolean isASerializable ;
	private boolean isAExternalizable ;

	private boolean isArray ;
	private boolean isEnum ;
	private boolean isInterface ;
	private boolean isProxyClass ;

	ClassInfo( Class cls ) {
	    isARemote = Remote.class.isAssignableFrom( cls ) ;
	    isARemoteException = RemoteException.class.isAssignableFrom( cls ) ;
	    isAUserException = UserException.class.isAssignableFrom( cls ) ;
	    isAObjectImpl = ObjectImpl.class.isAssignableFrom( cls ) ;
	    isAORB = ORB.class.isAssignableFrom( cls ) ;
	    isALogWrapperBase = LogWrapperBase.class.isAssignableFrom( cls ) ;
	    isAIDLEntity = IDLEntity.class.isAssignableFrom( cls ) ;
	    isAStreamable = Streamable.class.isAssignableFrom( cls ) ;
	    isAStreamableValue = StreamableValue.class.isAssignableFrom( cls ) ;
	    isACustomValue = CustomValue.class.isAssignableFrom( cls ) ;
	    isAValueBase = ValueBase.class.isAssignableFrom( cls ) ;
	    isACORBAObject = org.omg.CORBA.Object.class.isAssignableFrom( cls ) ;
	    isASerializable = Serializable.class.isAssignableFrom( cls ) ;
	    isAExternalizable = Externalizable.class.isAssignableFrom( cls ) ;

	    isArray = cls.isArray() ;
	    isEnum = cls.isEnum() ;
	    isInterface = cls.isInterface() ;
	    isProxyClass = Proxy.isProxyClass( cls ) ;
	}

	public boolean isARemote() { return isARemote ; }
	public boolean isARemoteException() { return isARemoteException ; }
	public boolean isAUserException() { return isAUserException ; }
	public boolean isAObjectImpl() { return isAObjectImpl ; }
	public boolean isAORB() { return isAORB ; }
	public boolean isALogWrapperBase() { return isALogWrapperBase ; }
	public boolean isAIDLEntity() { return isAIDLEntity ; }
	public boolean isAStreamable() { return isAStreamable ; }
	public boolean isAStreamableValue() { return isAStreamableValue ; }
	public boolean isACustomValue() { return isACustomValue ; }
	public boolean isAValueBase() { return isAValueBase ; }
	public boolean isACORBAObject() { return isACORBAObject ; }
	public boolean isASerializable() { return isASerializable ; }
	public boolean isAExternalizable() { return isAExternalizable ; }
	public boolean isArray() { return isArray ; }
	public boolean isEnum() { return isEnum ; }
	public boolean isInterface() { return isInterface ; }
	public boolean isProxyClass() { return isProxyClass ; }
    }

    private static Map<Class,ClassInfo> classData = new WeakHashMap<Class,ClassInfo>() ;

    public static synchronized ClassInfo get( Class cls ) {
	ClassInfo result = classData.get( cls ) ;
	if (result == null) {
	    result = new ClassInfo( cls ) ;
	    classData.put( cls, result ) ;
	}

	return result ;
    }
}
