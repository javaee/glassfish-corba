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
 * <P>
 * All of the isA methods on ClassInfo need to be passed the same Class that was
 * used in the get call!  This is an awkward interface, but the alternative is
 * to store the class in the ClassInfo, which would create a strong reference from
 * the value to the key, making the WeakHashMap useless.  It also appears to be
 * difficult to use a weak or soft reference here, because I can't handle the
 * case of an empty reference to the class inside the ClassInfo object.
 * If ClassInfoCache supported the methods directly, we could work around this,
 * but then we would in some case be doing multiple lookups for a class to get
 * class information, which would slow things down significantly (the get call
 * is a significant cost in the benchmarks).
 */
public class ClassInfoCache {
    // Do NOT put a strong reference to the Class in ClassInfo: we don't want to
    // pin ClassLoaders in memory!
    public static class ClassInfo {

	public static class LazyWrapper {
	    Class isAClass ;
	    boolean initialized ;
	    boolean value ;

	    public LazyWrapper( Class isAClass ) {
		this.isAClass = isAClass ;
		this.initialized = false ;
		this.value = false ;
	    }

	    synchronized boolean get( Class cls ) {
		if (!initialized) {
		    initialized = true ;
		    value = isAClass.isAssignableFrom( cls ) ;
		}

		return value ;
	    }
	}

	private LazyWrapper isARemote = new LazyWrapper( 
	    Remote.class ) ;
	private LazyWrapper isARemoteException = new LazyWrapper( 
	    RemoteException.class ) ;
	private LazyWrapper isAUserException = new LazyWrapper( 
	    UserException.class ) ;
	private LazyWrapper isAObjectImpl = new LazyWrapper( 
	    ObjectImpl.class ) ;
	private LazyWrapper isAORB = new LazyWrapper( 
	    ORB.class ) ;
	private LazyWrapper isALogWrapperBase = new LazyWrapper( 
	    LogWrapperBase.class ) ;
	private LazyWrapper isAIDLEntity = new LazyWrapper( 
	    IDLEntity.class ) ;
	private LazyWrapper isAStreamable = new LazyWrapper( 
	    Streamable.class ) ;
	private LazyWrapper isAStreamableValue = new LazyWrapper( 
	    StreamableValue.class ) ;
	private LazyWrapper isACustomValue = new LazyWrapper( 
	    CustomValue.class ) ;
	private LazyWrapper isAValueBase = new LazyWrapper( 
	    ValueBase.class ) ;
	private LazyWrapper isACORBAObject = new LazyWrapper( 
	    org.omg.CORBA.Object.class ) ;
	private LazyWrapper isASerializable = new LazyWrapper( 
	    Serializable.class ) ;
	private LazyWrapper isAExternalizable = new LazyWrapper( 
	    Externalizable.class ) ;
	private LazyWrapper isAString = new LazyWrapper( 
	    String.class ) ;
	private LazyWrapper isAClass = new LazyWrapper( 
	    Class.class ) ;

	private String repositoryId = null ;

	private boolean isArray ;
	private boolean isEnum ;
	private boolean isInterface ;
	private boolean isProxyClass ;

	ClassInfo( Class cls ) {
	    isArray = cls.isArray() ;
	    isEnum = cls.isEnum() ;
	    isInterface = cls.isInterface() ;
	    isProxyClass = Proxy.isProxyClass( cls ) ;
	}

	public synchronized String getRepositoryId() {
	    return repositoryId ;
	}

	public synchronized void setRepositoryId( String repositoryId ) {
	    this.repositoryId = repositoryId ;
	}

	public boolean isARemote( Class cls ) { 
	    return isARemote.get(cls) ; 
	}
	public boolean isARemoteException( Class cls ) { 
	    return isARemoteException.get(cls) ; 
	}
	public boolean isAUserException( Class cls ) { 
	    return isAUserException.get(cls) ; 
	}
	public boolean isAObjectImpl( Class cls ) { 
	    return isAObjectImpl.get(cls) ; 
	}
	public boolean isAORB( Class cls ) { 
	    return isAORB.get(cls) ; 
	}
	public boolean isALogWrapperBase( Class cls ) { 
	    return isALogWrapperBase.get(cls) ; 
	}
	public boolean isAIDLEntity( Class cls ) { 
	    return isAIDLEntity.get(cls) ; 
	}
	public boolean isAStreamable( Class cls ) { 
	    return isAStreamable.get(cls) ; 
	}
	public boolean isAStreamableValue( Class cls ) { 
	    return isAStreamableValue.get(cls) ; 
	}
	public boolean isACustomValue( Class cls ) { 
	    return isACustomValue.get(cls) ; 
	}
	public boolean isAValueBase( Class cls ) { 
	    return isAValueBase.get(cls) ; 
	}
	public boolean isACORBAObject( Class cls ) { 
	    return isACORBAObject.get(cls) ; 
	}
	public boolean isASerializable( Class cls ) { 
	    return isASerializable.get(cls) ; 
	}
	public boolean isAExternalizable( Class cls ) { 
	    return isAExternalizable.get(cls) ; 
	}
	public boolean isAString( Class cls ) { 
	    return isAString.get(cls) ; 
	}
	public boolean isAClass( Class cls ) { 
	    return isAClass.get(cls) ; 
	}

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
