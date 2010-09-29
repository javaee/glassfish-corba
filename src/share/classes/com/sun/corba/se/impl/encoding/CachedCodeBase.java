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

package com.sun.corba.se.impl.encoding;

import com.sun.org.omg.CORBA.ValueDefPackage.FullValueDescription;

import com.sun.org.omg.SendingContext.CodeBase;
import com.sun.org.omg.SendingContext.CodeBaseHelper;
import com.sun.org.omg.SendingContext._CodeBaseImplBase;

import com.sun.corba.se.spi.logging.ORBUtilSystemException;

import com.sun.corba.se.spi.transport.CorbaConnection;

import com.sun.corba.se.spi.ior.IOR ;

import com.sun.corba.se.spi.orb.ORB ;
import java.util.Hashtable;

/**
 * Provides the reading side with a per connection cache of
 * info obtained via calls to the remote CodeBase.
 *
 * Previously, most of this was in IIOPConnection.
 *
 * Features:
 *    Delays cache creation unless used
 *    Postpones remote calls until necessary
 *    Handles creating obj ref from IOR
 *    Maintains caches for the following maps:
 *         CodeBase IOR to obj ref (global)
 *         RepId to implementation URL(s)
 *         RepId to remote FVD
 *         RepId to superclass type list
 *
 * Needs cache management.
 */
public class CachedCodeBase extends _CodeBaseImplBase
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private Hashtable<String,String> implementations ;
    private Hashtable<String,FullValueDescription> fvds ;
    private Hashtable<String,String[]> bases ;

    private volatile CodeBase delegate;
    private CorbaConnection conn;

    private static Object iorMapLock = new Object() ; 
    private static Hashtable<IOR,CodeBase> iorMap = 
	new Hashtable<IOR,CodeBase>();

    public static synchronized void cleanCache( ORB orb ) {
        synchronized (iorMapLock) {
            for (IOR ior : iorMap.keySet()) {
                if (ior.getORB() == orb) {
                    iorMap.remove( ior ) ;
                }
            }
        }
    }

    public CachedCodeBase(CorbaConnection connection) {
        conn = connection;
    }

    public com.sun.org.omg.CORBA.Repository get_ir () {
        return null;
    }
        
    public synchronized String implementation (String repId) {
        String urlResult = null;

        if (implementations == null)
            implementations = new Hashtable<String,String>();
        else
            urlResult = implementations.get(repId);

        if (urlResult == null && connectedCodeBase()) {
            urlResult = delegate.implementation(repId);

            if (urlResult != null)
                implementations.put(repId, urlResult);
        }

        return urlResult;
    }

    public synchronized String[] implementations (String[] repIds) {
        String[] urlResults = new String[repIds.length];

        for (int i = 0; i < urlResults.length; i++)
            urlResults[i] = implementation(repIds[i]);

        return urlResults;
    }

    public synchronized FullValueDescription meta (String repId) {
        FullValueDescription result = null;

        if (fvds == null)
            fvds = new Hashtable<String,FullValueDescription>();
        else
            result = fvds.get(repId);

        if (result == null && connectedCodeBase()) {
            result = delegate.meta(repId);

            if (result != null)
                fvds.put(repId, result);
        }

        return result;
    }

    public synchronized FullValueDescription[] metas (String[] repIds) {
        FullValueDescription[] results 
            = new FullValueDescription[repIds.length];

        for (int i = 0; i < results.length; i++)
            results[i] = meta(repIds[i]);

        return results;
    }

    public synchronized String[] bases (String repId) {

        String[] results = null;

        if (bases == null)
            bases = new Hashtable<String,String[]>();
        else
            results = bases.get(repId);

        if (results == null && connectedCodeBase()) {
            results = delegate.bases(repId);

            if (results != null)
                bases.put(repId, results);
        }

        return results;
    }

    // Ensures that we've used the connection's IOR to create
    // a valid CodeBase delegate.  If this returns false, then
    // it is not valid to access the delegate.
    private synchronized boolean connectedCodeBase() {
        if (delegate != null)
            return true;

        if (conn.getCodeBaseIOR() == null) {
            // The delegate was null, so see if the connection's
            // IOR was set.  If so, then we just need to connect
            // it.  Otherwise, there is no hope of checking the
            // remote code base.  That could be a bug if the
            // service context processing didn't occur, or it
            // could be that we're talking to a foreign ORB which
            // doesn't include this optional service context.

            wrapper.codeBaseUnavailable( conn ) ;

            return false;
        }

        synchronized(iorMapLock) {
            // Do we have a reference initialized by another connection?
            delegate = iorMap.get( conn.getCodeBaseIOR() );
            if (delegate != null)
                return true;
            
            // Connect the delegate and update the cache
            delegate = CodeBaseHelper.narrow( getObjectFromIOR() );
            
            // Save it for the benefit of other connections
            iorMap.put( conn.getCodeBaseIOR(), delegate );
        }

        // It's now safe to use the delegate
        return true;
    }

    private org.omg.CORBA.Object getObjectFromIOR() {
        return CDRInputStream_1_0.internalIORToObject(
	    conn.getCodeBaseIOR(), null /*stubFactory*/, conn.getBroker());
    }
}

// End of file.

