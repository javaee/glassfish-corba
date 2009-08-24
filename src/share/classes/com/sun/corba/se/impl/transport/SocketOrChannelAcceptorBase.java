/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1996-2007 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.corba.se.impl.transport;

import com.sun.corba.se.impl.encoding.CDRInputObject;
import com.sun.corba.se.impl.encoding.CDROutputObject;
import com.sun.corba.se.impl.logging.ORBUtilSystemException;
import com.sun.corba.se.impl.oa.poa.Policies;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.spi.extension.RequestPartitioningPolicy;
import com.sun.corba.se.spi.ior.IORTemplate;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.ior.iiop.GIOPVersion;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress;
import com.sun.corba.se.spi.ior.iiop.IIOPFactories;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate;
import com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import com.sun.corba.se.spi.protocol.CorbaMessageMediator;
import com.sun.corba.se.spi.transport.EventHandler;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.transport.CorbaAcceptor;
import com.sun.corba.se.spi.transport.CorbaConnection;
import com.sun.corba.se.spi.transport.SocketInfo;
import com.sun.corba.se.spi.transport.CorbaInboundConnectionCache;
import com.sun.corba.se.spi.transport.CorbaTransportManager;
import com.sun.corba.se.spi.extension.LoadBalancingPolicy;
import java.nio.channels.SelectionKey;
import java.util.Iterator;
import org.omg.IOP.TAG_INTERNET_IOP;

/**
 *
 * @author ken
 */
public abstract class SocketOrChannelAcceptorBase
    extends
	EventHandlerBase
    implements
	CorbaAcceptor,
	Work,
        // BEGIN Legacy
	SocketInfo,
	LegacyServerSocketEndPointInfo
	// END Legacy
{
    protected int port ;
    protected long enqueueTime;
    protected boolean initialized;

    // BEGIN legacy
    protected String type = "";
    protected String name = "";
    protected String hostname ;
    protected int locatorPort;
    // END legacy

    protected ORBUtilSystemException wrapper;
    protected CorbaInboundConnectionCache connectionCache;

    public SocketOrChannelAcceptorBase(ORB orb)
    {
	this.orb = orb;
	wrapper = orb.getLogWrapperTable().get_RPC_TRANSPORT_ORBUtil() ;

	setWork(this);
	initialized = false;

	// BEGIN Legacy support.
	this.hostname = orb.getORBData().getORBServerHost();
	this.name = LegacyServerSocketEndPointInfo.NO_NAME;
	this.locatorPort = -1;
	// END Legacy support.
    }

    public SocketOrChannelAcceptorBase(ORB orb, int port)
    {
	this(orb);
	this.port = port;
    }

    // BEGIN Legacy support.
    public SocketOrChannelAcceptorBase(ORB orb, int port,
				       String name, String type)
    {
	this(orb, port);
	this.name = name;
	this.type = type;
    }

    public void addToIORTemplate(IORTemplate iorTemplate, Policies policies, String codebase) {
        Iterator iterator = iorTemplate.iteratorById(TAG_INTERNET_IOP.value);
        String hname = orb.getORBData().getORBServerHost();
        if (iterator.hasNext()) {
            IIOPAddress iiopAddress = IIOPFactories.makeIIOPAddress(orb, hname, port);
            AlternateIIOPAddressComponent iiopAddressComponent = 
                IIOPFactories.makeAlternateIIOPAddressComponent(iiopAddress);
            while (iterator.hasNext()) {
                TaggedProfileTemplate taggedProfileTemplate = 
                    (TaggedProfileTemplate)iterator.next();
                taggedProfileTemplate.add(iiopAddressComponent);
            }
        } else {
            IIOPProfileTemplate iiopProfile = makeIIOPProfileTemplate(policies, codebase);
            iorTemplate.add(iiopProfile);
        }
    }

    public String getName() {
        // Kluge alert:
        // Work and Legacy both define getName.
        // Try to make this behave best for most cases.
        String result = name.equals(LegacyServerSocketEndPointInfo.NO_NAME) ? this.toString() : name;
        return result;
    }

    public String getObjectAdapterId() {
        return null;
    }

    public String getObjectAdapterManagerId() {
        return null;
    }

    protected final IIOPProfileTemplate makeIIOPProfileTemplate(Policies policies, String codebase) {
        GIOPVersion version = orb.getORBData().getGIOPVersion();
        int templatePort;
        if (policies.forceZeroPort()) {
            templatePort = 0;
        } else if (policies.isTransient()) {
            templatePort = port;
        } else {
            templatePort = orb.getLegacyServerSocketManager()
                .legacyGetPersistentServerPort(SocketInfo.IIOP_CLEAR_TEXT);
        }
        IIOPAddress addr = IIOPFactories.makeIIOPAddress(orb, hostname, 
            templatePort);
        IIOPProfileTemplate iiopProfile = IIOPFactories.makeIIOPProfileTemplate(orb, 
            version, addr);

        if (version.supportsIORIIOPProfileComponents()) {
            iiopProfile.add(IIOPFactories.makeCodeSetsComponent(orb));
            iiopProfile.add(IIOPFactories.makeMaxStreamFormatVersionComponent());
            RequestPartitioningPolicy rpPolicy = 
                (RequestPartitioningPolicy) policies.get_effective_policy(
                ORBConstants.REQUEST_PARTITIONING_POLICY);

            if (rpPolicy != null) {
                iiopProfile.add(
                    IIOPFactories.makeRequestPartitioningComponent(rpPolicy.getValue()));
            }

	    LoadBalancingPolicy lbPolicy = (LoadBalancingPolicy)
		policies.get_effective_policy(
				  ORBConstants.LOAD_BALANCING_POLICY);
	    if (lbPolicy != null) {
		iiopProfile.add(
		     IIOPFactories.makeLoadBalancingComponent(
			 lbPolicy.getValue()));
	    }

            if (codebase != null && !codebase.equals("")) {
                iiopProfile.add(
                    IIOPFactories.makeJavaCodebaseComponent(codebase));
            }
            if (orb.getORBData().isJavaSerializationEnabled()) {
                iiopProfile.add(
                    IIOPFactories.makeJavaSerializationComponent());
            }
        }
        return iiopProfile;
    }

    @Override
    public String toString() {
        return toStringName() + "[" + port + " " + type + " " + shouldUseSelectThreadToWait() + " " + shouldUseWorkerThreadForEvent() + "]";
    }

    protected String toStringName() {
        return "SocketOrChannelAcceptorImpl";
    }

    protected void dprint(String msg) {
        ORBUtility.dprint(toStringName(), msg);
    }

    protected void dprint(String msg, Throwable t) {
        dprint(msg);
        t.printStackTrace(System.out);
    }

    public String getHost() {
        return hostname;
    }

    public String getHostName() {
        return hostname;
    }

    public int getLocatorPort() {
        return locatorPort;
    }

    public int getPort() {
        return port;
    }

    public String getType() {
        return type;
    }

    public void setLocatorPort(int port) {
        locatorPort = port;
    }

    public CorbaInboundConnectionCache getConnectionCache() {
        return connectionCache;
    }

    public String getConnectionCacheType() {
	return CorbaTransportManager.SOCKET_OR_CHANNEL_CONNECTION_CACHE;
    }

    public long getEnqueueTime() {
        return enqueueTime;
    }

    public String getMonitoringName() {
        return "AcceptedConnections";
    }

    public synchronized boolean initialized() {
        return initialized;
    }

    public void setConnectionCache(CorbaInboundConnectionCache connectionCache) {
        this.connectionCache = connectionCache;
    }

    public void setEnqueueTime(long timeInMillis) {
        enqueueTime = timeInMillis;
    }

    public EventHandler getEventHandler() {
        return this;
    }

    public CorbaAcceptor getAcceptor() {
        return this;
    }

    public CorbaConnection getConnection() {
        throw new RuntimeException("Should not happen.");
    }

    public CDRInputObject createInputObject(ORB broker, CorbaMessageMediator messageMediator) {
        return new CDRInputObject(broker, messageMediator.getConnection(), 
            messageMediator.getDispatchBuffer(), messageMediator.getDispatchHeader());
    }

    public CorbaMessageMediator createMessageMediator(ORB broker, CorbaConnection connection) {
        // REVISIT - no factoring so cheat to avoid code dup right now.
        // REVISIT **** COUPLING !!!!
        CorbaContactInfo contactInfo = new SocketOrChannelContactInfoImpl();
        return contactInfo.createMessageMediator(broker, connection);
    }

    public CDROutputObject createOutputObject(ORB broker, CorbaMessageMediator messageMediator) {
        return new CDROutputObject((ORB) broker, messageMediator, 
            messageMediator.getReplyHeader(), messageMediator.getStreamFormatVersion());
    }

    public boolean shouldRegisterAcceptEvent() {
        return true;
    }

    public int getInterestOps() {
        return SelectionKey.OP_ACCEPT;
    }

}
