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

package com.sun.corba.se.impl.util;

import java.rmi.Remote;
import java.rmi.NoSuchObjectException;

import java.rmi.server.RMIClassLoader;
import java.rmi.server.UnicastRemoteObject;

import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.CompletionStatus;

import java.util.Properties;

import java.io.File;
import java.io.FileInputStream;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.net.MalformedURLException;

import java.util.Map ;
import java.util.HashMap ;
import java.util.WeakHashMap ;

import java.lang.ref.SoftReference ;
import java.lang.ref.ReferenceQueue ;

import com.sun.corba.se.impl.orbutil.GetPropertyAction;

/**
 *  Utility methods for doing various method calls which are used
 *  by multiple classes
 */
public class JDKBridge {
 
    /**
     * Get local codebase System property (java.rmi.server.codebase).
     * May be null or a space separated array of URLS.
     */
    public static String getLocalCodebase () {
        return localCodebase;
    }
  
    /**
     * Return true if the system property "java.rmi.server.useCodebaseOnly"
     * is set, false otherwise.
     */
    public static boolean useCodebaseOnly () {
        return useCodebaseOnly;
    }
    
    // Building caches for loadClass
    //
    // There are two cases:
    // 1. loader == null
    //	    In this case, we need Maps remoteCodeBase -> className -> SoftReference to Class 
    // 2. loader != null,
    //	    In this case, we need Maps (weak) loader -> className -> SoftReference to Class
    //
    // We might also want to cache not found results.  This can be represented by:
    // 1. Map remoteCodeBase -> Set classname
    // 2. Map (weak) loader -> Set classname
    // But this is assuming that if a ClassLoader cannot load a class at one time, it also cannot
    // load it later! That is not always true, e.g. a new class file is added to a directory.
    // Best to avoid this!
    //
    // We reclaim soft references using a ReferenceQueue.  

    private static class LoadClassCache {
	private static Map<String,Map<String,Entry>> nullLoaderMap =
	    new HashMap<String,Map<String,Entry>>() ;
	private static Map<ClassLoader,Map<String,Entry>> nonNullLoaderMap =
	    new WeakHashMap<ClassLoader,Map<String,Entry>>() ;
	private static ReferenceQueue<Class> queue =
	    new ReferenceQueue<Class>() ;

	private static class Entry extends SoftReference<Class> {
	    String codeBase ;
	    ClassLoader loader ;

	    public Entry( Class cls, String codeBase, ClassLoader loader ) {
		super( cls, queue ) ;
		this.codeBase = codeBase ;
		this.loader = loader ;
	    }

	    public void clear() {
		codeBase = null ;
		loader = null ;
	    }
	}
 
	private static void checkQueue() {
	    while (true) {
		Entry entry = (Entry)queue.poll() ;
		if (entry == null) {
		    return ;
		} else {
		    String className = entry.get().getName() ;
		    if (entry.loader == null) {
			Map<String,Entry> mse = nullLoaderMap.get( entry.codeBase ) ;
			mse.remove( className ) ;
			if (mse.isEmpty()) {
			    nullLoaderMap.remove( entry.codeBase ) ;
			}
		    } else {
			Map<String,Entry> mse = nonNullLoaderMap.get( entry.loader ) ;
			mse.remove( className ) ;
			if (mse.isEmpty()) {
			    nonNullLoaderMap.remove( entry.loader ) ;
			}
		    }
		    entry.clear() ;
		}
	    } 
	}

	/** Returns Class if it is still known to be the resolution of the parameters,
	 * throws ClassNotFoundException if it is still known that the class 
	 * can NOT be resolved, or return null if nothing is known.
	 */
	public static synchronized Class get( String className, String remoteCodebase, 
	    ClassLoader loader ) throws ClassNotFoundException {
	    
	    checkQueue() ;

	    Map<String,Entry> scm ;
	    if (loader == null) {
		scm = nullLoaderMap.get( remoteCodebase ) ;
	    } else {
		scm = nonNullLoaderMap.get( loader ) ;
	    }

	    Class cls = null ;
	    if (scm != null) {
		Entry entry = scm.get( className ) ;
		if (entry != null)
		    cls = entry.get() ;
	    }

	    return cls ;
	}

	public static synchronized void put( String className, String remoteCodebase, 
	    ClassLoader loader, Class cls ) {
	    
	    checkQueue() ;

	    Map<String,Entry> scm ;
	    if (loader == null) {
		scm = nullLoaderMap.get( remoteCodebase ) ;
		if (scm == null) {
		    scm = new HashMap<String,Entry>() ;
		    nullLoaderMap.put( remoteCodebase, scm ) ;
		}
	    } else {
		scm = nonNullLoaderMap.get( loader ) ;
		if (scm == null) {
		    scm = new HashMap<String,Entry>() ;
		    nonNullLoaderMap.put( loader, scm ) ;
		}
	    }

	    scm.put( className, new Entry( cls, remoteCodebase, loader ) ) ;
	}
    }

    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @param remoteCodebase a space-separated array of urls at which
     * the class might be found. May be null.
     * @param loader a ClassLoader who may be used to
     * load the class if all other methods fail.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className,
                                   String remoteCodebase,
                                   ClassLoader loader)
	throws ClassNotFoundException {
        
	Class cls = LoadClassCache.get( className, remoteCodebase, loader ) ;
	if (cls == null) {
	    if (loader == null) {
		cls = loadClassM(className,remoteCodebase,useCodebaseOnly);
	    } else {
		try {
		    cls = loadClassM(className,remoteCodebase,useCodebaseOnly);
		} catch (ClassNotFoundException e) {
		    // XXX log at fine
		    cls = loader.loadClass(className);
		}
	    }
	    LoadClassCache.put( className, remoteCodebase, loader, cls ) ;
	}

	return cls ;
    }
    
    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @param remoteCodebase a space-separated array of urls at which
     * the class might be found. May be null.
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className,
                                   String remoteCodebase)
	throws ClassNotFoundException {
        return loadClass(className,remoteCodebase,null);
    }
    
    /**
     * Returns a class instance for the specified class. 
     * @param className the name of the class
     * @return the <code>Class</code> object representing the loaded class.
     * @exception throws ClassNotFoundException if class cannot be loaded.
     */
    public static Class loadClass (String className)
	throws ClassNotFoundException {
        return loadClass(className,null,null);
    }

    private static final String LOCAL_CODEBASE_KEY = "java.rmi.server.codebase";
    private static final String USE_CODEBASE_ONLY_KEY = "java.rmi.server.useCodebaseOnly";
    private static String localCodebase = null;
    private static boolean useCodebaseOnly;

    static {
        setCodebaseProperties();
    }
 
    /**
     * Set the codebase and useCodebaseOnly properties. This is public
     * only for test code.
     */
    public static synchronized void setCodebaseProperties () {
        String prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(LOCAL_CODEBASE_KEY));

        if (prop != null && prop.trim().length() > 0) {
            localCodebase = prop;
        }

        prop = (String)AccessController.doPrivileged(
            new GetPropertyAction(USE_CODEBASE_ONLY_KEY));

        if (prop != null && prop.trim().length() > 0) {
            useCodebaseOnly = Boolean.valueOf(prop).booleanValue();
        }
    }

    /**
     * Set the default code base. This method is here only
     * for test code.
     */
    public static synchronized void setLocalCodebase(String codebase) {
        localCodebase = codebase;    
    }
 
    private static Class loadClassM (String className, String remoteCodebase, 
	boolean useCodebaseOnly) throws ClassNotFoundException {

        try {
            return JDKClassLoader.loadClass(null,className);
        } catch (ClassNotFoundException e) {
	    // XXX log this
	}

        try {
            if (!useCodebaseOnly && remoteCodebase != null) {
                return RMIClassLoader.loadClass(remoteCodebase,
                                                className);
            } else {
                return RMIClassLoader.loadClass(className);
            }
        } catch (MalformedURLException e) {
            className = className + ": " + e.toString();
        }

        throw new ClassNotFoundException(className);
    }
}

