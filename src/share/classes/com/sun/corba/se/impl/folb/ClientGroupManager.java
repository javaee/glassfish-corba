/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
//
// Created       : 2005 Jun 07 (Tue) 13:04:09 by Harold Carr.
// Last Modified : 2005 Oct 03 (Mon) 15:09:46 by Harold Carr.
//

package com.sun.corba.se.impl.folb;

import java.util.LinkedList;

import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryHelper;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ForwardRequestHelper;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

import com.sun.corba.se.spi.transport.CorbaContactInfo;

import com.sun.corba.se.spi.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.se.spi.folb.ClusterInstanceInfo;
import com.sun.corba.se.spi.folb.ClusterInstanceInfoHelper;
import com.sun.corba.se.spi.folb.GroupInfoService;
import com.sun.corba.se.spi.folb.GroupInfoServiceBase;
import com.sun.corba.se.spi.folb.GroupInfoServiceObserver;

import com.sun.corba.se.spi.ior.IOR;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.orb.DataCollector ;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.orb.ORBConfigurator ;
import com.sun.corba.se.spi.transport.IIOPPrimaryToContactInfo;
import com.sun.corba.se.spi.transport.IORToSocketInfo;
import com.sun.corba.se.spi.transport.SocketInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfo;

import com.sun.corba.se.impl.interceptors.ClientRequestInfoImpl;
import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;

// BEGIN imports for IIOPPrimaryToContactInfo
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.sun.corba.se.spi.transport.CorbaContactInfoList;
// END imports for IIOPPrimaryToContactInfo

// BEGIN import for IORToSocketInfo
import java.util.ArrayList;
import com.sun.corba.se.spi.ior.iiop.AlternateIIOPAddressComponent;
import com.sun.corba.se.spi.ior.iiop.IIOPAddress;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NameComponent ;

import javax.rmi.PortableRemoteObject ;


// END import for IORToSocketInfo

// REVISIT - log messages must be internationalized.

/**
 * @author Harold Carr
 */
public class ClientGroupManager
    extends
        org.omg.CORBA.LocalObject
    implements 
	ClientRequestInterceptor,
	GroupInfoService,
	IIOPPrimaryToContactInfo,
	IORToSocketInfo,
	ORBConfigurator,
	ORBInitializer
{
    public final String baseMsg = ClientGroupManager.class.getName();

    private boolean debug = false;

    public static boolean sentMemberShipLabel = false; // For test.
    public static boolean receivedIORUpdate   = false; // For test.

    private ORB orb;
    private Codec codec;

    private boolean initialized = false;

    private IOR lastIOR;  // Initially null, thus the separate lock object.
    private Object lastIORLock = new Object();
    private CSIv2SSLTaggedComponentHandler csiv2SSLTaggedComponentHandler;
    private transient GIS gis = new GIS();

    public ClientGroupManager() {	
    }

    private void initialize()
    {
	if (initialized) {
	    return;
	}

	try {
	    if (debug) { dprint(".initialize->"); }
	    
	    initialized = true;

	    try {
		csiv2SSLTaggedComponentHandler =
		    (CSIv2SSLTaggedComponentHandler)
		    orb.resolve_initial_references(
                        ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER);
	    } catch (InvalidName e) {
		csiv2SSLTaggedComponentHandler = null;
		dprint(".initialize: not found: "
		       + ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER);
	    }
	    CodecFactory codecFactory =
		CodecFactoryHelper.narrow(
		  orb.resolve_initial_references(
                      ORBConstants.CODEC_FACTORY_NAME));

	    codec = codecFactory.create_codec(
                new Encoding((short)0, (byte)1, (byte)2));

	} catch (InvalidName e) {
	    // REVISIT: error string
	    dprint(".initialize: " + e);
	} catch (UnknownEncoding e) {
	    // REVISIT: error string
	    dprint(".initialize: " + e);
	} finally {
	    if (debug) { dprint(".initialize<-"); }
	}
    }

    ////////////////////////////////////////////////////
    //
    // IORToSocketInfo
    //

    public List getSocketInfo(IOR ior, List previous) 
    {
	initialize();
        try {
	    if (debug) {
		dprint(".getSocketInfo->:");
	    }

	    if (csiv2SSLTaggedComponentHandler != null) {
		List<SocketInfo> csiv2 =
		    csiv2SSLTaggedComponentHandler.extract(ior);
		if (csiv2 != null) {
		    /* The contract with CSIv2 says if SSL is to be used
		       then ONLY try SSL addresssses. */
		    return csiv2;
		}
	    }

	    if (debug) {
		dprint(".getSocketInfo: handling non SSL socketInfo");
	    }

	    if (! previous.isEmpty()) {
		if (debug) {
		    dprint(".getSocketInfo: returning previous socketInfo: "
			   + previous);
		}
		return previous;
	    }

            List result = new ArrayList();

	    //
	    // IIOPProfile Primary address
	    //

            IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
		ior.getProfile().getTaggedProfileTemplate();
            IIOPAddress primary = iiopProfileTemplate.getPrimaryAddress() ;
            String host = primary.getHost().toLowerCase();
	    int port = primary.getPort();
	    
	    SocketInfo primarySocketInfo = 
		createSocketInfo("primary", 
				 SocketInfo.IIOP_CLEAR_TEXT, host, port);
	    result.add(primarySocketInfo);

	    //
	    // List alternate cluster addresses
	    //

	    Iterator iterator = iiopProfileTemplate.iteratorById(
	        ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID);

	    while (iterator.hasNext()) {
		ClusterInstanceInfo clusterInstanceInfo = 
		    extractClusterInstanceInfo(
		        (com.sun.corba.se.spi.ior.TaggedComponent) 
			iterator.next());
		com.sun.corba.se.spi.folb.SocketInfo[] endpoints = 
		  clusterInstanceInfo.endpoints;
		for (int i = 0; i < endpoints.length; ++i) {
		    com.sun.corba.se.spi.folb.SocketInfo socketInfo = 
			endpoints[i];
		    result.add(createSocketInfo(
		        "ClusterInstanceInfo.endpoint",
			socketInfo.type, socketInfo.host, socketInfo.port));
		}
	    }

	    //
	    // List alternate TAG_ALTERNATE_IIOP_ADDRESS (for corbaloc)
	    //

	    iterator = iiopProfileTemplate.iteratorById(
	        org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS.value
		);

	    while (iterator.hasNext()) {
		AlternateIIOPAddressComponent alternate =
		    (AlternateIIOPAddressComponent) iterator.next();
		
		host = alternate.getAddress().getHost().toLowerCase();
		port = alternate.getAddress().getPort();
		
		result.add(createSocketInfo(
		    "AlternateIIOPAddressComponent",
		    SocketInfo.IIOP_CLEAR_TEXT, host, port));
	    }

	    return result;

        } catch (RuntimeException e) {
	    // REVISIT - error string.
	    dprint(".getSocketInfo:", e);
	    throw e;
        } catch (Exception e) {
	    // REVISIT - error string.
	    dprint(".getSocketInfo:", e);
	    RuntimeException rte = new RuntimeException(e.getMessage());
	    rte.initCause(e);
            throw rte;
        } finally {
	    if (debug) {
		dprint(".getSocketInfo<-:");
	    }
	}
    }

    private ClusterInstanceInfo extractClusterInstanceInfo(
        com.sun.corba.se.spi.ior.TaggedComponent sunTC)
    {
	ClusterInstanceInfo clusterInstanceInfo = null;
	try {
	    org.omg.IOP.TaggedComponent tc = sunTC.getIOPComponent(orb);
	    Any any = codec.decode_value(tc.component_data,
					 ClusterInstanceInfoHelper.type());
	    clusterInstanceInfo = ClusterInstanceInfoHelper.extract(any);
	} catch (FormatMismatch e) {
	    // REVISIT - error string
	    dprint(".extractClusterInstanceInfo: ", e);
	} catch (TypeMismatch e) {
	    // REVISIT - error string
	    dprint(".extractClusterInstanceInfo: ", e);
	}
	return clusterInstanceInfo;
    }

    private SocketInfo createSocketInfo(final String msg,
					final String type,
					final String host,
					final int port) 
    {
	if (debug) {
	    dprint(".getSocketInfo: address from: "
		   + msg
		   + "; type/address/port: "
		   + type + "/" + host + "/" + port);
	}

        return new SocketInfo() {
                public String getType() {
                    return type;
                }

                public String getHost() {
                    return host;
                }

                public int getPort() {
                    return port;
                }

		public boolean equals(Object o) {
		    if (o == null) {
			return false;
		    }
		    if (! (o instanceof SocketInfo)) {
			return false;
		    }
		    SocketInfo other = (SocketInfo)o;
		    if (other.getPort() != port) {
			return false;
		    }
		    if (! other.getHost().equals(host)) {
			return false;
		    }
		    if (! other.getType().equals(type)) {
			return false;
		    }
		    return true;
		}

		public String toString() {
		    return "SocketInfo[" + type + " " + host + " " + port +"]";
		}

                public int hashCode() {
                    return port ^ host.hashCode() ^ type.hashCode() ;
                }
            };
    }

    ////////////////////////////////////////////////////
    //
    // IIOPPrimaryToContactInfo
    //

    private Map map = new HashMap();

    public synchronized void reset(CorbaContactInfo primary)
    {
	initialize();
	try {
	    if (debug) {
		dprint(".reset->: " + getKey(primary));
	    }
	    map.remove(getKey(primary));
	} catch (Throwable t) {
            dprint(".reset: ", t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".reset error");
	    rte.initCause(t);
	    throw rte;
	} finally {
	    if (debug) {
		dprint(".reset<-: " + getKey(primary));
	    }
	}
    }

    public synchronized boolean hasNext(CorbaContactInfo primary,
					CorbaContactInfo previous,
					List contactInfos)
    {
	initialize();
	try {
	    if (debug) {
		dprint(".hasNext->: " 
		       + formatKeyPreviousList(getKey(primary),
					       previous,
					       contactInfos));
	    }
	    boolean result;
	    if (previous == null) {
		result = true;
	    } else {
		int previousIndex = contactInfos.indexOf(previous);
		int contactInfosSize = contactInfos.size();
		if (debug) {
		    dprint(".hasNext: " 
			   + previousIndex + " " + contactInfosSize);
		}
		if (previousIndex < 0) {
		    // This SHOULD not happen.
		    // It would only happen if the previous is NOT
		    // found in the current list of contactInfos.
		    RuntimeException rte = new RuntimeException(


			"Problem in " + baseMsg + ".hasNext: previousIndex: "
			+ previousIndex);
		    // REVISIT - error message
		    dprint(
			"Problem in " + baseMsg + ".hasNext: previousIndex: "
			+ previousIndex, rte);
		    throw rte;
		} else {
		    // Since this is a retry, ensure that there is a following
		    // ContactInfo for .next
		    result = (contactInfosSize - 1) > previousIndex;
		}
	    }
	    if (debug) {
		dprint(".hasNext<-: " + result);
	    }
	    return result;
	} catch (Throwable t) {
	    // REVISIT - error msg
            dprint("Problem in " + baseMsg + ".hasNext", t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".hasNext error");
	    rte.initCause(t);
	    throw rte;
	}
    }

    public synchronized CorbaContactInfo next(CorbaContactInfo primary,
					 CorbaContactInfo previous,
					 List contactInfos)
    {
	initialize();
	try {
	    String debugMsg = null;

	    if (debug) {
		debugMsg = "";
		dprint(".next->: " 
		       + formatKeyPreviousList(getKey(primary),
					       previous,
					       contactInfos));
		dprint(".next: map: " + formatMap(map));
	    }

	    Object result = null;

	    if (previous == null) {
		// This is NOT a retry.
		result = map.get(getKey(primary));
		if (result == null) {
		    if (debug) {
			debugMsg = ".next<-: initialize map: ";
		    }
		    // NOTE: do not map primary to primary.
		    // In case of local transport we NEVER use primary.
		    result = contactInfos.get(0);
		    map.put(getKey(primary), result);
		} else {
		    if (debug) {
			dprint(".next: primary mapped to: " + result);
		    }
		    int position = contactInfos.indexOf(result);
		    if (position == -1) {
			// It is possible that communication to the key
			// took place on SharedCDR, then a corbaloc to 
			// same location uses a SocketOrChannelContactInfo
			// and vice versa.
			if (debug) {
			    dprint(".next: cannot find mapped entry in current list.  "
                                + "Removing mapped entry and trying .next again.");
			}
			reset(primary);
			return next(primary, previous, contactInfos);
		    }
		    // NOTE: This step is critical.  You do NOT want to
		    // return contact info from the map.  You want to find
		    // it, as a SocketInfo, in the current list, and then
		    // return that ContactInfo.  Otherwise you will potentially
		    // return a ContactInfo pointing to an incorrect IOR.
		    result = contactInfos.get(position);
		    if (debug) {
			debugMsg = ".next<-: mapped: ";
		    }
		}
	    } else {
		// This is a retry.
		// If previous is last element then .next is not called
		// because hasNext will return false.
		result = contactInfos.get(contactInfos.indexOf(previous) + 1);
		map.put(getKey(primary), result);

		if (debug) { dprint("IIOP failover to: " + result); }

		if (debug) {
		    debugMsg = ".next<-: update map: " 
			+ " " + contactInfos.indexOf(previous)
			+ " " + contactInfos.size() + " ";
		}
	    }
	    if (debug) {
		dprint(debugMsg + result);
	    }
	    return (CorbaContactInfo) result;
	} catch (Throwable t) {
            dprint("Problem in " + baseMsg + ".next", t);
	    RuntimeException rte =
		new RuntimeException(baseMsg + ".next error");
	    rte.initCause(t);
	    throw rte;
	}
    }

    private Object getKey(CorbaContactInfo contactInfo)
    {
	if (((SocketInfo)contactInfo).getPort() == 0) {
	    // When CSIv2 is used the primary will have a zero port.
	    // Therefore type/host/port will NOT be unique.
	    // So use the entire IOR for the key in that case.
	    return ((CorbaContactInfoList)contactInfo.getContactInfoList())
		.getEffectiveTargetIOR();
	} else {
	    return contactInfo;
	}
    }

    private String formatKeyPreviousList(Object key,
					 CorbaContactInfo previous, List list)
    {
	String result =
	      "\n  key     : " + key
	    + "\n  previous: " + previous
	    + "\n  list:";
	Iterator i = list.iterator();
	int count = 1;
	while (i.hasNext()) {
	    result += "\n    " + count++ + "  " + i.next();
	}
	return result;
    }

    private String formatMap(Map map)
    {
	String result = "";
	synchronized (map) {
	    Iterator i = map.entrySet().iterator();
	    if (! i.hasNext()) {
		return "empty";
	    }
	    while (i.hasNext()) {
		Map.Entry entry = (Map.Entry) i.next();
		result += 
		      "\n    key  : " + entry.getKey()
		    + "\n    value: " + entry.getValue()
		    + "\n";
	    }
	}
	return result;
    }

    ////////////////////////////////////////////////////
    //
    // GroupInfoService
    //


    public List<ClusterInstanceInfo> getInitialClusterInstanceInfo(ORB orb) {
        try {
	  org.omg.CORBA.Object ref = orb.resolve_initial_references("NameService");
	  NamingContext nctx = NamingContextHelper.narrow(ref);
	  NameComponent[] path = { new NameComponent(ORBConstants.INITIAL_GROUP_INFO_SERVICE, "") };
	  InitialGroupInfoService.InitialGIS initGIS = (InitialGroupInfoService.InitialGIS)
	    PortableRemoteObject.narrow(nctx.resolve(path),
					InitialGroupInfoService.InitialGIS.class);
	  return initGIS.getClusterInstanceInfo();
	} catch (Exception e) {
	    if (debug) {
	        dprint("Exception in InitialGroupInfoService.getClusterInstanceInfo() ==> ", e);
	    }
	    return null;
	}
    }



    private class GIS
	extends GroupInfoServiceBase
    {
	public List<ClusterInstanceInfo> getClusterInstanceInfo(
            String[] adapterName)
	{

	    if (lastIOR == null) {	     
		return getInitialClusterInstanceInfo(orb);
	    }

	    IIOPProfileTemplate iiopProfileTemplate;
	    synchronized (lastIORLock) {
		iiopProfileTemplate = (IIOPProfileTemplate)
		    lastIOR.getProfile().getTaggedProfileTemplate();
	    }
	    Iterator iterator = iiopProfileTemplate.iteratorById(
	        ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID);

	    LinkedList<ClusterInstanceInfo> results = 
		new LinkedList<ClusterInstanceInfo>();

	    while (iterator.hasNext()) {
		ClusterInstanceInfo clusterInstanceInfo = 
		    extractClusterInstanceInfo(
		        (com.sun.corba.se.spi.ior.TaggedComponent) 
			iterator.next());
		results.add(clusterInstanceInfo);
	    }
	    return results;
	}

	public boolean shouldAddAddressesToNonReferenceFactory(String[] x)
	{
	    throw new RuntimeException("Should not be called in this context");
	}

	public boolean shouldAddMembershipLabel (String[] adapterName)
	{
	    throw new RuntimeException("Should not be called in this context");
	}
    }

    public boolean addObserver(GroupInfoServiceObserver x)
    {
	return gis.addObserver(x);
    }
    public void notifyObservers()
    {
	gis.notifyObservers();
    }
    public List<ClusterInstanceInfo> getClusterInstanceInfo(
        String[] adapterName)
    {
	return gis.getClusterInstanceInfo(adapterName);
    }
    public boolean shouldAddAddressesToNonReferenceFactory(String[] x)
    {
	return gis.shouldAddAddressesToNonReferenceFactory(x);
    }
    public boolean shouldAddMembershipLabel (String[] adapterName)
    {
	return gis.shouldAddMembershipLabel(adapterName);
    }

    ////////////////////////////////////////////////////
    //
    // Interceptor operations
    //

    public String name() 
    {
	return baseMsg; 
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //
    // ClientRequestInterceptor
    //

    public void send_request(ClientRequestInfo ri)
    {
	try {
	    if (debug) { dprint(".send_request->: " + ri.operation()); }
	    initialize(); // REVISIT - remove this one later?

	    org.omg.CORBA.Object ref = ri.effective_target();
	    IOR ior = orb.getIOR(ref,false);
	    IIOPProfileTemplate iiopProfileTemplate = (IIOPProfileTemplate)
		ior.getProfile().getTaggedProfileTemplate();
	    Iterator iterator = iiopProfileTemplate.iteratorById(
	        ORBConstants.FOLB_MEMBERSHIP_LABEL_TAGGED_COMPONENT_ID);
	    if (iterator.hasNext()) {
		org.omg.IOP.TaggedComponent membershipLabelTaggedComponent = 
		    ((com.sun.corba.se.spi.ior.TaggedComponent)iterator.next())
		        .getIOPComponent(orb);
		byte[] data = membershipLabelTaggedComponent.component_data;
		if (debug) {
		    sentMemberShipLabel = true; // For test
		    dprint(".send_request: " + ri.operation()
			   + ": sending membership label: "
			   + new String(data));
		}
		ServiceContext sc = new ServiceContext(
		    ORBConstants.FOLB_MEMBERSHIP_LABEL_SERVICE_CONTEXT_ID,
		    data);
		ri.add_request_service_context(sc, false);
	    } else {
		if (debug) {
		    sentMemberShipLabel = false; // For test
		    dprint(".send_request: " + ri.operation()
			   + ": no membership label");
		}
	    }
	} catch (RuntimeException e) {
	    if (debug) {
		dprint(".send_request: " + ri.operation()
		       + ": exception: " + e);
	    }
	    throw e;
	} finally {
	    if (debug) { dprint(".send_request<-: " + ri.operation()); }
	}
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
	receive_star(".receive_reply", ri);
    }

    public void receive_exception(ClientRequestInfo ri)
    {
	receive_star(".receive_exception", ri);
    }

    public void receive_other(ClientRequestInfo ri)
    {
	receive_star(".receive_other", ri);
    }

    private void receive_star(String point, ClientRequestInfo ri)
    {
	try {
	    if (debug) { dprint(point + "->: " + ri.operation()); }
	    ServiceContext iorServiceContext = null;
	    try {
		iorServiceContext = 
		    ri.get_reply_service_context(
	                ORBConstants.FOLB_IOR_UPDATE_SERVICE_CONTEXT_ID);
	    } catch (BAD_PARAM e) {
		// Not present.  Do nothing.
		// XXX log this to catch app server mis-configuration?
	    }

	    if (iorServiceContext == null) {
		if (debug) {
		    dprint(point + ": " + ri.operation() + ": no IOR update");
		    receivedIORUpdate = false; // For testing.
		}
		return;
	    }

	    if (debug) {
		dprint(point + ": " + ri.operation() + ": received IOR update");
		receivedIORUpdate = true; // For testing.
	    }

	    byte[] data = iorServiceContext.context_data;
	    Any any = null;
	    try {
		any = codec.decode_value(data, ForwardRequestHelper.type());
	    } catch (FormatMismatch e) {
		// REVISIT - error string
		dprint(point + ": " + ri.operation() + ": " + e);
	    } catch (TypeMismatch e) {
		// REVISIT - error string
		dprint(point + ": " + ri.operation() + ": " + e);
	    }
	    // ForwardRequest is used for convenience.
	    //  This code has nothing to do with PortableInterceptor.
	    ForwardRequest fr = ForwardRequestHelper.extract(any);
	    org.omg.CORBA.Object ref = fr.forward;
	    IOR ior = orb.getIOR(ref,false);
	    synchronized (lastIORLock) {
		lastIOR = ior; // Used by LB.
		gis.notifyObservers();
	    }
	    // REVISIT - interface;
	    ((ClientRequestInfoImpl)ri).setLocatedIOR(ior);

	} finally {
	    if (debug) { dprint(point + "<-: " + ri.operation()); }
	}
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) {
	if (debug) { dprint(".post_init->:"); }
	try {
	    info.add_client_request_interceptor(this);
	} catch (Exception e) {
	    // REVISIT - error string
	    dprint(".post_init: " + e);
	}
	if (debug) { dprint(".post_init<-:"); }
    }

    ////////////////////////////////////////////////////
    //
    // ORBConfigurator
    //

    public void configure(DataCollector collector, ORB orb) 
    {
	debug = debug || orb.transportDebugFlag;

	if (debug) { dprint(".configure->:"); }

	this.orb = orb;
	orb.getORBData().addORBInitializer(this);
	orb.getORBData().setIIOPPrimaryToContactInfo(this);
	orb.getORBData().setIORToSocketInfo(this);
	// So the load-balancer register to get get updates.
	try {
	    orb.register_initial_reference(
	        ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE,
	        this);
	} catch (InvalidName e) {
	    // REVISIT - error string
	    dprint(".configure: " + e);
	}

	if (debug) { dprint(".configure<-:"); }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    static void dprint(String msg)
    {
	ORBUtility.dprint("ClientGroupManager", msg);
    }

    static void dprint(String msg, Throwable t)
    {
	dprint(msg);
	dprint(t.toString());
    }
}

// End of file.
