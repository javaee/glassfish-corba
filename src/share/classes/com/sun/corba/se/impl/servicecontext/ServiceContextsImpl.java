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

package com.sun.corba.se.impl.servicecontext;

import java.util.Map ;
import java.util.HashMap ;
import java.util.concurrent.atomic.AtomicInteger ;

import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA_2_3.portable.OutputStream ;
import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.org.omg.SendingContext.CodeBase;

import com.sun.corba.se.spi.ior.iiop.GIOPVersion;

import com.sun.corba.se.spi.orb.ORB ;

import com.sun.corba.se.spi.servicecontext.ServiceContextDefaults ;
import com.sun.corba.se.spi.servicecontext.ServiceContext ;
import com.sun.corba.se.spi.servicecontext.ServiceContexts ;
import com.sun.corba.se.spi.servicecontext.ServiceContextFactoryRegistry ;
import com.sun.corba.se.spi.servicecontext.UEInfoServiceContext ;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.EncapsInputStream ;
import com.sun.corba.se.spi.logging.ORBUtilSystemException ;
import com.sun.corba.se.spi.orbutil.tf.annotation.InfoMethod;
import com.sun.corba.se.spi.trace.TraceServiceContext;

@TraceServiceContext
public class ServiceContextsImpl implements ServiceContexts 
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final AtomicInteger creationCount = new AtomicInteger(0) ;

    private final ORB orb ;

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

    private String getValidSCIds() {
	StringBuilder sb = new StringBuilder() ;
	sb.append( "(" ) ;
	boolean first = true ;
	for (int id : scMap.keySet()) {
	    if (first) {
                first = false;
            } else {
                sb.append(",");
            }

	    sb.append( id ) ;
	}
	sb.append( ")" ) ;
	return sb.toString() ;
    }

    @InfoMethod
    private void numberValid( int num ) { }

    @InfoMethod
    private void readingServiceContextId( int id ) { }

    @InfoMethod
    private void serviceContextLength( int len ) { }

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
    @TraceServiceContext
    private void createMapFromInputStream(InputStream is) {
        int numValid = is.read_long() ;
        numberValid( numValid ) ;

        for (int ctr = 0; ctr < numValid; ctr++) {
            int scId = is.read_long();
            readingServiceContextId(scId);

            byte[] data = OctetSeqHelper.read(is);
            serviceContextLength(data.length);

            scMap.put(scId, data);
        }
    }

    public ServiceContextsImpl( ORB orb ) {
	this.orb = orb ;

        scMap = new HashMap<Integer,Object>();

        // Use the GIOP version of the ORB.  Should
        // be specified in ServiceContext.  
        // See REVISIT below concerning giopVersion.
        giopVersion = orb.getORBData().getGIOPVersion();
	codeBase = null ;
    }

    /** 
     * Read the Service contexts from the input stream.
     */
    public ServiceContextsImpl(InputStream s) {
	this( (ORB)(s.orb()) ) ;

        // We need to store this so that we can have access
        // to the CodeBase for unmarshaling possible
        // RMI-IIOP valuetype data within an encapsulation.
        // (Known case: UnknownExceptionInfo)
        codeBase = ((CDRInputObject)s).getCodeBase();


        createMapFromInputStream(s);

        // Fix for bug 4904723
        giopVersion = ((CDRInputObject)s).getGIOPVersion();
    }

    @InfoMethod
    private void couldNotFindServiceContextFactory( int scid ) { }

    @InfoMethod
    private void foundServiceContextFactory( int scid ) { }

    /**
     * Find the ServiceContextData for a given scId and unmarshal
     * the bytes.
     */
    @TraceServiceContext
    private ServiceContext unmarshal(int scId, byte[] data) {
        ServiceContextFactoryRegistry scr = 
            orb.getServiceContextFactoryRegistry();

        ServiceContext.Factory factory = scr.find(scId);
        ServiceContext sc = null;

        if (factory == null) {
            couldNotFindServiceContextFactory(scId);
            sc = ServiceContextDefaults.makeUnknownServiceContext(scId, data);
        } else {
            foundServiceContextFactory(scId);

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

            if (sc == null) {
                throw wrapper.svcctxUnmarshalError();
            }
        }

        return sc;
    }

    /** 
     * Write the service contexts to the output stream.
     *
     * If they haven't been unmarshaled, we don't have to
     * unmarshal them.
     */
    @TraceServiceContext
    public void write(OutputStream os, GIOPVersion gv) {
        int numsc = scMap.size();
        os.write_long( numsc ) ;

        writeServiceContextsInOrder(os, gv);
    }

    /**
     * Write the service contexts in scMap in a desired order.
     * Right now, the only special case we have is UnknownExceptionInfo,
     * so I'm merely writing it last if present.
     */
    @TraceServiceContext
    private void writeServiceContextsInOrder(OutputStream os, GIOPVersion gv) {
        int ueid = UEInfoServiceContext.SERVICE_CONTEXT_ID ;

        for (int i : scMap.keySet() ) {
            if (i != ueid) {
                writeMapEntry(os, i, scMap.get(i), gv);
            }
        }

        // Write the UnknownExceptionInfo service context last
        // (so it will be after the CodeBase) 
        Object uesc = scMap.get(ueid) ;
        if (uesc != null) {
            writeMapEntry(os, ueid, uesc, gv);
        }
    }

    @InfoMethod
    private void writingServiceContextBytesFor( int id ) { }

    @InfoMethod
    private void writingServiceContext( ServiceContext sc ) { }

    /**
     * Write the given entry from the scMap to the OutputStream.
     * See note on giopVersion.  The service context should
     * know the GIOP version it is meant for.
     */
    @TraceServiceContext
    private void writeMapEntry(OutputStream os, int id, Object scObj, 
	GIOPVersion gv) {
        if (scObj instanceof byte[]) {
            // If it's still in byte[] form, we don't need to
            // unmarshal it here, just copy the bytes into
            // the new stream.
            byte[] sc = (byte[])scObj ;

            writingServiceContextBytesFor(id);
            OctetSeqHelper.write(os, sc);
        } else if (scObj instanceof ServiceContext) {
            // We actually unmarshaled it into a ServiceContext
            // at some point.
            ServiceContext sc = (ServiceContext)scObj;

            writingServiceContext(sc);
            sc.write(os, gv);
        } else {
            wrapper.errorInServiceContextMap() ;
        }
    }

    @TraceServiceContext
    public void put( ServiceContext sc ) 
    {
        scMap.put(sc.getId(), sc);
    }

    @TraceServiceContext
    public void delete( int scId ) 
    {
        scMap.remove(scId);
    }

    @InfoMethod
    private void serviceContextIdFound( int id ) { }

    @InfoMethod
    private void serviceContextIdNotFound( int id ) { }

    @InfoMethod
    private void unmarshallingServiceContext( int id ) {  }

    @TraceServiceContext
    public ServiceContext get(int id) {
        Object result = scMap.get(id);
        if (result == null) {
            serviceContextIdNotFound(id);
            return null ;
        }

        serviceContextIdFound(id);
        
        // Lazy unmarshaling on first use.
        if (result instanceof byte[]) {
            unmarshallingServiceContext(id) ;

            ServiceContext sc = unmarshal(id, (byte[])result);

            scMap.put(id, sc);

            return sc;
        } else {
            return (ServiceContext)result;
        }
    }

    private ServiceContextsImpl(  ServiceContextsImpl scimpl ) {
	this( scimpl.orb ) ;

        this.codeBase = scimpl.codeBase ;
        this.giopVersion = scimpl.giopVersion ;
        for (Map.Entry<Integer,Object> entry : scimpl.scMap.entrySet() ) {
            this.scMap.put( entry.getKey(), entry.getValue() ) ;
        }
    }

    /**
     * send back a shallow copy of the ServiceContexts container
     */
    @TraceServiceContext
    public ServiceContexts copy() {
        ServiceContexts result = new ServiceContextsImpl( this ) ;
        return result;
    }
}
