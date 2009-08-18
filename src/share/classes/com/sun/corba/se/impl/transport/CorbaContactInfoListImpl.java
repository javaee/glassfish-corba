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

package com.sun.corba.se.impl.transport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.sun.corba.se.pept.transport.ContactInfo;

import com.sun.corba.se.spi.ior.IOR ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfile ;
import com.sun.corba.se.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.se.spi.ior.iiop.LoadBalancingComponent ;
import com.sun.corba.se.spi.ior.TaggedProfileTemplate ;
import com.sun.corba.se.spi.ior.TaggedComponent ;
import com.sun.corba.se.spi.oa.ObjectAdapterFactory;
import com.sun.corba.se.spi.orb.ORB;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcherFactory;
import com.sun.corba.se.spi.transport.CorbaContactInfoList ;
import com.sun.corba.se.spi.transport.SocketInfo;
import com.sun.corba.se.spi.transport.CorbaContactInfo;
import com.sun.corba.se.spi.orbutil.generic.UnaryBooleanFunction ;

import com.sun.corba.se.spi.orbutil.ORBConstants;
import com.sun.corba.se.impl.orbutil.ORBUtility;
import com.sun.corba.se.impl.protocol.NotLocalLocalCRDImpl;

/**
 * @author Harold Carr
 */
public class CorbaContactInfoListImpl 
    implements
	CorbaContactInfoList
{
    protected ORB orb;
    protected LocalClientRequestDispatcher localClientRequestDispatcher;
    protected IOR targetIOR;
    protected IOR effectiveTargetIOR;
    protected List<CorbaContactInfo> effectiveTargetIORContactInfoList;
    protected ContactInfo primaryContactInfo;
    private boolean usePerRequestLoadBalancing = false ;

    private int startCount = 0 ;

    private UnaryBooleanFunction<CorbaContactInfo> testPred =
        new UnaryBooleanFunction<CorbaContactInfo>() {
            public boolean evaluate( CorbaContactInfo arg ) {
                return !arg.getType().equals( SocketInfo.IIOP_CLEAR_TEXT ) ;
            }
        } ;

    private <T> List<T> filter( List<T> arg, UnaryBooleanFunction<T> pred ) {
        List<T> result = new ArrayList<T>() ;
        for (T elem : arg ) {
            if (pred.evaluate( elem )) {
                result.add( elem ) ;
            }
        }

        return result ;
    }

    private static ThreadLocal<Boolean> skipRotate = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false ;
        }
    } ;

    // This is an ugly hack to avoid rotating the iterator when the
    // ClientGroupManager decides to update the iterator, which is only
    // supposed to happen when the cluster shape changes.  Unfortunately it
    // is happening on EVERY REQUEST, at least in the Argela test.
    // XXX Fix the wrong behavior and remove this hack!
    public static void setSkipRotate() {
        skipRotate.set( true ) ;
    }

    // Move the first startCount elements of the list to the end, so that
    // the list starts at the startCount'th element and continues
    // through all elements.  Each time we rotate, increment
    // startCount for load balancing.
    private synchronized List<CorbaContactInfo> rotate( List<CorbaContactInfo> arg ) {
        if (skipRotate.get()) {
            skipRotate.set( false ) ;
            return arg ;
        }

        if (usePerRequestLoadBalancing) {
            if (orb.folbDebugFlag) {
                dprint( ".rotate->: arg = " + arg + " startCount = " + startCount ) ;
            }

            LinkedList<CorbaContactInfo> tempList = null ; 

            try {
                // XXX  This may be the best way to support PRLB for now.
                // The GIS will return types like "iiop-listener-1", but we also get
                // IIOP_CLEAR_TEXT for some, for both SSL and non-SSL ports.  Invoking
                // clear on an SSL port leads to bad failures that are not retryable.
                tempList = new LinkedList<CorbaContactInfo>( filter( arg, testPred ) ) ;

                // XXX Really should just be this:
                // tempList = new LinkedList<CorbaContactInfo>( arg ) ;

                if (startCount >= tempList.size()) {
                    startCount = 0 ;
                }

                for (int ctr=0; ctr<startCount; ctr++) {
                    CorbaContactInfo element = tempList.removeLast() ;
                    tempList.addFirst( element ) ;
                }

                startCount++ ;
            
                return tempList ;
            } finally {
                if (orb.folbDebugFlag) {
                    dprint( ".rotate<-: result = " + tempList ) ;
                }
            }
        } else {
            return arg ;
        }
    }

    // XREVISIT - is this used?
    public CorbaContactInfoListImpl(ORB orb)
    {
	this.orb = orb;
    }

    public CorbaContactInfoListImpl(ORB orb, IOR targetIOR)
    {
	this(orb);
	setTargetIOR(targetIOR);
    }
    
    ////////////////////////////////////////////////////
    //
    // pept.transport.ContactInfoList
    //

    public synchronized Iterator<CorbaContactInfo> iterator()
    {
	createContactInfoList();
	Iterator<CorbaContactInfo> result = new CorbaContactInfoListIteratorImpl(
            orb, this, primaryContactInfo, 
	    rotate( effectiveTargetIORContactInfoList ),
            usePerRequestLoadBalancing );

        /* This doesn't work due to some strange behavior in FOLB: we are getting far
         * too many IOR updates.  Updates are received even when the cluster shape has not changed.
         * XXX This should be found and fixed at some point.
         */
        /*
        if (usePerRequestLoadBalancing) {
            // Copy the list, otherwise we will get a ConcurrentModificationException as
            // soon as next() is called on the iterator.
            List<CorbaContactInfo> newList = new ArrayList( effectiveTargetIORContactInfoList ) ;
            CorbaContactInfo head = newList.remove(0) ;
            newList.add( head ) ;
            effectiveTargetIORContactInfoList = newList ;
        }
        */

        return result ;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfoList
    //

    public synchronized void setTargetIOR(IOR targetIOR)
    {
	this.targetIOR = targetIOR;
	setEffectiveTargetIOR(targetIOR);
    }

    public synchronized IOR getTargetIOR()
    {
	return targetIOR;
    }

    public synchronized void setEffectiveTargetIOR(IOR effectiveTargetIOR)
    {
	this.effectiveTargetIOR = effectiveTargetIOR;
	effectiveTargetIORContactInfoList = null;
	if (primaryContactInfo != null &&
	    orb.getORBData().getIIOPPrimaryToContactInfo() != null)
	{
	    orb.getORBData().getIIOPPrimaryToContactInfo()
		.reset(primaryContactInfo);
	}
	primaryContactInfo = null;
	setLocalSubcontract();

        // Set the per request load balancing flag.
        IIOPProfile prof = effectiveTargetIOR.getProfile() ;
        TaggedProfileTemplate temp = prof.getTaggedProfileTemplate() ;
        Iterator<TaggedComponent> lbcomps = 
            temp.iteratorById( ORBConstants.TAG_LOAD_BALANCING_ID ) ;
        if (lbcomps.hasNext()) {
            LoadBalancingComponent lbcomp = null ;
            lbcomp = (LoadBalancingComponent)(lbcomps.next()) ;
            usePerRequestLoadBalancing = 
                lbcomp.getLoadBalancingValue() == ORBConstants.PER_REQUEST_LOAD_BALANCING ; 
        }
    }

    public synchronized IOR getEffectiveTargetIOR()
    {
	return effectiveTargetIOR;
    }

    public synchronized LocalClientRequestDispatcher getLocalClientRequestDispatcher()
    {
	return localClientRequestDispatcher;
    }

    ////////////////////////////////////////////////////
    //
    // org.omg.CORBA.portable.Delegate
    //

    // REVISIT - hashCode(org.omg.CORBA.Object self)

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    public synchronized int hashCode()
    {
	return targetIOR.hashCode();
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    private void createContactInfoList()
    {
	IIOPProfile iiopProfile = effectiveTargetIOR.getProfile();

	if (effectiveTargetIORContactInfoList == null) {

	    effectiveTargetIORContactInfoList = 
		new ArrayList<CorbaContactInfo>();

	    String hostname = 
		((IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate())
	            .getPrimaryAddress().getHost().toLowerCase();
	    int    port     = 
		((IIOPProfileTemplate)iiopProfile.getTaggedProfileTemplate())
	            .getPrimaryAddress().getPort();
	    // For use by "sticky manager" if one is registered.
	    primaryContactInfo = 
		createContactInfo(SocketInfo.IIOP_CLEAR_TEXT, hostname, port);

	    if (iiopProfile.isLocal()) {
		// NOTE: IMPORTANT:
		// Only do local.  The APP Server interceptors check
		// effectiveTarget.isLocal - which is determined via
		// the IOR - so if we added other addresses then
		// transactions and interceptors would not execute.
		CorbaContactInfo contactInfo = new SharedCDRContactInfoImpl(
	            orb, this, effectiveTargetIOR, 
	    	    orb.getORBData().getGIOPAddressDisposition());
		effectiveTargetIORContactInfoList.add(contactInfo);
	    } else {
		addRemoteContactInfos(effectiveTargetIOR,
				      effectiveTargetIORContactInfoList);
	    }
	    if (orb.transportDebugFlag) {
		dprint(".createContactInfoList: first time for: "
		       + iiopProfile
		       + " list: "
		       + effectiveTargetIORContactInfoList);
	    }
	} else {
	    if (! iiopProfile.isLocal()) {
		// 6152681 - this is so SSL can change its selection on each
		// invocation
		addRemoteContactInfos(effectiveTargetIOR,
				      effectiveTargetIORContactInfoList);
	    } else {
		if (orb.transportDebugFlag) {
		    dprint(".createContactInfoList: subsequent for: "
			   + iiopProfile
			   + " colocated so no change");
		}
	    }
	}

        if (orb.folbDebugFlag) {
            dprint( ".createContactInfoList: effectiveTargetIORContactInfoList = " +
                effectiveTargetIORContactInfoList ) ;
        }
    }

    private void addRemoteContactInfos(
        IOR  effectiveTargetIOR,
	List<CorbaContactInfo> effectiveTargetIORContactInfoList)
    {
	CorbaContactInfo contactInfo;
	List<? extends SocketInfo> socketInfos = orb.getORBData()
	    .getIORToSocketInfo().getSocketInfo(
                effectiveTargetIOR,
		// 6152681
		effectiveTargetIORContactInfoList);

	if (socketInfos == effectiveTargetIORContactInfoList) {
	    if (orb.transportDebugFlag) {
		dprint(".addRemoteContactInfos: no change: " + socketInfos);
	    }
	    return;
	}

	for (SocketInfo socketInfo : socketInfos) {
	    String type = socketInfo.getType();
	    String host = socketInfo.getHost().toLowerCase();
	    int    port = socketInfo.getPort();
	    contactInfo = createContactInfo(type, host, port);
	    effectiveTargetIORContactInfoList.add(contactInfo);
	}
    }

    protected CorbaContactInfo createContactInfo(String type, 
					    String hostname, int port)
    {
	return new SocketOrChannelContactInfoImpl(
	    orb, this, 
	    // XREVISIT - See Base Line 62
	    effectiveTargetIOR,
	    orb.getORBData().getGIOPAddressDisposition(),
	    type, hostname, port);
    }

    /**
     * setLocalSubcontract sets cached information that is set whenever
     * the effectiveTargetIOR changes.
     * 
     * Note: this must be maintained accurately whether or not the ORB
     * allows local optimization, because ServantManagers in the POA
     * ALWAYS use local optimization ONLY (they do not have a remote case).
     */
    protected void setLocalSubcontract()
    {
	if (!effectiveTargetIOR.getProfile().isLocal()) {
	    localClientRequestDispatcher = new NotLocalLocalCRDImpl();
	    return;
	}

	// XXX Note that this always uses the first IIOP profile to get the
	// scid.  What about multi-profile IORs?  This should perhaps be
	// tied to the current ContactInfo in some way, together with an
	// implementation of ClientDelegate that generally prefers co-located
	// ContactInfo.  This may in fact mean that we should do this at
	// the ContactInfo level, rather than the IOR/profile level.
	int scid = effectiveTargetIOR.getProfile().getObjectKeyTemplate().
	    getSubcontractId() ;
	LocalClientRequestDispatcherFactory lcsf = 
            orb.getRequestDispatcherRegistry().getLocalClientRequestDispatcherFactory( scid ) ;
	localClientRequestDispatcher = lcsf.create( scid, effectiveTargetIOR ) ;
    }

    // For timing test.
    public ContactInfo getPrimaryContactInfo()
    {
	return primaryContactInfo;
    }

    protected void dprint(String msg)
    {
	ORBUtility.dprint("CorbaContactInfoListImpl", msg);
    }
}

// End of file.
