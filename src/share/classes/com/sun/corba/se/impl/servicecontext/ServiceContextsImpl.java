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

package com.sun.corba.se.impl.servicecontext;

import java.lang.reflect.InvocationTargetException ;
import java.lang.reflect.Modifier ;
import java.lang.reflect.Field ;
import java.lang.reflect.Constructor ;
import java.util.Iterator ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

import com.sun.corba.se.impl.orbutil.newtimer.TimingPoints ;

import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults ;
import com.sun.corba.se.spi.servicecontext.ServiceContext ;
import com.sun.corba.se.spi.servicecontext.ServiceContexts ;
import com.sun.corba.se.spi.servicecontext.ServiceContextFactoryRegistry ;
import com.sun.corba.se.spi.servicecontext.UnknownServiceContext ;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext ;

import com.sun.corba.se.impl.encoding.CDRInputStream;
import com.sun.corba.se.impl.encoding.EncapsInputStream ;
import com.sun.corba.se.impl.orbutil.ORBUtility ;
import com.sun.corba.se.impl.util.Utility ;
import com.sun.corba.se.impl.logging.ORBUtilSystemException ;

public class ServiceContextsImpl implements ServiceContexts 
{
    private final ORB orb ;
    private static final AtomicInteger creationCount = new AtomicInteger(0) ;
    private final int instance ;

    /** 
     * Map of all ServiceContext objects in this container.
     *
     * Keys are java.lang.Integers for service context IDs.
     * Values are either instances of ServiceContext or the
     * unmarshaled byte arrays (unmarshaled on first use).
     *
     * This provides a mild optimization if we don't happen to
     * use a given service context, but it's main advantage is
     * that it allows us to change the order in which we
     * unmarshal them.  We need to do the UnknownExceptionInfo service 
     * context after the SendingContextRunTime service context so that we can
     * get the CodeBase if necessary.
     */
    private final Map<Integer,Object> scMap;

    private CodeBase codeBase;
    private GIOPVersion giopVersion;
    private TimingPoints tp ;
    private final ORBUtilSystemException wrapper ; 

    private String getValidSCIds() {
	StringBuilder sb = new StringBuilder() ;
	sb.append( "(" ) ;
	boolean first = true ;
	for (int id : scMap.keySet()) {
	    if (first)
		first = false ;
	    else
		sb.append( "," ) ;

	    sb.append( id ) ;
	}
	sb.append( ")" ) ;
	return sb.toString() ;
    }

    private void dprint( String msg ) 
    {
	ORBUtility.dprint( this, "SC" + instance
	    + getValidSCIds() + msg ) ;
    }

    /**
     * Given the input stream, this fills our service
     * context map.  See the definition of scMap for
     * details.  Creates a HashMap.
     *
     * Note that we don't actually unmarshal the
     * bytes of the service contexts here.  That is
     * done when they are actually requested via
     * get(int).
     */
    private void createMapFromInputStream(InputStream is)
    {
	tp.enter_serviceContextsCreateMap() ;
        if (orb.serviceContextDebugFlag)
            dprint( "->createMapFromInputStream" ) ;

	try {
	    int numValid = is.read_long() ;
	    if (orb.serviceContextDebugFlag)
		dprint(".createMapFromInputStream: numValid = " + numValid);

	    for (int ctr = 0; ctr < numValid; ctr++) {
		int scId = is.read_long();

		if (orb.serviceContextDebugFlag)
		    dprint(".createMapFromInputStream: Reading service context id " + scId);

		byte[] data = OctetSeqHelper.read(is);

		if (orb.serviceContextDebugFlag)
		    dprint(".createMapFromInputStream: Service context" 
			+ scId + " length: " + data.length);

		scMap.put(scId, data);
	    }
	} finally {
	    if (orb.serviceContextDebugFlag)
		dprint( "<-createMapFromInputStream" ) ;

	    tp.exit_serviceContextsCreateMap() ;
	}
    }

    public ServiceContextsImpl( ORB orb )
    {
	this.orb = orb ;
	if (orb.serviceContextDebugFlag)
	    instance = creationCount.getAndIncrement() ;
	else
	    instance = 0 ;

	tp = orb.getTimerManager().points() ;
	wrapper = orb.getLogWrapperTable().get_RPC_PROTOCOL_ORBUtil() ;

        scMap = new HashMap<Integer,Object>();

        // Use the GIOP version of the ORB.  Should
        // be specified in ServiceContext.  
        // See REVISIT below concerning giopVersion.
        giopVersion = orb.getORBData().getGIOPVersion();
	codeBase = null ;

	if (orb.serviceContextDebugFlag) 
	    dprint( "<init>(ORB)" ) ;
    }

    /** 
     * Read the Service contexts from the input stream.
     */
    public ServiceContextsImpl(InputStream s)
    {
	this( (ORB)(s.orb()) ) ;

	if (orb.serviceContextDebugFlag) {
	    dprint( "-> <init>(InputStream)" ) ;
	}
	
	try {
	    // We need to store this so that we can have access
	    // to the CodeBase for unmarshaling possible
	    // RMI-IIOP valuetype data within an encapsulation.
	    // (Known case: UnknownExceptionInfo)
	    codeBase = ((CDRInputStream)s).getCodeBase();


	    createMapFromInputStream(s);

	    // Fix for bug 4904723
	    giopVersion = ((CDRInputStream)s).getGIOPVersion();
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<- <init>(InputStream)" ) ;
	    }
	}
    }

    /**
     * Find the ServiceContextData for a given scId and unmarshal
     * the bytes.
     */
    private ServiceContext unmarshal(int scId, byte[] data) 
    {
	tp.enter_serviceContextsUnmarshal() ;
	if (orb.serviceContextDebugFlag) {
	    dprint( "->unmarshal" ) ;
	}

	try {
	    ServiceContextFactoryRegistry scr = 
		orb.getServiceContextFactoryRegistry();

	    ServiceContext.Factory factory = scr.find(scId);
	    ServiceContext sc = null;

	    if (factory == null) {
		if (orb.serviceContextDebugFlag) {
		    dprint(".unmarshal: Could not find ServiceContext.Factory for "
			   + scId
			   + " using UnknownServiceContext");
		}

		sc = ServiceContextDefaults.makeUnknownServiceContext(scId, data);
	    } else {
		if (orb.serviceContextDebugFlag) {
		    dprint(".unmarshal: Found " + factory + " id " + scId );
		}

		// REVISIT.  GIOP version should be specified as
		// part of a service context's definition, so should
		// be accessible from ServiceContextData via
		// its ServiceContext implementation class.
		//
		// Since we don't have that, yet, I'm using the GIOP
		// version of the input stream, presuming that someone
		// can't send a service context of a later GIOP
		// version than its stream version.
		//
		// Note:  As of Jan 2001, no standard OMG or Sun service contexts
		// ship wchar data or are defined as using anything but GIOP 1.0 CDR.
		EncapsInputStream eis = new EncapsInputStream(orb, data, data.length, 
		    giopVersion, codeBase);

		try {
		    eis.consumeEndian();
		    // Now the input stream passed to a ServiceContext
		    // constructor is already the encapsulation input
		    // stream with the endianness read off, so the
		    // service context should just unmarshal its own
		    // data.
		    sc =factory.create(eis, giopVersion);
		} finally {
		    try {
			eis.close();
		    } catch (java.io.IOException e) {
			wrapper.ioexceptionDuringStreamClose(e);
		    }
		}

		if (sc == null)
		    throw wrapper.svcctxUnmarshalError( 
			CompletionStatus.COMPLETED_MAYBE);
	    }

	    return sc;
	} finally {
	    if (orb.serviceContextDebugFlag)
		dprint( "<-unmarshal" ) ;
	    tp.exit_serviceContextsUnmarshal() ;
	}
    }

    /** 
     * Write the service contexts to the output stream.
     *
     * If they haven't been unmarshaled, we don't have to
     * unmarshal them.
     */
    public void write(OutputStream os, GIOPVersion gv)
    {
	tp.enter_serviceContextsWrite() ;
	if (orb.serviceContextDebugFlag) {
	    dprint( "->write" ) ;
	}

	try {
	    int numsc = scMap.size();
	    os.write_long( numsc ) ;

	    writeServiceContextsInOrder(os, gv);
	} finally {
	    if (orb.serviceContextDebugFlag)
		dprint( "<-write" ) ;
	    tp.exit_serviceContextsWrite() ;
	}
    }

    /**
     * Write the service contexts in scMap in a desired order.
     * Right now, the only special case we have is UnknownExceptionInfo,
     * so I'm merely writing it last if present.
     */
    private void writeServiceContextsInOrder(OutputStream os, GIOPVersion gv) 
    {
	tp.enter_serviceContextsWriteInOrder() ;
	if (orb.serviceContextDebugFlag) {
	    dprint( "->writeServiceContextsInOrder" ) ;
	}

	try {
	    int ueid = UEInfoServiceContext.SERVICE_CONTEXT_ID ;

	    for (int i : scMap.keySet() ) {
		if (i != ueid)
		    writeMapEntry( os, i, scMap.get(i), gv ) ;
	    }

	    // Write the UnknownExceptionInfo service context last
	    // (so it will be after the CodeBase) 
	    Object uesc = scMap.get(ueid) ;
	    if (uesc != null)
		writeMapEntry( os, ueid, uesc, gv ) ; 
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "->writeServiceContextsInOrder" ) ;
	    }
	    tp.exit_serviceContextsWriteInOrder() ;
	}
    }

    /**
     * Write the given entry from the scMap to the OutputStream.
     * See note on giopVersion.  The service context should
     * know the GIOP version it is meant for.
     */
    private void writeMapEntry(OutputStream os, int id, Object scObj, 
	GIOPVersion gv) 
    {
	tp.enter_serviceContextsWriteMapEntry() ;
	if (orb.serviceContextDebugFlag) {
	    dprint( "->writeMapEntry: id = " + id ) ;
	}

	try {
	    if (scObj instanceof byte[]) {
		// If it's still in byte[] form, we don't need to
		// unmarshal it here, just copy the bytes into
		// the new stream.
		byte[] sc = (byte[])scObj ;

		if (orb.serviceContextDebugFlag) {
		    dprint( ".writeMapEntry: writing service context bytes for id " 
			+ id);
		}

		OctetSeqHelper.write(os, sc);
	    } else if (scObj instanceof ServiceContext) {
		// We actually unmarshaled it into a ServiceContext
		// at some point.
		ServiceContext sc = (ServiceContext)scObj;

		if (orb.serviceContextDebugFlag) {
		    dprint( ".writeMapEntry: Writing service context " + sc ) ;
		}
		
		sc.write(os, gv);
	    } else {
		wrapper.errorInServiceContextMap() ;
	    }
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<-writeMapEntry" ) ;
	    } 
	    tp.exit_serviceContextsWriteMapEntry() ;
	}
    }

    public void put( ServiceContext sc ) 
    {
	if (orb.serviceContextDebugFlag) {
	    dprint( "->put: sc.id = " + sc.getId() ) ;
	}

	try {
	    scMap.put(sc.getId(), sc);
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<-put" ) ;
	    }
	}
    }

    public void delete( int scId ) 
    {
	if (orb.serviceContextDebugFlag) {
	    dprint( "->delete: scId = " + scId ) ;
	}
	
	try {
	    scMap.remove(scId);
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<-delete" ) ;
	    }
	}
    }

    public ServiceContext get(int id) 
    {
	tp.enter_serviceContextsGet() ;
	if (orb.serviceContextDebugFlag) {
	    dprint( "->get: id = " + id ) ;
	}

	try {
	    Object result = scMap.get(id);
	    if (result == null) {
		if (orb.serviceContextDebugFlag) {
		    dprint( ".get: id " + id + " not found " ) ;
		}

		return null ;
	    }

	    if (orb.serviceContextDebugFlag) {
		dprint( ".get: id " + id + " found " ) ;
	    }
	    
	    // Lazy unmarshaling on first use.
	    if (result instanceof byte[]) {
		if (orb.serviceContextDebugFlag)
		    dprint( ".get: unmarshalling id = " + id ) ;

		ServiceContext sc = unmarshal(id, (byte[])result);

		scMap.put(id, sc);

		return sc;
	    } else {
		return (ServiceContext)result;
	    }
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<-get" ) ;
	    }
	    tp.exit_serviceContextsGet() ;
	}
    }

    private ServiceContextsImpl(  ServiceContextsImpl scimpl ) {
	this( scimpl.orb ) ;

	if (orb.serviceContextDebugFlag) {
	    dprint( "-> <init>(ServiceContextsImpl)" ) ;
	}

	try {
	    this.codeBase = scimpl.codeBase ;
	    this.giopVersion = scimpl.giopVersion ;
	    for (Map.Entry<Integer,Object> entry : scimpl.scMap.entrySet() ) {
		this.scMap.put( entry.getKey(), entry.getValue() ) ;
	    }
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<- <init>(ServiceContextsImpl)" ) ;
	    }
	}
    }

    /**
     * send back a shallow copy of the ServiceContexts container
     */
    public ServiceContexts copy() {
	if (orb.serviceContextDebugFlag) {
	    dprint( "->copy" ) ;
	}

	try {
	    ServiceContexts result = new ServiceContextsImpl( this ) ;
	    return result; 
	} finally {
	    if (orb.serviceContextDebugFlag) {
		dprint( "<-copy" ) ;
	    }
	}
    }
}
